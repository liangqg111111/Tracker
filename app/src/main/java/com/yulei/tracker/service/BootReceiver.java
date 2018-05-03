package com.yulei.tracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yulei.tracker.util.LogUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("start");
    }
}
