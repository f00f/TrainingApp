package de.uwr1.training;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by f00f on 12.09.2014.
 */
public abstract class JsonHelper {
    // Extract String array of 'name' out of the JSON object 'jo'.
    public static String[] getStringArray(JSONObject jo, String name) throws JSONException {
        JSONArray ja = jo.getJSONArray(name);
        if (0 == ja.length()) {
            return null;
        }

        String[] res = new String[ja.length()];
        for (int i = 0; i < ja.length(); i++) {
            res[i] = ja.getString(i);
        }

        if (1 == res.length && res[0].isEmpty()) {
            return null;
        }

        return res;
    }
}
