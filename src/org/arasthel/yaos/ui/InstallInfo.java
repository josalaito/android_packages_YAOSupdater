package org.arasthel.yaos.ui;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.arasthel.yaos.R;
import org.arasthel.yaos.utils.InstallListAdapter;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.CheckBox;
import android.view.View.OnClickListener;

public class InstallInfo extends Fragment {

	private SharedPreferences sp;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View containerView = inflater.inflate(R.layout.installinfo, container);
		return containerView;
	}
	
	@Override
	public void onResume() {
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if(sp.getBoolean("restoreLast", false))
			restoreLast();
		super.onResume();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		ListView toInstallList = (ListView) getView().findViewById(R.id.to_install_items_list);
		ArrayList<String> list = new ArrayList<String>();
		InstallListAdapter adapter = new InstallListAdapter(getActivity(), android.R.layout.simple_list_item_1, list );
		toInstallList.setAdapter(adapter);
		CheckBox backup = (CheckBox) this.getView().findViewById(R.id.doBackup);
		backup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				TextView backupName = (TextView) getView().findViewById(R.id.backupName);
				backupName.setEnabled(isChecked);
			}
		});
	}
	
	private boolean restoreLast(){
		String fileList =  sp.getString("lastInstall", null);
		//HashSet<String> set = (HashSet<String>) sp.getStringSet("lastInstall", null);
		if(fileList != null){
			ArrayList<String> list = new ArrayList<String>();
			Scanner scan = new Scanner(fileList);
			while(scan.hasNext()){
				list.add(scan.nextLine());
			}
			ListView toInstallList = (ListView) getView().findViewById(R.id.to_install_items_list);
			((InstallListAdapter) toInstallList.getAdapter()).getLista_archivos().clear();
			((InstallListAdapter) toInstallList.getAdapter()).getLista_archivos().addAll(list);
			((InstallListAdapter) toInstallList.getAdapter()).notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	public boolean addItemToInstallList(String item){
		ListView toInstallList = (ListView) getView().findViewById(R.id.to_install_items_list);
		if(((InstallListAdapter) toInstallList.getAdapter()).getLista_archivos().contains(item)){
			return false;
		}
		if(((InstallListAdapter) toInstallList.getAdapter()).getLista_archivos().add(item)){
			((InstallListAdapter) toInstallList.getAdapter()).notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
}
