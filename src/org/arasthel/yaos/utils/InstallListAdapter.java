package org.arasthel.yaos.utils;

import java.util.ArrayList;
import java.util.List;

import org.arasthel.yaos.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class InstallListAdapter extends ArrayAdapter<String>{

	private ArrayList<String> lista_archivos;
	private Context context;

	public InstallListAdapter(Context context,
			int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		lista_archivos = (ArrayList<String>) objects;
		this.context = context;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater li = LayoutInflater.from(context);
		View row = li.inflate(R.layout.install_list_item, null);
		TextView titulo = (TextView) row.findViewById(android.R.id.text1);
		titulo.setText(lista_archivos.get(position));
		ImageView clear = (ImageView) row.findViewById(R.id.clearElement);
		clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				remove(getItem(position));
			}
		});
		ImageView up = (ImageView) row.findViewById(R.id.moveUp);
		up.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(position == 0){
					return;
				}else{
					String aux = getItem(position-1);
					remove(getItem(position-1));
					insert(aux, position);
				}
				
			}
		});
		ImageView down = (ImageView) row.findViewById(R.id.moveDown);
		down.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(position == getCount()-1){
					return;
				}else{
					String aux = getItem(position+1);
					remove(getItem(position+1));
					insert(aux, position);
				}
			}
		});
		return row;
	}

	public ArrayList<String> getLista_archivos() {
		return lista_archivos;
	}

	public void setLista_archivos(ArrayList<String> lista_archivos) {
		this.lista_archivos = lista_archivos;
	}
	
	public void clear(){
		clear();
	}
}
