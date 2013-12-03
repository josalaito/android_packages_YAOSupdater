package org.arasthel.yaos.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import org.arasthel.yaos.R;
import org.arasthel.yaos.ui.Preferences;
import org.arasthel.yaos.ui.Principal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;

public class DownloadService extends Service {

	private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private NotificationManager mNM;
    private String downloadUrl;
    private String auxFile;
    private BroadcastReceiver cancelDownload;
    private boolean cancelDownloadRegistered;
    private Thread downloadThread;
    private TreeMap<String,Integer> needsToStop; // 1st one is the code, 2nd one is the file
    /*
     * needsToStop:
     * 0 : don't stop service
     * 1 : stop service, user canceled download
     * 2 : stop service, it failed somewhere
     */
    private Bundle extras;
    private TreeMap <String,Integer> downloading;
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
        	final String filename = msg.getData().getString("filename");
        	downloadThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					needsToStop.put(filename, 2);
					downloadFile(downloadUrl, filename);
					switch(needsToStop.get(filename)){
					case(0):
						showEndNotification("Descarga finalizada","Archivo \""+filename+"\" descargado","YAOS Updater",downloading.get(filename));
						break;
					case(1):
						showEndNotification("Descarga detenida","El archivo se dejó de descargar", "YAOS Updater: descarga detenida",downloading.get(filename));
						break;
					case(2):
						showEndNotification("Descarga fallida","Falló la descarga del archivo "+filename, "YAOS Updater: descarga fallida",downloading.get(filename));
					}
					downloading.remove(filename);
	   				if(downloading.isEmpty()){
						stopSelf();
					}
	   				needsToStop.put(filename, 0);
				}
				
			});
        	downloadThread.start();
        }
    }
	
	@Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        needsToStop = new TreeMap<String, Integer>();
        downloading = new TreeMap<String, Integer>();
        HandlerThread thread = new HandlerThread("ServiceStartArguments",1);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler 
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	cancelDownloadRegistered = false;
         Log.d("SERVICE-ONCOMMAND","onStartCommand");  

           extras = intent.getExtras();
           final String filename = extras.getString("filename");
           if(!downloading.isEmpty()){
        	   if(downloading.containsKey(filename)){
        		   Principal.showToast("Ya está descargando este archivo.");
        		   return 0;
        	   }
           }
           downloading.put(filename, new Random().nextInt());
		   StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		   StrictMode.setThreadPolicy(policy);
           if(extras != null){
        	   boolean found = false;
        	   int size = 0;
               ArrayList<String> mirrors = extras.getStringArrayList("downloadUrl");
               if(mirrors != null){
            	   size = mirrors.size();
               }
               int i;
               for (i = 0; i < size && !found; i++) {
            	   URL url;
				try {
					Log.d("URL",mirrors.get(i)+filename);
					url = new URL(mirrors.get(i)+filename);
					URLConnection urlCon = url.openConnection();
	                InputStreamReader is = new InputStreamReader(urlCon.getInputStream());
	                found = true;
	                is.close();
				} catch (FileNotFoundException e){
					// It means that the file was not found on this folder
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
               }
               if(!found){
            	   	Principal.showToast("No se pudo encontrar el archivo "+filename);
            	   	downloading.remove(filename);
	   				if(downloading.isEmpty()){
						stopSelf();
					}
	   				return -1;
               }
               this.downloadUrl=mirrors.get(i-1)+filename;
           }
           cancelDownload = new BroadcastReceiver() {
   			
	   			@Override
	   			public void onReceive(Context context, Intent intent) {
	   				needsToStop.put(intent.getExtras().getString("name"),1);
	   			}
   			};
           registerReceiver(cancelDownload, new IntentFilter("org.arasthel.yaos.CANCEL_DOWNLOAD"));
           cancelDownloadRegistered = true;
          Message msg = mServiceHandler.obtainMessage();
          msg.arg1 = startId;
          Bundle b = new Bundle();
          b.putString("filename", filename);
          msg.setData(b);
          mServiceHandler.sendMessage(msg);

          // If we get killed, after returning from here, restart
          return START_STICKY;
      }



    	@Override
      public void onDestroy() {
    		if(cancelDownloadRegistered)
    			unregisterReceiver(cancelDownload);
         Log.d("SERVICE-DESTROY","DESTROY");
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show(); 
      }

  void showEndNotification(String shortMessage, String message,String title, int id) {
	  // In this sample, we'll use the same text for the ticker and the expanded notification
	  CharSequence text = message;

	  // Set the icon, scrolling text and timestamp
	   Notification notification = new Notification(R.drawable.yaos_small, shortMessage,
	            System.currentTimeMillis());
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
	     //The PendingIntent to launch our activity if the user selects this notification
	    PendingIntent contentIntent = PendingIntent.getActivity(this.getBaseContext(), 0,
	          new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
	
	    // Set the info for the views that show in the notification panel.
	    notification.setLatestEventInfo(this, title, text, contentIntent);
	    // Send the notification.
	    // We use a layout id because it is a unique number.  We use it later to cancel.
	    mNM.notify(id, notification);
	}
  
  Notification showProgressNotification(String filename, int id) {
	  // Set the icon, scrolling text and timestamp
	  	Notification notification = new Notification();
	    notification.flags |= Notification.FLAG_ONGOING_EVENT;
	    notification.contentView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.progress_notification);
	    notification.icon = R.drawable.yaos_icon;
	    notification.contentView.setTextViewText(R.id.notif_title, filename);
	    notification.contentView.setImageViewResource(R.id.notifIcon, R.drawable.yaos_icon);
	    Intent i = new Intent("org.arasthel.yaos.CANCEL_DOWNLOAD");
	    i.putExtra("name", filename);
	    notification.contentIntent = PendingIntent.getBroadcast(this, downloading.get(filename), i, PendingIntent.FLAG_UPDATE_CURRENT);
	    notification.flags |= Notification.FLAG_AUTO_CANCEL; 
	    mNM.notify(id, notification);
	    return notification;
	}

    public void downloadFile(String fileURL, final String fileName) {
		
		StatFs stat_fs = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double avail_sd_space = (double)stat_fs.getAvailableBlocks() *(double)stat_fs.getBlockSize();
		//double GB_Available = (avail_sd_space / 1073741824);
		double MB_Available = (avail_sd_space / 10485783);
		//System.out.println("Available MB : " + MB_Available);
		Log.d("MB",""+MB_Available);
		try {
		    File root =new File(extras.getString("dir"));
		    if(root.exists() && root.isDirectory()) {
		
		    }else{
		        root.mkdir();
		    }
		    Log.d("CURRENT PATH",root.getPath());
		    auxFile = fileName;
		    URL u = new URL(fileURL);
		    URLConnection c = (URLConnection) u.openConnection();
		    c.connect();
		      final long fileSize  = c.getContentLength();
		      Log.d("FILESIZE",""+fileSize);
		      if(MB_Available <= fileSize/1048576 ){
		    	  needsToStop.put(fileName, 2);
		          this.showEndNotification("Error al descargar","No queda memoria","Error",downloading.get(fileName));
		          return;
		      } 
		    final File downloadFile = new File(root.getPath(), fileName+".part");
		    FileOutputStream f = new FileOutputStream(downloadFile);
		
		    InputStream in = c.getInputStream();
		    final Notification notification = showProgressNotification(fileName,downloading.get(fileName));
		    
		    byte[] buffer = new byte[1024];
		    int len1 = 0;
		    Integer onePercent = (int) (fileSize/100);
		    long oldDownloaded = 0;
		    int progress = 0;
		    long downloadedData = 0;
		    while ((len1 = in.read(buffer)) > 0) {
		    	if(needsToStop.get(fileName) == 1){
		    		deleteFile(downloadFile);
		    		return;
		    	}
		    	downloadedData += len1;
		        f.write(buffer, 0, len1);
		        if((downloadedData - oldDownloaded) >= onePercent){
		        	progress++;
		        	notification.contentView.setTextViewText(R.id.textPercent, String.valueOf(progress)+"%");
		        	notification.contentView.setProgressBar(R.id.downloadProgressBar, 100, progress, false);
		        	mNM.notify(downloading.get(fileName), notification);
		        	downloadedData = 0;
		        }
		    }
		    f.close();
	        in.close();
	        if(downloadFile.length() == fileSize){
	        	needsToStop.put(fileName, 0);
	        }
	        mNM.cancel(downloading.get(fileName));
		    downloadFile.renameTo(new File(root.getPath(),fileName));
		    URL md5con = new URL(fileURL+".md5sum");
		    c = md5con.openConnection();
		    c.connect();
		    try{
			    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
			    File md5sum = new File(root.getPath(),fileName+".md5sum");
			    md5sum.createNewFile();
			    FileWriter fw = new FileWriter(md5sum);
			    fw.write(br.readLine());
			    br.close();
			    fw.close();
		    }catch(FileNotFoundException e){
		    	Principal.showToast("Archivo de verificación MD5SUM no encontrado. No se podrá comprobar la integridad de la descarga.");
		    }
		    Intent i = new Intent();
		    i.setAction("org.arasthel.yaos.DOWNLOAD_COMPLETE");
		    sendBroadcast(i);
		    Log.d("DownloadService","New Progress: "+progress);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    private void deleteFile(File archivo){
    	archivo.delete();
    }

}
