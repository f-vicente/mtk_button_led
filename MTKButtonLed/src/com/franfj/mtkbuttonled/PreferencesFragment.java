package com.franfj.mtkbuttonled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PreferencesFragment extends PreferenceFragment {
	boolean showOnlyLoggedApps = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
		showOnlyLoggedApps = prefs.getBoolean("showOnlyLoggedApps", false);
		addPreferencesFromResource(R.xml.preferences);
		final MultiSelectListPreference list = (MultiSelectListPreference) this
				.findPreference("filter_list");
		final PackageManager pm = this.getActivity().getApplicationContext()
				.getPackageManager();
		ArrayList<String> packages;
		if (showOnlyLoggedApps) {
			packages = LogUtils.loadPackagesFromLog();
			Collections.reverse(packages);
		} else {
			packages = new ArrayList<String>();
			List<PackageInfo> packs = this.getActivity()
					.getApplicationContext().getPackageManager()
					.getInstalledPackages(0);
			List<ApplicationInfo> appInfoList = new ArrayList<ApplicationInfo>();
			for (PackageInfo ai : packs) {
				if (ai.applicationInfo.flags != ApplicationInfo.FLAG_SYSTEM){
					appInfoList.add(ai.applicationInfo);
				}
			}
			Collections.sort(appInfoList,
					new ApplicationInfo.DisplayNameComparator(pm));
			for (ApplicationInfo ai : appInfoList) {
				packages.add(ai.packageName);
			}
		}
		ArrayList<String> names = new ArrayList<String>();
		for (String p : packages) {
			try {
				names.add(pm.getApplicationLabel(pm.getApplicationInfo(p, 0))
						.toString());
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				names.add(p);
				e.printStackTrace();
			}
		}
		String[] packagesVect = new String[packages.size()];
		String[] namesVect = new String[names.size()];
		packages.toArray(packagesVect);
		names.toArray(namesVect);
		list.setEntries(namesVect);
		list.setEntryValues(packagesVect);
		list.setPersistent(true);
	}

}
