package org.arasthel.yaos.services;

import java.sql.Time;

import org.arasthel.yaos.R;
import org.arasthel.yaos.online.OnlineJSON;
import org.arasthel.yaos.ui.Principal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Thread updateThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				Log.d("THREAD","THREAD STARTED");
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				OnlineJSON json = new OnlineJSON(sp.getBoolean("only_stables", false), false);
				Log.d("THREAD","JSON READ at "+new Time(System.currentTimeMillis()).toGMTString());
				if(json.getUpdates().length != 0){
					NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
					Notification.Builder builder = new Notification.Builder(getApplicationContext());
					builder.setTicker("Actualizaciones encontradas");
					builder.setContentTitle("Actualizaciones encontradas");
					builder.setContentText("Se encontraron actualizaciones para la ROM");
					builder.setSmallIcon(R.drawable.yaos_small);
					builder.setWhen(System.currentTimeMillis());
					builder.setAutoCancel(true);
					Intent i = new Intent(getApplicationContext(), Principal.class);
					i.putExtra("jsonFile", json.getJsonFile());
					PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
					builder.setContentIntent(pi);
					nm.notify(0, builder.getNotification());
				}
					
			}
		});
		updateThread.start();
		super.onStart(intent, startId);
	}
}
