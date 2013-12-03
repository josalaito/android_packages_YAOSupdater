package org.arasthel.yaos.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.arasthel.yaos.R;
import org.arasthel.yaos.R.id;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SelectDirDialog extends DialogPreference{
	
	private View dialog;
	private File curDir;
	private ListView dirList;
	private SharedPreferences sp;

	public SelectDirDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected View onCreateDialogView() {
		// TODO Auto-generated method stub
		LayoutInflater li = LayoutInflater.from(getContext());
		dialog = li.inflate(R.layout.select_dir_dialog, null);
		sp = PreferenceManager.getDefaultSharedPreferences(getContext());
		curDir = new File("/mnt/sdcard/");
		updateList();
		Button createDir = (Button) dialog.findViewById(R.id.createFolder);
		createDir.setOnClickListener(onCreateClick);
		return dialog;
	}
	
	private OnItemClickListener onItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			String newDir = (String) arg0.getAdapter().getItem(arg2);
			if(newDir.equals("..")){
				curDir = curDir.getParentFile();
			}else{
				curDir = new File(curDir.getAbsolutePath()+File.separator+newDir+File.separator);
			}
			updateList();
		}
	};
	
	private OnClickListener onCreateClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v.equals(dialog.findViewById(R.id.createFolder))){
				EditText newDirEditText = (EditText) dialog.findViewById(R.id.dirName);
				String newDirStr = newDirEditText.getEditableText().toString();
				if(newDirStr.isEmpty()){
					return;
				}
				File newDir = new File(curDir.getAbsolutePath()+File.separator+newDirStr);
				if(newDir.mkdir()){
					Principal.auxHandler.post(new Runnable() {
						
						@Override
						public void run() {
							Principal.showToast("Carpeta creada");
						}
					});
					updateList();
				}else{
					Principal.auxHandler.post(new Runnable() {
						
						@Override
						public void run() {
							Principal.showToast("Error al crear carpeta");
						}
					});
				}
			}
		}
	};
	
	private void updateList(){
		TextView thisDir = (TextView) dialog.findViewById(R.id.curDir);
		thisDir.setText("Carpeta actual: " + curDir.getAbsolutePath());
		File[] files = curDir.listFiles();
		ArrayList<String> filesAL = new ArrayList<String>();
		if(!curDir.getAbsolutePath().equals("/mnt"))
			filesAL.add("..");
		for (int i = 0; i < files.length; i++) {
			if(files[i].isDirectory())
				filesAL.add(files[i].getName());
		}
		Collections.sort(filesAL);
		dirList = (ListView) dialog.findViewById(R.id.dirList);
		ArrayAdapter<String> arad = new ArrayAdapter<String>(getContext(), 
				android.R.layout.simple_list_item_1, filesAL);
		dirList.setAdapter(arad);
		dirList.setOnItemClickListener(onItemClick);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if(positiveResult == true){
			if(curDir.canWrite()){
				Editor edit = sp.edit();
				edit.putString("downloadDir", curDir.getAbsolutePath());
				edit.apply();
				Principal.auxHandler.post(new Runnable() {
					
					@Override
					public void run() {
						Principal.showToast("La nueva carpeta de descarga es: "+sp.getString("downloadDir", "a"));
					}
				});
			}else{
				Principal.auxHandler.post(new Runnable() {
					
					@Override
					public void run() {
						Principal.showToast("No se pueden guardar archivos en la carpeta seleccionada.");
					}
				});
			}
		}
	}
}
