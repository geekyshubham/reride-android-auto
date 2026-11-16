# Device-specific firmware gate

The public release intentionally has no general-purpose partition writer.
Phone model + bike brand is enough to begin research, not enough to safely
flash a phone. A logo never determines a boot image, key map or partition layout.

A contribution adding a writer needs all of the following in a reviewed adapter:

- Exact manufacturer/model/codename, board, SDK, build fingerprint/version range,
  ABI, partition scheme/slots/dynamic-partition status and verified boot policy.
- OEM-authorized unlock/flash path, trusted tool/programmer provenance, power/
  USB requirements and recovery that works with the actual physical buttons.
- Device's own complete verified backups and preserved manifest; rollback images
  and their sizes/hashes. Store everything device-specific in private/.
- Explicit allowlist of writable partitions, actual device-derived offsets only
  if needed, size limits, original hash expectations and protected partitions.
- Host image checks: format/header, fsck, original kernel/framework dependencies,
  JNI ABI/sidecars, labels/permissions, exact trim inventory and retained services.
- A concrete plan containing input/output hashes and expected target identity,
  explicit owner authorization immediately before device writes, full readback
  comparison and an observed boot gate. Abort on mismatch/error/USB loss/boot loop.
- Model-specific physical-key/layout tests and recovery tests before publishing
  a supported status. Hardware, battery and road tests must not be fabricated.

Adapter statuses: `unsupported` → `research` → `host-candidate` →
`device-tested` → `maintained`. A case study is not an adapter. A passed fsck is
not a passed boot, an AA motorcycle field is not a motorcycle route, and an OS
brightness setting is not an endurance test.

The agent can develop an adapter after intake and research; until it is reviewed,
use the app-only route. No LYF images, offsets, bootloader unlock code or OEM
programmer are copied into generic tools. Do not automate arbitrary shell
commands supplied by an untrusted profile.
