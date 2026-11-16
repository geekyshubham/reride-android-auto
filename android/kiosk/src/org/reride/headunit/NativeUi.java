package org.reride.headunit;
import android.app.*;import android.content.*;import android.graphics.Color;import android.graphics.drawable.GradientDrawable;import android.os.*;import android.view.*;import android.widget.*;import com.andrerinas.openheadunit.R;
/** Native in-layout header and a single Wi-Fi connection surface. No system overlay window. */
public final class NativeUi {
 public static boolean admin(){return Session.configured(AppHolder.context)&&Session.unlocked();}
 public static void requestAdmin(Context c){Session.gate(c);}
 static final class AppHolder{static Context context;}
 public static void initialize(Context c){AppHolder.context=c.getApplicationContext();Brand.load(c);
  com.andrerinas.openheadunit.utils.Settings identity=new com.andrerinas.openheadunit.utils.Settings(c);identity.setShowToastMessages(false);if(((android.hardware.SensorManager)c.getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(android.hardware.Sensor.TYPE_LIGHT)!=null)identity.setNightMode(com.andrerinas.openheadunit.utils.Settings.NightMode.LIGHT_SENSOR);identity.setVehicleType(3);identity.setVehicleMake(Brand.name());identity.setVehicleModel("Motorcycle");identity.setVehicleDisplayName(Brand.unit());
  android.content.SharedPreferences p=c.getSharedPreferences("firmware",0);
  if(!p.getBoolean("native200",false))try{
   java.io.File profile=new java.io.File("/system/etc/open-headunit/reride-profile.json");
   String json=readAsset(c,"reride-profile.json");
   com.andrerinas.openheadunit.utils.SettingsBackupManager.INSTANCE.importFromJson(c,json,false);
   com.andrerinas.openheadunit.utils.Settings s=new com.andrerinas.openheadunit.utils.Settings(c);
   s.setEnableCarLauncher(false);s.setEnableFloatingButton(false);s.setHasCompletedSetupWizard(true);s.setOnboardingVersion(2);s.setViewMode(com.andrerinas.openheadunit.utils.Settings.ViewMode.TEXTURE);
   p.edit().putBoolean("native200",true).commit();
  }catch(Exception e){android.util.Log.e("RERIDE_KIOSK","Native defaults",e);}
 }
 static String readFile(java.io.File f)throws Exception{java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=new java.io.FileInputStream(f)){byte[] a=new byte[4096];for(int n;(n=in.read(a))!=-1;)b.write(a,0,n);}return b.toString("UTF-8");}
 static String readAsset(Context c,String name)throws Exception{java.io.ByteArrayOutputStream b=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=c.getAssets().open(name)){byte[] a=new byte[4096];for(int n;(n=in.read(a))!=-1;)b.write(a,0,n);}return b.toString("UTF-8");}
 public static void enforce(Activity a){String n=a.getClass().getName();if((n.endsWith(".MainActivity")||n.endsWith(".AapProjectionActivity"))&&Session.configured(a)&&!Session.unlocked()&&Session.owner(a)){if(((ActivityManager)a.getSystemService(Context.ACTIVITY_SERVICE)).isInLockTaskMode())a.stopLockTask();Session.policy(a,true);}}
 public static void install(Activity a){
  String name=a.getClass().getName();if(!name.endsWith(".MainActivity"))return;
  if(!Session.configured(a)){Session.startHome(a);a.finish();return;}
  enforce(a);
  ViewGroup content=(ViewGroup)a.findViewById(android.R.id.content);if(content.getChildCount()!=1||content.getTag()!=null)return;content.setTag("reride-native");View child=content.getChildAt(0);content.removeView(child);
  LinearLayout column=new LinearLayout(a);column.setOrientation(LinearLayout.VERTICAL);LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);int gutter=Math.round(16*a.getResources().getDisplayMetrics().density);row.setPadding(gutter,0,gutter,0);row.setBackgroundColor(Brand.accent());
  TextView brand=new TextView(a);brand.setText(Brand.unit());brand.setTextColor(Color.BLACK);brand.setTypeface(null,1);brand.setTextSize(14);brand.setGravity(Gravity.CENTER_VERTICAL);row.addView(brand,new LinearLayout.LayoutParams(0,-1,1));
  row.addView(new HeaderStatusView(a));
  final Handler h=new Handler();final Runnable gate=()->requestAdmin(a);brand.setOnTouchListener((v,e)->{if(e.getAction()==0)h.postDelayed(gate,4000);if(e.getAction()==1||e.getAction()==3)h.removeCallbacks(gate);return true;});
  if(a instanceof androidx.activity.ComponentActivity)((androidx.activity.ComponentActivity)a).getOnBackPressedDispatcher().addCallback((androidx.lifecycle.LifecycleOwner)a,new androidx.activity.OnBackPressedCallback(true){public void handleOnBackPressed(){}});
  column.addView(row,new LinearLayout.LayoutParams(-1,(int)(32*a.getResources().getDisplayMetrics().density)));FrameLayout canvas=new FrameLayout(a);canvas.addView(child,new FrameLayout.LayoutParams(-1,-1));column.addView(canvas,new LinearLayout.LayoutParams(-1,0,1));content.addView(column,new ViewGroup.LayoutParams(-1,-1));
 }
 public static void home(View root){
  if(!(root instanceof FrameLayout))return;
  FrameLayout host=(FrameLayout)root;
  RideHomeView panel=(RideHomeView)host.findViewWithTag("reride-bounded-home");
  if(panel==null){
   for(int i=0;i<host.getChildCount();i++)host.getChildAt(i).setVisibility(View.INVISIBLE);
   panel=new RideHomeView(host.getContext());panel.bind(root);panel.setTag("reride-bounded-home");
   host.addView(panel,new FrameLayout.LayoutParams(-1,-1));
  }
  panel.update(root);
 }
}
