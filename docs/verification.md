# Public release verification

## Passed on the host

- Five workflow checks: unknown firmware blocked; managed SDK/model/binding
  gates; same-model/different-device rejected; backup corruption rejected;
  manifest not rewritten; unsafe paths rejected.
- Pure Java allowlist/key checks: only the configured navigator is allowed;
  network setup is bounded; escape keys blocked; PIN/media/power preserved.
- Generic API22/ARMv7 managed APK compiled, aligned and locally signed. Pinned
  upstream native libraries remained byte-identical. A synthetic private profile
  was used; this is not a connected-device or road test.
- Managed APK SHA256: `08a142b8b6571792f1272ea4dcb1cd3fc31c3416b12837ded95c9c1b9aaaaffc`.
- Public source audit: private artifacts, signing material, known credential
  patterns and personal host paths excluded. Fresh public history; the original
  project/history is stored only in the ignored local archive.
- HyperFrames check: zero errors/warnings. Visual sheets and independent review
  found no major issues. Delivery24s,1920×1080,H.264/AAC,30fps,720frames.
  The poster is baked as frame0; audio measured, not physically auditioned.

## Not claimed

No generic firmware was flashed, no arbitrary phone was validated, and the
managed key/filter/radio UI port has not been tested on a new physical device.
Offline map/voice/motorcycle accuracy is the chosen navigator's responsibility
and requires real downloaded-region tests. No guarantee of complete security,
current OS patches or a brick-free flash is made.
