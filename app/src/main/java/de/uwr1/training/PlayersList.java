package de.uwr1.training;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by f00f on 09.09.2014.
 */
public class PlayersList implements OnApiCallCompletedListener {
    public class PlayersListData {
        String[] Names;
        long Expires; // [s] wann sollen die Daten auf jeden Fall neu geladen werden?
        long Timestamp; // [ms] wann wurde das JSON runtergeladen?

        boolean isExpired() {
            long now = new Date().getTime();
            boolean isExpired = now > (this.Expires * 1000);
            Log.d("PlayerList::isExpired", isExpired + "; now = " + Long.toString(now) + "; Exp = " + Long.toString(this.Expires));
            return isExpired;
        }

        boolean setExpiration(long millis) {
            // TODO: implement
            return true;
        }

        // Load data from several JSON strings
        boolean parseJSON(String json) {
            //DBG
            if (BuildConfig.DEBUG && Config.EMULATE_NETWORK_CONNECTION) {
                JSONArray joTest;
                try {
                    // Test if the names array exists
                    joTest = new JSONObject(json).getJSONArray("names");
                } catch(JSONException e) {
                    // names array is not present -> generate a stub data set
                    json = "{\"names\":[\"Lorem\", \"Ipsum\", \"Dolor\", \"Lorem2\", \"Ipsum2\", \"Dolor2\", \"Lorem3\", \"Ipsum3\", \"Dolor3\", \"Lorem4\", \"Ipsum4\", \"Dolor4\", \"Lorem5\", \"Ipsum5\", \"Dolor5\"]}";
                }
            }
            // /DBG

            JSONObject jo;
            try {
                jo = new JSONObject(json);
            } catch(JSONException e) {
                Log.e("PlayersListData::parseJSON", "Unable to parse JSON data.");
                return false;
            }

            try {
                Names = JsonHelper.getStringArray(jo, "names");
            } catch(JSONException e) { /* empty */ }

            Timestamp = new java.util.Date().getTime();
            Expires = Timestamp + CACHE_MAX_AGE;

            return true;
        }
    }

    private static Context context;
    // Data and the callback listener
    private static PlayersListData data = null;
    private static OnAsyncDataLoadedListener onAsyncDataLoadedListener;
    // Cache
    private static HashMap<String, PlayersListData> cache = new HashMap<String, PlayersListData>();
    private static final int CACHE_MAX_AGE = 60 * 60; // cache lifetime in seconds

    // CONSTRUCTOR

    private PlayersList() {}

    // PUBLIC METHODS

    public static void Init(Context _context/*, OnApiCallCompletedListener _onReplySentListener*/, OnAsyncDataLoadedListener _onAsyncDataLoadedListener) {
        PlayersList.data = null;

        PlayersList.context = _context;
        PlayersList.onAsyncDataLoadedListener = _onAsyncDataLoadedListener;
    }

    public static boolean isLoaded() {
        return PlayersList.data != null;
    }

    public static void load() {
        boolean forceReload = false;
        load(forceReload);
    }
    public static void load(boolean forceReload) {
        PlayersList.data = null;
        String url = Config.getURL(context, Config.KEY_JSON_PLAYERS_URL);
        loadPlayersListCached(url, forceReload);
    }

    public static String[] getPlayerNames() {
        return isLoaded() ? data.Names : null;
    }
    public static long getTimestamp() {
        return isLoaded() ? data.Timestamp : 0;
    }

    @Override
    // This callback is called after the PlayerList was loaded (or loading failed)
    public void onApiCallCompleted(String url, String result) {
        // A JSON-load request returned -> parse playersList and notify listener
        PlayersList.data = new PlayersListData();
        boolean res = PlayersList.data.parseJSON(result);
        if (!res) {
            PlayersList.data = null;
            if (null != onAsyncDataLoadedListener) {
                onAsyncDataLoadedListener.onAsyncDataLoaded(OnAsyncDataLoadedListener.STATUS_ERROR);
            }
            return;
        }

        /*
        if (PlayersList.data.isExpired()) {
            PlayersList.data = null;
            Training.requestServerReload();
            return;
        }
        */

        cache.put(url, PlayersList.data);

        if (null != onAsyncDataLoadedListener) {
            onAsyncDataLoadedListener.onAsyncDataLoaded(OnAsyncDataLoadedListener.STATUS_SUCCESS);
        }
    }

    // PRIVATE METHODS

    // Load trainingData from web, cached
    private static void loadPlayersListCached(String url, boolean forceReload) {
        if (!forceReload) {
            if (cache.containsKey(url)) {
                PlayersList.data = cache.get(url);
                long now = new Date().getTime();
                long age = now - PlayersList.data.Timestamp;
                if (age < CACHE_MAX_AGE * 1000) {
                    if (!PlayersList.data.isExpired()){
                        Log.i("PlayersList::loadPlayersListCached", "Found valid cache entry.");
                        onAsyncDataLoadedListener.onAsyncDataLoaded(OnAsyncDataLoadedListener.STATUS_CACHED);
                        return;
                    }
                }
                // Cache entry is old or expired
                cache.remove(url);
            }
        }

        loadPlayersListAsync(url);
    }

    // Load trainingData from web, async
    private static void loadPlayersListAsync(String url) {
        new API_CALL(new PlayersList()).execute(url);
    }
}
