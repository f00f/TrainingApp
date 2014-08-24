package de.uwr1.training;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class ShowTrainingActivity extends ActionBarActivity implements OnTrainingDataLoadedListener, OnApiCallCompletedListener {
    private static final String[] othersTexts = new String[] {
            "Die Anderen:",
            "Die Gang:",
    };
    private static final String[][] buttonTexts = new String[][] {
            {"Zusage", "Absage"},
            {"Zu", "Ab"},
            {"Yo", "No"},
            {"Yay", "Nay"},
            {"Ja", "Nein"},
            {"Jarp", "Narp"},
            {"Yes", "No"},
            {"Go", "Low"},
            {"Dabei", "Daheim"},
            {"+1", "-1"},
            {"Plus", "Minus"},
            {"In", "Out"},
            {"Mit", "Ohne"},
            {"Mit", "Mit ohne"},
            {"Zocken", "Faulenzen"},
            {"Zockbock", "Chlorallergie"},
            {"nass", "trocken"},
            {"dafür", "dagegen"},
            {"okokok", "mimimi"},
            {"Spiel", "-verderber"},
            {"<3", "#%$!"},
            {":)", ":("},
            {"Atemnot", "Chillen"},
            {"rein", "raus"},
            {"unten", "oben"},
            {"Seebär", "Landratte"},
            {"Wasserratte", "Trockenbrot"},
            {"saftig", "döör"},
            {"UWR", "Hallenhalma"},
            {"zaubern", "langweilen"},
            {"Mitläufer", "Wegläufer"},
            {"Warmduscher", "Stinker"},
            {"Sauber", "Saubär"},
            {"Alge", "Flechte"},
            {"4-check", "0-check"},
            {"Flosse", "Sneaker"},
            {"UW", "ÜW"},
            {"Chlorig", "Miefig"},
            {"all-in", "passe"},
            /*
            {"Untenrum", "???"},
            {"Go Deeper", "???"},
            {"*platsch*", "???"},
            {"*blubb*", "???"},
            {"*spritz*", "???"},
            {"runter", "???"},
            {"???", "faul"},
            */
    };
    private static final String[][][] buttonTextsExtra = new String[][][] {
            {
                    {"Bambados", "Bullerbü"},
                    {"-4,00m", "0,00m"},
            },
            {
                    {"Zapfendorf", "Bullerbü"},
                    {"-3,50m", "0,00m"},
            },
    };
    private int buttonTextIndex = 0;
    private boolean isConfigured;

    private void ChangeButtonTexts() {
        Random rand = new Random();
        int padding = 16;
        int maxButtonTextIndex = Math.min(10, buttonTexts.length);
        buttonTextIndex = rand.nextInt(maxButtonTextIndex);
        Button btnYes = (Button)findViewById(R.id.buttonYes);
        Button btnNo = (Button)findViewById(R.id.buttonNo);

        btnYes.setText(buttonTexts[buttonTextIndex][0]);
        btnNo.setText(buttonTexts[buttonTextIndex][1]);

        ViewGroup.LayoutParams lp;

        lp = btnYes.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        btnYes.setLayoutParams(lp);
        btnYes.setPadding(2 * padding, padding, 2 * padding, padding);

        lp = btnNo.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        btnNo.setLayoutParams(lp);
        btnNo.setPadding(padding, padding, padding, padding);

        btnYes.invalidate();
        btnNo.invalidate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Training.Init(this, this, this);

        setContentView(R.layout.activity_overview);

        ChangeButtonTexts();

        // Reset view visibility
        findViewById(R.id.view_show_overview).setVisibility(View.GONE);
        findViewById(R.id.view_loading).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        isConfigured = true;
        // check if username is set
        String usernamePref = Config.getUsername(this);
        if (usernamePref.isEmpty())
            isConfigured = false;
        // check if config was loaded
        String appConfig = Config.getClubId(this);
        if (appConfig.isEmpty())
            isConfigured = false;

        if (isConfigured) {
            ((TextView)findViewById(R.id.t_loading))
                    .setText(R.string.loading_data);
            findViewById(R.id.btn_settings).setVisibility(View.GONE);

            refresh();
        } else {
            ((TextView)findViewById(R.id.t_loading))
                    .setText(R.string.msg_welcome);
            findViewById(R.id.btn_settings).setVisibility(View.VISIBLE);
        }
    }

    private void refresh() {
        boolean forceReload = false;
        refresh(forceReload);
    }
    private void refresh(boolean forceReload) {
        Log.d("UWR_Training::ShowTraining::refresh", "Refreshing data.");

        // load JSON data, rendering will happen async after the data was loaded
        Training.loadTrainingData(forceReload);
    }

    // render Training data
    private void render() {
        if (!Training.isLoaded()) {
            // Switch views
            findViewById(R.id.view_loading).setVisibility(View.VISIBLE);
            findViewById(R.id.view_show_overview).setVisibility(View.GONE);

            TextView loading = (TextView) findViewById(R.id.t_loading);
            loading.setText("Error: Loading training data failed.");

            Log.e("UWR_Training::ShowTraining::render", "Loading training data failed.");
            return;
        }

        Log.d("UWR_Training::ShowTraining::render", "Rendering new data.");

        findViewById(R.id.view_show_overview).scrollTo(0, 0);

        Color normalBtnTextColor = getResources().getColor(android.R.color.primary_text_light);
        Color darkBtnBgColor = getResources().getColor(android.R.color.secondary_text_light);
        Color darkBtnTextColor = getResources().getColor(android.R.color.primary_text_dark);

        boolean hatZugesagt = Training.hatZugesagt();
        boolean hatAbgesagt = Training.hatAbgesagt();
        Button btnYes = (Button) findViewById(R.id.buttonYes);
        Button btnNo = (Button) findViewById(R.id.buttonNo);

        btnYes.setBackgroundColor(hatAbgesagt
                                    ? darkBtnBgColor
                                    : getResources().getColor(android.R.color.uwr_green));
        btnYes.setTextColor(hatAbgesagt
                                    ? darkBtnTextColor
                                    : normalBtnTextColor);
        btnNo.setBackgroundColor(hatZugesagt
                                    ? darkBtnBgColor
                                    : getResources().getColor(android.R.color.uwr_red));
        btnNo.setTextColor(hatZugesagt
                                    ? darkBtnTextColor
                                    : normalBtnTextColor);

        // Switch views
        findViewById(R.id.view_loading).setVisibility(View.GONE);
        findViewById(R.id.view_show_overview).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.training_general_info)).setText(Training.getGeneralInfo());
        ((TextView) findViewById(R.id.sum_zu)).setText(Integer.toString(Training.getNumZusagen()));
        ((TextView) findViewById(R.id.sum_ab)).setText(Integer.toString(Training.getNumAbsagen()));

        TextView zu = (TextView)findViewById(R.id.training_zu);
        zu.setText(Training.getZusagen());
        TextView ab = (TextView)findViewById(R.id.training_ab);
        ab.setText(Training.getAbsagen());

		// set visibility of Abgesagt and Nixgesagt
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		setSectionVisibility((TextView)findViewById(R.id.title_absagen),
							(TextView)findViewById(R.id.training_ab),
							sharedPref.getBoolean(SettingsActivity.KEY_PREF_ABSAGER_VISIBLE, false));
		setSectionVisibility((TextView)findViewById(R.id.title_nixsagen),
							(TextView)findViewById(R.id.training_nix),
							sharedPref.getBoolean(SettingsActivity.KEY_PREF_NIXSAGER_VISIBLE, false));

        /*
        ListView nixListView = (ListView) findViewById(R.id.training_nix_list);
        if (0 == Training.getNumNixsager()) {
            ((TextView) findViewById(R.id.training_nix)).setVisibility(View.VISIBLE);
            nixListView.setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.training_nix)).setVisibility(View.GONE);
            nixListView.setVisibility(View.VISIBLE);
            ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, Training.getNixsagerArray());
            nixListView.setAdapter(adapter);
        }
        */

        // update stats
        StringBuilder stats = new StringBuilder();
        stats.append("Daten geladen:  \t").append(formatDateTime(Training.getTimestampOfDownload())).append("\n");
        stats.append("Letzte Meldung:\t").append(formatDateTime(Training.getTimestampOfLastEntry()));
        if (Training.hasExtraTemp()) {
            stats.append("\n")
                .append("Temperatur: ").append(Training.getExtraTemp()).append("\t").append(formatDateTime(Training.getExtraTempUpdated())).append("");
        }
        ((TextView)findViewById(R.id.training_stats)).setText(stats);
    }

    // Click handler for the yes/no buttons
    public void onYesNoClick(View view) {
        boolean success = false;
        String msg = getString(R.string.msg_reply_failed);

        String text = Config.getUsername(this);
        String comment = ((EditText)findViewById(R.id.edit_comment)).getText().toString();
        if (!comment.isEmpty()) {
            text += " (" + comment + ")";
        }
        switch (view.getId()) {
            case R.id.buttonYes:
                success = Training.sendZusage(text);
                if (success) {
                    msg = getString(R.string.msg_reply_yes);
                }
                break;
            case R.id.buttonNo:
                success = Training.sendAbsage(text);
                if (success) {
                    msg = getString(R.string.msg_reply_no);
                }
                break;
        }

        //refresh();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    // Click handler for anything else
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_absagen:
                toggleSectionVisibility((TextView)view, (TextView)findViewById(R.id.training_ab));
                break;
            case R.id.title_nixsagen:
                toggleSectionVisibility((TextView)view, (TextView)findViewById(R.id.training_nix));
                break;
        }
    }
    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_refresh) {
            boolean forceReload = true;
            refresh(forceReload);
            return true;
        }
        if (id == R.id.action_reset) {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
            Toast.makeText(this, R.string.msg_reset, Toast.LENGTH_LONG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrainingDataLoaded() {
        render();
    }

    @Override
    public void onApiCallCompleted(String url, String result) {
        // this method is only invoked after sending a reply.
        boolean forceReload = true;
        refresh(forceReload);
    }

	private void setSectionVisibility(TextView header, TextView content, boolean visible) {
		int newVisibility;
		if (visible) {
			newVisibility = View.VISIBLE;
			hideExpandIcon(header);
		} else {
			newVisibility = View.Gone;
			showExpandIcon(header);
		}
		content.setVisibility(newVisibility);

		// save to prefs
		String key = "";
		switch (header.getId()) {
            case R.id.title_absagen:
				key = SettingsActivity.KEY_PREF_ABSAGER_VISIBLE;
				break;
            case R.id.title_nixsagen:
				key = SettingsActivity.KEY_PREF_NIXSAGER_VISIBLE;
				break;
		}
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.setBoolean(key, visible);
	}
	private void toggleSectionVisibility(TextView header, TextView content) {
		setVisibility(header, content, View.GONE == content.getVisibility());
	}
	private void showExpandIcon(TextView view) {
		String text = view.getText();
		if (!text.endsWith("+")) {
			text = text + "+";
			view.setText(text);
		}
	}
	private void hideExpandIcon(TextView view) {
		String text = view.getText();
		if (text.endsWith("+")) {
			text = text.substring(0, text.length - 1).trim();
			view.setText(text);
		}
	}

    private CharSequence formatDateTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time);
    }
}
