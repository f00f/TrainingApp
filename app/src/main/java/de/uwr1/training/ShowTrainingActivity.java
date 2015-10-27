package de.uwr1.training;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.util.Random;

public class ShowTrainingActivity
        extends ActionBarActivity
        implements OnAsyncDataLoadedListener, OnApiCallCompletedListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String[][] buttonTexts = new String[][] {
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
            {"Pirat", "privat"},// L & J
            {"Aye Käpt'n", "Meuterei"},// L & J
            {"Yo Ho!", "Oh no"},// L & J
            {"Flut", "Ebbe"},// L & J
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
    private SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        // Create a dummy connection to measure peak concurrency
        Firebase myFirebaseRef = new Firebase("https://uwr-training.firebaseio.com/peak-concurrency-test");
        setContentView(R.layout.activity_overview);

        // Initialization stuff
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Training.Init(this, this, this);

        // Version check: see if the user upgraded the app
        int oldVersionId = Config.getInstalledVersionId(this);
        if (-1 != oldVersionId) {
            // This is an update, show ChangeLog
            int currentVersionId = Config.getVersionId(this);
            if (oldVersionId != currentVersionId) {
                // Show change log.
                AppUpdatedDialogFragment dlg = new AppUpdatedDialogFragment();
                // Supply old version ID as an argument.
                Bundle args = new Bundle();
                args.putInt(AppUpdatedDialogFragment.KEY_OLD_VERSION_ID, oldVersionId);
                dlg.setArguments(args);
                dlg.show(getSupportFragmentManager(), null);
            }
        }

        // Init GUI
        // Reset view visibility
        showView(R.id.view_loading);
        // Initialize SwipeRefreshLayout
        swipeLayout = (SwipeRefreshLayout)findViewById(R.id.view_show_overview);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(
                android.R.color.holo_blue_dark,
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_green_dark
        );
    }

    @Override
    // Handler for SwipeRefreshLayout.
    // Force a data refresh
    public void onRefresh() {
        if (null != swipeLayout) {
            swipeLayout.setRefreshing(true);
        }
        boolean forceReload = true;
        Training.loadTrainingData(forceReload);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isConfigured = true;
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
        Log.d("UWR_Training", "Refreshing data.");

        // load JSON data, rendering will happen async after the data was loaded
        Training.loadTrainingData(forceReload);
        Training.loadPlayersList(forceReload);
    }

    // Render Training data
    // - No TrainingData: Render nothing
    // - No PlayersList: Render TrainingData
    // - Both: render everything
    private void render() {
        if (null != swipeLayout) {
            swipeLayout.setRefreshing(false);
        }

        if (!Training.isTrainingDataLoaded()) {
            Log.e("UWR_Training", "render: Loading training data failed.");
            renderLoadingTrainingDataFailed();
            return;
        }

        Log.d("UWR_Training", "Rendering new data.");

        findViewById(R.id.view_show_overview).scrollTo(0, 0);

        // Seed for random button texts and mission
        long seed = Training.getTimestampOfExpiration() + Config.getUsername(this).hashCode();

        changeButtonTexts(seed);
        showMission(seed);

        setSectionVisibilities();
        renderTrainingData();
        renderNixsagerList();
    }

    private void showView(int viewId) {
        switch (viewId) {
            case R.id.view_loading:
                findViewById(R.id.view_loading).setVisibility(View.VISIBLE);
                findViewById(R.id.view_show_overview).setVisibility(View.GONE);
                break;
            case R.id.view_show_overview:
                findViewById(R.id.view_loading).setVisibility(View.GONE);
                findViewById(R.id.view_show_overview).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void renderLoadingTrainingData() {
        showView(R.id.view_loading);

        TextView loading = (TextView) findViewById(R.id.t_loading);
        loading.setText(getString(R.string.loading_data));
        findViewById(R.id.buttonReloadTrainingData).setVisibility(View.GONE);
    }

    private void renderLoadingTrainingDataFailed() {
        showView(R.id.view_loading);

        // TODO: Maybe the PlayersList load request finishes first (e.g. from Cache), then this would be the wrong message.
        // TODO: But it would be replaced soon, when the TrainingData load request finishes
        TextView loading = (TextView) findViewById(R.id.t_loading);
        loading.setText(getString(R.string.loading_data_failed));
        findViewById(R.id.buttonReloadTrainingData).setVisibility(View.VISIBLE);
    }

    // Set correct visibility of Abgesagt and Nixgesagt sections
    private void setSectionVisibilities() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        TextView detailsBtn = (TextView)findViewById(R.id.details_button);
        if (!Training.hatZugesagt() && !Training.hatAbgesagt()) {
            // Force details to be open and hide button
            overrideSectionVisibility(detailsBtn, findViewById(R.id.details), true);
            detailsBtn.setVisibility(View.GONE);
        } else {
            // Show or hide details according to preferences, and show button
            setSectionVisibility(detailsBtn,
                    findViewById(R.id.details),
                    sharedPref.getBoolean(Config.KEY_PREF_DETAILS_VISIBLE, false));
            detailsBtn.setVisibility(View.VISIBLE);
        }
        setSectionVisibility((TextView)findViewById(R.id.title_absagen),
                findViewById(R.id.training_ab_list),
                sharedPref.getBoolean(Config.KEY_PREF_ABSAGER_VISIBLE, true));
        setSectionVisibility((TextView)findViewById(R.id.title_nixsagen),
                findViewById(R.id.training_nix_container),
                sharedPref.getBoolean(Config.KEY_PREF_NIXSAGER_VISIBLE, false));
    }

    private void renderTrainingData() {
        boolean hatZugesagt = Training.hatZugesagt();
        boolean hatAbgesagt = Training.hatAbgesagt();

        // Button colors
        int normalBtnTextColor = getResources().getColor(android.R.color.primary_text_light);
        int darkBtnBgColor = getResources().getColor(android.R.color.secondary_text_light);
        int darkBtnTextColor = getResources().getColor(android.R.color.primary_text_dark);
        Button btnYes = (Button) findViewById(R.id.buttonYes);
        Button btnNo = (Button) findViewById(R.id.buttonNo);
        btnYes.setBackgroundColor(hatAbgesagt
                ? darkBtnBgColor
                : getResources().getColor(R.color.uwr_green));
        btnYes.setTextColor(hatAbgesagt
                ? darkBtnTextColor
                : normalBtnTextColor);
        btnNo.setBackgroundColor(hatZugesagt
                ? darkBtnBgColor
                : getResources().getColor(R.color.uwr_red));
        btnNo.setTextColor(hatZugesagt
                ? darkBtnTextColor
                : normalBtnTextColor);

        // Fill comment field, if appropriate
        String comment = "";
        if (hatZugesagt || hatAbgesagt) {
            comment = Training.getComment();
            if (null == comment) {
                comment = "";
            }
        }
        ((TextView) findViewById(R.id.edit_comment)).setText(comment);

        showView(R.id.view_show_overview);

        // Display date
        ((TextView) findViewById(R.id.training_date)).setText(getResources().getString(R.string.prefix_next_training) + " " + Training.getDateWithWeekday());
        // Display time and location
        ((TextView) findViewById(R.id.training_time_and_location)).setText(Training.getTimeAndLocation());
        // Display number of Zusagen
        int numZusagen = Training.getNumZusagen();
        if (1 == numZusagen) {
            ((TextView) findViewById(R.id.num_zusagen)).setText("1 " + getResources().getString(R.string.zusagen_singular));
        } else {
            ((TextView) findViewById(R.id.num_zusagen)).setText(Integer.toString(numZusagen) + " " + getResources().getString(R.string.zusagen_plural));
        }

        // Display Zusagen and Absagen (lists of names)
        ((TextView) findViewById(R.id.training_zu_list)).setText(Training.getZusagen());
        ((TextView) findViewById(R.id.training_ab_list)).setText(Training.getAbsagen());

        // Display meta-info
        StringBuilder stats = new StringBuilder();
        stats.append("Daten geladen:  \t").append(formatDateTime(Training.getTimestampOfDownload())).append("\n");
        stats.append("Letzte Meldung:\t").append(formatDateTime(Training.getTimestampOfLastEntry()));
        if (Training.hasExtraTemp()) {
            stats.append("\n")
                    .append("Temperatur: ").append(Training.getExtraTemp()).append("\t").append(formatDateTime(Training.getExtraTempUpdated())).append("");
        }
        ((TextView) findViewById(R.id.training_stats)).setText(stats);

        // hide soft keyboard
        /*getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);*/
        /*
        // clear focus of comment field to hide soft keyboard
        View focused = getCurrentFocus();
        EditText commentField = (EditText)findViewById(R.id.edit_comment);
        if (null != focused && focused == commentField) {
            commentField.clearFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(commentField.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            btnYes.requestFocus();
        }
        */
    }

    private void renderNixsagerList() {
        View nixListNoData = findViewById(R.id.training_nix_no_data);
        LinearLayout nixListView = (LinearLayout) findViewById(R.id.training_nix_list);

        nixListView.removeAllViews();
        Training.populateNixsager(); // TODO: would be nice if that happened in a callback

        if (!Training.isPlayersListLoaded() || 0 == Training.getNumNixsager()) {
            nixListView.setVisibility(View.GONE);
            nixListNoData.setVisibility(View.VISIBLE);
            return;
        }

        NixSagerAdapter adapter = new NixSagerAdapter(this, R.layout.nixsager_list_item, Training.getNixsager());
        final int adapterCount = adapter.getCount();
        for (int i = 0; i < adapterCount; i++) {
            View item = adapter.getView(i, null, null);
            if (i == adapterCount - 1) {
                item.findViewById(R.id.nixsager_list_item_separator).setVisibility(View.GONE);
            }
            nixListView.addView(item);
        }

        nixListNoData.setVisibility(View.GONE);
        nixListView.setVisibility(View.VISIBLE);
    }

    // Click handler for the yes/no buttons
    public void onYesNoClick(View view) {
        boolean success;
        String msg = getString(R.string.msg_reply_failed);

        String text = Config.getUsername(this);
        String comment = ((EditText)findViewById(R.id.edit_comment)).getText().toString();
        if (!comment.isEmpty()) {
            text += " (" + comment.trim() + ")";
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

    // Click handler for the NixsagerList items: shows a dialog
    public void onNixSagerListItemClick(View view) {
        // TODO: create dialog
        CharSequence name = ((TextView)view).getText();
        Log.d("", name.toString());
        NixsagerDialogFragment d = new NixsagerDialogFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putCharSequence(NixsagerDialogFragment.KEY_NAME, name);
        d.setArguments(args);
        d.show(getSupportFragmentManager(), null);
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
            case R.id.action_refresh:
                boolean forceReload = true;
                refresh(forceReload);
                return true;
            case R.id.action_goto_website:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Config.getURL(this, Config.KEY_BASE_URL)));
                startActivity(browserIntent);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Click handler for anything else
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.details_button:
                toggleSectionVisibility((TextView)view, findViewById(R.id.details));
                break;
            case R.id.title_absagen:
                toggleSectionVisibility((TextView)view, findViewById(R.id.training_ab_list));
                break;
            case R.id.title_nixsagen:
                toggleSectionVisibility((TextView) view, findViewById(R.id.training_nix_container));
                break;
            case R.id.btn_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonReloadNixsager:
                {
                    boolean forceReload = true;
                    Training.loadPlayersList(forceReload);
                }
                break;
            case R.id.buttonReloadTrainingData:
                renderLoadingTrainingData();
                {
                    boolean forceReload = true;
                    Training.loadTrainingData(forceReload);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overview, menu);
        return true;
    }

    @Override
    // The callback after loading data async
    public void onAsyncDataLoaded(int statusCode) {
        if (OnAsyncDataLoadedListener.STATUS_ERROR == statusCode) {
            Log.e("", "ShowTrainingActivity: Error loading data.");
        }

        render();
    }

    @Override
    // This callback is only invoked after sending a reply.
    // TODO: Is it?
    public void onApiCallCompleted(String url, String result) {
        Log.e("", "Is this callback really required? Can this be handled inside the Training class?");
        boolean forceReload = true;
        refresh(forceReload);
    }

    // PRIVATE METHODS

    private void showMission(long seed) {
        String missionStr;
        if (Training.hatZugesagt()) {
            Random rand = new Random(seed);
            String[] missions = getResources().getStringArray(R.array.missions);
            missionStr = getResources().getString(R.string.lbl_mission) + " "
                    + missions[rand.nextInt(missions.length)];
        } else {
            if (Training.hatAbgesagt()) {
                missionStr = getString(R.string.mission_abgesagt);
            } else {
                missionStr = getString(R.string.mission_nixgesagt);
            }
        }

        TextView missionView = (TextView)findViewById(R.id.view_mission);
        missionView.setText(missionStr);
    }

    private void changeButtonTexts(long seed) {
        Random rand = new Random(seed);
        int padding = 16;
        int maxButtonTextIndex = Math.min(Config.getNumButtonTexts(), Config.getNumAvailableButtonTexts());
        int buttonTextIndex = rand.nextInt(maxButtonTextIndex);
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

    // This changes the UI look, but does not store the preference
	private void overrideSectionVisibility(TextView header, View content, boolean visible) {
        // display correct icon and content
		showExpandCollapseIcon(header, visible);
        content.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
    // This method changes the UI look and stores the setting in the preferences
    private void setSectionVisibility(TextView header, View content, boolean visible) {
        overrideSectionVisibility(header, content, visible);

        // save to prefs
        String key = "";
        switch (header.getId()) {
            case R.id.details_button:
                key = Config.KEY_PREF_DETAILS_VISIBLE;
                break;
            case R.id.title_absagen:
                key = Config.KEY_PREF_ABSAGER_VISIBLE;
                break;
            case R.id.title_nixsagen:
                key = Config.KEY_PREF_NIXSAGER_VISIBLE;
                break;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEd = sharedPref.edit();
        prefEd.putBoolean(key, visible);
        prefEd.apply();
    }
    private void toggleSectionVisibility(TextView header, View content) {
        setSectionVisibility(header, content, View.GONE == content.getVisibility());
    }

	private void showExpandCollapseIcon(TextView view, boolean isExpanded) {
        Drawable d;
        if (isExpanded) {
			d = getResources().getDrawable(R.drawable.ic_action_collapse);
		} else {
			d = getResources().getDrawable(R.drawable.ic_action_expand);
		}
        if (R.id.details_button == view.getId()) {
            // draw at bottom
            view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d);
        } else {
            // draw at right
            view.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
        }
	}

    private CharSequence formatDateTime(long time) {
        return DateUtils.getRelativeTimeSpanString(time);
    }
}
