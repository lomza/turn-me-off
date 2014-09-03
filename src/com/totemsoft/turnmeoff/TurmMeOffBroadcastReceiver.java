package com.totemsoft.turnmeoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.totemsoft.turnmeoff.utils.Utils;

/**
 * Custom BroadcastReceiver which handles time & interval set callbacks.
 * <p/>
 * Created by Antonina on 08.02.14.
 */
public class TurmMeOffBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Utils.showNotificationAndEnableConnection(context, intent.getStringExtra(C.EXTRA_KEY), intent.getStringExtra(C.EXTRA_TIME));
        Utils.disableSwitchAfterAction(context, intent.getStringExtra(C.EXTRA_KEY));
    }
}