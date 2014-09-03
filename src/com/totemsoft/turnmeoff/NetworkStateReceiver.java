package com.totemsoft.turnmeoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.totemsoft.turnmeoff.utils.Utils;

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null) {
            Utils.changeCurrentStatus(context);
        }
    }
}