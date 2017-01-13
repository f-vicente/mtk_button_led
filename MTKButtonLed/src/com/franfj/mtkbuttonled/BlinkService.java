package com.franfj.mtkbuttonled;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BlinkService extends IntentService {

	private boolean blinking = false;
	private boolean running = false;
	private boolean blink = true;

	private Process p;

	public BlinkService() {
		super("MTKBlinkButtonLedService");
		Log.d(MainActivity.LOGTAG, "Constructor BlinkService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		blink = prefs.getBoolean("blink", false);
		if (!blinking) {
			synchronized (this) {
				blinking = true; running = true;
			}
			try {
				if (PermissionChecker
						.isWritable("/sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness ")) {
					p = Runtime.getRuntime().exec("/system/bin/sh");
				} else {
					p = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(
							p.getOutputStream());
					os.writeBytes("chmod 666 /sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness\n");
					os.flush();
				}
				DataOutputStream os = new DataOutputStream(p.getOutputStream());
				Log.d(MainActivity.LOGTAG, "Start led notification, blink:"+blink);
				//if(blink){
					while (running) {
						os.writeBytes("echo `cat /sys/devices/platform/leds-mt65xx/leds/button-backlight/max_brightness` > /sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness \n");
						os.flush();
						Long time1 = System.currentTimeMillis();
						android.os.SystemClock.sleep(800);
						Long time2 = System.currentTimeMillis();
						Log.d(MainActivity.LOGTAG,"On for 800 msecs, but msecs = "+(time1-time2));
						if (!blink) {
							os.writeBytes("echo `cat /sys/devices/platform/leds-mt65xx/leds/button-backlight/max_brightness` > /sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness \n");
							android.os.SystemClock.sleep(20000);
						}else{
							os.writeBytes("echo 0 > /sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness \n");							
						}
						os.flush();
						android.os.SystemClock.sleep(1200);
					}
					os.writeBytes(" exit \n");
					os.flush();
					p.waitFor();
					blinking = false;
					running = false;				
			} catch (IOException e) {
				Log.e(MainActivity.LOGTAG, "Exception!: " + e.getMessage());
				blinking = false;
				running = false;
			} catch (InterruptedException e) {
				Log.e(MainActivity.LOGTAG, "Exception!: " + e.getMessage());
				blinking = false;
				running = false;
			}

		}
	}

	@Override
	public void onDestroy() {
		Log.d(MainActivity.LOGTAG, "Stop Service... stop blinking and destroy service");
		running = false;
		//if (!blink){
			try {
				Process p2 = Runtime.getRuntime().exec("/system/bin/sh");
				DataOutputStream os = new DataOutputStream(p2.getOutputStream());
				os.writeBytes("echo 0 > /sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness \n");
				os.writeBytes(" exit \n");
				os.flush();
				p2.waitFor();
			} catch (IOException e) {
				Log.e(MainActivity.LOGTAG, "Exception!: " + e.getMessage());
			} catch (InterruptedException e) {
				Log.e(MainActivity.LOGTAG, "Exception!: " + e.getMessage());
			}
		//}
		Log.d(MainActivity.LOGTAG,"stop service super.onDestroy");
		super.onDestroy();
	}

}

