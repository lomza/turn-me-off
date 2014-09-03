package com.totemsoft.turnmeoff;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

/**
 * Custom CheckedChangeListener for switch in every preference.
 *
 * User: Antonina
 * Date: 17.11.13
 */
public class MySwitchEnabler implements OnCheckedChangeListener {

    protected final Context mContext;
    private Switch mSwitch;
    private String mKey;

    public MySwitchEnabler(Context context, Switch swtch, String key) {
        mContext = context;
        mKey = key;

        setSwitch(swtch);
        resume();
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        SharedPreferences prefs;
        SharedPreferences.Editor editor;

        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();

        editor.putBoolean(mKey, isChecked);
        editor.commit();
    }

    public boolean isSwitchOn() {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        return prefs.getBoolean(mKey, false);
    }

    public void resume() {
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setChecked(isSwitchOn());
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }

    public void setSwitch(Switch swtch) {
        if (mSwitch == swtch)
            return;

        if (mSwitch == null)
            mSwitch = swtch;
    }

    public void updatePreference() {
        boolean isSwitchOn = isSwitchOn();
        if (mSwitch.isChecked() != isSwitchOn)
            mSwitch.setChecked(isSwitchOn);
    }
}
