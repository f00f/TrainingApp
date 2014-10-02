package de.uwr1.training;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by f00f on 26.06.2014.
 */
public class Training implements OnApiCallCompletedListener {
    private static TrainingData trainingData;
    private static PlayersList playersList;
    private static Context context;
    private static OnApiCallCompletedListener onReplySentListener;
    private static OnAsyncDataLoadedListener onAsyncDataLoadedListener;
    private static HashMap<String, TrainingData> cache = new HashMap<String, TrainingData>();
    private static final int CACHE_MAX_AGE = 5 * 60; // cache lifetime in seconds

    private static boolean serverReloadRequested = false;

    private static String[] NixsagerArr;

    // CONSTRUCTOR

    private Training() {}

    // PUBLIC METHODS

    public static void Init(Context _context, OnApiCallCompletedListener _onReplySentListener, OnAsyncDataLoadedListener _onAsyncDataLoadedListener) {
        Training.trainingData = null;
        Training.playersList = null;

        Training.context = _context;
        Training.onReplySentListener = _onReplySentListener;
        Training.onAsyncDataLoadedListener = _onAsyncDataLoadedListener;

        PlayersList.Init(_context, _onAsyncDataLoadedListener);
    }

    public static boolean isLoaded() {
        return isTrainingDataLoaded() && isPlayersListLoaded();
    }

    public static boolean isTrainingDataLoaded() {
        return Training.trainingData != null;
    }

    public static boolean isPlayersListLoaded() {
        return PlayersList.isLoaded();
    }

    @Override
    // TODO: maybe this should be moved into the TrainingData class?
    public void onApiCallCompleted(String url, String result) {
        // A request other than loading the JSON trainingData returned.
        // This can be after a user reply or after a server reload request.
        // Do reload JSON trainingData now.
        if (!url.equals(Config.getURL(context, Config.KEY_JSON_URL))) {
            Training.trainingData = null;
            boolean forceReload = true;
            loadTrainingData(forceReload);
            return;
        }

        // A JSON-load request returned -> parse trainingData and notify listener
        Training.trainingData = new TrainingData();
        boolean res = Training.trainingData.parseJSON(result);
        if (!res) {
            Training.trainingData = null;
            if (null != onAsyncDataLoadedListener) {
                onAsyncDataLoadedListener.onAsyncDataLoaded(OnAsyncDataLoadedListener.STATUS_ERROR);
            }
            return;
        }

        if (Training.trainingData.isExpired()) {
            Training.trainingData = null;
            if (!serverReloadRequested) {
                // issue server refresh, trainingData reload will happen in callbacks
                requestServerReload();
            } else {
                serverReloadRequested = false;
            }
            return;
        }

        cache.put(url, Training.trainingData);

        if (null != onAsyncDataLoadedListener) {
            onAsyncDataLoadedListener.onAsyncDataLoaded(OnAsyncDataLoadedListener.STATUS_SUCCESS);
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
        return trainingData.getGeneralInfo();
    }

    public static long getTimestampOfDownload() {
        return trainingData.Timestamp;
    }
    public static long getTimestampOfLastEntry() {
        return trainingData.Updated * 1000;
    }
    public static boolean hasExtraTemp() {
        return null != trainingData && null != trainingData.Temp;
    }
    public static String getExtraTemp() {
        return trainingData.Temp;
    }
    public static long getExtraTempUpdated() {
        return trainingData.TempUpdated * 1000;
    }

    public static String getZusagen() {
        return trainingData.Zusagen;
    }
    public static String getAbsagen() {
        return trainingData.Absagen;
    }
    public static int getNumZusagen() {
        return trainingData.getNumZusagen();
    }
    public static int getNumAbsagen() {
        return trainingData.getNumAbsagen();
    }

    // check whether the current user will not participate
    public static boolean hatAbgesagt() {
        return hatAbgesagt(Config.getUsername(context));
    }
    // check whether a user will not participate
    public static boolean hatAbgesagt(String username) {
        return trainingData.hatAbgesagt(username);
    }

    // check whether the current user will participate
    public static boolean hatZugesagt() {
        return hatZugesagt(Config.getUsername(context));
    }
    // check whether a user will participate
    public static boolean hatZugesagt(String username) {
        return trainingData.hatZugesagt(username);
    }

    public static String getComment() {
        String username = Config.getUsername(context);
        String replyText = trainingData.getReplyText(username);
        String comment = null;
        if (null != replyText && !replyText.isEmpty()) {
            comment = replyText.substring(username.length()).trim();
            // remove parentheses
            int commLen = comment.length();
            if (commLen >= 2 && comment.charAt(0) == '(' && comment.charAt(commLen - 1) == ')') {
                comment = comment.substring(1, commLen - 1);
            }
        }
        return comment;
    }

    public static String[] getNixsager() {
        if (null == NixsagerArr) {
            populateNixsager();
        }
        return NixsagerArr;
    }
    public static int getNumNixsager() {
        return NixsagerArr != null ? NixsagerArr.length : 0;
    }
    public static void resetNixsager() {
        NixsagerArr = null;
    }
    public static void populateNixsager() {
        if (!isLoaded()) {
            return;
        }

        String[] allPlayers = PlayersList.getPlayerNames();
        if (null == allPlayers || 0 == allPlayers.length) {
            NixsagerArr = new String[] {};
            return;
        }

        ArrayList<String> NixsagerList = new ArrayList<String>();
        for (String player : allPlayers) {
            if (player.trim().isEmpty())
                continue;
            if (hatZugesagt(player))
                continue;
            if (hatAbgesagt(player))
                continue;
            NixsagerList.add(player);
        }
        NixsagerArr = NixsagerList.toArray(new String[]{});
        Arrays.sort(NixsagerArr);

        if (null == NixsagerArr)
            NixsagerArr = new String[0];
    }

    public void loadTrainingData() {
        boolean forceReload = false;
        loadTrainingData(forceReload);
    }
    public static void loadTrainingData(boolean forceReload) {
        resetNixsager();
        String url = Config.getURL(context, Config.KEY_JSON_URL);
        loadTrainingDataCached(url, forceReload);
    }

    public void loadPlayersList() {
        boolean forceReload = false;
        loadPlayersList(forceReload);
    }
    public static void loadPlayersList(boolean forceReload) {
        resetNixsager();
        PlayersList.load(forceReload);
    }

    // Issue a server reload to update the JSON files.
    // This is only needed when the cached training session has expired, or when for some reason the server screwed up.
    public static void requestServerReload() {
        String url = Config.getURL(context, Config.KEY_REFRESH_URL);
        serverReloadRequested = true;
        new API_CALL(new Training()).execute(url);
    }

    // PRIVATE METHODS

    // Load trainingData from web, cached
    private static void loadTrainingDataCached(String url, boolean forceReload) {
        if (!forceReload) {
            if (cache.containsKey(url)) {
                Training.trainingData = cache.get(url);
                long now = new Date().getTime();
                long age = now - Training.trainingData.Timestamp;
                if (age < CACHE_MAX_AGE * 1000) {
                    if (!Training.trainingData.isExpired()){
                        Log.i("Training::loadTrainingDataCached", "Found valid cache entry.");
                        onAsyncDataLoadedListener.onAsyncDataLoaded(OnAsyncDataLoadedListener.STATUS_CACHED);
                        return;
                    }
                }
                // Cache entry is old or expired
                cache.remove(url);
            }
        }

        loadTrainingDataFromURLAsync(url);
    }

    // Load trainingData from web, async
    private static void loadTrainingDataFromURLAsync(String url) {
        new API_CALL(new Training()).execute(url);
    }
}
