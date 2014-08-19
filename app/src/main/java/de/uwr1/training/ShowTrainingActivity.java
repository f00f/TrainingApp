package de.uwr1.training;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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


/* TODO
 * + Add version auto-inc
 * + Make cache persistent
 * + Add link to Einteilung (or software)
 * + Add Badbild (PHOTO_[THUMB_]URL, replace ${location}, temp replace path in JSON pic.thumb/full)
 * + Show Temp
 * + Show stats: TimeStamp, Updated, etc.
 * + Nachfragen, x Std vorm Training nachfragen: hey, was is los?
 * + Add Nichtsgesagt list / load all-players JSON / an-/abmelden von anderen
 * + Push-Notifications (mit ja/nein)
 * + Toasts abwechslungsreich machen
 * + Toasts erst zeigen, nachdem der Async call fertig ist.
 * + Tilman: Fahrplanung, Statistik-Tool
 */
public class ShowTrainingActivity extends ActionBarActivity implements OnTrainingDataLoadedListener, OnApiCallCompletedListener {
    private static final String[] othersTexts = new String[] {
            "Die Anderen:",
            "Die Gang:",
    };
    private static final String[][] buttonTexts = new String[][] {
            {"Yo", "No"},
            {"Yay", "Nay"},
            {"Ja", "Nein"},
            {"Jarp", "Narp"},
            {"Yes", "No"},
            {"Zu", "Ab"},
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
        int maxButtonTextIndex = Math.min(7, buttonTexts.length);
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

        boolean hatZugesagt = Training.hatZugesagt();
        boolean hatAbgesagt = false;
        Button btnYes = (Button) findViewById(R.id.buttonYes);
        Button btnNo = (Button) findViewById(R.id.buttonNo);
        btnYes.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        btnNo.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        if (hatZugesagt) {
            btnNo.setBackgroundColor(getResources().getColor(android.R.color.secondary_text_light));
            btnNo.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        } else {
            hatAbgesagt = Training.hatAbgesagt();
            if (hatAbgesagt) {
                btnYes.setBackgroundColor(getResources().getColor(android.R.color.secondary_text_light));
                btnYes.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
            }
        }

        // Switch views
        findViewById(R.id.view_loading).setVisibility(View.GONE);
        findViewById(R.id.view_show_overview).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.training_meta)).setText(Training.getMeta());
        ((TextView) findViewById(R.id.sum_zu)).setText(Integer.toString(Training.getNumZusagen()));
        ((TextView) findViewById(R.id.sum_ab)).setText(Integer.toString(Training.getNumAbsagen()));

        TextView zu = (TextView)findViewById(R.id.training_zu);
        zu.setText(Training.getZusagen());
        TextView ab = (TextView)findViewById(R.id.training_ab);
        ab.setText(Training.getAbsagen());
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
    }

    //
    public void onYesNoClick(View view) {
        boolean success = false;
        String msg = getString(R.string.msg_reply_failed);

        String text = Config.getUsername(this) + "@";
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
}
