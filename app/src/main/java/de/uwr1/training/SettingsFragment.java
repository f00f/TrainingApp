package de.uwr1.training;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.machinarius.preferencefragment.PreferenceFragment;

/**
 * Created by f00f on 07.07.2014.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String summary;
        summary = sharedPreferences.getString(key, "");
        if (key.equals(SettingsActivity.KEY_PREF_USERNAME)) {
            if (summary.isEmpty()) {
                summary = getString(R.string.pref_username_summary_default);
            }
            findPreference(key)
                    .setSummary(summary);
        }
        if (key.equals(SettingsActivity.KEY_PREF_CLUB)) {
            if (summary.isEmpty()) {
                summary = getString(R.string.pref_club_summary_default);
            }
            findPreference(key)
                    .setSummary(summary);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Update summaries
        String key;
        String summary;
        key = SettingsActivity.KEY_PREF_USERNAME;
        summary = sharedPreferences.getString(key, "");
        if (summary.isEmpty()) {
            summary = getString(R.string.pref_username_summary_default);
        }
        findPreference(key)
                .setSummary(summary);

        key = SettingsActivity.KEY_PREF_CLUB;
        summary = sharedPreferences.getString(key, "");
        //getArray(R.array.pref_club_entryValues)
        if (summary.isEmpty()) {
            summary = getString(R.string.pref_club_summary_default);
        }
        findPreference(key)
                .setSummary(summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
