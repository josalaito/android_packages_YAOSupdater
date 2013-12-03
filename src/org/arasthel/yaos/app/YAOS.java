package org.arasthel.yaos.app;
import android.app.Application;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import org.acra.*;
import org.acra.annotation.*;


@ReportsCrashes(formKey = "dGFtY1o0dFd6QkVkNHJuVUFpNWRtRGc6MQ") 
public class YAOS extends Application {
	
	public static YAOS app;
	public static WindowManager wm;

	@Override
	public void onCreate() {
		ACRA.init(this);
		app = this;
		wm = (WindowManager) app.getSystemService(WINDOW_SERVICE);
		super.onCreate();
	}
	
	public static boolean isBigScreen(){
		Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        if(metrics.widthPixels > 1024 || metrics.heightPixels > 1024){
        	return true;
        }
        return false;
	}
	
}
