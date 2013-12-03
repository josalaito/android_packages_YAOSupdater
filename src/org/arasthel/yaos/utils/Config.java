package org.arasthel.yaos.utils;

public class Config {

	public final static byte ACTUALIZACIONES = 0;
	public final static byte PARCHES = 1;
	public final static byte GAPPS = 2;
	public final static byte INSTALL = 3;
	
	public static String urlJson = "http://content.wuala.com/contents/Arasthel/Android/ICS/Xoom";
	public static String downloadDir = "/sdcard/YAOS/";
	public static String versionProp = "ro.update.version";
}
