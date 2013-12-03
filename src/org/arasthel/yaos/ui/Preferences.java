package org.arasthel.yaos.ui;

import java.util.Calendar;

import org.arasthel.yaos.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.preference.Preference.OnPreferenceChangeListener;

public class Preferences extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		updateTimePickerTime();
		updateIntervalPreference();
		SwitchPreference autoUpdate = (SwitchPreference) findPreference("autoUpdate");
		autoUpdate.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if((Boolean) newValue == true){
					doScheduledSearch();
				}else{
					cancelSchedule();
				}
				return true;
			}
		});
		TimePickerDialog time = (TimePickerDialog) findPreference("hourtoSearch");
		time.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				doScheduledSearch();
				Log.d("TIMEPICKER","PREFERENCE CHANGED");
				return false;
			}
		});
		Preference donate = findPreference("donate");
		donate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=FQP2G9WG82CAC&lc=ES&item_name=EDGE%20Xoom%20y%20YAOS%20Updater&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"));
				startActivity(i);
				return false;
			}
		});
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus){
			Preference downloadDir = findPreference("selectDir");
			downloadDir.setSummary("Carpeta de descarga actual: "+PreferenceManager.getDefaultSharedPreferences(this).getString("downloadDir", "/mnt/sdcard/"));
		}
	}
	
	private void doScheduledSearch(){
		sendBroadcast(new Intent("YAOS.START_AUTOUPDATE"));
    }
	
	private void cancelSchedule(){
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent("YAOS.AUTOUPDATE");
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.cancel(pi);
	}


	private void updateTimePickerTime(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		int minutes = sp.getInt("minute", 0);
		String minutesStr;
		if(minutes < 10){
			minutesStr = "0"+minutes;
		}else{
			minutesStr = String.valueOf(minutes);
		}
		Preference tp = findPreference("hourtoSearch");
		tp.setTitle("Hora a la que buscar actualizaciones: "+sp.getInt("hour", 12)+":"+minutesStr);
	}
	
	private void updateIntervalPreference(){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String interval = sp.getString("interval", "86400000");
		String text = "";
		if(interval.equals("86400000")){
			text = "diariamente";
		}else if(interval.equals("3600000")){
			text = "cada hora";
		}else if(interval.equals("1800000")){
			text = "cada media hora";
		}else if(interval.equals("900000")){
			text = "cada 15 minutos";
		}
		Preference intervalPref = findPreference("updateInterval");
		intervalPref.setTitle("Intervalo entre búsquedas: "+text);
	}
}
