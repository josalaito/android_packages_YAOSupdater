package org.arasthel.yaos.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.arasthel.yaos.R;
import org.arasthel.yaos.utils.Config;
import org.arasthel.yaos.utils.InstallListAdapter;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class InstallList extends Fragment{
	
	private static int SELECTED = -1;
	private static short ROM = 1;
	private static short GAPPS = 2;
	private static short PATCH = 3;
	private static short OTHER = 4;
	private boolean buttonsDisabled = false;

	private BroadcastReceiver receiver;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View containerInflated = inflater.inflate(R.layout.install_list_fragment, container);
		return containerInflated;
	}
	

	
	public String getListItem(){
		ListView aux = (ListView) getView().findViewById(R.id.install_list);
		return (String) aux.getSelectedItem();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				setListItems();
			}
		};
		getActivity().registerReceiver(receiver, new IntentFilter("org.arasthel.yaos.DOWNLOAD_COMPLETE"));
		setListItems();
	}
	
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(receiver);
		super.onPause();
	}
	
	private OnClickListener addTo = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(SELECTED == -1){
				return;
			}
			ListView aux = (ListView) getView().findViewById(R.id.install_list);
			InstallInfo info = (InstallInfo) getFragmentManager().findFragmentById(R.id.install_info_fragment);
			info.addItemToInstallList((String) aux.getItemAtPosition(SELECTED));
		}
	};
	
	@Override
	public void onStart() {
		Button addToButton = (Button) getView().findViewById(R.id.addTo);
		addToButton.setOnClickListener(addTo);
		Button delete = (Button) getView().findViewById(R.id.delete);
		delete.setOnClickListener(deleteListener);
		super.onStart();
	}
	
	public void setListItems(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
		File root = new File(sp.getString("downloadDir", Config.downloadDir));
		String[] zipList = root.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				if(filename.endsWith(".zip"))
					return true;
				return false;
			}
		});
		Arrays.sort(zipList);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, zipList);
		final ListView aux = (ListView) getView().findViewById(R.id.install_list);
		aux.setAdapter(adapter);
		aux.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(!buttonsDisabled){
					enableButtons();
					SELECTED = arg2;
				}
				
			}
		});
	}
	
	private OnClickListener deleteListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ListView list = (ListView) getView().findViewById(R.id.install_list);
			list.clearChoices();
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			for (int i = 0; i < list.getCount(); i++) {
				list.setItemChecked(i, false);
			}
			list.invalidate();
			Principal.showToast(getActivity().getString(R.string.delete_instructions));
			buttonsDisabled = true;
			disableButtons();
			((TextView) v).setText(getActivity().getString(R.string.accept));
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ListView list = (ListView) getView().findViewById(R.id.install_list);
					SparseBooleanArray selected = list.getCheckedItemPositions();
					if(selected == null){
						return;
					}
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
					String directorio = sp.getString("directorio", "/sdcard/YAOS/");
					for (int i = 0; i < list.getCount(); i++) {
						if(selected.get(i)){
							File aux = new File(directorio+list.getAdapter().getItem(i));
							aux.delete();
						}
					}
					Principal.showToast(getActivity().getString(R.string.delete_message));
					setListItems();
					list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
					v.setOnClickListener(deleteListener);
					buttonsDisabled = false;
					((TextView) v).setText(getActivity().getString(R.string.delete));
					enableButtons();
				}
			});
		}
	};
	
	private void enableButtons(){
		Button addTo = (Button) getView().findViewById(R.id.addTo);
		addTo.setEnabled(true);
	}
	
	private void disableButtons(){
		Button addTo = (Button) getView().findViewById(R.id.addTo);
		addTo.setEnabled(false);
	}
}
