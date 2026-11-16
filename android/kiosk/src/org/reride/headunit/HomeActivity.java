package org.reride.headunit;
public final class HomeActivity extends GateActivity {
 public void onResume(){super.onResume();if(Session.configured(this)&&!Session.unlocked()){
  if(!Session.owner(this)){message.setText("Kiosk management is unavailable. Maintenance setup is required.");return;}
  Session.policy(this,true);Session.receiver(this);finish();
 }}
}
