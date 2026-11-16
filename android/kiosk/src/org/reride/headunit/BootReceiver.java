package org.reride.headunit;
import android.content.*;
public final class BootReceiver extends BroadcastReceiver {
 public void onReceive(Context c,Intent i){if(Intent.ACTION_POWER_CONNECTED.equals(i.getAction())){IdleScreen.wake(c);c.startService(new Intent(c,GuardianService.class));return;}if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction())){RadioControls.resetBootMode(c);Session.until=0;c.startService(new Intent(c,GuardianService.class));c.startActivity(new Intent(c,HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}}
}
