package de.uwr1.training;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by f00f on 03.07.2014.
 */
public class Config {
    public static final String logTag = "UWR_Training";

    public static final boolean EMULATE_NETWORK_CONNECTION = false;

    private static final int NUM_BUTTON_TEXTS = 32;

    // Keys for the APP_CONFIG object
    private static final String KEY_NUM_BUTTON_TEXTS = "NUM_BUTTON_TEXTS";
    private static final String KEY_PLAY_STORE_URL = "PLAY_STORE_URL";
    public static final String KEY_BASE_URL = "BASE_URL";
    private static final String KEY_JSON_BASE_URL = "JSON_BASE_URL";
    public static final String KEY_JSON_URL = "JSON_URL";
    public static final String KEY_JSON_PLAYERS_URL = "JSON_PLAYERS_URL";
    public static final String KEY_REFRESH_URL = "REFRESH_URL";
    private static final String KEY_REPLY_URL_YES = "REPLY_URL_YES";
    private static final String KEY_REPLY_URL_NO = "REPLY_URL_NO";
    private static final String KEY_PHOTO_URL = "PHOTO_URL";
    private static final String KEY_PHOTO_THUMB_URL = "PHOTO_THUMB_URL";
    // Keys for SharedPreferences
    public static final String KEY_PREF_DETAILS_VISIBLE = "DETAILS_VISIBLE";
    public static final String KEY_PREF_ABSAGER_VISIBLE = "ABSAGER_VISIBLE";
    public static final String KEY_PREF_NIXSAGER_VISIBLE = "NIXSAGER_VISIBLE";
    private static final String KEY_PREF_INSTALLED_VERSION_ID = "INSTALLED_VERSION_ID";
    private static final String KEY_PREF_INSTALLED_VERSION_NAME = "INSTALLED_VERSION_NAME";

