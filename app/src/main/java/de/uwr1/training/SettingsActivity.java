package de.uwr1.training;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by f00f on 07.07.2014.
 */
public class SettingsActivity extends ActionBarActivity {
    static final String KEY_PREF_USERNAME = "username";
    static final String KEY_PREF_CLUB = "club";
    static final String KEY_PREF_APP_CONFIG = "x-invalid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // clean preferences (e.g. trim strings)
    }
}
