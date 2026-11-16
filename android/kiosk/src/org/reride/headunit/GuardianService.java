package org.reride.headunit;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.provider.Settings;
/** Managed-only policy. No OEM package names or handset-specific hardware assumptions. */
public final class GuardianService extends Service {
 private final Handler handler=new Handler();private boolean previouslyUnlocked=false;
 private final BroadcastReceiver power=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(Intent.ACTION_POWER_CONNECTED.equals(i.getAction()))IdleScreen.wake(c);}};
 private final Runnable tick=new Runnable(){public void run(){
  if(!Session.owner(GuardianService.this)){stopSelf();return;}
  try{
   if(Brand.autoBrightness()&&((android.hardware.SensorManager)getSystemService(SENSOR_SERVICE)).getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT)!=null)Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,1);
   boolean unlocked=Session.unlocked();if(previouslyUnlocked&&!unlocked){Session.policy(GuardianService.this,true);Session.startHome(GuardianService.this);}previouslyUnlocked=unlocked;
  }catch(Exception e){android.util.Log.w("RERIDE","Policy needs reviewed permissions: "+e.getClass().getSimpleName());}
  handler.postDelayed(this,5000);
 }};
 public void onCreate(){super.onCreate();registerReceiver(power,new IntentFilter(Intent.ACTION_POWER_CONNECTED));
  if(!Session.owner(this)){stopSelf();return;}
  try{
   if(Brand.disableCellular()){Settings.Global.putString(getContentResolver(),"airplane_mode_radios","cell");Settings.Global.putString(getContentResolver(),"airplane_mode_toggleable_radios","bluetooth,wifi");Settings.Global.putInt(getContentResolver(),"airplane_mode_on",1);sendBroadcast(new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).putExtra("state",true));}
   Settings.System.putInt(getContentResolver(),"accelerometer_rotation",0);Settings.System.putInt(getContentResolver(),"user_rotation",1);
   String key=new ComponentName(this,WindowGuard.class).flattenToString();String old=Settings.Secure.getString(getContentResolver(),"enabled_accessibility_services");
   if(Session.configured(this)){if(old==null||!java.util.Arrays.asList(old.split(":")).contains(key))Settings.Secure.putString(getContentResolver(),"enabled_accessibility_services",old==null||old.isEmpty()?key:old+":"+key);Settings.Secure.putInt(getContentResolver(),"accessibility_enabled",1);}
   Session.policy(this,!Session.unlocked());Session.restrictUserApps(this);
  }catch(Exception e){android.util.Log.w("RERIDE","Managed provisioning incomplete: "+e.getClass().getSimpleName());}
  handler.post(tick);
 }
 public int onStartCommand(Intent i,int flags,int id){return START_STICKY;}
 public IBinder onBind(Intent i){return null;}
 public void onDestroy(){unregisterReceiver(power);handler.removeCallbacksAndMessages(null);super.onDestroy();}
}
