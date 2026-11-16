package org.reride.headunit;
import android.content.*;import android.os.*;import android.view.*;import android.widget.*;
/** Native app-header status; no overlay and no repeated prose. */
public final class HeaderStatusView extends LinearLayout {
 private final Handler handler=new Handler();private final TextView time,percent;private final RideIcon battery;private final ImageView charging;
 private final Runnable update=new Runnable(){public void run(){Intent b=getContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));int n=b==null?-1:b.getIntExtra("level",-1),scale=b==null?0:b.getIntExtra("scale",0);int value=n>=0&&scale>0?n*100/scale:-1;time.setText(new java.text.SimpleDateFormat("HH:mm",java.util.Locale.US).format(new java.util.Date()));percent.setText(value>=0?value+"%":"--");battery.level(Math.max(value,0));charging.setVisibility(b!=null&&b.getIntExtra("plugged",0)!=0?VISIBLE:GONE);handler.postDelayed(this,10000);}};
 public HeaderStatusView(Context c){super(c);setGravity(Gravity.CENTER_VERTICAL);addIcon(new RideIcon("clock",0xff151b20),16,0);time=text();LayoutParams gap=new LayoutParams(dp(14),1);addView(new View(c),gap);battery=new RideIcon("battery",0xff151b20);addIcon(battery,22,0);percent=text();charging=addIcon(new RideIcon("charging",0xff151b20),12,4);}
 private TextView text(){TextView t=new TextView(getContext());t.setTextColor(0xff151b20);t.setTextSize(12);t.setSingleLine(true);LayoutParams p=new LayoutParams(-2,-2);p.leftMargin=dp(4);addView(t,p);return t;}
 private ImageView addIcon(RideIcon d,int size,int margin){ImageView i=new ImageView(getContext());i.setImageDrawable(d);i.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);LayoutParams p=new LayoutParams(dp(size),dp(size));p.leftMargin=dp(margin);addView(i,p);return i;}
 private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
 protected void onAttachedToWindow(){super.onAttachedToWindow();handler.post(update);}protected void onDetachedFromWindow(){handler.removeCallbacksAndMessages(null);super.onDetachedFromWindow();}
}
