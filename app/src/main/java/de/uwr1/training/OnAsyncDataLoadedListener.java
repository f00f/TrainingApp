package de.uwr1.training;

/**
 * Created by f00f on 03.07.2014.
 */
public interface OnAsyncDataLoadedListener {
    public static int STATUS_SUCCESS = 0; // Freshly loaded
    public static int STATUS_CACHED = 1;  // Loaded from cache
    public static int STATUS_ERROR = 2;   // Loading failed
    void onAsyncDataLoaded(int statusCode);
}
