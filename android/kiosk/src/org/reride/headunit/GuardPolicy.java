package org.reride.headunit;
/** Window allowlist inside the device-owner package allowlist. */
public final class GuardPolicy {
 /** Android KeyEvent codes: pass=0, block=1, return from Maps=2. */
 public static int keyAction(int code,boolean maps){
  if(code==3||code==82||code==84||code==187||code==219)return 1;
  return code==4&&maps?2:0;
 }
 public static boolean allowsInputMethod(String selected,String pkg,String cls){return selected!=null&&selected.startsWith(pkg+"/")&&!cls.toLowerCase(java.util.Locale.US).contains("settings")&&!cls.endsWith("Activity");}
 public static boolean allows(String pkg,String cls,String own,String navigation){
  if(pkg.equals(own)||cls.contains("InputMethod")||cls.contains("SoftInput"))return true;
  if(pkg.equals(navigation))return true;
  return pkg.equals("com.android.settings")&&((cls.equals("com.android.settings.Settings$WifiSettingsActivity")||cls.startsWith("com.android.settings.wifi.")||cls.equals("android.app.AlertDialog")||cls.contains("BluetoothPairingDialog"))||cls.contains("RequestPermissionActivity"));
 }
}
