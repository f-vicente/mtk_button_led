package com.franfj.mtkbuttonled;

import java.util.List;
import java.util.Set;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class MTKButtonLedAccessibilityService extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		List<CharSequence> notificationList = event.getText();
		for ( CharSequence text : notificationList){
			Log.d(MainActivity.LOGTAG,"Notification text: "+text);
			LogUtils.appendLog("Notification text: "+text);
		}
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		// TODO save this in preferences 
		if (!LogUtils.loadPackagesFromLog().contains(event.getPackageName())){
			LogUtils.appendLog("New notification, screenOn = "+pm.isScreenOn()+" " + LogUtils.PACKAGE_START_TAG + event.getPackageName());
		}else{

			LogUtils.appendLog("New notification, screenOn = "+pm.isScreenOn()+" " + LogUtils.PACKAGE_START_TAG + event.getPackageName() //+" text:"+event.getText()
					//+" parcelableData: "+event.getParcelableData().toString() + " parcelable contents:" +event.getParcelableData().describeContents() + "\n"
					//+"\n\t "+getNotificationText((Notification)event.getParcelableData()));
			);
		}
		if (pm.isScreenOn()){	
			return; // Do nothing if screen is on
		}
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			final Set<String> list = prefs.getStringSet("filter_list", null);
			final boolean blacklistMode = prefs.getBoolean("filter_by_blacklist", false);
			if (blacklistMode){
				for (String str : list){
					if (str.equals(event.getPackageName())){
						return; 
					}
				}
			}else{
				boolean exists = false;
				for (String str : list){
					if (str.equals(event.getPackageName())){
						exists = true;
						break;
					}
				}
				if(!exists){
					return;
				}
			}
			if (event.getPackageName().equals("com.whatsapp")){
				
				if( prefs.getBoolean("filter_whatsapp_groups", false) ){
					for ( CharSequence text : notificationList){
						if ( text.toString().indexOf(" @ ") != -1){
							return;
						}
					}
				}
			}
			Log.d(MainActivity.LOGTAG,"Starting service to light the leds");
			
			Intent service = new Intent(this,BlinkService.class);
			this.startService(service);			
		}		
	}

	@Override
	public void onServiceConnected() {
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
		info.notificationTimeout = 100;
		setServiceInfo(info);
		
		BroadcastReceiver receiver = new UnlockReceiver();
		
		IntentFilter filter = new IntentFilter();//Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(receiver,filter);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("showOnlyLoggedApps", false)){
        	LogUtils.LOG_FILE_PATH = "/dev/null";
        }
        LogUtils.cleanLog();

	}
	
	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}
	
	
	/*
	private String getNotificationText(Notification notification){
	    RemoteViews views = notification.contentView;
	    Class secretClass = views.getClass();

	    try {
	        Map<Integer, String> text = new HashMap<Integer, String>();

	        Field outerFields[] = secretClass.getDeclaredFields();
	        for (int i = 0; i < outerFields.length; i++) {
	            if (!outerFields[i].getName().equals("mActions")) continue;

	            outerFields[i].setAccessible(true);

	            ArrayList<Object> actions = (ArrayList<Object>) outerFields[i].get(views);
	            for (Object action : actions) {
	                Field innerFields[] = action.getClass().getDeclaredFields();

	                Object value = null;
	                Integer type = null;
	                Integer viewId = null;
	                for (Field field : innerFields) {
	                    field.setAccessible(true);
	                    if (field.getName().equals("value")) {
	                        value = field.get(action);
	                    } else if (field.getName().equals("type")) {
	                        type = field.getInt(action);
	                    } else if (field.getName().equals("viewId")) {
	                        viewId = field.getInt(action);
	                    }
	                }

	                if (type == 9 || type == 10) {
	                    text.put(viewId, value.toString());
	                }
	            }

	            //System.out.println("title is: " + text.get(16908310));
	            //System.out.println("info is: " + text.get(16909082));
	            //System.out.println("text is: " + text.get(16908358));
	            return new String("Title: "+text.get(16908310)+"; info: "+text.get(16909082)+"; text: "+text.get(16908358));
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return new String("error while obtaining notification text");
	}*/

}
