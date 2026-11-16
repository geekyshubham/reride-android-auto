import org.reride.headunit.GuardPolicy;
public class GuardPolicyTest {
 public static void main(String[] args){
  String own="org.reride.headunit", nav="net.osmand.plus";
  if(!GuardPolicy.allows(nav,"MapActivity",own,nav)||GuardPolicy.allows("other.app","Main",own,nav))throw new AssertionError("Selected navigation allowlist");
  if(GuardPolicy.allows("com.google.android.apps.maps","MapsActivity",own,nav))throw new AssertionError("Unselected navigation app allowed");
  if(!GuardPolicy.allows("com.android.settings","com.android.settings.wifi.WifiDialog",own,nav)||GuardPolicy.allows("com.android.settings","com.android.settings.Settings",own,nav))throw new AssertionError("Network setup boundary");
  for(int code:new int[]{3,82,84,187,219})if(GuardPolicy.keyAction(code,true)!=1)throw new AssertionError("Escape key");
  if(GuardPolicy.keyAction(4,true)!=2||GuardPolicy.keyAction(4,false)!=0)throw new AssertionError("Back behavior");
  for(int code:new int[]{7,8,24,25,26,85,87,88,90})if(GuardPolicy.keyAction(code,true)!=0)throw new AssertionError("PIN/media/power key");
  System.out.println("PASS selected navigation, Wi-Fi boundary and keyboard/media policy");
 }
}
