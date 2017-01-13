package com.franfj.mtkbuttonled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

public class UnlockReceiver extends BroadcastReceiver {
	private static String lastKnownPhoneState = null;
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
			Log.d(MainActivity.LOGTAG,"Unlock screen sending stop service...");
			Intent service = new Intent(context,BlinkService.class);
			context.stopService(service);
        }		
		else if( intent.getAction().equals(SMS_RECEIVED)){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			if (!prefs.getBoolean("notifySMS", false)){
				return;
			}
//			final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
//
//			Cursor c = context.getContentResolver().query(SMS_INBOX, null, "read = 0", null, null);
//			int unreadMessagesCount = c.getCount();
//			if (unreadMessagesCount > 0){
				Intent service = new Intent(context,BlinkService.class);
				context.startService(service);
			//}
		}
		else if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			if (!prefs.getBoolean("notifyTelephone", true)){
				return;
			}
			String newPhoneState = intent.hasExtra(TelephonyManager.EXTRA_STATE) ? intent.getStringExtra(TelephonyManager.EXTRA_STATE) : null;
			Log.d(MainActivity.LOGTAG,"New phone call " + newPhoneState + " EXTRA_STATE_IDLE="+TelephonyManager.EXTRA_STATE_IDLE);
			//See if the new state is 'ringing'
            if(newPhoneState != null && newPhoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            	lastKnownPhoneState = TelephonyManager.EXTRA_STATE_RINGING;
            }
            newPhoneState = intent.hasExtra(TelephonyManager.EXTRA_STATE) ? intent.getStringExtra(TelephonyManager.EXTRA_STATE) : null;
            if(newPhoneState != null && newPhoneState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            	lastKnownPhoneState = TelephonyManager.EXTRA_STATE_OFFHOOK;
            }
            newPhoneState = intent.hasExtra(TelephonyManager.EXTRA_STATE) ? intent.getStringExtra(TelephonyManager.EXTRA_STATE) : null;
            if(newPhoneState != null && newPhoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            	if( lastKnownPhoneState != null && lastKnownPhoneState.equals(TelephonyManager.EXTRA_STATE_RINGING) ){
            		// TODO this works, but it's not the best way...
            		android.os.SystemClock.sleep(1000);
            		String[] projection = { CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE };
            		String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1";          
            	    Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,where, null, null);
        	        if(c.getCount() > 0){
        	        	Intent service = new Intent(context,BlinkService.class);
        				context.startService(service);	
        				lastKnownPhoneState = null;
        	        }
        	        Log.d(MainActivity.LOGTAG,"Notify a missed call, count = "+c.getCount());
            	}
            }
		}
	}


}
