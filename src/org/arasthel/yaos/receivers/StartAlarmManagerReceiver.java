package org.arasthel.yaos.receivers;

import java.util.Calendar;

import org.arasthel.yaos.services.UpdateService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartAlarmManagerReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("YAOS.AUTOUPDATE")){
			Intent i = new Intent(context,UpdateService.class);
			Log.d("SERVICE","STARTING UPDATE SERVICE");
			context.startService(i);
		}else if(intent.getAction().equals("YAOS.START_AUTOUPDATE") || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
	    	if(!sp.getBoolean("autoUpdate", false)){
	    		return;
	    	}
			Calendar c = Calendar.getInstance();
			int hour = sp.getInt("hour", -1);
			if(hour < 0){
				return;
			}
			int minute = sp.getInt("minute", -1);
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND, 0);
			AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
			Intent i = new Intent("YAOS.AUTOUPDATE");
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			am.cancel(pi);
			am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), Long.parseLong(sp.getString("updateInterval", String.valueOf(AlarmManager.INTERVAL_DAY))), pi);
		}else if(intent.getAction().equals("YAOS.CANCEL_AUTOUPDATE")){
			AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
			Intent i = new Intent("YAOS.AUTOUPDATE");
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			am.cancel(pi);
		}
	}

}
