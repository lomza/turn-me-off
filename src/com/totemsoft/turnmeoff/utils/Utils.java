package com.totemsoft.turnmeoff.utils;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.totemsoft.turnmeoff.C;
import com.totemsoft.turnmeoff.MainActivity;
import com.totemsoft.turnmeoff.R;
import com.totemsoft.turnmeoff.TurmMeOffBroadcastReceiver;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * This class holds helper methods.
 * <p/>
 * Created by Antonina on 14.01.14.
 */
public abstract class Utils {
    private static DateTime initialDateTime;

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    /**
     * Cancels event.
     *
     * @param context context to which is connected
     * @param key     one of eight keys indicating the option chosen by user
     */
    public static void cancelEvent(Context context, String key) {
        if (context == null || key == null)
            return;

        Intent intent = new Intent(context, TurmMeOffBroadcastReceiver.class);

        PendingIntent.getBroadcast(context, getCodeFromKey(key), intent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }

    public static void changeCurrentStatus(Context context) {
        if (context == null)
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        boolean isWifiOn = Utils.isWiFiEnabledStatus(context);
        if ((isWifiOn && !prefs.getBoolean(C.KEY_PREF_STATUS_WIFI, false)) || (!isWifiOn && prefs.getBoolean(C.KEY_PREF_STATUS_WIFI, false))) {
            editor.putBoolean(C.KEY_PREF_STATUS_WIFI + C.KEY_PREF_SWITCH_SUFFIX, isWifiOn);
            editor.commit();
        }

        boolean isMobileOn = Utils.isMobileNetworkEnabledStatus(context);
        if ((isMobileOn && !prefs.getBoolean(C.KEY_PREF_STATUS_MOBILE, false)) || (!isMobileOn && prefs.getBoolean(C.KEY_PREF_STATUS_MOBILE, false))) {
            editor.putBoolean(C.KEY_PREF_STATUS_MOBILE + C.KEY_PREF_SWITCH_SUFFIX, isWifiOn);
            editor.commit();
        }
    }

    public static void changeCurrentStatusSwitch(Context context, String key) {
        if (context == null || key == null || key.isEmpty())
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        // update wifi/mobile data preference
        if (key.contains("wifi")) {
            editor.putBoolean(C.KEY_PREF_STATUS_WIFI + C.KEY_PREF_SWITCH_SUFFIX, !prefs.getBoolean(C.KEY_PREF_STATUS_WIFI + C.KEY_PREF_SWITCH_SUFFIX, false));
        } else {
            editor.putBoolean(C.KEY_PREF_STATUS_MOBILE + C.KEY_PREF_SWITCH_SUFFIX, !prefs.getBoolean(C.KEY_PREF_STATUS_MOBILE + C.KEY_PREF_SWITCH_SUFFIX, false));
        }

        editor.commit();
    }

    /**
     * After any action was performed, the switch should be set to OFF,
     * because the action won't repeat anyway and most likely user doesn't want it to.
     *
     * @param context context in which to execute
     * @param key     preference key
     */
    public static void disableSwitchAfterAction(Context context, String key) {
        if (context == null || key == null || key.isEmpty())
            return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(key + C.KEY_PREF_SWITCH_SUFFIX, false);
        editor.commit();
    }

    public static int getCodeFromKey(String key) {
        if (key == null || key.isEmpty())
            return -1;

        if (key.equals(C.KEY_PREF_WIFI_OFF_TIME))
            return C.PENDING_INTENT_WIFI_OFF_TIME_CODE;

        if (key.equals(C.KEY_PREF_WIFI_OFF_INTERVAL))
            return C.PENDING_INTENT_WIFI_OFF_INTERVAL_CODE;

        if (key.equals(C.KEY_PREF_MOBILE_OFF_TIME))
            return C.PENDING_INTENT_MOBILE_OFF_TIME_CODE;

        if (key.equals(C.KEY_PREF_MOBILE_OFF_INTERVAL))
            return C.PENDING_INTENT_MOBILE_OFF_INTERVAL_CODE;

        if (key.equals(C.KEY_PREF_WIFI_ON_TIME))
            return C.PENDING_INTENT_WIFI_ON_TIME_CODE;

        if (key.equals(C.KEY_PREF_WIFI_ON_INTERVAL))
            return C.PENDING_INTENT_WIFI_ON_INTERVAL_CODE;

        if (key.equals(C.KEY_PREF_MOBILE_ON_TIME))
            return C.PENDING_INTENT_MOBILE_ON_TIME_CODE;

        if (key.equals(C.KEY_PREF_MOBILE_ON_INTERVAL))
            return C.PENDING_INTENT_MOBILE_ON_INTERVAL_CODE;

        return -1;
    }

    public static int getFormattedHour(String time) {
        if (time == null)
            return 0;

        String[] pieces = time.split(":");
        int hour = Integer.parseInt(pieces[0]);

        if (hour == 0)
            hour = 12;
        else if (hour > 12)
            hour -= 12;

        return hour;
    }

    public static String getFormattedTime(String time) {
        if (time == null)
            return "";

        return getFormattedHour(time) + time.substring(time.indexOf(":"));
    }

    public static int getHour() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("h");

        return Integer.valueOf(dateTimeFormatter.print(initialDateTime));
    }

