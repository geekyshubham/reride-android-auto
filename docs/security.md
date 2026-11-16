# Security model

- **App-first.** No device write on discovery, profile generation or planning.
- **Bound identity.** Explicit device selection and exact reported model binding.
- **Backups first.** Existing hashes are verified; manifests are never rewritten.
- **No universal ROM.** Unknown device firmware remains blocked.
- **Private by default.** Keys, PINs, dumps, logs and device identifiers stay local.
- **Stable signing.** Per-project private keys; no shared release key or default PIN.
- **Local PIN setup.** Salted PBKDF2; code never needs a user's PIN in a profile.
- **Privileges are explicit.** Managed ownership/accessibility/secure-settings
  provisioning needs a device-specific review and owner consent.
- **Hardware stays intact.** Preserve audio, GPS, Bluetooth, storage, power and
  thermal services. No copied OEM kill list or hidden security downgrade.
- **Truthful status.** Host checks, simulations and real hardware tests are separate.

An unsupported Android version still lacks current OS security fixes. ReRide
cannot provide complete security or guarantee no brick. Keep personal accounts
and sensitive data off a dedicated unsupported device. Do not remove ADB/recovery
access until the physical administrator escape and rollback have been tested.

For reports, use the repository's private GitHub security reporting if enabled;
otherwise open an issue containing only a non-sensitive summary and request a
private contact. Never attach dumps, tokens, passwords or device identity data.
