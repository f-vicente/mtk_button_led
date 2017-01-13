package com.franfj.mtkbuttonled;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesScreen extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new PreferencesFragment())
        .commit();
    }
	
}
