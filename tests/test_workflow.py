import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("reride", Path(__file__).parents[1] / "reride.py")
r = importlib.util.module_from_spec(spec); spec.loader.exec_module(r)

class Workflow(unittest.TestCase):
    def test_unknown_firmware_never_becomes_ready(self):
        p = r.profile_new("Example phone", "Example bike", "firmware")
        p["deployment"]["identity_confirmed"] = True
        p["phone"]["model"] = "Example phone"
        value = r.plan(p, {"model":"Example phone", "sdk":22,"abis":["armeabi-v7a"]})
        self.assertEqual(value["status"], "BLOCKED")
        self.assertFalse(value["automatic_flash_available"])

    def test_binding_and_managed_sdk_gates(self):
        p = r.profile_new("Example", "Bike", "managed")
        self.assertEqual(r.plan(p)["status"], "BLOCKED")
        p["deployment"]["identity_confirmed"] = True; p["phone"].update(model="Example",serial_sha256="binding",device="sample",board="sample",sdk=22)
        self.assertEqual(r.plan(p,{"model":"Example","sdk":34,"abis":["arm64-v8a"]})["status"],"BLOCKED")
        self.assertEqual(r.plan(p,{"model":"Other","sdk":22,"abis":["armeabi-v7a"]})["status"],"BLOCKED")
        self.assertEqual(r.plan(p,{"model":"Example","sdk":22,"abis":["armeabi-v7a"],"serial_sha256":"binding","device":"sample","board":"sample"})["status"],"READY_FOR_APP_PREPARATION")

    def test_same_model_different_device_is_blocked(self):
        p=r.profile_new("Example","Bike");p["deployment"]["identity_confirmed"]=True
        p["phone"].update(model="Example",device="sample",board="sample",sdk=22,serial_sha256="one")
        self.assertEqual(r.plan(p,{"model":"Example","device":"sample","board":"sample","sdk":22,"serial_sha256":"two"})["status"],"BLOCKED")

    def test_backup_manifest_is_not_rewritten(self):
        with tempfile.TemporaryDirectory() as d:
            p=Path(d);(p/"stock.img").write_bytes(b"original")
            data=(hashlib.sha256(b"original").hexdigest()+"  stock.img\n").encode()
            (p/"SHA256SUMS").write_bytes(data)
            self.assertEqual(r.verify_backups(p)["status"],"PASS")
            self.assertEqual((p/"SHA256SUMS").read_bytes(),data)
            (p/"stock.img").write_bytes(b"corrupt")
            with self.assertRaises(ValueError):r.verify_backups(p)
            self.assertEqual((p/"SHA256SUMS").read_bytes(),data)

    def test_manifest_escape_and_duplicates_fail(self):
        for name in ["../outside.img", "/tmp/outside.img"]:
            with tempfile.TemporaryDirectory() as d:
                p=Path(d);(p/"SHA256SUMS").write_text("0"*64+"  "+name+"\n")
                with self.assertRaises(ValueError):r.verify_backups(p)

if __name__ == "__main__": unittest.main()
