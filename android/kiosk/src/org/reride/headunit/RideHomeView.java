package org.reride.headunit;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import com.andrerinas.openheadunit.R;

/** One bounded owner for the visible home. Receiver controls remain its action source. */
public final class RideHomeView extends LinearLayout {
    private static final int ORANGE = Brand.accent();
    private static final int INK = 0xff151b20;
    private final TextView status;
    private final TextView connection;
    private final TextView maps;
    private final FrameLayout power;
    private final Utility wifiControl, bluetoothControl, gpsControl, powerControl;
    private Runnable connectionAction;
    private View source;
    private final android.os.Handler refresh = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable renderTick = new Runnable() {
        public void run() {
            if (source != null && getWindowVisibility() == VISIBLE) update(source);
            refresh.postDelayed(this, 1000);
        }
    };

    public void bind(View source) { this.source = source; }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); refresh.post(renderTick); }
    @Override protected void onDetachedFromWindow() { refresh.removeCallbacksAndMessages(null); super.onDetachedFromWindow(); }


    public RideHomeView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(ORANGE);
        setPadding(dp(16), dp(8), dp(16), dp(16));
        setClipChildren(true);
        setClickable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

        FrameLayout hero = new FrameLayout(context);
        ImageView logo = new ImageView(context);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setAdjustViewBounds(true);
        logo.setMaxWidth(dp(300));
        logo.setMaxHeight(dp(100));
        logo.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        try (java.io.InputStream in = context.getAssets().open("brand-logo.png")) {
            logo.setImageBitmap(BitmapFactory.decodeStream(in));
        } catch (Exception error) {
            TextView wordmark=new TextView(context);wordmark.setText(Brand.name());wordmark.setTextSize(42);wordmark.setTextColor(INK);wordmark.setTypeface(null,Typeface.BOLD);wordmark.setGravity(Gravity.CENTER);
            hero.addView(wordmark,new FrameLayout.LayoutParams(-1,-1));
        }
        hero.addView(logo, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        addView(hero, new LayoutParams(-1, 0, 1));

        status = new TextView(context);
        status.setTag("reride-home-status");
        status.setTextSize(14);
        status.setTextColor(INK);
        status.setGravity(Gravity.CENTER);
        status.setMaxLines(3);
        status.setEllipsize(TextUtils.TruncateAt.END);
        status.setMinHeight(dp(36));
        status.setPadding(0, dp(4), 0, dp(8));
        status.setAccessibilityLiveRegion(ACCESSIBILITY_LIVE_REGION_POLITE);
        addView(status, new LayoutParams(-1, -2));

        LinearLayout actions = new LinearLayout(context);
        actions.setTag("reride-action-row");
        actions.setOrientation(HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        connection = action("reride-connect", "Connect wirelessly");
        maps = action("reride-bike", "Bike navigation");
        LayoutParams left = new LayoutParams(0, dp(56), 1);
        left.rightMargin = dp(12);
        actions.addView(connection, left);
        LayoutParams middle = new LayoutParams(0, dp(56), 1);

        actions.addView(maps, middle);

        addView(actions, new LayoutParams(-1, dp(56)));
        LinearLayout utilities = new LinearLayout(context);
        utilities.setTag("reride-utility-row"); utilities.setOrientation(HORIZONTAL);
        wifiControl = new Utility("reride-wifi", "wifi", "Wi-Fi on");
        bluetoothControl = new Utility("reride-bluetooth", "bluetooth", "BT on");
        gpsControl = new Utility("reride-gps", "gps", "GPS on");
        powerControl = new Utility("reride-screen-sleep", "power", "Sleep");
        power = powerControl.shell;
        Utility[] controls = {wifiControl, bluetoothControl, gpsControl, powerControl};
        for (int i=0;i<controls.length;i++) { LayoutParams lp=new LayoutParams(0,dp(56),1);if(i<controls.length-1)lp.rightMargin=dp(12);utilities.addView(controls[i].shell,lp); }
        LayoutParams utilityLayout = new LayoutParams(-1,dp(56));utilityLayout.topMargin=dp(8);addView(utilities,utilityLayout);
        wifiControl.shell.setOnClickListener(view -> RadioControls.openWifiSettings(getContext()));
        wifiControl.shell.setOnLongClickListener(view -> {RadioControls.setWifi(getContext(), !RadioControls.wifiOn(getContext()));return true;});
        bluetoothControl.shell.setOnClickListener(view -> RadioControls.setBluetooth(getContext(), !RadioControls.bluetoothOn()));
        gpsControl.shell.setOnClickListener(view -> RadioControls.setGps(getContext(), !RadioControls.gpsOn(getContext())));
        power.setContentDescription("Turn screen off");power.setOnClickListener(view -> IdleScreen.sleep(getContext()));powerControl.state(true,"Sleep");

        connection.setOnClickListener(view -> {
            if (RadioControls.bikeMode(getContext()) || connection.getText().toString().equals("Connect wirelessly")) RadioControls.startAuto(getContext());
            else if (connectionAction != null) connectionAction.run();
        });
        maps.setOnClickListener(view -> openBikeMaps());
    }

    private TextView action(String tag, String label) {
        TextView button = new TextView(getContext());
        button.setTag(tag);
        button.setText(label);
        button.setTextSize(14);
        button.setTypeface(null, Typeface.BOLD);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setPadding(dp(12), 0, dp(12), 0);
        decorate(button);
        return button;
    }

    private void decorate(View view) {
        GradientDrawable fill = new GradientDrawable();
        fill.setColor(INK);
        fill.setCornerRadius(dp(14));
        GradientDrawable mask = new GradientDrawable();
        mask.setColor(Color.WHITE);
        mask.setCornerRadius(dp(14));
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44ffffff), fill, mask));
        view.setClickable(true);
        view.setFocusable(true);
    }

    /** Also usable by the signed layout check without changing connection state. */
    public void applyState(CharSequence message, String label, boolean enabled, Runnable callback) {
        if (!TextUtils.equals(status.getText(), message)) status.setText(message);
        if (!TextUtils.equals(connection.getText(), label)) connection.setText(label);
        connection.setEnabled(enabled);
        connection.setAlpha(enabled ? 1f : .55f);
        connectionAction = callback;
    }

    public void update(View source) {
        TextView primary = source.findViewById(R.id.home_primary_action);
        TextView cancel = source.findViewById(R.id.home_primary_cancel);
        TextView title = source.findViewById(R.id.home_primary_title);
        TextView stage = source.findViewById(R.id.home_primary_subtitle);
        String label = primary == null ? "Connect wirelessly" : primary.getText().toString();
        boolean connected = label.contains("Back to Android Auto") || label.contains("Open Android Auto");
        boolean cancelling = !connected && primary != null && primary.getVisibility() != VISIBLE
                && cancel != null && cancel.getVisibility() == VISIBLE;
        TextView target = cancelling ? cancel : primary;
        String visibleLabel = cancelling ? "Cancel connection" : connected ? "Open Android Auto" : "Connect wirelessly";
        String message = title == null ? "Connect your phone over Wi-Fi" : title.getText().toString();
        String step = stage == null ? "" : stage.getText().toString();
        if (!connected && !step.isEmpty() && !step.equals("Over WiFi") && !step.equals("Over Wi-Fi")
                && !message.equals(step)) message += "\n" + step;
        Activity activity = activity(getContext());
        if (activity != null) {
            View warning = activity.findViewById(R.id.connection_issue_banner);
            TextView warningText = activity.findViewById(R.id.connection_issue_text);
            if (!connected && warning != null && warning.getVisibility() == VISIBLE && warningText != null
                    && !TextUtils.isEmpty(warningText.getText())) message = warningText.getText().toString();
            IdleScreen.connection(activity, connected);
        }
        boolean bikeMode = RadioControls.bikeMode(getContext());
        if(bikeMode){message=!RadioControls.wifiOn(getContext())?"Wi-Fi is off. Enable Wi-Fi for bike navigation.":RadioControls.internet(getContext())?"Navigation ready":"Navigation · Wi-Fi is not connected";visibleLabel="Android Auto";}
        wifiControl.state(RadioControls.wifiOn(getContext()),RadioControls.wifiOn(getContext())?"Wi-Fi on":"Wi-Fi off");
        bluetoothControl.state(RadioControls.bluetoothOn(),RadioControls.bluetoothOn()?"BT on":"BT off");
        gpsControl.state(RadioControls.gpsOn(getContext()),RadioControls.gpsOn(getContext())?"GPS on":"GPS off");
        applyState(message, visibleLabel, bikeMode || target != null && target.isEnabled(),
                () -> { if (target != null) target.performClick(); });
    }

    private void openBikeMaps() {
        RadioControls.prepareBike(getContext());
        launchBikeMaps();
    }

    private void launchBikeMaps() {
        Context context=getContext();
        Intent intent = Brand.navigationUri().isEmpty()
                ? context.getPackageManager().getLaunchIntentForPackage(Brand.navigation())
                : new Intent(Intent.ACTION_VIEW,Uri.parse(Brand.navigationUri())).setPackage(Brand.navigation());
        if(intent==null){status.setText("Install the selected navigation app during setup.");return;}
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            status.setText("The selected navigation app is unavailable.");return;
        }
        Activity host = activity(context);
        if (host != null) {
            ActivityManager manager = (ActivityManager) host.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager.isInLockTaskMode()) host.stopLockTask();
            Session.policy(host, true);
        }
        context.startActivity(intent);
    }

    private final class Utility {
        final FrameLayout shell;final TextView label;final RideIcon glyph;private Boolean on;
        Utility(String tag,String icon,String text){
            shell=new FrameLayout(getContext());shell.setTag(tag);shell.setClickable(true);shell.setFocusable(true);
            LinearLayout contents=new LinearLayout(getContext());contents.setOrientation(VERTICAL);contents.setGravity(Gravity.CENTER);
            glyph=new RideIcon(icon,Color.WHITE);ImageView image=new ImageView(getContext());image.setImageDrawable(glyph);image.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);contents.addView(image,new LayoutParams(dp(22),dp(22)));
            label=new TextView(getContext());label.setTextSize(12);label.setSingleLine(true);label.setGravity(Gravity.CENTER);label.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);contents.addView(label,new LayoutParams(-1,-2));shell.addView(contents,new FrameLayout.LayoutParams(-1,-1));state(true,text);
        }
        void state(boolean enabled,String text){
            if(on==null||on!=enabled){on=enabled;GradientDrawable fill=new GradientDrawable();fill.setColor(enabled?INK:ORANGE);fill.setCornerRadius(dp(14));if(!enabled)fill.setStroke(dp(2),INK);GradientDrawable mask=new GradientDrawable();mask.setColor(Color.WHITE);mask.setCornerRadius(dp(14));shell.setBackground(new RippleDrawable(ColorStateList.valueOf(enabled?0x44ffffff:0x22000000),fill,mask));glyph.color(enabled?Color.WHITE:INK);label.setTextColor(enabled?Color.WHITE:INK);}
            if(!TextUtils.equals(label.getText(),text))label.setText(text);shell.setContentDescription(text);
        }
    }

    private static Activity activity(Context context) {
        while (context instanceof ContextWrapper && !(context instanceof Activity))
            context = ((ContextWrapper) context).getBaseContext();
        return context instanceof Activity ? (Activity) context : null;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