    private static final Map<String, String> APP_CONFIG;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(KEY_NUM_BUTTON_TEXTS, String.valueOf(NUM_BUTTON_TEXTS));
        aMap.put(KEY_PLAY_STORE_URL, "https://play.google.com/store/apps/details?id=de.uwr1.training");
        aMap.put(KEY_BASE_URL, "http://%club_id%.uwr1.de/training/");
        // JSON URLs
        aMap.put(KEY_JSON_BASE_URL, aMap.get(KEY_BASE_URL) + "json/");
        aMap.put(KEY_JSON_URL, aMap.get(KEY_JSON_BASE_URL) + "training.json");
        aMap.put(KEY_JSON_PLAYERS_URL, aMap.get(KEY_JSON_BASE_URL) + "all-players.json");
        // Refresh and reply URLs
        aMap.put(KEY_REFRESH_URL, aMap.get(KEY_BASE_URL) + "training.php?app=android&app_ver=%app_ver%&club_id=%club_id%");
        aMap.put(KEY_REPLY_URL_YES, aMap.get(KEY_REFRESH_URL) + "&zusage=1&text=");
        aMap.put(KEY_REPLY_URL_NO, aMap.get(KEY_REFRESH_URL) + "&absage=1&text=");
        // Photo URLs
        aMap.put(KEY_PHOTO_URL, aMap.get(KEY_BASE_URL) + "badbilder/${location}/");
        aMap.put(KEY_PHOTO_THUMB_URL, aMap.get(KEY_BASE_URL) + "badbilder/thumbs/${location}/");
        APP_CONFIG = Collections.unmodifiableMap(aMap);
    }

    private static Vector<String> versionNames = new Vector<String>();
    private static Vector<String> versionReleaseDates = new Vector<String>();
    private static Vector<String> newFeaturesByVersion = new Vector<String>();

    // PUBLIC METHODS

    public static String getClubId(Context ctx) {
        return Config.getUserConfigValue(ctx, SettingsActivity.KEY_PREF_CLUB);
    }

    public static String getReplyUrl(Context ctx, String text, boolean isZusage) {
        String url = null;
        String url_key = isZusage ? KEY_REPLY_URL_YES : KEY_REPLY_URL_NO;
        try {
            url = Config.getURL(ctx, url_key) + URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) { /* empty */ }
        return url;
    }

    public static String getURL(Context ctx, String key) {
        String club_id = Config.getClubId(ctx);
        int app_ver = Config.getVersionId(ctx);
        String url = Config.getAppConfigValue(key);
        url = url.replace("%club_id%", club_id);
        url = url.replace("%app_ver%", Integer.toString(app_ver));
        return url;
    }

    public static String getUsername(Context ctx) {
        return Config.getUserConfigValue(ctx, SettingsActivity.KEY_PREF_USERNAME).trim();
    }

    // Get the version ID stored in the APK
    public static int getVersionId(Context ctx) {
        int myVersionId = -1; // initialize version id

        try {
            myVersionId = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return myVersionId;
    }
    // Get the version name stored in the APK
    public static String getVersionName(Fragment f) {
        return getVersionName(f.getActivity());
    }

    // Get the version ID which is stored in the user preferences.
    // After an app update this will be out-dated
    public static int getInstalledVersionId(Context ctx) {
        String versionId = "";
        try {
            versionId = getUserConfigValue(ctx, KEY_PREF_INSTALLED_VERSION_ID);
        } catch (Exception e) { /* empty */ }
        if (versionId.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(versionId);
    }
    public static void setInstalledVersion(Context ctx) {
        setUserConfigValue(ctx, KEY_PREF_INSTALLED_VERSION_ID, Integer.toString(getVersionId(ctx)));
        setUserConfigValue(ctx, KEY_PREF_INSTALLED_VERSION_NAME, getVersionName(ctx));
    }

    public static String getAppName(Context ctx) {
        int stringId = ctx.getApplicationInfo().labelRes;
        return ctx.getString(stringId);
    }

    public static int getNumButtonTexts() {
        String num = Config.getAppConfigValue(KEY_NUM_BUTTON_TEXTS);
        return Integer.parseInt(num);
    }

    public static int getNumAvailableButtonTexts() {
        return ShowTrainingActivity.buttonTexts.length;
    }

    public static String getChangeLogSinceVersion(Context ctx, int oldVersionId) {
        populateChangeLog();

        if (oldVersionId < 0) {
            oldVersionId = 0;
        }

        int currentVersionId = getVersionId(ctx);

        StringBuilder changeLog = new StringBuilder();
        for (int i = currentVersionId - 1; i >= oldVersionId; i--) {
            changeLog.append("Version " + versionNames.elementAt(i) + " (" + versionReleaseDates.elementAt(i) + "):\n");
            changeLog.append(newFeaturesByVersion.elementAt(i));
            changeLog.append("\n");
        }

        return changeLog.toString();
    }

    // PRIVATE METHODS

    private static String getAppConfigValue(String key) {
        if (APP_CONFIG.containsKey(key)) {
            return APP_CONFIG.get(key);
        }

        Log.d("UWR_Training::Config", "Parameter " + key + " not set in app config.");
        return null;
    }
    private static String getUserConfigValue(Context ctx, String key) {
        return getUserConfigValue(ctx, key, "");
    }
    private static String getUserConfigValue(Context ctx, String key, String defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getString(key, defaultValue);
    }
    private static void setUserConfigValue(Context ctx, String key, String value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor ed = sharedPref.edit();
        ed.putString(key, value);
        ed.apply();
    }

    // Get the version name that is stored in the APK
    private static String getVersionName(Context ctx) {
        String myVersionName = "not available"; // initialize String

        try {
            myVersionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return myVersionName;
    }

    // Get the version name which is stored in the user preferences.
    // After an app update this will be out-dated
    private static String getInstalledVersionName(Context ctx) {
        return getUserConfigValue(ctx, KEY_PREF_INSTALLED_VERSION_NAME);
    }

    private static void populateChangeLog() {
        addVersionToChangeLog(1, "2014-08-21", "1.0",
                "- Erste Veröffentlichung.\n");
        addVersionToChangeLog(2, "2014-08-21", "1.1",
                "- Infos wie Temperatur und Zeit seit Aktualisierungen werden angezeigt.\n" +
                "- Inhalt ist jetzt scrollbar.\n" +
                "- 3 neue Button-Texte.\n");
        addVersionToChangeLog(3, "2014-08-26", "1.2",
                "- Die Abschnitte Absagen und Nichtssagend sind jetzt klappbar.\n" +
                "- Kleinere Bugs gefixt (Buttons manchmal beide schwarz).\n" +
                "- Icons aktualisiert.\n" +
                "- @-Zeichen nach Namen entfernt.\n" +
                "- 3 neue Button-Texte.\n");
        addVersionToChangeLog(4, "2014-09-08", "1.2.1",
                "- Bug #3 gefixt: Temperatur nicht anzeigen, falls nicht verfügbar\n" +
                "- app-Parameter für alle URLs eingeführt.\n" +
                "- Soft-Keyboard sollte nach Neuladen der Daten verschwinden.\n" +
                "- 1 neuer Button-Text.\n");
        addVersionToChangeLog(5, "2014-09-16", "1.3",
                "- Nixsager Liste funktioniert.\n");
        addVersionToChangeLog(6, "2014-09-16", "1.3.1",
                "- 2 neue Button-Texte (bei v1.3 vergessen).\n");
        addVersionToChangeLog(7, "2014-09-23", "1.4",
                "- Umlaute in Namen und Kommentar funktionieren.\n" +
                "- Menü aufgeteilt: Eines in der normalen Ansicht, und eines für den Einstellungen-Screen.\n" +
                "- \"Zur Webseite\" zum Einstellungen-Menü hinzugefügt.\n" +
                "- Detailiertere Versionsinfos zur Einstellungen-Ansicht hinzugefügt.\n" +
                "- Feedback-Text zur Einstellungen-Ansicht hinzugefügt.\n" +
                "- 2 neue Button-Texte.\n");
        addVersionToChangeLog(8, "2014-09-24", "1.5",
                "- Zeigt eigenen Kommentar wieder an.\n" +
                "- Bug fixes.\n" +
                "- 1 neuer Button Text.\n");
        addVersionToChangeLog(9, "2014-09-25", "1.5.1",
                "- Bug fix: Crash wenn man keinen Kommentar eingegeben hatte.\n" +
                "- 1 neuer Button Text.\n");
        addVersionToChangeLog(10, "2014-10-02", "1.6",
                "- Kann besser damit umgehen, wenn keine Internetverbindung besteht.\n" +
                "- Zeigt beim ersten Start nach einem App-Update die Neuerungen an.\n" +
                "- Die Trainingsdaten können jetzt mit einem Swipe nach unten neu geladen werden.\n" +
                "- 2 neue Button-Texte.\n");
        addVersionToChangeLog(11, "2015-02-20", "1.6.1",
                "- Kaiserslautern aufgenommen.\n" +
                "- 1 neuer Button-Text.\n");
        addVersionToChangeLog(12, "2015-03-19", "1.6.2",
                "- Bug fix: 'Sometimes, the comments field is not cleared' (issue #5)\n" +
                "- 1 neuer Button-Text.\n");
        addVersionToChangeLog(13, "2015-10-22", "1.7.0",
                "- Neu: Deine Mission für jedes Training.\n" +
                "- Buttontexte ändern sich erst, wenn ein neues Training ist.\n" +
                "- Kraken und Damen Süd aufgenommen.\n" +
                "- 3 neue Button Texte.\n");
        addVersionToChangeLog(14, "2015-10-26", "1.8.0",
                "- Übersichtlicheres Layout.\n" +
                "- 3 neue Button Texte.\n");
        addVersionToChangeLog(15, "2015-10-27", "1.8.1",
                "- Bug fix: Crash der PlayStore-Version behoben. Sorry.\n" +
                "- 2 neue Button Texte.\n");
    }

    private static void addVersionToChangeLog(int versionId, String versionReleaseDate, String versionName, String newFeatures) {
        versionNames.add(versionName);
        versionReleaseDates.add(versionReleaseDate);
        newFeaturesByVersion.add(newFeatures);
    }
}
