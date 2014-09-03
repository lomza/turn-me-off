package com.totemsoft.turnmeoff;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.totemsoft.turnmeoff.utils.Utils;

import org.joda.time.chrono.ISOChronology;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private enum TIME_OR_INTERVAL {
        NORMAL, TIME, INTERVAL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferencesFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_goto_settings:
                goToSettings();
                break;

            case R.id.action_feedback:
                goToFeedback();
                break;

            case R.id.action_help:
                goToHelp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToFeedback() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "tonia.tkachuk@totem-soft.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_title));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_chooser)));
    }

    private void goToHelp() {
        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    private void goToSettings() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        startActivity(intent);
    }

    public static class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private HashMap<String, TIME_OR_INTERVAL> KEYS_ARRAY = new HashMap<String, TIME_OR_INTERVAL>();
        private HashMap<String, TIME_OR_INTERVAL> MOBILE_KEYS_ARRAY = new HashMap<String, TIME_OR_INTERVAL>();
        private Activity activity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            this.activity = activity;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_fragment);

            populatePreferenceArray();
            populateMobilePreferencesArray();

            ISOChronology.getInstance();
            Utils.initTime();

            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        private void populatePreferenceArray() {
            KEYS_ARRAY.put(C.KEY_PREF_STATUS_WIFI, TIME_OR_INTERVAL.NORMAL);
            KEYS_ARRAY.put(C.KEY_PREF_STATUS_MOBILE, TIME_OR_INTERVAL.NORMAL);
            KEYS_ARRAY.put(C.KEY_PREF_WIFI_OFF_TIME, TIME_OR_INTERVAL.TIME);
            KEYS_ARRAY.put(C.KEY_PREF_WIFI_OFF_INTERVAL, TIME_OR_INTERVAL.INTERVAL);
            KEYS_ARRAY.put(C.KEY_PREF_MOBILE_OFF_TIME, TIME_OR_INTERVAL.TIME);
            KEYS_ARRAY.put(C.KEY_PREF_MOBILE_OFF_INTERVAL, TIME_OR_INTERVAL.INTERVAL);
            KEYS_ARRAY.put(C.KEY_PREF_WIFI_ON_TIME, TIME_OR_INTERVAL.TIME);
            KEYS_ARRAY.put(C.KEY_PREF_WIFI_ON_INTERVAL, TIME_OR_INTERVAL.INTERVAL);
            KEYS_ARRAY.put(C.KEY_PREF_MOBILE_ON_TIME, TIME_OR_INTERVAL.TIME);
            KEYS_ARRAY.put(C.KEY_PREF_MOBILE_ON_INTERVAL, TIME_OR_INTERVAL.INTERVAL);
        }

        private void populateMobilePreferencesArray() {
            MOBILE_KEYS_ARRAY.put(C.KEY_PREF_STATUS_MOBILE, TIME_OR_INTERVAL.NORMAL);
            MOBILE_KEYS_ARRAY.put(C.KEY_PREF_MOBILE_OFF_TIME, TIME_OR_INTERVAL.TIME);
            MOBILE_KEYS_ARRAY.put(C.KEY_PREF_MOBILE_OFF_INTERVAL, TIME_OR_INTERVAL.INTERVAL);
            MOBILE_KEYS_ARRAY.put(C.KEY_PREF_MOBILE_ON_TIME, TIME_OR_INTERVAL.TIME);
            MOBILE_KEYS_ARRAY.put(C.KEY_PREF_MOBILE_ON_INTERVAL, TIME_OR_INTERVAL.INTERVAL);
        }

        @Override
        public void onResume() {
            super.onResume();

            if (activity != null) {
                Utils.initTime();
                setSummaries();
                setMobileDataDisabled();
            }
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            if (activity != null) {
                String originalKey = key.replace(C.KEY_PREF_SWITCH_SUFFIX, "");
                Preference p = findPreference(originalKey);

                if (key.contains(C.KEY_PREF_SWITCH_SUFFIX)) {
                    p = findPreference(originalKey);

                    if (KEYS_ARRAY.get(originalKey).equals(TIME_OR_INTERVAL.TIME)) {
                        ((TimeDialogFragment) p).getSwitchEnabler().updatePreference();
                    } else if (KEYS_ARRAY.get(originalKey).equals(TIME_OR_INTERVAL.INTERVAL)) {
                        ((TimeIntervalDialogFragment) p).getSwitchEnabler().updatePreference();
                    } else if (KEYS_ARRAY.get(originalKey).equals(TIME_OR_INTERVAL.NORMAL)) {
                        boolean value = ((SwitchPreference) p).isChecked();

                        if (originalKey.equals(C.KEY_PREF_STATUS_WIFI)) {
                            ((SwitchPreference) p).setChecked(!value);
                        } else if (originalKey.equals(C.KEY_PREF_STATUS_MOBILE)) {
                            ((SwitchPreference) p).setChecked(!value);
                        }

                        return;
                    }
                }

                if (KEYS_ARRAY.get(originalKey).equals(TIME_OR_INTERVAL.TIME)) {
                    String time = sharedPreferences.getString(originalKey, Utils.getNowTime());
                    final String timeForEvent = sharedPreferences.getString(originalKey, Utils.getNowTime()).substring(0, sharedPreferences.getString(originalKey, Utils.getNowTime()).indexOf(" "));
                    final String oldHour = time.substring(0, time.indexOf(":"));
                    int newHour = Utils.getFormattedHour(oldHour);
                    time = time.replaceFirst(oldHour, String.valueOf(newHour));

                    p.setSummary(time);

                    if (((TimeDialogFragment) p).getSwitchEnabler() != null) {
                        if (((TimeDialogFragment) p).isSwitchOn())
                            Utils.scheduleTimeEvent(activity, timeForEvent, originalKey);
                        else
                            Utils.cancelEvent(activity, originalKey);
                    }
                } else if (KEYS_ARRAY.get(originalKey).equals(TIME_OR_INTERVAL.INTERVAL)) {
                    p.setSummary(activity.getString(R.string.interval_format, sharedPreferences.getInt(originalKey, 0)));

                    if (((TimeIntervalDialogFragment) p).getSwitchEnabler() != null) {
                        if (((TimeIntervalDialogFragment) p).isSwitchOn())
                            Utils.scheduleIntervalEvent(activity, Integer.valueOf(p.getSummary().toString().replace(" min", "")), originalKey);
                        else
                            Utils.cancelEvent(activity, originalKey);
                    }
                } else if (KEYS_ARRAY.get(originalKey).equals(TIME_OR_INTERVAL.NORMAL)) {
                    boolean value = ((SwitchPreference) p).isChecked();

                    if (originalKey.equals(C.KEY_PREF_STATUS_WIFI)) {
                        boolean isWifiOn = Utils.isWiFiEnabledStatus(activity);
                        if ((isWifiOn && !value) || (!isWifiOn && value)) {
                            SwitchConnectionAsyncTask task = new SwitchConnectionAsyncTask(p, value, true);
                            task.execute();
                        }
                    } else if (originalKey.equals(C.KEY_PREF_STATUS_MOBILE)) {
                        boolean isMobileOn = Utils.isMobileNetworkEnabledStatus(activity);
                        if ((isMobileOn && !value) || (!isMobileOn && value)) {
                            SwitchConnectionAsyncTask task = new SwitchConnectionAsyncTask(p, value, false);
                            task.execute();
                        }
                    }
                }
            }
        }

        /**
         * Sets summary text in all preferences.
         */
        private void setSummaries() {
            if (activity != null) {
                SharedPreferences sp = getPreferenceScreen().getSharedPreferences();

                if (sp != null) {
                    for (String key : KEYS_ARRAY.keySet()) {
                        Preference p = findPreference(key);
                        if (p != null) {
                            if (KEYS_ARRAY.get(key).equals(TIME_OR_INTERVAL.NORMAL)) {
                                if (key.equals(C.KEY_PREF_STATUS_WIFI)) {
                                    boolean isWifiOn = Utils.isWiFiEnabledStatus(activity);
                                    if ((isWifiOn && !sp.getBoolean(key, false)) || (!isWifiOn && sp.getBoolean(key, false))) {
                                        //Utils.changeBooleanPreference(activity, key, isWifiOn);
                                        ((SwitchPreference) p).setChecked(isWifiOn);
                                    }
                                } else if (key.equals(C.KEY_PREF_STATUS_MOBILE)) {
                                    boolean isMobileOn = Utils.isMobileNetworkEnabledStatus(activity);
                                    if ((isMobileOn && !sp.getBoolean(key, false)) || (!isMobileOn && sp.getBoolean(key, false))) {
                                        //Utils.changeBooleanPreference(activity, key, isMobileOn);
                                        ((SwitchPreference) p).setChecked(isMobileOn);
                                    }
                                }
                            } else if (KEYS_ARRAY.get(key).equals(TIME_OR_INTERVAL.TIME)) {
                                if (!sp.getBoolean(key + C.KEY_PREF_SWITCH_SUFFIX, false)) { // if switch is OFF, set time to now
                                    p.setSummary(Utils.getNowTime());
                                    ((TimeDialogFragment) p).setNowValue(Utils.getNowTime());
                                } else
                                    p.setSummary(Utils.getFormattedTime(sp.getString(key, Utils.getNowTime())));
                            } else if (KEYS_ARRAY.get(key).equals(TIME_OR_INTERVAL.INTERVAL)) {
                                p.setSummary(activity.getString(R.string.interval_format, sp.getInt(key, 0)));
                            }
                        }
                    }
                }
            }
        }

        /**
         * If network operator is not found, make Mobile Data preferences disabled.
         */
        private void setMobileDataDisabled() {
            if (Utils.isNetworkOperatorAvailable(activity)) {
                // enable mobile data preferences
                setEnabledPreference(true);
            } else {
                // disable mobile data preferences
                setEnabledPreference(false);
            }
        }

        private void setEnabledPreference(boolean shouldBeEnable) {
            Iterator iterator = MOBILE_KEYS_ARRAY.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Preference p = findPreference(key);
                if (shouldBeEnable && !p.isEnabled())
                    p.setEnabled(true);
                else if (!shouldBeEnable && p.isEnabled())
                    p.setEnabled(false);
            }
        }

        private class SwitchConnectionAsyncTask extends AsyncTask<Void, Void, Boolean> {
            private Preference preference;
            private boolean shouldBeOn;
            private boolean isWifi;

            public SwitchConnectionAsyncTask(Preference preference, boolean shouldBeOn, boolean isWifi) {
                this.preference = preference;
                this.shouldBeOn = shouldBeOn;
                this.isWifi = isWifi;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                preference.setEnabled(false);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                if (isWifi) {
                    Utils.turnWiFiEnabled(activity, shouldBeOn);
                    while(!Utils.isWiFiStatusEqual(activity, shouldBeOn ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED)) {
                        // wait...
                    }

                    return true;
                } else {
                    Utils.turnMobileNetworkEnabled(activity, shouldBeOn);
                    while(!Utils.isMobileStatusEqual(activity, shouldBeOn)) {
                        // wait...
                    }

                    return true;
                }
            }

            @Override
            protected void onPostExecute(Boolean isSwitched) {
                int resId = shouldBeOn ? R.string.notification_msg_turn_on_wifi_done : R.string.notification_msg_turn_off_wifi_done;
                if (!isWifi)
                    resId = shouldBeOn ? R.string.notification_msg_turn_on_network_done : R.string.notification_msg_turn_off_network_done;

                Utils.showToastShort(activity, resId);
                preference.setEnabled(true);
            }
        }
    }
}