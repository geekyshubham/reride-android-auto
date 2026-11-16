package org.reride.headunit;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.provider.Settings;

/** Explicit user radio choices and the phone-hotspot/AA hotspot mode boundary. */
public final class RadioControls {
    private static android.content.SharedPreferences preferences(Context c) { return c.getSharedPreferences("radios", 0); }
    public static boolean wanted(Context c, String radio) { return preferences(c).getBoolean(radio, true); }
    public static boolean bikeMode(Context c) { return preferences(c).getBoolean("bike-mode", false); }
    public static void resetBootMode(Context c) { preferences(c).edit().putBoolean("bike-mode", false).apply(); }
    public static boolean allowAuto(Context c) { return !bikeMode(c) && wanted(c,"wifi") && wanted(c,"bluetooth"); }
    public static boolean bluetoothOn() { BluetoothAdapter b=BluetoothAdapter.getDefaultAdapter();return b!=null&&b.isEnabled(); }
    public static boolean gpsOn(Context c) { android.location.LocationManager m=(android.location.LocationManager)c.getSystemService(Context.LOCATION_SERVICE);return m.getProvider("gps")!=null&&m.isProviderEnabled("gps"); }
    private static WifiManager wifi(Context c) { return (WifiManager)c.getApplicationContext().getSystemService(Context.WIFI_SERVICE); }
    public static boolean wifiOn(Context c) {
        WifiManager w=wifi(c);if(w.isWifiEnabled())return true;
        try{return ((Integer)w.getClass().getMethod("getWifiApState").invoke(w))==13;}catch(Exception e){return false;}
    }
    public static boolean internet(Context c) {
        ConnectivityManager m=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo n=m.getActiveNetworkInfo();return n!=null&&n.isConnected();
    }
    private static void stopAuto(Context c) { c.stopService(new Intent().setClassName(c,"com.andrerinas.openheadunit.aap.AapService")); }
    private static void accessPointOff(Context c) {
        try{WifiManager w=wifi(c);w.getClass().getMethod("setWifiApEnabled",WifiConfiguration.class,boolean.class).invoke(w,null,false);}
        catch(Exception e){android.util.Log.w("RERIDE_RADIOS","Hotspot stop: "+e.getClass().getSimpleName());}
    }
    public static void setWifi(Context c, boolean on) {
        preferences(c).edit().putBoolean("wifi",on).apply();
        if(!on){stopAuto(c);accessPointOff(c);}wifi(c).setWifiEnabled(on);
    }
    public static void setBluetooth(Context c, boolean on) {
        preferences(c).edit().putBoolean("bluetooth",on).apply();BluetoothAdapter b=BluetoothAdapter.getDefaultAdapter();if(b!=null){if(on)b.enable();else b.disable();}
    }
    public static void setGps(Context c, boolean on) {
        preferences(c).edit().putBoolean("gps",on).apply();Settings.Secure.putInt(c.getContentResolver(),"location_mode",on?1:0);Settings.Secure.setLocationProviderEnabled(c.getContentResolver(),"gps",on);
    }
    public static void prepareBike(Context c) {
        preferences(c).edit().putBoolean("bike-mode",true).putBoolean("wifi",true).putBoolean("bluetooth",true).putBoolean("gps",true).apply();
        stopAuto(c);accessPointOff(c);wifi(c).setWifiEnabled(true);setBluetooth(c,true);setGps(c,true);
        final Context app=c.getApplicationContext();final android.os.Handler h=new android.os.Handler(android.os.Looper.getMainLooper());
        h.postDelayed(new Runnable(){int attempts=0;public void run(){if(!bikeMode(app)||!wanted(app,"wifi")||attempts++>=10)return;try{int ap=(Integer)wifi(app).getClass().getMethod("getWifiApState").invoke(wifi(app));if(ap==13||ap==12){accessPointOff(app);h.postDelayed(this,500);return;}}catch(Exception ignored){}if(!wifi(app).isWifiEnabled()){wifi(app).setWifiEnabled(true);h.postDelayed(this,500);}}},500);
    }
    public static void openWifiSettings(Context c) {
        prepareBike(c);
        Context host=c;
        while(host instanceof ContextWrapper && !(host instanceof android.app.Activity)) host=((ContextWrapper)host).getBaseContext();
        if(host instanceof android.app.Activity){
            android.app.Activity activity=(android.app.Activity)host;
            if(((android.app.ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE)).isInLockTaskMode()) activity.stopLockTask();
        }
        Session.policy(c,true);
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        c.startActivity(intent);
    }
    public static void startAuto(Context c) {
        preferences(c).edit().putBoolean("bike-mode",false).putBoolean("wifi",true).putBoolean("bluetooth",true).apply();setBluetooth(c,true);
        c.sendBroadcast(new Intent("com.andrerinas.openheadunit.ACTION_CONNECT").setClassName(c,"com.andrerinas.openheadunit.automation.AutomationReceiver"));
    }
}
