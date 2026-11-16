package org.reride.headunit;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;
public class GateActivity extends Activity {
 LinearLayout body;TextView message;EditText pin,confirm;boolean busy=false;
 public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);startService(new Intent(this,GuardianService.class));show();}
 void base(String title){body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(32,18,32,18);body.setBackgroundColor(Color.BLACK);getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.BLACK));ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(Color.BLACK);scroll.setFillViewport(true);scroll.addView(body);setContentView(scroll);TextView heading=new TextView(this);heading.setText(title);heading.setTextColor(Brand.accent());heading.setTextSize(24);body.addView(heading);message=new TextView(this);message.setTextColor(Color.WHITE);message.setTextSize(16);body.addView(message);}
 EditText entry(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(Color.WHITE);e.setHintTextColor(0xffa4afb8);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);e.setSaveEnabled(false);body.addView(e);return e;}
 void button(String text,View.OnClickListener action){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(16);b.setTextColor(0xff151b20);float dp=getResources().getDisplayMetrics().density;android.graphics.drawable.GradientDrawable fill=new android.graphics.drawable.GradientDrawable();fill.setColor(Brand.accent());fill.setCornerRadius(18*dp);b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x22000000),fill,null));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,(int)(56*dp));lp.topMargin=(int)(8*dp);body.addView(b,lp);b.setOnClickListener(action);}
 void show(){
  if(Session.unlocked()){menu();return;}
  boolean setup=!Session.configured(this);base(setup?"ReRide · Set administrator PIN":"ReRide · Administrator access");
  if(setup&&!Session.owner(this)){message.setText("Preparing firmware management. Please keep this device connected for provisioning.");return;}
  message.setText(setup?"Choose 6–12 digits. This PIN protects settings and other apps. There is no default PIN.":"Enter your administrator PIN. Maintenance access closes after five minutes.");
  pin=entry("Administrator PIN");if(setup)confirm=entry("Confirm PIN");
  pin.setImeOptions(setup?android.view.inputmethod.EditorInfo.IME_ACTION_NEXT:android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
  EditText last=setup?confirm:pin;last.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);last.setOnEditorActionListener((v,id,event)->{if(id==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){submit(setup);return true;}return false;});
  button(setup?"Set PIN":"Unlock",v->submit(setup));
  if(!setup)button("Return to head unit",v->{Session.receiver(this);finish();});
 }
 void submit(boolean setup){
  if(pin!=null)((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(pin.getWindowToken(),0);
  if(busy)return;if(setup&&(!Session.owner(this)||(Session.configured(this)&&!Session.unlocked()))){show();return;}String value=pin.getText().toString();
  if(!PinCodec.valid(value)){message.setText("Use 6–12 digits.");return;}
  if(setup&&!value.equals(confirm.getText().toString())){message.setText("The PINs do not match.");return;}
  long remaining=Session.prefs(this).getLong("retryAfter",0)-System.currentTimeMillis();
  if(!setup&&remaining>0){message.setText("Please wait "+((remaining+999)/1000)+" seconds.");return;}
  busy=true;message.setText("Checking…");
  new Thread(()->{
   try{
    boolean accepted=setup||PinCodec.verify(value,Session.prefs(this).getString("pin",null));
    String record=setup?PinCodec.encode(value):null;
    runOnUiThread(()->{busy=false;pin.setText("");if(confirm!=null)confirm.setText("");
     if(accepted){android.util.Log.i("RERIDE_AUTH","Administrator authentication accepted");if(setup&&Session.configured(this)&&!Session.unlocked()){show();return;}if(setup)Session.prefs(this).edit().putString("pin",record).commit();Session.prefs(this).edit().putInt("failures",0).remove("retryAfter").commit();if(setup){Session.until=0;Session.policy(this,true);Session.receiver(this);finish();return;}Session.until=SystemClock.elapsedRealtime()+300000;Session.policy(this,false);try{if(((ActivityManager)getSystemService(ACTIVITY_SERVICE)).isInLockTaskMode())stopLockTask();}catch(Exception ignored){}menu();}
     else{android.util.Log.i("RERIDE_AUTH","Administrator authentication denied");int n=Session.prefs(this).getInt("failures",0)+1;Session.prefs(this).edit().putInt("failures",n).putLong("retryAfter",n>=5?System.currentTimeMillis()+30000:0).commit();message.setText(n>=5?"Incorrect PIN. Wait 30 seconds.":"Incorrect PIN.");}
    });
   }catch(Exception e){runOnUiThread(()->{busy=false;message.setText("PIN check unavailable. No access granted.");});}
  },"RERIDE-PIN").start();
 }
 void menu(){base("ReRide · Maintenance");button("Back to head unit",v->{Session.until=0;Session.policy(this,true);Session.receiver(this);finish();});message.setText("Administrator access is temporary. Hold RERIDE in the app header for four seconds to open this gate again.");
  button("Head unit settings",v->startActivity(new Intent().setClassName(this,"com.andrerinas.openheadunit.main.SettingsActivity")));
  button("Android Settings",v->startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)));
  button("Notification panel",v->{try{Object bar=getApplicationContext().getSystemService("statusbar");bar.getClass().getMethod("expandNotificationsPanel").invoke(bar);}catch(Exception e){message.setText("Notification panel unavailable.");}});

  button("Change administrator PIN",v->{base("Change PIN");pin=entry("New PIN (6–12 digits)");confirm=entry("Confirm new PIN");button("Save new PIN",w->submit(true));});
  button("Lock and start head unit",v->{Session.until=0;Session.policy(this,true);Session.receiver(this);finish();});
 }
 void apps(){if(!Session.unlocked()){show();return;}base("Maintenance · Apps");android.content.pm.PackageManager pm=getPackageManager();ArrayList<android.content.pm.ResolveInfo> apps=new ArrayList<>(pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0));Collections.sort(apps,(a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));for(android.content.pm.ResolveInfo a:apps){String pkg=a.activityInfo.packageName;button(a.loadLabel(pm).toString(),v->{if(Session.unlocked()){Intent i=pm.getLaunchIntentForPackage(pkg);if(i!=null)startActivity(i);}else show();});}button("Back",v->menu());}
 public void onBackPressed(){if(Session.unlocked())menu();else{Session.receiver(this);finish();}}
}
