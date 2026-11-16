package org.reride.headunit;
import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import java.util.*;
final class Session {
 static final String OHU="org.reride.headunit";
 static long until=0;
 static boolean unlocked(){return SystemClock.elapsedRealtime()<until;}
 static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("administrator",Context.MODE_PRIVATE);}
 static boolean configured(Context c){return prefs(c).contains("pin");}
 static DevicePolicyManager dpm(Context c){return (DevicePolicyManager)c.getSystemService(Context.DEVICE_POLICY_SERVICE);}
 static ComponentName admin(Context c){return new ComponentName(c,AdminReceiver.class);}
 static boolean owner(Context c){return dpm(c).isDeviceOwnerApp(c.getPackageName());}
 static void startHome(Context c){c.startActivity(new Intent(c,HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));}
 static void gate(Context c){c.startActivity(new Intent(c,GateActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP));}
 static void receiver(Context c){Intent i=new Intent();i.setComponent(new ComponentName(OHU,"com.andrerinas.openheadunit.main.MainActivity"));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);c.startActivity(i);}
 static void restrictUserApps(Context c){
  if(!owner(c)||!configured(c))return;
  Set<String> allowed=new HashSet<>(Arrays.asList(c.getPackageName(),Brand.navigation(),"com.android.settings"));
  String ime=Settings.Secure.getString(c.getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);if(ime!=null)allowed.add(ime.split("/")[0]);
  android.content.pm.PackageManager pm=c.getPackageManager();Intent launcher=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
  org.json.JSONArray hidden=Brand.hidden();for(int index=0;index<hidden.length();index++){String name=hidden.optString(index);if(allowed.contains(name))continue;try{android.content.pm.ApplicationInfo app=pm.getApplicationInfo(name,0);if(app.uid<10000||(app.flags&android.content.pm.ApplicationInfo.FLAG_SYSTEM)!=0||pm.getPackagesForUid(app.uid).length>1)throw new SecurityException("System and shared-UID packages require a reviewed device adapter");dpm(c).setApplicationHidden(admin(c),name,true);}catch(Exception e){android.util.Log.w("RERIDE","Package not hidden: "+name);}}

 }
 static void policy(Context c,boolean locked){
  if(!owner(c))return;
  Settings.Global.putString(c.getContentResolver(),"policy_control",locked?"immersive.full=*":"immersive.navigation=*");
  try{Object bar=c.getApplicationContext().getSystemService("statusbar");int flags=0;if(locked)for(String key:new String[]{"DISABLE_EXPAND","DISABLE_NOTIFICATION_ICONS","DISABLE_NOTIFICATION_ALERTS","DISABLE_NOTIFICATION_TICKER","DISABLE_SYSTEM_INFO","DISABLE_CLOCK","DISABLE_RECENT","DISABLE_SEARCH"})flags|=bar.getClass().getField(key).getInt(null);if(locked)bar.getClass().getMethod("collapsePanels").invoke(bar);bar.getClass().getMethod("disable",int.class).invoke(bar,flags);}catch(Exception e){android.util.Log.w("RERIDE_KIOSK","Status policy unavailable: "+e.getClass().getSimpleName());}
  if(!owner(c))return;
  DevicePolicyManager p=dpm(c);ComponentName a=admin(c);
  p.clearUserRestriction(a,UserManager.DISALLOW_UNINSTALL_APPS);
  boolean restricted=locked&&configured(c);
  String[] restrictions={UserManager.DISALLOW_APPS_CONTROL,UserManager.DISALLOW_MODIFY_ACCOUNTS,UserManager.DISALLOW_ADD_USER,UserManager.DISALLOW_FACTORY_RESET};
  for(String r:restrictions){if(restricted)p.addUserRestriction(a,r);else p.clearUserRestriction(a,r);}
  if(locked)p.setLockTaskPackages(a,new String[]{c.getPackageName(),"com.android.settings",Brand.navigation()});
  else {ArrayList<String> packages=new ArrayList<>();for(android.content.pm.ApplicationInfo app:c.getPackageManager().getInstalledApplications(0))packages.add(app.packageName);p.setLockTaskPackages(a,packages.toArray(new String[0]));}
  IntentFilter f=new IntentFilter(Intent.ACTION_MAIN);f.addCategory(Intent.CATEGORY_HOME);f.addCategory(Intent.CATEGORY_DEFAULT);p.addPersistentPreferredActivity(a,f,new ComponentName(c,"com.andrerinas.openheadunit.main.MainActivity"));

 }
}
