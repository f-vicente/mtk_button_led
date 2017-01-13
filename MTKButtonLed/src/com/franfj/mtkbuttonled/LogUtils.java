package com.franfj.mtkbuttonled;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtils {

	public static String PACKAGE_START_TAG = "packageName: ";
	public static String LOG_FILE_PATH = Environment.getExternalStorageDirectory()
			+ "/MTKButtonLed.log";
	
	public static ArrayList<String> loadPackagesFromLog() {
		ArrayList<String> list = new ArrayList<String>();
		File logFile = new File(LOG_FILE_PATH);
		try {
			BufferedReader r = new BufferedReader(new FileReader(logFile));
			String line = r.readLine();
			while (line != null) {				
				if (line.indexOf(PACKAGE_START_TAG) != -1) {
					int substrStart = line.indexOf(PACKAGE_START_TAG)
						+ new String(PACKAGE_START_TAG).length();
					String packageName = line.substring(substrStart,
							line.length());
					if (!list.contains(packageName)) {
						list.add(packageName);
					}
				}
				line = r.readLine();
			}
			r.close();
		} catch (FileNotFoundException e) {
			Log.w(MainActivity.LOGTAG, "Log file not found");
			return list;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	public static void appendLog(String text) {
		File logFile = new File(LOG_FILE_PATH);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(DateFormat.getDateInstance().format(new Date()) + text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void cleanLog(){
		ArrayList<String> packages = loadPackagesFromLog();
		File logFile = new File(LOG_FILE_PATH);
		logFile.delete();
		for ( String pkg : packages){
			appendLog(PACKAGE_START_TAG+pkg);
		}
	}
}
