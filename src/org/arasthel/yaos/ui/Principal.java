package org.arasthel.yaos.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.arasthel.yaos.R;
import org.arasthel.yaos.app.YAOS;
import org.arasthel.yaos.online.OnlineJSON;
import org.arasthel.yaos.services.DownloadService;
import org.arasthel.yaos.utils.ApplyUpdate;
import org.arasthel.yaos.utils.Config;
import org.arasthel.yaos.utils.InstallListAdapter;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class Principal extends Activity {
	
	private static byte OPCION = Config.ACTUALIZACIONES;
	private static int LISTITEM = -1;
	private static Context myContext;
	private volatile OnlineJSON onlineJSON;
	private static final String TAG = "YAOS - Principal";
	private static boolean justBooted = true;
	private BroadcastReceiver br;
	private ProgressDialog searchingPd;
	ViewSwitcher vs;
	public static Handler auxHandler;
	public static String update = null;
	public static String patch = null;
	public static String gapps = null;
	public static String other = null;
	private static SharedPreferences sp;
	private UpdateInfo updateInfo;
	
	public static Context getContext(){
		return myContext;
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        updateInfo = (UpdateInfo) getFragmentManager().findFragmentById(R.id.update_info_fragment);
        auxHandler = new Handler(){
        	@Override
        	public void handleMessage(Message msg) {
        		super.handleMessage(msg);
        		switch(msg.what){
        		case 0:
        			showToast("No se pudo encontrar el archivo JSON.");
        			break;
        		case 1:
        			showToast("Error en el archivo JSON.");
        		}
        	}
        };
        Log.d(TAG, "Default external storage dir:" +System.getProperty("java.io.tmpdir"));
        myContext = this;
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor edit = sp.edit();
		edit.remove("lastJson");
        String jsonLine;
        if(!(jsonLine = getJsonFromBuild()).isEmpty()){
        	Config.urlJson = jsonLine;
        }
        String dirStr = sp.getString("downloadDir", Config.downloadDir);
        File directorio = new File(dirStr);
        if(!directorio.exists()){
        	if(!directorio.mkdir()){
        		showToast("No se pudo crear el directorio" + dirStr + "puede que la memoria esté llena. Elimine algunos ficheros y vuelva a intentarlo.");
        		finish();
        	}
        }
        vs = (ViewSwitcher) findViewById(R.id.switcher);
		ActionBar bar = getActionBar();
        TabListener updatesListener = new TabListener() {
			
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				if(justBooted){
					justBooted = false;
					return;
				}
				OPCION = (Byte) tab.getTag();
				Log.d(TAG,"Opcion: "+OPCION);
				invalidateOptionsMenu();
				onOptionChanged();
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}
		};
		if(YAOS.isBigScreen()){
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			bar.addTab(bar.newTab().setText(getResources().getString(R.string.updates)).setTag(Config.ACTUALIZACIONES).setTabListener(updatesListener));
	        bar.addTab(bar.newTab().setText(getResources().getString(R.string.patches)).setTag(Config.PARCHES).setTabListener(updatesListener));
	        bar.addTab(bar.newTab().setText(getResources().getString(R.string.gapps)).setTag(Config.GAPPS).setTabListener(updatesListener));
	        bar.addTab(bar.newTab().setText(getResources().getString(R.string.install)).setTag(Config.INSTALL).setTabListener(updatesListener));
		}else{
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			//bar.setDisplayShowTitleEnabled(false);
			bar.setDisplayShowHomeEnabled(false);
			ArrayList<String> options = new ArrayList<String>();
			options.add(getResources().getString(R.string.updates));
			options.add(getResources().getString(R.string.patches));
			options.add(getResources().getString(R.string.gapps));
			options.add(getResources().getString(R.string.install));
			SpinnerAdapter adapter = new ArrayAdapter<String>(myContext, R.layout.actionbar_list_item, options);
			bar.setListNavigationCallbacks(adapter, new OnNavigationListener() {
				
				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId) {
					if(justBooted){
						justBooted = false;
						return false;
					}
					OPCION = (byte) itemPosition;
					Log.d(TAG,"Opcion: "+OPCION);
					invalidateOptionsMenu();
					onOptionChanged();
					return false;
				}
			});
		}
        Button addTo = (Button) findViewById(R.id.addToList);
        addTo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ListView lv = (ListView) findViewById(R.id.updateList);
				String name = OnlineJSON.findUpdateOrPatchByName((String) lv.getAdapter().getItem(LISTITEM)).getFilename();
				InstallInfo info = (InstallInfo) getFragmentManager().findFragmentById(R.id.install_info_fragment);
				info.addItemToInstallList(name);
			}
		});
        if(sp.getBoolean("searchOnInit", false) && !getIntent().hasExtra("jsonFile")){
        	Thread onlineJSONThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					onlineJSON = new OnlineJSON(sp.getBoolean("only_stables", false),
			    			sp.getBoolean("any_version",false));
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dismissSearchingDialog();
							onOptionChanged();
						}
					});
				}
			});
        	showSearchingDialog();
        	onlineJSONThread.start();
        }
        if(getIntent().hasExtra("jsonFile")){
        	onlineJSON = new OnlineJSON(sp.getBoolean("only_stables", false),
	    			sp.getBoolean("any_version",false),
	    			getIntent().getExtras().getString("jsonFile"));
        	onOptionChanged();
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if(sp.getString("lastJson", null) != null){
    		onlineJSON = new OnlineJSON(sp.getBoolean("only_stables", false),
    				sp.getBoolean("any_version", false),
    				sp.getString("lastJson", null));
    		Editor edit = sp.edit();
    		edit.remove("lastJson");
    		edit.commit();
    	}
    	br = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				if(LISTITEM == -1){
					return;
				}
				String filename = "";
				switch(OPCION){
				case Config.ACTUALIZACIONES:
					filename = OnlineJSON.updateList.get(LISTITEM).getFilename();
					break;
				case Config.PARCHES:
					filename = OnlineJSON.patchList.get(LISTITEM).getFilename();
					break;
				case Config.GAPPS:
					filename = OnlineJSON.gappsList.get(LISTITEM).getFilename();
					break;
				}
				File aux = new File(sp.getString("downloadDir", Config.downloadDir)+File.separator+filename);
				if(aux.exists()){
					findViewById(R.id.addToList).setEnabled(true);
				}
			}
		};
		registerReceiver(br, new IntentFilter("org.arasthel.yaos.DOWNLOAD_COMPLETE"));
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	LISTITEM = -1;
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	if(onlineJSON != null){
	    	Editor edit = sp.edit();
	    	edit.putString("lastJsonString", onlineJSON.getJsonFile());
	    	edit.commit();
    	}
    	unregisterReceiver(br);
    }
    
    public void downloadFile(MenuItem item){
    	Log.d(TAG,"Item: "+((ListView) findViewById(R.id.updateList)).getSelectedItem());
    	if(LISTITEM == -1){
    		return;
    	}
    	Intent i = new Intent(this, DownloadService.class);
    	i.putExtra("downloadUrl", onlineJSON.getMirrors());
    	i.putExtra("dir", sp.getString("downloadDir", Config.downloadDir));
    	if(OPCION == Config.ACTUALIZACIONES)
    		i.putExtra("filename", OnlineJSON.updateList.get(LISTITEM).getFilename());
    	else if(OPCION == Config.PARCHES)
    		i.putExtra("filename", OnlineJSON.patchList.get(LISTITEM).getFilename());
    	else if(OPCION == Config.GAPPS)
    		i.putExtra("filename", OnlineJSON.gappsList.get(LISTITEM).getFilename());
    	else
    		return;
    	startService(i);
    	findViewById(R.id.addToList).setEnabled(false);
    }
    
    public void openPreferences(MenuItem item){
    	Intent i = new Intent(this, Preferences.class);
    	startActivity(i);
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    public void updateJSON(MenuItem v){
    	LISTITEM = -1;
    	showSearchingDialog();
		Thread oj = new Thread(new Runnable() {
			
			@Override
			public void run() {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				onlineJSON = new OnlineJSON(sp.getBoolean("only_stables", false),
		    			sp.getBoolean("any_version",false));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dismissSearchingDialog();
						onOptionChanged();
					}
				});
			}
		});
		oj.start();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if(OPCION == Config.INSTALL){
    		menu.findItem(R.id.applyUpdate).setVisible(true);
    		menu.findItem(R.id.download).setVisible(false);
    		
    	}else{
    		menu.findItem(R.id.applyUpdate).setVisible(false);
    		menu.findItem(R.id.download).setVisible(true);
    	}
    	return super.onPrepareOptionsMenu(menu);
    }
    
    public void onOptionChanged(){
    	LISTITEM = -1;
    	ListView lista = (ListView) findViewById(R.id.updateList);
		updateInfo.changeContent(getResources().getString(R.string.welcome));
    	Button addTo = (Button) findViewById(R.id.addToList);
		addTo.setVisibility(View.GONE);
		ArrayAdapter<String> adapter;
    	switch(OPCION){
    	case(Config.ACTUALIZACIONES):
    		if(vs.getCurrentView().equals(findViewById(R.id.container_install))){
    			vs.showPrevious();
    		}
    		if(!OnlineJSON.isRead()){
				return;
			}
	    	adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice, OnlineJSON.getUpdates());
	    	lista.setAdapter(adapter);
	    	lista.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    	lista.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					LISTITEM = arg2;
					String desc = getResources().getString(R.string.desc_name)+" "+OnlineJSON.updateList.get(arg2).getName()+"</br></br>"+
						getResources().getString(R.string.desc_version)+" "+OnlineJSON.updateList.get(arg2).getVersionStr().toString()+"</br></br>"+
						getResources().getString(R.string.desc_file)+" "+OnlineJSON.updateList.get(arg2).getFilename()+"</br></br>"+
						getResources().getString(R.string.desc_desc)+" "+"</br>"+OnlineJSON.updateList.get(arg2).getDescUrl();
					updateInfo.changeContent(desc);
					File aux = new File(sp.getString("downloadDir", Config.downloadDir)+File.separator+OnlineJSON.updateList.get(arg2).getFilename());
					Log.d(TAG,"File: "+OnlineJSON.updateList.get(arg2).getFilename());
					Button addTo = (Button) findViewById(R.id.addToList);
					addTo.setVisibility(View.VISIBLE);
					addTo.setEnabled(aux.exists());
				}
			});
	    	break;
    	case(Config.PARCHES):
    		if(vs.getCurrentView().equals(findViewById(R.id.container_install))){
    			vs.showPrevious();
    		}
    		if(!OnlineJSON.isRead()){
				return;
			}
    	
	    	adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice, OnlineJSON.getPatches());
	    	lista.setAdapter(adapter);
	    	lista.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    	lista.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					LISTITEM = arg2;
					String desc = getResources().getString(R.string.desc_name)+" "+OnlineJSON.patchList.get(arg2).getName()+"</br></br>"+
							getResources().getString(R.string.desc_file)+" "+OnlineJSON.patchList.get(arg2).getFilename()+"</br></br>"+
							getResources().getString(R.string.desc_desc)+" "+"</br>"+OnlineJSON.patchList.get(arg2).getDescUrl();
					updateInfo.changeContent(desc);
					File aux = new File(sp.getString("downloadDir", Config.downloadDir)+File.separator+OnlineJSON.patchList.get(arg2).getFilename());
					Log.d(TAG,"File: "+OnlineJSON.patchList.get(arg2).getFilename());
					Button addTo = (Button) findViewById(R.id.addToList);
					addTo.setVisibility(View.VISIBLE);
					addTo.setEnabled(aux.exists());
				}
			});
	    	break;
    	case(Config.GAPPS):
    		if(vs.getCurrentView().equals(findViewById(R.id.container_install))){
    			vs.showPrevious();
    		}
    		if(!OnlineJSON.isRead()){
				return;
			}
    	
	    	adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_single_choice, OnlineJSON.getGapps());
	    	lista.setAdapter(adapter);
	    	lista.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	    	lista.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					LISTITEM = arg2;
					String desc = getResources().getString(R.string.desc_name)+" "+OnlineJSON.gappsList.get(arg2).getName()+"</br></br>"+
							getResources().getString(R.string.desc_file)+" "+OnlineJSON.gappsList.get(arg2).getFilename()+"</br></br>" +
							getResources().getString(R.string.desc_desc)+" "+OnlineJSON.gappsList.get(arg2).getDescUrl();
					updateInfo.changeContent(desc);
					File aux = new File(sp.getString("downloadDir", Config.downloadDir)+File.separator+OnlineJSON.gappsList.get(arg2).getFilename());
					Log.d(TAG,"File: "+OnlineJSON.gappsList.get(arg2).getFilename());
					Button addTo = (Button) findViewById(R.id.addToList);
					addTo.setVisibility(View.VISIBLE);
					addTo.setEnabled(aux.exists());
				}
			});
	    	break;
    	case(Config.INSTALL):
    		if(vs.getCurrentView().equals(findViewById(R.id.container))){
    			vs.showNext();
    		}
    		break;
    	}
    }
    
    public static void showToast(final String mensaje){
    	if(Principal.getContext() != null) {
    		auxHandler.post(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(Principal.getContext(), mensaje, Toast.LENGTH_LONG).show();
				}
			});
    	}
	}
        
    public void clear(View v){
    	LISTITEM = -1;
    	ListView toInstall = (ListView) findViewById(R.id.to_install_items_list);
    	((InstallListAdapter) toInstall.getAdapter()).clear();
	}
    
    public void applyUpdate(MenuItem m){
    	InstallInfo info = (InstallInfo) getFragmentManager().findFragmentById(R.id.install_info_fragment);
    	ListView list = (ListView) info.getView().findViewById(R.id.to_install_items_list);
    	ApplyUpdate.aplicarUpdate(((InstallListAdapter) list.getAdapter()).getLista_archivos(),
    			true /* MD5 */, 
    			((CheckBox) findViewById(R.id.doWipe)).isChecked(), 
    			((CheckBox) findViewById(R.id.doBackup)).isChecked(), 
    			((TextView) findViewById(R.id.backupName)).getText());
    }
    
    void showSearchingDialog(){
    	searchingPd = new ProgressDialog(getContext());
    	searchingPd.setTitle("Obteniendo listado de versiones.");
    	searchingPd.setMessage("Espere, por favor...");
    	searchingPd.setIndeterminate(true);
    	searchingPd.show();
    }
    
    void dismissSearchingDialog(){
    	searchingPd.dismiss();
    }
    
    @Override
    protected void onStart() {
    	if(sp.getBoolean("firstLaunch", true))
    		firstLaunch();
    	super.onStart();
    }
    
    private void firstLaunch(){
    	Intent i = new Intent(this, Cling.class);
    	startActivity(i);
    }
    
    private static String getJsonFromBuild(){
		String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop ro.update.json");
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
            return line;
        }catch (IOException ex) {
            Log.e("MIUIESUPDATER", "Unable to read sysprop ro.product.device", ex);
            return null;
        }
	}
   
}