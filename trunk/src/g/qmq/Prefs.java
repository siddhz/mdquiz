package g.qmq;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class Prefs extends PreferenceActivity {
	private SharedPreferences prefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load Prefs from code. (Must before xml.)
		setPreferenceScreen(createPreferenceHierarchy());
		// Load Prefs from xml.
		addPreferencesFromResource(R.xml.settings);
		this.setTitle(R.string.setting_title);
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		if (prefs.getString("music_dir", "none") == "none") {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("music_dir", "/sdcard/");
			editor.commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		PreferenceScreen mPrefScreen = (PreferenceScreen) getPreferenceScreen()
				.findPreference("music_dir");
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		mPrefScreen.setSummary(prefs.getString("music_dir", "none"));
		PreferenceScreen lPref = (PreferenceScreen) getPreferenceScreen()
				.findPreference("music_lib");
		lPref.setSummary(prefs.getString("music_lib", this.getResources()
				.getString(R.string.lib_defSum)));
	}

	private PreferenceScreen createPreferenceHierarchy() {
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(
				this);
		PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
		inlinePrefCat.setTitle(R.string.cat1);
		root.addPreference(inlinePrefCat);

		/*
		 * Music folder selection pref. (Intent)
		 */
		PreferenceScreen intentPref = getPreferenceManager()
				.createPreferenceScreen(this);
		intentPref.setIntent(new Intent(this, DirBrowser.class));
		intentPref.setTitle(R.string.config_musicFolder);
		intentPref.setDefaultValue("/");
		intentPref.setKey("music_dir");
		prefs = getSharedPreferences("g.qmq_preferences", 0);
		intentPref.setSummary(prefs.getString("music_dir", "none"));
		inlinePrefCat.addPreference(intentPref);

		/*
		 * Search sub-folder.(CheckBox)
		 */
		CheckBoxPreference nextScreenCheckBoxPref = new CheckBoxPreference(this);
		nextScreenCheckBoxPref.setKey("music_searchSubFolder");
		nextScreenCheckBoxPref.setTitle(R.string.config_subFolder);
		nextScreenCheckBoxPref.setSummary(R.string.config_subFolder_summOn);
		nextScreenCheckBoxPref.setSummaryOff(R.string.config_subFolder_summOff);
		nextScreenCheckBoxPref.setDefaultValue(true);
		inlinePrefCat.addPreference(nextScreenCheckBoxPref);
		
		/*
		 * Music library set up. (Intent)
		 */
		PreferenceScreen libPref = getPreferenceManager()
				.createPreferenceScreen(this);
		libPref.setIntent(new Intent(this, libBuilder.class));
		libPref.setKey("music_lib");
		libPref.setTitle(R.string.lib_title);
		libPref.setSummary(R.string.lib_defSum);
		inlinePrefCat.addPreference(libPref);



		return root;
	}
}
