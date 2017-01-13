package com.franfj.mtkbuttonled;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String LOGTAG = "MTKLED";
	private static final String ACCESSIBILITY_SERVICE_NAME = "com.franfj.mtkbuttonled/com.franfj.mtkbuttonled.MTKButtonLedAccessibilityService";

	public boolean isAccessibilityEnabled() {
		int accessibilityEnabled = 0;
		boolean accessibilityFound = false;
		try {
			accessibilityEnabled = Settings.Secure.getInt(
					this.getContentResolver(),
					android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
			Log.d(LOGTAG, "ACCESSIBILITY: " + accessibilityEnabled);
		} catch (SettingNotFoundException e) {
			Log.d(LOGTAG,
					"Error finding setting, default accessibility to not found: "
							+ e.getMessage());
		}

		TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(
				':');

		if (accessibilityEnabled == 1) {
			Log.d(LOGTAG, "***ACCESSIBILIY IS ENABLED***: ");

			String settingValue = Settings.Secure.getString(
					getContentResolver(),
					Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			Log.d(LOGTAG, "Setting: " + settingValue);
			if (settingValue != null) {
				TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
				splitter.setString(settingValue);
				while (splitter.hasNext()) {
					String accessabilityService = splitter.next();
					Log.d(LOGTAG, "Setting: " + accessabilityService);
					if (accessabilityService
							.equalsIgnoreCase(ACCESSIBILITY_SERVICE_NAME)) {
						Log.d(LOGTAG,
								"We've found the correct setting - accessibility is switched on!");
						return true;
					}
				}
			}

			Log.d(LOGTAG, "***END***");
		} else {
			Log.d(LOGTAG, "***ACCESSIBILIY IS DISABLED***");
		}
		return accessibilityFound;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivityForResult(intent, 0);
			}
		});

		final Button ledon_b = (Button) findViewById(R.id.led_on_b);
		ledon_b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Process p;
				try {
					p = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(p
							.getOutputStream());
					os.writeBytes("echo 1 > /sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness \n");
					os.writeBytes("exit\n");
					os.flush();
					p.waitFor();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		final Button led_b = (Button) findViewById(R.id.led_off_b);
		led_b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Process p;
				try {
					p = Runtime.getRuntime().exec("su");
					DataOutputStream os = new DataOutputStream(p
							.getOutputStream());
					os.writeBytes("echo 0 > /sys/devices/platform/leds-mt65xx/leds/button-backlight/brightness \n");
					os.writeBytes("exit\n");
					os.flush();
					p.waitFor();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		final TextView copyright_tv = (TextView) findViewById(R.id.textView3);
		Linkify.addLinks(copyright_tv, Linkify.ALL);
		final TextView version_tv = (TextView) findViewById(R.id.textViewVersion);
		try {
			version_tv.append(getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final Button updates_b = (Button)findViewById(R.id.check_updates_b);
		updates_b.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				class MyTask extends AsyncTask<Void, Void, Boolean>{
					private View v;
					MyTask(View _v){
						v = _v;
					}

					protected Boolean doInBackground(Void... params) {					
						try{
						HttpClient httpclient = new DefaultHttpClient();
					    HttpResponse response = httpclient.execute(new HttpGet("http://fvicente.es/documents/MTKButtonLed/latest.version"));
					    StatusLine statusLine = response.getStatusLine();
					    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
					        ByteArrayOutputStream out = new ByteArrayOutputStream();
					        response.getEntity().writeTo(out);
					        out.close();
					        String responseString = out.toString();
					        if( !responseString.equals(getPackageManager().getPackageInfo(
							getPackageName(), 0).versionName)){
					        	return true;					        	
					        }else{
					        	return false;					        	
					        }
					    } else{
					        //Closes the connection.
					        response.getEntity().getContent().close();
					        //Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_checking_updates) , Toast.LENGTH_LONG).show();
					    }
						}catch (ClientProtocolException e) {
							//Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_checking_updates) , Toast.LENGTH_LONG).show();
				        } catch (IOException e) {
				        	//Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_checking_updates) , Toast.LENGTH_LONG).show();
				        } catch (NameNotFoundException e) {
				        	//Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_checking_updates) , Toast.LENGTH_LONG).show();
							e.printStackTrace();
						}
						return null;
					}
					 @Override
				      protected void onPostExecute(Boolean result) { 
						 if (result){
							 Toast.makeText(v.getContext(), v.getContext().getString(R.string.update_available) , Toast.LENGTH_LONG).show();
						 }else{
							 Toast.makeText(v.getContext(), v.getContext().getString(R.string.update_not_available) , Toast.LENGTH_LONG).show();
						 }
				      }

				      @Override
				      protected void onPreExecute() {
				      }

				      @Override
				      protected void onProgressUpdate(Void... values) {
				      }
				}
				Toast.makeText(v.getContext(), v.getContext().getString(R.string.checking_updates) , Toast.LENGTH_SHORT).show();
				new MyTask(v).execute();
			}
		});
		
		final CheckBox showOnlyLoggedApps_cb = (CheckBox)findViewById(R.id.cb_showOnlyLogApps);
		showOnlyLoggedApps_cb.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final CheckBox cb = ((CheckBox)v.findViewById(R.id.cb_showOnlyLogApps));
				//cb.setChecked(!cb.isChecked());
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
				prefs.edit().putBoolean("showOnlyLoggedApps", cb.isChecked()).commit();				
			}
		
		});
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int storedVersion = prefs.getInt("version", 0);
		try {
			final int currentVersion = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
			if (storedVersion < currentVersion) {
				Toast.makeText(this, this.getString(R.string.updated_toast),
						Toast.LENGTH_LONG).show();
				prefs.edit().putInt("version", currentVersion).commit();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(this, PreferencesScreen.class));
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void settingsMenuClick(MenuItem item) {
		// One of the group items (using the onClick attribute) was clicked
		// The item parameter passed here indicates which item it is
		// All other menu item clicks are handled by onOptionsItemSelected()
		startActivity(new Intent(this, PreferencesScreen.class));
	}
}
