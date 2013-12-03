package org.arasthel.yaos.ui;

import org.arasthel.yaos.R;
import org.arasthel.yaos.app.YAOS;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class UpdateInfo extends Fragment {
	
	private WebView htmlView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View containerView = inflater.inflate(R.layout.update_info, container);
		return containerView;
	}
	
	@Override
	public void onStart() {
		htmlView = (WebView) getView().findViewById(R.id.webView1);
        Paint p = new Paint();
		htmlView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, p);
		htmlView.setBackgroundColor(Color.TRANSPARENT);
		changeContent(getResources().getString(R.string.welcome));
		super.onStart();
	}
	
	public void changeContent(String s){
		htmlView = (WebView) getView().findViewById(R.id.webView1);
		String msg = "";
		if(!YAOS.isBigScreen()){
			msg = "<span style=\"font-size:medium\">";
		}
		msg += s;
		htmlView.loadDataWithBaseURL(null,"<h2><font color=\"white\">"+msg+"</font></h2>", "text/html", "utf-8", null);
	}

}
