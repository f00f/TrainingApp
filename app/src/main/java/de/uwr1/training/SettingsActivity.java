package de.uwr1.training;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * Created by f00f on 07.07.2014.
 */
public class SettingsActivity extends ActionBarActivity {
    static final String KEY_PREF_USERNAME = "username";
    static final String KEY_PREF_CLUB = "club";
    static final String KEY_PREF_ABSAGER_VISIBLE = "ABSAGER_VISIBLE";
    static final String KEY_PREF_NIXSAGER_VISIBLE = "NIXSAGER_VISIBLE";

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    // Click handler for the menu
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_goto_website:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.getURL(this, Config.KEY_BASE_URL)));
                startActivity(browserIntent);
                return true;
            case R.id.action_server_reload:
                Training.requestServerReload();
                return true;
            case R.id.action_reset_app:
                PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
                Toast.makeText(this, R.string.msg_reset, Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
