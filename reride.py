#!/usr/bin/env python3
"""ReRide: local, app-first preparation. No partition-writing command is shipped."""
import argparse
import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
ABIS = {"armeabi-v7a", "arm64-v8a", "x86", "x86_64"}
PROPERTIES = {
    "manufacturer": "ro.product.manufacturer", "model": "ro.product.model",
    "device": "ro.product.device", "board": "ro.board.platform",
    "hardware": "ro.hardware", "android": "ro.build.version.release",
    "sdk": "ro.build.version.sdk", "security_patch": "ro.build.version.security_patch",
    "abis": "ro.product.cpu.abilist", "abi": "ro.product.cpu.abi",
}


def run(argv):
    return subprocess.check_output(argv, text=True, timeout=20).strip()


def slug(value):
    value = re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")
    if not value:
        raise ValueError("Use at least one ASCII letter or number for the project ID")
    return value[:70]


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")
    path.chmod(0o600)


def load_project(value):
    path = Path(value).resolve()
    profile = json.loads((path / "profile.json").read_text())
    if profile.get("schema_version") != 1:
        raise ValueError("Unsupported project schema")
    return path, profile


def profile_new(model, brand, mode="app", navigation="net.osmand.plus", region=""):
    if not re.fullmatch(r"(?:[a-zA-Z]\w*\.)+[a-zA-Z]\w*", navigation):
        raise ValueError("Navigation package must be an Android package name")
    if not model.strip() or not brand.strip():
        raise ValueError("Phone model and bike brand are required")
    return {
        "schema_version": 1, "phone": {"requested_model": model.strip()},
        "brand": {"name": brand.strip(), "unit_name": brand.strip() + " Ride",
                  "accent": "#d7ff62", "background": "#101612", "logo": None},
        "deployment": {"mode": mode, "identity_confirmed": False, "firmware_adapter": None},
        "navigation": {"package": navigation, "offline_region": region,
                       "motorcycle_profile_verified": False, "offline_route_verified": False},
        "policy": {"idle_sleep_seconds": 60, "automatic_brightness": True,
                   "disable_cellular": False, "hidden_packages": [], "allowed_packages": [],
                   "physical_keylayout": None},
    }


def inspect_device(serial, adb="adb"):
    if not serial or serial.startswith("-") or any(c.isspace() for c in serial):
        raise ValueError("Select one explicit adb serial")
    prefix = [adb, "-s", serial]
    if run(prefix + ["get-state"]) != "device":
        raise ValueError("ADB device is not authorized and online")
    values = {key: run(prefix + ["shell", "getprop", prop]) for key, prop in PROPERTIES.items()}
    values["sdk"] = int(values["sdk"])
    values["abis"] = [s for s in (values["abis"] or values["abi"]).split(",") if s]
    values.pop("abi")
    values["display"] = run(prefix + ["shell", "wm", "size"])
    values["serial_sha256"] = hashlib.sha256(serial.encode()).hexdigest()
    return values


def plan(profile, inventory=None):
    reasons = []
    mode = profile["deployment"]["mode"]
    if not inventory:
        reasons.append("Read-only device inventory is missing")
    if not profile["deployment"]["identity_confirmed"]:
        reasons.append("Reported phone model has not been explicitly bound")
    if inventory and profile["phone"].get("model") != inventory["model"]:
        reasons.append("Bound phone model differs from inventory")
    if inventory and profile["deployment"]["identity_confirmed"]:
        expected=profile["phone"]
        if not expected.get("serial_sha256") or expected.get("serial_sha256") != inventory.get("serial_sha256"):
            reasons.append("Device binding differs or is missing")
        for field in ["device", "board", "sdk"]:
            if expected.get(field) != inventory.get(field): reasons.append("Bound identity changed: " + field)
    if mode == "managed" and inventory:
        if inventory["sdk"] != 22:
            reasons.append("The managed source port is experimental for API22; other SDKs need a port")
        if not (set(inventory["abis"]) & ABIS):
            reasons.append("No matching upstream native ABI")
    if mode == "firmware":
        reasons.append("No reviewed model-specific firmware writer is shipped; never reuse LYF offsets")
    return {
        "project": profile["phone"]["requested_model"], "bike_brand": profile["brand"]["name"],
        "mode": mode, "status": "BLOCKED" if reasons else "READY_FOR_APP_PREPARATION",
        "blockers": reasons,
        "steps": ["Confirm exact model/codename/SDK/ABI and recovery method",
                  "Keep original signed backups and a separately verified rollback",
                  "Build or obtain a compatible, signature-verified head-unit app",
                  "Install a compatible navigation app and download the chosen region",
                  "Test a motorcycle route with networking disabled for offline acceptance",
                  "Test microphone/audio/GPS, keys, PIN recovery, screen sleep and power wake"],
        "writes_device": False, "automatic_flash_available": False,
        "security": "Debloating an old phone does not restore missing OS security updates",
    }


