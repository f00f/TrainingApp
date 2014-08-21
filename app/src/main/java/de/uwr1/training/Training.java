package de.uwr1.training;

import android.content.Context;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by f00f on 26.06.2014.
 */
public class Training implements OnApiCallCompletedListener {
    private static TrainingData data;
    private static Context context;
    private static OnApiCallCompletedListener onReplySentListener;
    private static OnTrainingDataLoadedListener onTrainingDataLoadedListener;
    private static HashMap<String, TrainingData> cache = new HashMap<String, TrainingData>();
    private static final int CACHE_MAX_AGE = 5 * 60; // cache lifetime in seconds

    private boolean serverRefreshRequested = false;

    // PUBLIC METHODS

    private Training() {}

    public static void Init(Context _context, OnApiCallCompletedListener _onReplySentListener, OnTrainingDataLoadedListener _onTrainingDataLoadedListener) {
        Training.data = null;
        Training.context = _context;
        Training.onReplySentListener = _onReplySentListener;
        Training.onTrainingDataLoadedListener = _onTrainingDataLoadedListener;
    }

    public static boolean isLoaded() {
        return Training.data != null;
    }

    @Override
    public void onApiCallCompleted(String url, String result) {
        // Some non-load request returned -> reload data
        if (!url.equals(Config.getURL(context, Config.KEY_JSON_URL))) {
            Training.data = null;
            boolean forceReload = true;
            loadTrainingData(forceReload);
            return;
        }

        // A load request returned -> parse data and notify listener
        Training.data = new TrainingData();
        boolean res = Training.data.parseJSON(result);
        if (!res) {
            return;
        }

        if (Training.data.isExpired()) {
            Training.data = null;
            if (!serverRefreshRequested) {
                // issue server refresh, data reload will happen in callbacks
                RequestServerRefresh();
            } else {
                serverRefreshRequested = false;
            }
            return;
        }

        cache.put(url, Training.data);

        if (null != onTrainingDataLoadedListener) {
            onTrainingDataLoadedListener.onTrainingDataLoaded();
        }
    }

    public static boolean sendZusage() {
        return sendZusage(Config.getUsername(context));
    }
    public static boolean sendZusage(String text) {
        return sendReply(text, true);
    }
    public static boolean sendAbsage() {
        return sendAbsage(Config.getUsername(context));
    }
    public static boolean sendAbsage(String text) {
        return sendReply(text, false);
    }
    public static boolean sendReply(String text, boolean isZusage) {
        String url = Config.getReplyUrl(context, text, isZusage);
        if (null == url) {
            return false;
        }

        new API_CALL(new Training()).execute(url);

        return true;
    }

    // compose string with meta information about the training
    public static String getGeneralInfo() {
        return data.getGeneralInfo();
    }

    public static long getTimestampOfDownload() {
        return data.Timestamp;
    }
    public static long getTimestampOfLastEntry() {
        return data.Updated * 1000;
    }
    public static boolean hasExtraTemp() {
        return true;
    }
    public static String getExtraTemp() {
        return data.Temp;
    }
    public static long getExtraTempUpdated() {
        return data.TempUpdated * 1000;
    }

    public static String getZusagen() {
        return data.Zusagen;
    }
    public static String getAbsagen() {
        return data.Absagen;
    }
    public static String[] getNixsagerArray() {
        return data.NixsagenArr;
    }
    public static int getNumZusagen() {
        return data.getNumZusagen();
    }
    public static int getNumAbsagen() {
        return data.getNumAbsagen();
    }
    public static int getNumNixsager() {
        return data.getNumNixsager();
    }

    // check whether the current user will not participate
    public static boolean hatAbgesagt() {
        return hatAbgesagt(Config.getUsername(context));
    }
    // check whether a user will not participate
    public static boolean hatAbgesagt(String username) {
        return data.hatAbgesagt(username);
    }

    // check whether the current user will participate
    public static boolean hatZugesagt() {
        return hatZugesagt(Config.getUsername(context));
    }
    // check whether a user will participate
    public static boolean hatZugesagt(String username) {
        return data.hatZugesagt(username);
    }

    public void loadTrainingData() {
        boolean forceReload = false;
        loadTrainingData(forceReload);
    }
    public static void loadTrainingData(boolean forceReload) {
        String url = Config.getURL(context, Config.KEY_JSON_URL);
        loadTrainingDataCached(url, forceReload);
    }

    // PRIVATE METHODS

    private void RequestServerRefresh() {
        String url = Config.getURL(context, Config.KEY_REFRESH_URL);
        serverRefreshRequested = true;
        new API_CALL(this).execute(url);
    }

    // Load data from web, cached
    private static void loadTrainingDataCached(String url, boolean forceReload) {
        if (!forceReload) {
            if (cache.containsKey(url)) {
                Training.data = cache.get(url);
                long now = new Date().getTime();
                long age = now - Training.data.Timestamp;
                if (age < CACHE_MAX_AGE * 1000) {
                    if (!Training.data.isExpired()){
                        Log.w("UWR_Training::Training::loadTrainingDataCached", "Found valid cache entry.");
                        onTrainingDataLoadedListener.onTrainingDataLoaded();
                        return;
                    }
                }
                // Cache entry is old or expired
                cache.remove(url);
            }
        }

        loadDataFromURLAsync(url);
    }

    // Load data from web, async
    private static void loadDataFromURLAsync(String url) {
        new API_CALL(new Training()).execute(url);
    }
}
