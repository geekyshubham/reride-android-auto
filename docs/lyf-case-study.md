# Origin: one LYF Flame 1

The original private project used a LYF Flame 1 LS-4503 (MSM8909, ARMv7,
Android5.1/API22,1GB RAM,854×480 landscape) as a motorcycle head unit.
It inspired this toolkit; its images and assumptions are not universal support.

What was observed on that unit:

- Original partition backups and complete system readback comparisons.
- Open Headunit hardware H.264 projection near30fps in sampled sessions.
- Microphone/audio software paths, screen sleep and simulated power-event wake.
- Native GPS provider requests, an accelerometer and an ambient light sensor;
  no gyroscope exposed by its hardware/HAL.
- Brand-specific landscape UI, local administrator gate and direct Wi-Fi setup.

Lessons carried forward:

1. Keep the stock signed boot chain when the app-only/stock-system route works.
2. Never overwrite an original backup manifest while verifying it.
3. Repair only a separate host copy after explicit authorization.
4. Android5 immutable system APKs need correctly placed native-library sidecars.
5. Changing a name/icon/vehicle type does not prove motorcycle routing.
6. Physical Recents/Menu mappings differ from virtual App-switch key codes.
7. Screen pinning can emit system toasts; key filtering itself needs hardware tests.
8. Retired app/service support and map format compatibility need real runtime tests.
9. A simulated USB wake and a driver request are not a physical wake/GPS road test.
10. Native Maps and brand artwork are separate from the reusable open-source toolkit.

The latest generic key-kiosk port is a host candidate; its hardware filtering
and brand-independent firmware are not claimed tested on arbitrary models.
No private serial, PIN, MAC, SSID, key, firmware image or OEM programmer is here.
