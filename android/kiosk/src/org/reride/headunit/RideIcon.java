package org.reride.headunit;
import android.graphics.*;import android.graphics.drawable.Drawable;
/** One stroke/geometry system for the compact native status and radio controls. */
public final class RideIcon extends Drawable {
 private final String kind;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private int color;private int level=100;
 public RideIcon(String kind,int color){this.kind=kind;this.color=color;}
 public void color(int value){color=value;invalidateSelf();}
 public void level(int value){level=Math.max(0,Math.min(100,value));invalidateSelf();}
 public void draw(Canvas c){int save=c.save();Rect b=getBounds();c.translate(b.left,b.top);c.scale(b.width()/24f,b.height()/24f);p.setColor(color);p.setStrokeWidth(1.8f);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
  if(kind.equals("power")){c.drawArc(new RectF(4,4,20,20),-50,280,false,p);c.drawLine(12,2,12,11,p);}
  else if(kind.equals("wifi")){c.drawArc(new RectF(2,3,22,23),225,90,false,p);c.drawArc(new RectF(5,8,19,22),225,90,false,p);c.drawArc(new RectF(8,13,16,21),225,90,false,p);p.setStyle(Paint.Style.FILL);c.drawCircle(12,21,1.1f,p);}
  else if(kind.equals("bluetooth")){Path x=new Path();x.moveTo(6,6);x.lineTo(17,17);x.lineTo(12,22);x.lineTo(12,2);x.lineTo(17,7);x.lineTo(6,18);c.drawPath(x,p);}
  else if(kind.equals("gps")){c.drawCircle(12,12,7,p);c.drawLine(12,2,12,5,p);c.drawLine(12,19,12,22,p);c.drawLine(2,12,5,12,p);c.drawLine(19,12,22,12,p);p.setStyle(Paint.Style.FILL);c.drawCircle(12,12,2.5f,p);}
  else if(kind.equals("clock")){c.drawCircle(12,12,9,p);c.drawLine(12,6,12,12,p);c.drawLine(12,12,16,14,p);}
  else if(kind.equals("battery")){c.drawRoundRect(new RectF(2,6,20,18),2,2,p);c.drawLine(22,10,22,14,p);p.setStyle(Paint.Style.FILL);c.drawRect(4,8,4+14*level/100f,16,p);}
  else if(kind.equals("charging")){Path x=new Path();x.moveTo(14,2);x.lineTo(7,13);x.lineTo(12,13);x.lineTo(10,22);x.lineTo(18,9);x.lineTo(13,9);x.close();p.setStyle(Paint.Style.FILL);c.drawPath(x,p);}
  c.restoreToCount(save);
 }
 public void setAlpha(int a){p.setAlpha(a);invalidateSelf();}public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
