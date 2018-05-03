package com.yulei.tracker;

import android.app.Application;

import com.yulei.tracker.service.TrackerService;
import com.yulei.tracker.util.LogUtils;
import com.yulei.tracker.util.Utils;

public class TrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Utils.init(this);
        LogUtils.Config config = LogUtils.getConfig().setLog2FileSwitch(true);
        /*LogUtils.d(config.toString());*/

        TrackerService.startTrackerService(this);
    }
}
