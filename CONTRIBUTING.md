# Contributing

- Start with an app-only profile and a redacted compatibility report.
- Include exact model/codename/SDK/ABI, tested app versions and PASS/FAIL/NOT TESTED.
- Keep per-device dumps, identities and keys in private/; run the public audit.
- Do not claim support from an emulator or one similar-looking handset.
- Add one focused regression check for a new safety decision or parser.
- Preserve upstream notices and provide corresponding source for modified builds.
- Firmware adapters follow docs/firmware-adapters.md; never contribute vendor images.

Checks: `python3 -m unittest discover -s tests -v` and
`python3 tools/public_audit.py`.
