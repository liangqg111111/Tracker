package com.yulei.tracker.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yulei.tracker.util.LocationUtils;
import com.yulei.tracker.util.LogUtils;
import com.yulei.tracker.util.PhoneUtils;
import com.yulei.tracker.util.ToastUtils;

import java.lang.reflect.Method;


public class TrackerService extends Service{

    private boolean isSuccess;
    private String lastLatitude  = "loading...";
    private String lastLongitude = "loading...";
    private String latitude      = "loading...";
    private String longitude     = "loading...";
    private String country       = "loading...";
    private String locality      = "loading...";
    private String street        = "loading...";
    private OnGetLocationListener mOnGetLocationListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public void setOnGetLocationListener(OnGetLocationListener onGetLocationListener) {
        mOnGetLocationListener = onGetLocationListener;
    }

    private LocationUtils.OnLocationChangeListener mOnLocationChangeListener = new LocationUtils.OnLocationChangeListener() {
        @Override
        public void getLastKnownLocation(Location location) {
            lastLatitude = String.valueOf(location.getLatitude());
            lastLongitude = String.valueOf(location.getLongitude());
            if (mOnGetLocationListener != null) {
                mOnGetLocationListener.getLocation(lastLatitude, lastLongitude, latitude, longitude, country, locality, street);
            }
        }

        @Override
        public void onLocationChanged(final Location location) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            if (mOnGetLocationListener != null) {
                mOnGetLocationListener.getLocation(lastLatitude, lastLongitude, latitude, longitude, country, locality, street);
            }
            country = LocationUtils.getCountryName(Double.parseDouble(latitude), Double.parseDouble(longitude));
            locality = LocationUtils.getLocality(Double.parseDouble(latitude), Double.parseDouble(longitude));
            street = LocationUtils.getStreet(Double.parseDouble(latitude), Double.parseDouble(longitude));
            if (mOnGetLocationListener != null) {
                mOnGetLocationListener.getLocation(lastLatitude, lastLongitude, latitude, longitude, country, locality, street);
            }
            reportLocation();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("onCreate");
        String imei = PhoneUtils.getIMEI();
        String meid = PhoneUtils.getMEID();
        LogUtils.d("imei:" + imei);
        LogUtils.d("meid:" + meid);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        setWifiApEnabled(wifiManager, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                isSuccess = LocationUtils.register(0, 0, mOnLocationChangeListener);
                if (isSuccess){
                    mHandler.removeCallbacks(mReportLocationRunnable);
                    mHandler.postDelayed(mReportLocationRunnable, 3000);
                    ToastUtils.showShort("init success");
                }
                Looper.loop();
            }
        }).start();
    }

    public boolean setWifiApEnabled(WifiManager wifiManager, boolean enabled) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = "MIFI953-8CE9";
            //配置热点的密码
            apConfig.preSharedKey="ETCjsz!";
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("setWifiApEnabled exception:" + e);
            return false;
        }
    }

    private void reportLocation() {
        LogUtils.d("latitude:" + latitude);
        LogUtils.d("longitude:" + longitude);
        LogUtils.d("country:" + country);
        LogUtils.d("locality:" + locality);
        LogUtils.d("street:" + street);

        mHandler.postDelayed(mReportLocationRunnable, 3000);
    }

    private Runnable mReportLocationRunnable = new Runnable() {
        @Override
        public void run() {
            reportLocation();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TrakcernBinder();
    }

    public class TrakcernBinder extends Binder {
        public TrackerService getService() {
            return TrackerService.this;
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d("onDestroy");
        LocationUtils.unregister();
        // 一定要制空，否则内存泄漏
        mOnGetLocationListener = null;
        mHandler.removeCallbacks(mReportLocationRunnable);
        super.onDestroy();
    }

    /**
     * 获取位置监听器
     */
    public interface OnGetLocationListener {
        void getLocation(
                String lastLatitude, String lastLongitude,
                String latitude, String longitude,
                String country, String locality, String street
        );
    }

    public static void startTrackerService(Context context) {
        Intent intent = new Intent(context, TrackerService.class);
        context.startService(intent);
    }
}
