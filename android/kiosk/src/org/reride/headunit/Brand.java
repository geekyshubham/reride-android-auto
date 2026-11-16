package org.reride.headunit;
import android.content.Context;
import android.graphics.Color;
import org.json.*;
/** Public configuration contains no PIN, device serial or signing material. */
public final class Brand {
 private static JSONObject config=new JSONObject();
 public static void load(Context c){try{config=new JSONObject(NativeUi.readAsset(c,"reride-device.json"));}catch(Exception e){android.util.Log.e("RERIDE","Profile unavailable",e);}}
 private static JSONObject section(String key){JSONObject v=config.optJSONObject(key);return v==null?new JSONObject():v;}
 public static String name(){return section("brand").optString("name","ReRide");}
 public static String unit(){return section("brand").optString("unit_name","ReRide");}
 public static int accent(){try{return Color.parseColor(section("brand").optString("accent","#d7ff62"));}catch(Exception e){return 0xffd7ff62;}}
 public static String navigation(){return section("navigation").optString("package","net.osmand.plus");}
 public static String navigationUri(){return section("navigation").optString("launch_uri","");}
 public static boolean autoBrightness(){return section("policy").optBoolean("automatic_brightness",true);}
 public static boolean disableCellular(){return section("policy").optBoolean("disable_cellular",false);}
 public static long idleMillis(){return Math.max(15,section("policy").optInt("idle_sleep_seconds",60))*1000L;}
 public static JSONArray hidden(){JSONArray a=section("policy").optJSONArray("hidden_packages");return a==null?new JSONArray():a;}
}
