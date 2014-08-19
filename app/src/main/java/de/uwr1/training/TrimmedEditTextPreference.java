package de.uwr1.training;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by f00f on 10.07.2014.
 */
public class TrimmedEditTextPreference extends EditTextPreference {
    public TrimmedEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public TrimmedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TrimmedEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public void setText(String text) {
        super.setText(text.trim());
    }
}
