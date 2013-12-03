package org.arasthel.yaos.ui;

import org.arasthel.yaos.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Lista extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View containerView = inflater.inflate(R.layout.listfragment, container);
		return containerView;
	}
	
}