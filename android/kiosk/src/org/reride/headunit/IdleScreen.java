package org.reride.headunit;
import android.app.Activity;import android.content.*;import android.os.*;import java.lang.ref.WeakReference;
/** Display-only sleep. No shutdown/reboot or partition operations. */
public final class IdleScreen {
 private static final Handler ui=new Handler(Looper.getMainLooper());
 private static WeakReference<Activity> current=new WeakReference<>(null);
 private static boolean connected=false;
 private static final Runnable sleep=()->{Activity a=current.get();if(a!=null&&!a.isFinishing()&&!connected)sleep(a);};
 public static void resume(Activity a){if(!a.getClass().getName().endsWith(".MainActivity"))return;current=new WeakReference<>(a);touch(a);}
 public static void pause(Activity a){if(current.get()==a){ui.removeCallbacks(sleep);current.clear();}}
 public static void touch(Activity a){if(current.get()==a&&!connected){ui.removeCallbacks(sleep);ui.postDelayed(sleep,Brand.idleMillis());}}
 public static void connection(Activity a,boolean ready){boolean previous=connected;connected=ready;if(ready)ui.removeCallbacks(sleep);else if(previous)touch(a);}
 public static void ready(Activity a){connected=true;ui.removeCallbacks(sleep);wake(a);}
 public static void sleep(Context c){ui.removeCallbacks(sleep);try{Session.dpm(c).lockNow();android.util.Log.i("RERIDE_KIOSK","Display asleep");}catch(Exception e){android.util.Log.e("RERIDE_KIOSK","Display sleep unavailable",e);}}
 public static void wake(Context c){try{PowerManager p=(PowerManager)c.getSystemService(Context.POWER_SERVICE);PowerManager.WakeLock lock=p.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP|PowerManager.ON_AFTER_RELEASE,"RERIDE:DisplayWake");lock.setReferenceCounted(false);lock.acquire(1000);android.util.Log.i("RERIDE_KIOSK","Display awake");}catch(Exception e){android.util.Log.e("RERIDE_KIOSK","Display wake unavailable",e);}}
}