def verify_backups(folder):
    folder = Path(folder).resolve()
    manifest = folder / "SHA256SUMS"
    original = manifest.read_bytes()
    records = []
    seen = set()
    for line in original.decode().splitlines():
        if not line.strip():
            continue
        match = re.fullmatch(r"([a-fA-F0-9]{64}) [ *](.+)", line)
        if not match:
            raise ValueError("Invalid SHA256SUMS line")
        name = match[2]
        relative = Path(name)
        if relative.is_absolute() or ".." in relative.parts or name in seen:
            raise ValueError("Unsafe or duplicate backup manifest path")
        target = folder / relative
        if target.is_symlink() or not target.is_file() or folder not in target.resolve().parents:
            raise ValueError("Missing, linked or escaped backup file: " + name)
        with target.open("rb") as stream:
            digest = hashlib.file_digest(stream, "sha256").hexdigest()
        if digest.lower() != match[1].lower():
            raise ValueError("Backup mismatch: " + name)
        seen.add(name)
        records.append({"file": name, "bytes": target.stat().st_size, "sha256": digest})
    if not records:
        raise ValueError("Empty backup manifest")
    if manifest.read_bytes() != original:
        raise ValueError("Manifest changed during verification")
    return {"status": "PASS", "files": len(records), "bytes": sum(r["bytes"] for r in records),
            "manifest_sha256": hashlib.sha256(original).hexdigest(), "manifest_rewritten": False}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)
    init = sub.add_parser("init", help="Ask for the phone model and bike brand; write a private profile")
    init.add_argument("--phone-model"); init.add_argument("--bike-brand")
    init.add_argument("--mode", choices=["app", "managed", "firmware"], default="app")
    init.add_argument("--navigation-package", default="net.osmand.plus")
    init.add_argument("--offline-region", default=""); init.add_argument("--id")
    for name in ["inspect", "bind", "plan"]:
        item = sub.add_parser(name)
        item.add_argument("--project", required=True)
        if name == "inspect": item.add_argument("--serial", required=True)
        if name == "bind": item.add_argument("--confirm-model", required=True)
    backup = sub.add_parser("verify-backup"); backup.add_argument("folder")
    sub.add_parser("flash", help="Explain why a reviewed device-specific adapter is required")
    args = parser.parse_args()
    try:
        if args.command == "init":
            model = args.phone_model or (input("Exact phone model: ") if sys.stdin.isatty() else "")
            brand = args.bike_brand or (input("Bike brand / display name: ") if sys.stdin.isatty() else "")
            profile = profile_new(model, brand, args.mode, args.navigation_package, args.offline_region)
            path = ROOT / "private/projects" / slug(args.id or model + "-" + brand)
            if path.exists(): raise ValueError("Project exists; refusing to overwrite")
            save(path / "profile.json", profile)
            print(path.relative_to(ROOT))
        elif args.command == "verify-backup":
            print(json.dumps(verify_backups(args.folder), indent=2))
        elif args.command == "flash":
            raise ValueError("BLOCKED: firmware needs an audited adapter for the exact model/build and explicit write approval. See docs/firmware-adapters.md")
        else:
            path, profile = load_project(args.project)
            if args.command == "inspect":
                inventory = inspect_device(args.serial)
                save(path / "inventory.json", inventory)
                print(json.dumps({k: v for k, v in inventory.items() if k != "serial_sha256"}, indent=2))
            elif args.command == "bind":
                inventory = json.loads((path / "inventory.json").read_text())
                if args.confirm_model != inventory["model"]:
                    raise ValueError("Confirmation must exactly match the reported model")
                profile["phone"].update(inventory)
                profile["deployment"]["identity_confirmed"] = True
                save(path / "profile.json", profile)
                print("Bound this private project to the observed device identity")
            else:
                inventory_path = path / "inventory.json"
                inventory = json.loads(inventory_path.read_text()) if inventory_path.exists() else None
                value = plan(profile, inventory)
                save(path / "plan.json", value)
                print(json.dumps(value, indent=2))
    except (ValueError, OSError, subprocess.SubprocessError, KeyError) as error:
        parser.exit(2, str(error) + "\n")


if __name__ == "__main__": main()
