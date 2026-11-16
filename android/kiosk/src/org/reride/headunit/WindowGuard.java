package org.reride.headunit;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
public final class WindowGuard extends AccessibilityService {
 private String foregroundPackage="";
 protected void onServiceConnected(){android.accessibilityservice.AccessibilityServiceInfo info=getServiceInfo();info.flags|=android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;setServiceInfo(info);if(Session.configured(this)&&!Session.unlocked())Session.startHome(this);}
 protected boolean onKeyEvent(android.view.KeyEvent event){
  if(!Session.owner(this)||!Session.configured(this)||Session.unlocked())return false;
  int action=GuardPolicy.keyAction(event.getKeyCode(),foregroundPackage.equals(Brand.navigation()));
  if(action==2&&event.getAction()==android.view.KeyEvent.ACTION_UP)new android.os.Handler(android.os.Looper.getMainLooper()).post(()->Session.receiver(WindowGuard.this));
  return action!=0;
 }
 public void onAccessibilityEvent(AccessibilityEvent event){
  if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED||!Session.owner(this)||!Session.configured(this)||Session.unlocked())return;
  String pkg=String.valueOf(event.getPackageName()),cls=String.valueOf(event.getClassName());
  String ime=android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.DEFAULT_INPUT_METHOD);
  if(GuardPolicy.allowsInputMethod(ime,pkg,cls))return;
  if(pkg.equals(getPackageName())){foregroundPackage=pkg;return;}
  if(cls.contains("InputMethod")||cls.contains("SoftInput"))return;
  if(pkg.equals("com.android.systemui")&&cls.endsWith("RecentsActivity")){performGlobalAction(GLOBAL_ACTION_BACK);if(foregroundPackage.equals(Brand.navigation())){android.content.Intent i=getPackageManager().getLaunchIntentForPackage(Brand.navigation());if(i!=null)startActivity(i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK|android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));}else Session.startHome(this);return;}
  if(pkg.equals("com.android.systemui")&&(cls.contains("Volume")||cls.equals("android.app.Dialog")))return;
  if(pkg.equals("com.android.phone")){performGlobalAction(GLOBAL_ACTION_BACK);Session.startHome(this);return;}
  if(!Session.configured(this))return;
  if(GuardPolicy.allows(pkg,cls,getPackageName(),Brand.navigation())){foregroundPackage=pkg;return;}
  // Package lock-task policy blocks other apps. This gate also protects the
  // receiver's private SettingsActivity, which shares its allowed package.
  android.util.Log.i("RERIDE_KIOSK","Protected window blocked: "+pkg+" / "+cls);
  performGlobalAction(GLOBAL_ACTION_BACK);Session.gate(this);
 }
 public void onInterrupt(){}
}
