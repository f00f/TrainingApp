package de.uwr1.training;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by f00f on 12.07.2014.
 */
public class TrainingData {
    public String Wochentag;
    public String Date;
    public String Time;
    public String Location;
    public String Zusagen = "---";
    public String Absagen = "---";
    public long Expires; // wann ist das Training (zu Ende)?
    public long Updated; // von wann ist der letzte Eintrag?
    public long Timestamp; // wann wurde das JSON runtergeladen?
    public String Temp;
    public long TempUpdated;
    public String PhotoURL;
    public String PhotoThumbURL;
    // private
    private String[] ZusagenArr;
    private String[] AbsagenArr;
    private String[] ZusagenNamesArr;
    private String[] AbsagenNamesArr;

    // PUBLIC METHODS

    // compose string with meta information about the training
    public String getGeneralInfo() {
        String info = Date + " um " + Time + " in " + Location;
        if (null != Wochentag)
            info = Wochentag + ", " + info;
        return info;
    }

    public int getNumZusagen() {
        return ZusagenArr.length;
    }
    public int getNumAbsagen() {
        return AbsagenArr.length;
    }
    /*public int getNumNixsager() {
        return NixsagerArr.length;
    }*/

    // check whether a user will not participate
    public boolean hatAbgesagt(String username) {
        return containsName(AbsagenNamesArr, username);
    }
    // check whether a user will participate
    public boolean hatZugesagt(String username) {
        return containsName(ZusagenNamesArr, username);
    }

    // check if the training data is for a session in the past
    public boolean isExpired() {
        long now = new Date().getTime();
        return now > (this.Expires * 1000);
    }

    // Load data from several JSON strings
    public boolean parseJSON(String json) {
        Zusagen = "---";
        Absagen = "---";

        JSONObject jo;
        try {
            jo = new JSONObject(json).getJSONObject("train");
        } catch(JSONException e) {
            Log.w("UWR_Training::Training::parseJSON", "Unable to parse JSON data.");
            return false;
        }
        // get basic information
        try {
            Expires = jo.getLong("end");
            Wochentag = jo.getString("wtag");
            Date = jo.getString("datum");
            Time = jo.getString("zeit");
            Location = jo.getString("ort");
            Updated = jo.getLong("updated");
        } catch(JSONException e) {
            Log.w("UWR_Training::Training::parseJSON", "Unable to parse JSON data.");
            return false;
        }
        // get optional information
        try {
            ZusagenArr = JsonHelper.getStringArray(jo, "zu");
        } catch(JSONException e) { /* empty */ }
        try {
            AbsagenArr = JsonHelper.getStringArray(jo, "ab");
        } catch(JSONException e) { /* empty */ }
        // get optional information
        JSONObject joX = null;
        try {
            joX = jo.getJSONObject("x");
        } catch(JSONException e) { /* empty */ }
        if (null != joX) {
            try {
                JSONObject joTemp = joX.getJSONObject("temp");
                Temp = joTemp.getString("deg");
                TempUpdated = joTemp.getLong("updated");
            } catch(JSONException e) { /* empty */ }
            try {
                JSONObject joPic = joX.getJSONObject("pic");
                PhotoURL = joPic.getString("full");
                PhotoThumbURL = joPic.getString("thumb");
            } catch(JSONException e) { /* empty */ }
        }

        Timestamp = new java.util.Date().getTime();

        if (null == ZusagenArr)
            ZusagenArr = new String[0];
        if (null == AbsagenArr)
            AbsagenArr = new String[0];
        if (ZusagenArr.length > 0) {
            Zusagen = android.text.TextUtils.join(", ", ZusagenArr);
            ZusagenNamesArr = new String[ZusagenArr.length];
            for (int i = 0; i < ZusagenArr.length; i++){
                ZusagenNamesArr[i] = getName(ZusagenArr[i]);
            }
        }
        if (AbsagenArr.length > 0) {
            Absagen = android.text.TextUtils.join(", ", AbsagenArr);
            AbsagenNamesArr = new String[AbsagenArr.length];
            for (int i = 0; i < AbsagenArr.length; i++){
                AbsagenNamesArr[i] = getName(AbsagenArr[i]);
            }
        }

        return true;
    }

    // PRIVATE METHODS

    private static boolean containsName(String[] _sagenNamesArr, String username) {
        if (null == _sagenNamesArr || null == username || username.isEmpty())
            return false;

        return getReplyText(_sagenNamesArr, username) != null;
    }

    public String getReplyText(String username) {
        String replyText;

        if (null == username || username.isEmpty())
            return null;

        replyText = getReplyText(ZusagenArr, username);
        if (null != replyText) {
            return replyText;
        }

        replyText = getReplyText(AbsagenArr, username);
        return replyText;
    }
    // Returns: null on not found, full text on found.
    private static String getReplyText(String[] _sagenNamesArr, String username) {
        if (null == _sagenNamesArr)
            return null;

        for (String text : _sagenNamesArr) {
            if (!text.startsWith(username)) {
                continue;
            }
            if (text.equalsIgnoreCase(username)) {
                return text;
            }
            if (text.length() <= username.length()) {
                continue;
            }
            char c = text.charAt(username.length());
            boolean isLowercaseLetter = (c >= 'a' && c <= 'z');
            boolean isUppercaseLetter = (c >= 'A' && c <= 'Z');
            boolean isUmlaut = false; // BUG HERE: yes, we ignore Umlaute
            if (!isLowercaseLetter && !isUppercaseLetter && !isUmlaut) {
                return text;
            }
        }
        return null;
    }

    // TODO: implement FirstWord
    private static String getName(String s) {
        return s;
    }
}
