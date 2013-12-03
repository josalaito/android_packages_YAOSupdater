package org.arasthel.yaos.online;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.arasthel.yaos.ui.Principal;
import org.arasthel.yaos.utils.Config;
import org.arasthel.yaos.utils.Patch;
import org.arasthel.yaos.utils.Update;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;
import android.util.Log;

public class OnlineJSON {
	
	public static ArrayList<Update> updateList = new ArrayList<Update>();
	public static ArrayList<Patch> patchList = new ArrayList<Patch>();
	public static ArrayList<Patch> gappsList = new ArrayList<Patch>();
	private Update myVersion;
	private static ArrayList<String> mirrors;
	private String url;
	private String device;
	private JSONObject json;
	private static boolean read = false;
	private final static String TAG = "YAOS-OnlineJSON";
	private boolean onlyStables;
	private boolean anyVersion;
	public String jsonFile;
	
	
	public OnlineJSON(boolean onlyStables, boolean anyVersion, String jsonFile){
		String v;
		if((v = getProp(Config.versionProp)).isEmpty()){
			v = "0.0";
		}
		myVersion = new Update("", "", "",v);
		this.url = Config.urlJson;
		this.onlyStables = onlyStables;
		this.anyVersion = anyVersion;
		this.updateList = new ArrayList<Update>();
		this.patchList = new ArrayList<Patch>();
		this.gappsList = new ArrayList<Patch>();
		String deviceRead;
		if((deviceRead = getProp("ro.product.device")) != null){
			this.device = deviceRead;
		}else{
			if((deviceRead = getProp("ro.build.product")) != null){
				this.device = deviceRead;
			}else{
				Principal.showToast("Device Unknow. The application will close");
				return;
			}
		}
		readJsonFromText(jsonFile);
		
	}
	
	public OnlineJSON(boolean onlyStables, boolean anyVersion){
		myVersion = new Update("", "", "", getProp(Config.versionProp));
		this.url = Config.urlJson;
		this.onlyStables = onlyStables;
		this.anyVersion = anyVersion;
		this.updateList = new ArrayList<Update>();
		this.patchList = new ArrayList<Patch>();
		this.gappsList = new ArrayList<Patch>();
		String deviceRead;
		if((deviceRead = getProp("ro.product.device")) != null){
			this.device = deviceRead;
		}else{
			if((deviceRead = getProp("ro.build.product")) != null){
				this.device = deviceRead;
			}else{
				Principal.showToast("Device Unknow. The application will close");
				return;
			}
		}
		readJson();
		
	}
	
