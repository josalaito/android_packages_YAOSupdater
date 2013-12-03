package org.arasthel.yaos.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.arasthel.yaos.ui.Principal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ApplyUpdate {
	
	final static short SHOWDIALOG = 0, SHOWPROGRESS = 1, CANCELPROGRESS = 2, ERRORDIALOG = 3, SHOWTOAST = 4;
	private static Handler mHandler;
	
	public static void aplicarUpdate(final ArrayList<String> archivos, final boolean md5, final boolean wipe, final boolean backup, final CharSequence backupName){
		final ProgressDialog pd = new ProgressDialog(Principal.getContext());
		mHandler = new Handler(){
			@Override
			public void handleMessage(final Message msg) {
				super.handleMessage(msg);
				switch(msg.what){
				case(SHOWDIALOG):
					AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Principal.getContext());
					alertBuilder.setMessage("El teléfono se reiniciará.");
					alertBuilder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String dispositivo = reconocerDispositivo();
							try {
								SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Principal.getContext());
								String carpeta = sp.getString("downloadDir", "/sdcard/YAOS");
								boolean cwm5 = sp.getBoolean("cwm5", false);
								//Creamos el proceso su y hacemos un buffer os para escribir en una consola de linux
								Process su = Runtime.getRuntime().exec("su");
								DataOutputStream os = new DataOutputStream(su.getOutputStream());
								Log.d("DISPOSITIVO",dispositivo);
								//Para estos dispositivos es necesaria un pequeño hack, de lo contrario, el recovery no aplica la actualización
								if(dispositivo.equals("a00ref")){
									String archivo = archivos.get(0);
									FileInputStream fis = new FileInputStream(new File("/sdcard/"+archivo));
									ZipInputStream zin = new ZipInputStream(fis);
									ZipEntry entry;
									while((entry = zin.getNextEntry()) != null){
										Log.d("ZIP", "Extracting: "+entry);
										int buffer = 2048;
										FileOutputStream fos = new FileOutputStream("/sdcard/"+entry.getName());
										BufferedOutputStream dest = new BufferedOutputStream(fos, buffer);
										int count;
										byte data[] = new byte[buffer];
										while((count = zin.read(data, 0, buffer)) != -1){
											dest.write(data, 0, count);
										}
										dest.flush();
										dest.close();
									}
									zin.close();
									os.writeBytes("reboot\n");
									os.flush();
								}else{
									/*os.writeBytes("echo boot-recovery > /cache/recovery/command\n");
									for (int i = 0; i < archivos.length; i++) {
										os.writeBytes("echo --update_package=SDCARD:"+archivos[i]+" >> /cache/recovery/command\n");
									}
									os.flush();*/
									boolean ntu = nothingToUpdate(archivos);
									if(!ntu){ // Si no hay nada para instalar, el usuario puede querer reiniciar, hacer wipes o un backup
										if(dispositivo.equals("T1001")){
											if(carpeta.contains("sdcard2")){
												if(cwm5)
													os.writeBytes("echo -e 'mount /sdcard' >> /cache/recovery/openrecoveryscript\n");
												else
													os.writeBytes("echo -e 'run_program(\"/sbin/mount /sdcard\");' >> /cache/recovery/extendedcommand\n");
												carpeta = carpeta.split("/sdcard2/")[1];
												carpeta = "/sdcard/"+carpeta;
											}else{
												if(cwm5)
													os.writeBytes("echo -e 'mount /data' >> /cache/recovery/openrecoveryscript\n");
												else
													os.writeBytes("echo -e 'run_program(\"/sbin/mount /data\");' >> /cache/recovery/extendedcommand\n");
												carpeta = carpeta.split("/sdcard/")[1];
												carpeta = "/data/media/"+carpeta;
											}
										}else if(dispositivo.equals("umts_everest") || dispositivo.contains("ingray")){
											if(carpeta.contains("external1")){
												if(cwm5)
													os.writeBytes("echo -e 'mount /sdcard' >> /cache/recovery/openrecoveryscript\n");
												else
													os.writeBytes("echo -e 'run_program(\"/sbin/mount /sdcard\");' >> /cache/recovery/extendedcommand\n");
												carpeta = carpeta.split("/external1/")[1];
												carpeta = "/sdcard/"+carpeta;
											}else{
												if(cwm5)
													os.writeBytes("echo -e 'mount /data' >> /cache/recovery/openrecoveryscript\n");
												else
													os.writeBytes("echo -e 'run_program(\"/sbin/mount /data\");' >> /cache/recovery/extendedcommand\n");
												carpeta = carpeta.split("/sdcard/")[1];
												carpeta = "/data/media/"+carpeta;
											}
										}
										Log.d("CARPETA",carpeta);
									}
									if(backup){
										if(cwm5)
											os.writeBytes("echo -e 'backup SDBOM "+backupName+"' >> /cache/recovery/openrecoveryscript\n");
										else
											os.writeBytes("echo -e 'backup_rom(\"/sdcard/clockworkmod/backup/"+backupName+"\");' >> /cache/recovery/extendedcommand\n");
									}
									if(wipe){
										if(cwm5){
											os.writeBytes("echo -e 'wipe data' >> /cache/recovery/openrecoveryscript\n");
											os.writeBytes("echo -e 'wipe cache' >> /cache/recovery/openrecoveryscript\n");
											os.writeBytes("echo -e 'wipe dalvik' >> /cache/recovery/openrecoveryscript\n");
										}else{
											os.writeBytes("echo -e 'format(\"/data\");' >> /cache/recovery/extendedcommand\n");
										}
									}
									if(!ntu){
										Editor edit = sp.edit();
										
										for (String archivo : archivos) {
											if(archivo != null && !archivo.isEmpty()){
												Log.d("INSTALL","Install SCRIPT:");
												Log.d("INSTALL",archivo);
												String install = carpeta+File.separator+archivo;
												install.replaceAll(" ", "\\ "); // we look for whitespaces and replace it with "\ "
												if(cwm5)
													os.writeBytes("echo 'install "+install+"' >> /cache/recovery/openrecoveryscript\n");
												else
													os.writeBytes("echo 'install_zip(\""+install+"\");' >> /cache/recovery/extendedcommand\n");
											}
											StringBuilder sb = new StringBuilder();
											for(String a : archivos){
												sb.append(a+"\n");
											}
											edit.putString("lastInstall", sb.toString());
											edit.commit();
										}
									}
									os.writeBytes("exit\n");
									os.flush();
									try {
										if((su.waitFor() == 127) || (su.waitFor() == 255)){
											Toast.makeText(Principal.getContext(),"No se pudo acceder como Root", Toast.LENGTH_SHORT).show();
											Log.e("YAOSUPDATER","Cannot get root access");
											su.destroy();
										}else{
										  	su.destroy();
											su = Runtime.getRuntime().exec("su");
											os = new DataOutputStream(su.getOutputStream());
											os.writeBytes("reboot recovery\n");
										}
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								//Se ejecutan las líneas
								os.flush();
								os.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					alertBuilder.setTitle("Atención");
					alertBuilder.setNegativeButton("Cancelar", null);
					AlertDialog dialog = alertBuilder.create();
					dialog.show();
				break;
				case SHOWPROGRESS:
					pd.setCancelable(false);
					pd.setIndeterminate(true);
					pd.setTitle("Comprobando integridad de los archivos a instalar...");
					pd.setMessage("Comprobando MD5SUM...");
					pd.show();
				break;
				case CANCELPROGRESS:
					pd.dismiss();
				break;
				case ERRORDIALOG:
					Principal.showToast("El archivo "+msg.getData().getString("file")+" está corrupto. Vuelva a descargarlo antes de intentar instalarlo.");
				break;
				}
			}
		};
		
		Thread md5thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(md5){
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Principal.getContext());
					String carpeta = sp.getString("directorio", "/sdcard/YAOS/");
					mHandler.sendEmptyMessage(SHOWPROGRESS);
					//Si el método de verificación devuelve false, volvemos atrás
					for (String archivo : archivos) {
						if(archivo != null){
							if(!comprobarMD5SUM(carpeta+archivo)){
								Message m = new Message();
								Bundle b = new Bundle();
								b.putString("file", archivo);
								m.setData(b);
								m.what = ERRORDIALOG;
								mHandler.sendMessage(m);
								mHandler.sendEmptyMessage(CANCELPROGRESS);
								return;
							}
						}
					}
					mHandler.sendEmptyMessage(CANCELPROGRESS);
					mHandler.sendEmptyMessage(SHOWDIALOG);
				}
			}
		});
		
		md5thread.start();
	}
	
	private static String reconocerDispositivo(){
		String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.product.device");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            return line;
        }catch (IOException ex) {
            Log.e("MIUIESUPDATER", "Unable to read sysprop ro.product.device", ex);
            return null;
        }
	}
	
	private static boolean comprobarMD5SUM(final String archivo){
		//Se lee el archivo .md5sum
		File md5File = new File(archivo+".md5sum");
		try {
			FileInputStream fis = new FileInputStream(md5File);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String md5 = br.readLine();
			//Se leen los 32 primeros caracteres (la cadena md5 generada)
			md5 = md5.substring(0, 32);
			File archivoFile = new File(archivo);
			//Comprobamos que la cadena leída sea la misma que la generada al comprobar el archivo descargado
			if(MD5.comprobarMD5(md5, archivoFile)){
				return true;
			}
		} catch (FileNotFoundException e) {
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private static boolean nothingToUpdate(ArrayList<String> archivos){
		return archivos.isEmpty();
	}
	
}
