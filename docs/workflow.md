# From drawer to handlebar

1. **Identify.** Phone model/codename, SDK, ABI, display, GPS/light sensors,
   functioning buttons, battery condition and recovery method. Bind one device.
2. **Choose.** Bike brand/text, accessible accent and an optional owned PNG logo.
   Choose Android Auto projection, local offline navigation, or both.
3. **Start with apps.** Obtain compatible signed apps from their official sources.
   Android Auto needs a separate supported main phone. Install offline maps
   separately and download the region before disconnecting data.
4. **Add managed features only when supported.** Device-owner/system privileges
   differ by Android release. The managed source port currently targets API22
   and is experimental after generalization. It requires reviewed provisioning;
   an ordinary APK cannot silently remove Android's security UI.
5. **Review firmware per model.** There is no universal flash command or ROM.
   See [firmware-adapters.md](firmware-adapters.md). Unknown models stay blocked.
6. **Validate hardware.** Real route, motorcycle mode, audible guidance, GPS fix,
   media/headset/microphone, keys, PIN recovery, display wake, heat and battery.
   Confirm local navigation with Wi-Fi/mobile data disabled before calling it offline.

## Local commands

```sh
python3 reride.py init --phone-model "Your exact model" --bike-brand "Your bike" --offline-region "Your region"
python3 reride.py inspect --project private/projects/YOUR-ID --serial YOUR_ADB_SERIAL
python3 reride.py bind --project private/projects/YOUR-ID --confirm-model "EXACT REPORTED MODEL"
python3 reride.py plan --project private/projects/YOUR-ID
python3 reride.py verify-backup private/backups/YOUR-ID
```

No command above writes to a phone. Inventory, binding, plan, backup files and
signing material remain private. `inspect` uses `adb get-state`, a fixed getprop
whitelist and `wm size`; it does not reboot, install, unlock, root or flash.

## Navigation choices

- **Open Headunit:** [official project](https://github.com/andreknieriem/open-headunit).
- **Offline motorcycle navigation:** choose a compatible
  [OsmAnd](https://osmand.net/docs/user/navigation/routing/car-based-routing/)
  build, enable Motorcycle, download maps, then test without networking.
- **Other offline apps:** selectable package/launch intent; validate SDK, ABI,
  map format, voice and whether the app actually supports motorcycles.
- **Google Maps:** optional online choice, not a promise of offline motorcycle
  routes. Retired Android/client versions may be blocked by Google. No Google
  APK, modified client, map tiles or account services are distributed here.

## Optional managed APK host build

Set `mode` to `managed` in your private profile and bind an API22 device. Install
JDK21 and Android SDK/platform/build-tools36; obtain the pinned official upstream
APK described in integrations/open-headunit/upstream.json.

```sh
python3 tools/build_managed.py --project private/projects/YOUR-ID --upstream-apk private/upstream.apk
```

Set JAVA_HOME and ANDROID_HOME. The output is build/YOUR-ID/ReRide.apk plus a
checksum manifest. The builder creates a strong random local signing password
and a private per-project key; back both up securely. It does not install or
provision anything. A new signing key cannot replace an existing signed owner.

The branded managed UI uses the configured navigation package, brand name and
optional logo, native header/radio/sleep controls and a first-run administrator
PIN. No PIN default is bundled. Privileged operations require a reviewed device
port; no OEM app is hidden and cellular is not disabled by default. The accent
must contrast with dark instrument text; use a lighter accent or a logo for dark
manufacturer colors. Native libraries remain identical to the pinned official APK.