	public void readJsonFromText(String jsonString){
		try {
			this.json = new JSONObject(jsonString);
			searchForUpdates();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readJson(){
		try {
			URL onlineJson = new URL(url+"/"+device+"/updater.json");
			Log.d(TAG,onlineJson.toString());
			URLConnection urlCon = onlineJson.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
			StringBuilder builder = new StringBuilder();
			for(String line = null; (line = reader.readLine()) != null;){
				builder.append(line).append("\n");
			}
			reader.close();
			jsonFile = builder.toString();
			json = new JSONObject(jsonFile);
			searchForUpdates();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			if(Principal.getContext() != null){
				Principal.auxHandler.post(new Runnable() {
					
					@Override
					public void run() {
						Principal.showToast("Error en el formato del archivo JSON.");
					}
				});
			}
		} catch (IOException e) {
			if(Principal.getContext() != null){
				Principal.auxHandler.post(new Runnable() {
					
					@Override
					public void run() {
						Principal.showToast("No se pudo encontrar el archivo JSON.");
					}
				});
			}
			
		}
	}
	
	private void searchForUpdates(){
		try {
			JSONArray mirrorlists = json.getJSONArray("MirrorList");
			mirrors = new ArrayList<String>();
			for(int i= 0; i < mirrorlists.length(); i++){
				mirrors.add(mirrorlists.getString(i));
				Log.d(TAG,mirrorlists.getString(i));
			}
			updateList = new ArrayList<Update>();
			JSONArray updates = json.getJSONArray("Updates");
			for (int i = 0; i < updates.length(); i++) {
				if(onlyStables){
					if(updates.getJSONObject(i).has("stable")){
						Update aux = new Update(updates.getJSONObject(i).getString("name"), 
								updates.getJSONObject(i).getString("filename"), 
								updates.getJSONObject(i).getString("descurl"),
								updates.getJSONObject(i).getString("version"));
						if(anyVersion)
							updateList.add(aux);
						else{
							if(aux.compareTo(myVersion) == 1){
								updateList.add(aux);
							}
						}
					}
				}else{
					Update aux = new Update(updates.getJSONObject(i).getString("name"), 
							updates.getJSONObject(i).getString("filename"), 
							updates.getJSONObject(i).getString("descurl"),
							updates.getJSONObject(i).getString("version"));
					if(anyVersion)
						updateList.add(aux);
					else{
						if(aux.compareTo(myVersion) == 1){
							updateList.add(aux);
						}
					}
				}
			}
			Collections.sort(updateList);
			Collections.reverse(updateList);
			if(updateList.isEmpty()){
				if(Principal.getContext() != null){
					Principal.auxHandler.post(new Runnable() {
						
						@Override
						public void run() {
							Principal.showToast("No se encontraron actualizaciones");
						}
					});
				}
			}
			patchList = new ArrayList<Patch>();
			if(json.opt("Patches")!= null){
				JSONArray patches = json.getJSONArray("Patches");
				for (int i = 0; i < patches.length(); i++) {
					Patch aux = new Patch(patches.getJSONObject(i).getString("name"), 
							patches.getJSONObject(i).getString("filename"), 
							patches.getJSONObject(i).getString("descurl"),
							patches.getJSONObject(i).getString("versionMax"),
							patches.getJSONObject(i).getString("versionMin"));
					Log.d(TAG,patches.getJSONObject(i).getString("name"));
					if(aux.aplicable(myVersion)){
						patchList.add(aux);
					}
				}
			}
			Collections.sort(patchList);
			Collections.reverse(patchList);
			gappsList = new ArrayList<Patch>();
			if(json.opt("GoogleApps")!= null){
				JSONArray gapps = json.getJSONArray("GoogleApps");
				for (int i = 0; i < gapps.length(); i++) {
					Patch aux = new Patch(gapps.getJSONObject(i).getString("name"), 
							gapps.getJSONObject(i).getString("filename"), 
							gapps.getJSONObject(i).getString("descurl"),
							gapps.getJSONObject(i).getString("versionMax"),
							gapps.getJSONObject(i).getString("versionMin"));
					Log.d(TAG,gapps.getJSONObject(i).getString("name"));
					if(aux.aplicable(myVersion)){
						gappsList.add(aux);
					}
				}
			}
			Collections.sort(gappsList);
			Collections.reverse(gappsList);
			read = true;
		} catch (JSONException e) {
			if(Principal.getContext() != null){
				Principal.auxHandler.post(new Runnable() {
					
					@Override
					public void run() {
						Principal.showToast("No se pudieron leer los datos del JSON");
					}
				});
			}
			return;
		}
	}
	
	public static ArrayList<Integer> getVersionFromString(String versionStr,String delimiter){
		ArrayList<Integer> version = new ArrayList<Integer>();
		StringTokenizer token = new StringTokenizer(versionStr,delimiter);
		while(token.hasMoreTokens()){
			version.add(Integer.parseInt(token.nextToken()));
		}
		return version;
	}
	
	public static String getProp(String prop){
		String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop "+prop);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            line = input.readLine();
        }
        catch (IOException ex) {
            Log.e("YAOSUPDATER", "Unable to read sysprop "+prop, ex);
            return null;
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException e) {
                    Log.e("YAOSUPDATER", "Exception while closing InputStream", e);
                }
            }
        }
        if(line.isEmpty()){
        	line = "0";
        }
        return line;
	}
	
	public static String[] getUpdates(){
		String[] aux = new String[updateList.size()];
		Iterator<Update> iter = updateList.iterator();
		for (int i = 0; i < updateList.size(); i++) {
			aux[i] = iter.next().getName();
		}
		return aux;
	}
	
	public static String[] getPatches(){
		String[] aux = new String[patchList.size()];
		Iterator<Patch> iter = patchList.iterator();
		for (int i = 0; i < patchList.size(); i++) {
			aux[i] = iter.next().getName();
		}
		return aux;
	}
	
	public static String[] getGapps(){
		String[] aux = new String[gappsList.size()];
		Iterator<Patch> iter = gappsList.iterator();
		for (int i = 0; i < gappsList.size(); i++) {
			aux[i] = iter.next().getName();
		}
		return aux;
	}
	
	public static Update findUpdateOrPatchByName(String name){
		for (int i = 0; i < updateList.size(); i++) {
			if(updateList.get(i).getName().equals(name))
				return updateList.get(i);
		}
		for (int i = 0; i < patchList.size(); i++) {
			if(patchList.get(i).getName().equals(name))
				return patchList.get(i);
		}
		for (int i = 0; i < gappsList.size(); i++) {
			if(gappsList.get(i).getName().equals(name))
				return gappsList.get(i);
		}
		return null;
	}
	
	public static Update findUpdateOrPatchByFilename(String filename){
		for (int i = 0; i < updateList.size(); i++) {
			if(updateList.get(i).getFilename().equals(filename))
				return updateList.get(i);
		}
		for (int i = 0; i < patchList.size(); i++) {
			if(patchList.get(i).getFilename().equals(filename))
				return patchList.get(i);
		}
		for (int i = 0; i < gappsList.size(); i++) {
			if(gappsList.get(i).getFilename().equals(filename))
				return gappsList.get(i);
		}
		return null;
	}

	public void setUpdateSet(ArrayList<Update> updateSet) {
		this.updateList = updateSet;
	}
	
	public static boolean isRead(){
		return read;
	}
	
	public ArrayList<String> getMirrors(){
		return mirrors;
	}
	
	public static String[] getMirrorlist(){
		String[] mirrorlist = new String[mirrors.size()];
		for(int i = 0; i < mirrors.size(); i++){
			mirrorlist[i] = mirrors.get(i); 
		}
		return mirrorlist;
	}
	
	public String getJsonFile(){
		return jsonFile;
	}
}