    public static int getMinute() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("mm");

        return Integer.valueOf(dateTimeFormatter.print(initialDateTime));
    }

    public static int getNotificationIdFromKey(String key) {
        if (key == null || key.isEmpty())
            return -1;

        if (key.equals(C.KEY_PREF_WIFI_OFF_TIME) || key.equals(C.KEY_PREF_WIFI_OFF_INTERVAL))
            return C.NOTIFICATION_KEY_WIFI_OFF;

        if (key.equals(C.KEY_PREF_WIFI_ON_TIME) || key.equals(C.KEY_PREF_WIFI_ON_INTERVAL))
            return C.NOTIFICATION_KEY_WIFI_ON;

        if (key.equals(C.KEY_PREF_MOBILE_OFF_TIME) || key.equals(C.KEY_PREF_MOBILE_OFF_INTERVAL))
            return C.NOTIFICATION_KEY_MOBILE_OFF;

        if (key.equals(C.KEY_PREF_MOBILE_ON_TIME) || key.equals(C.KEY_PREF_MOBILE_ON_INTERVAL))
            return C.NOTIFICATION_KEY_MOBILE_ON;

        return -1;
    }

    public static String getNotificationTitleDoneFromKey(Context context, String key) {
        if (context == null || key == null || key.isEmpty())
            return "";

        if (key.equals(C.KEY_PREF_WIFI_OFF_TIME) || key.equals(C.KEY_PREF_WIFI_OFF_INTERVAL))
            return context.getString(R.string.notification_msg_turn_off_wifi_done);

        if (key.equals(C.KEY_PREF_WIFI_ON_TIME) || key.equals(C.KEY_PREF_WIFI_ON_INTERVAL))
            return context.getString(R.string.notification_msg_turn_on_wifi_done);

        if (key.equals(C.KEY_PREF_MOBILE_OFF_TIME) || key.equals(C.KEY_PREF_MOBILE_OFF_INTERVAL))
            return context.getString(R.string.notification_msg_turn_off_network_done);

        if (key.equals(C.KEY_PREF_MOBILE_ON_TIME) || key.equals(C.KEY_PREF_MOBILE_ON_INTERVAL))
            return context.getString(R.string.notification_msg_turn_on_network_done);

        return "";
    }

    public static String getNotificationTitleInProgressFromKey(Context context, String key) {
        if (context == null || key == null || key.isEmpty())
            return "";

        if (key.equals(C.KEY_PREF_WIFI_OFF_TIME) || key.equals(C.KEY_PREF_WIFI_OFF_INTERVAL))
            return context.getString(R.string.notification_msg_turn_off_wifi);

        if (key.equals(C.KEY_PREF_WIFI_ON_TIME) || key.equals(C.KEY_PREF_WIFI_ON_INTERVAL))
            return context.getString(R.string.notification_msg_turn_on_wifi);

        if (key.equals(C.KEY_PREF_MOBILE_OFF_TIME) || key.equals(C.KEY_PREF_MOBILE_OFF_INTERVAL))
            return context.getString(R.string.notification_msg_turn_off_network);

        if (key.equals(C.KEY_PREF_MOBILE_ON_TIME) || key.equals(C.KEY_PREF_MOBILE_ON_INTERVAL))
            return context.getString(R.string.notification_msg_turn_on_network);

        return "";
    }

    public static String getNowTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("h:mm a");

        return dateTimeFormatter.print(initialDateTime);
    }

    public static void initTime() {
        initialDateTime = new DateTime();
        initialDateTime.plusMinutes(1); // so the date is the same as user's date when the app is showed or 1min in future
    }

    public static boolean isAm() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("mm");

        return dateTimeFormatter.print(initialDateTime).equals("AM");
    }

    public static boolean isMobileNetworkEnable(Context context, String key) {
        if (context == null || key == null || key.isEmpty())
            return false;

        if (key.equals(C.KEY_PREF_MOBILE_OFF_TIME) || key.equals(C.KEY_PREF_MOBILE_OFF_INTERVAL))
            return false;

        if (key.equals(C.KEY_PREF_MOBILE_ON_TIME) || key.equals(C.KEY_PREF_MOBILE_ON_INTERVAL))
            return true;

        return false;
    }

    public static boolean isMobileNetworkEnabledStatus(Context context) {
        if (context == null)
            return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class conmanClass;
        try {
            conmanClass = Class.forName(connectivityManager.getClass().getName());
            Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
            Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            Method method = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            boolean mobileDataEnabled = (Boolean) method.invoke(iConnectivityManager);

            return mobileDataEnabled;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isMobileStatusEqual(Context context, boolean mobileStatusEnable) {
        if (context == null)
            return false;

        return isMobileNetworkEnabledStatus(context) == mobileStatusEnable;
    }

    public static boolean isNetworkOperatorAvailable(Context context) {
        if (context == null)
            return false;

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return tm.getNetworkOperator() != null && !tm.getNetworkOperator().isEmpty() && tm.getNetworkOperatorName() != null && !tm.getNetworkOperatorName().isEmpty();
    }

    public static boolean isWiFiEnable(Context context, String key) {
        if (context == null || key == null || key.isEmpty())
            return false;

        if (key.equals(C.KEY_PREF_WIFI_OFF_TIME) || key.equals(C.KEY_PREF_WIFI_OFF_INTERVAL))
            return false;

        if (key.equals(C.KEY_PREF_WIFI_ON_TIME) || key.equals(C.KEY_PREF_WIFI_ON_INTERVAL))
            return true;

        return false;
    }

    public static boolean isWiFiEnabledStatus(Context context) {
        if (context == null)
            return false;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    public static boolean isWiFiStatusEqual(Context context, int wifiStatusEnable) {
        if (context == null)
            return false;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getWifiState() == wifiStatusEnable;
    }

    /**
     * Schedules event in future.
     *
     * @param context  context to which is connected
     * @param interval 1 to 60 min added to current time
     * @param key      one of eight keys indicating the option chosen by user
     */
    public static void scheduleIntervalEvent(Context context, int interval, String key) {
        if (context == null || interval <= 0 || key == null)
            return;

        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusMinutes(interval);

        Intent intent = new Intent(context, TurmMeOffBroadcastReceiver.class);
        intent.putExtra(C.EXTRA_KEY, key);
        intent.putExtra(C.EXTRA_TIME, dateTime.toString(C.NOTIFICATION_TIME_FORMAT));
        intent.putExtra(C.EXTRA_IS_TIME, false);

        PendingIntent sender = PendingIntent.getBroadcast(context, getCodeFromKey(key), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, dateTime.getMillis(), sender);
    }

    /**
     * Schedules event in future.
     *
     * @param context context to which is connected
     * @param time    00:00 to 23:59 added to current date
     * @param key     one of eight keys indicating the option chosen by user
     */
    public static void scheduleTimeEvent(Context context, String time, String key) {
        if (context == null || time == null || key == null)
            return;

        String[] pieces = time.split(":");
        String hourPiece = pieces[0];
        String minPiece = pieces[1];

        DateTime dateTime = new DateTime();
        DateTime returnDateTime;

        if (Integer.valueOf(hourPiece) < dateTime.getHourOfDay() ||
                (Integer.valueOf(hourPiece) == dateTime.getHourOfDay() && Integer.valueOf(minPiece) < dateTime.getMinuteOfHour())) {
            dateTime = dateTime.plusDays(1);
        }

        returnDateTime = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), Integer.valueOf(hourPiece), Integer.valueOf(minPiece));

        Intent intent = new Intent(context, TurmMeOffBroadcastReceiver.class);
        intent.putExtra(C.EXTRA_KEY, key);
        intent.putExtra(C.EXTRA_TIME, returnDateTime.toString(C.NOTIFICATION_TIME_FORMAT));
        intent.putExtra(C.EXTRA_IS_TIME, true);

        PendingIntent sender = PendingIntent.getBroadcast(context, getCodeFromKey(key), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, returnDateTime.getMillis(), sender);
    }

    public static void showNotificationAndEnableConnection(Context context, String key, String time) {
        if (context == null || key == null || key.isEmpty()
                || time == null || time.isEmpty())
            return;

        final long when = System.currentTimeMillis();
        final String title = getNotificationTitleInProgressFromKey(context, key);
        final String titleWhenDone = getNotificationTitleDoneFromKey(context, key);
        final int notificationKey = getNotificationIdFromKey(key);
        boolean isKeyWifi = true;
        if (!(notificationKey == C.NOTIFICATION_KEY_WIFI_OFF || notificationKey == C.NOTIFICATION_KEY_WIFI_ON))
            isKeyWifi = false;

        if (isKeyWifi) {
            if (isWiFiEnable(context, key) && isWiFiEnabledStatus(context)) { // if want to turn on Wifi but it is turned on already
                return;

            }
            if (!isWiFiEnable(context, key) && !isWiFiEnabledStatus(context)) { // if want to turn off Wifi but it is turned off already
                return;
            }
        } else {
            if (isMobileNetworkEnable(context, key) && isMobileNetworkEnabledStatus(context)) { // if want to turn on mobile network but it is turned on already
                return;

            }
            if (!isMobileNetworkEnable(context, key) && !isMobileNetworkEnabledStatus(context)) { // if want to turn off mobile network but it is turned off already
                return;
            }
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.notification_icon);
        notificationBuilder.setTicker(title);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setWhen(when);
        notificationBuilder.setOnlyAlertOnce(true);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationKey, notificationBuilder.build());

        if (isKeyWifi)
            turnWiFiEnabled(context, isWiFiEnable(context, key));
        else
            turnMobileNetworkEnabled(context, isMobileNetworkEnable(context, key));

        // just wait for some time during which turn on/off operation should occur,
        // and only then show another notification
        try {
            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (InterruptedException e) {
            Log.e("", "Sleep has been interrupted.");
        }

        notificationBuilder.setTicker(titleWhenDone);
        notificationBuilder.setContentTitle(titleWhenDone);
        notificationBuilder.setContentText(time);

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager.notify(notificationKey, notificationBuilder.build());

        changeCurrentStatusSwitch(context, key);
    }

    public static void showToastShort(Context context, int textResId) {
        if (context == null)
            return;

        Toast.makeText(context, context.getString(textResId), Toast.LENGTH_SHORT).show();
    }

    public static void turnMobileNetworkEnabled(Context context, boolean enabled) {
        if (context == null)
            return;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class conmanClass;
        try {
            conmanClass = Class.forName(connectivityManager.getClass().getName());
            Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
            Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            Method method = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            boolean mobileDataEnabled = (Boolean) method.invoke(iConnectivityManager);

            if (enabled) {
                if (!mobileDataEnabled)
                    setMobileDataEnabledMethod.invoke(iConnectivityManager, true);
            } else {
                if (mobileDataEnabled)
                    setMobileDataEnabledMethod.invoke(iConnectivityManager, false);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void turnWiFiEnabled(Context context, boolean enabled) {
        if (context == null)
            return;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (enabled) {
            if (!(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED))
                wifiManager.setWifiEnabled(true);
        } else {
            if (!(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED))
                wifiManager.setWifiEnabled(false);
        }
    }
}