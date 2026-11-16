# ReRide agent workflow

You are helping someone reuse their own Android phone as a motorcycle head unit.
Do not touch any connected phone merely because this repository is open.

## Start with intake

Ask for (1) exact phone model/codename and Android version if known,
(2) bike brand/display name and a logo they have permission to use, and
(3) Android Auto, offline navigation, or both, including the map region.
Explain that Android Auto projects a separate main phone; offline navigation
runs locally and needs downloaded maps. Do not promise a universal firmware or
complete security on an unsupported OS. Never ask for or record an admin PIN.

## Work in order

1. Read README.md, docs/workflow.md, docs/security.md and the chosen device profile.
2. `python3 reride.py init` creates a gitignored private project. Do not put
   serials, IMEI, MAC addresses, SSIDs, credentials or keys in public profiles.
3. With consent to inspect the chosen device, run `inspect --serial ...`.
   This performs only the documented identity/display reads. Ask the owner to
   confirm the reported model before `bind --confirm-model ...`.
4. Prefer app-only installation of a compatible official Open Headunit release
   and a separately chosen navigation app. Verify APK signer/hash/SDK/ABI.
   Do not automate a factory reset, bootloader unlock, device-owner provisioning,
   accessibility activation, disabling an app, or changing a partition as intake.
5. If managed firmware is requested, research that exact model/build. Do not
   copy LYF sector offsets, a programmer, key maps, a trim list, boot images or
   package names onto another model. Follow docs/firmware-adapters.md. No generic
   firmware writer is shipped. Prepare a concrete reviewed adapter and evidence.
6. Preserve original signed backups, an existing manifest and rollback sources.
   Verify using `verify-backup`; never generate a new manifest to bless corruption.
   Repair only a separately authorized host copy. Stop on unexplained failures.
7. Build on the host with the selected brand and device capabilities. Keep the
   release signing identity stable and private. Preserve the upstream license.
8. Present the exact images, partitions, hashes, model/board match, rollback
   plan and failed/passed tests for explicit device-write authorization. Existing
   session authorization counts only for that same identified device/scope.
9. A supported adapter writes one approved partition, reads it back fully,
   compares hash, then runs its boot gate. Abort on mismatch, tool error, USB
   loss or boot loop. Never improvise writes to recovery, bootloaders, GPT,
   calibration, encryption or identity partitions. Never power off a phone whose
   buttons/recovery path are unusable. A readback cannot guarantee no future brick.
10. Test the real device: AA session/audio/mic; local GPS fix; motorcycle route
    mode; a downloaded-region route with networking off; voice; physical keys,
    PIN/escape recovery, layout, sleep, USB wake, heat/battery and Bluetooth.
    Log PASS/FAIL/NOT TESTED plus evidence privately. Do not turn a candidate,
    simulated wake or advertised motorcycle identity into a road-test pass.

## Publishing

Run `python3 tools/public_audit.py` and tests. Publish only public source/docs/
assets; never private/, work/, build/, firmware dumps, keys, APKs or device logs.
The public Git history starts fresh; the original local LYF clone/history is
kept under ignored private/lyf-source. Do not publish that directory or history.
