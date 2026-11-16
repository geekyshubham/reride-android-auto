#!/usr/bin/env python3
"""Reject private artifacts and common credential/personal-path leaks before publication."""
from pathlib import Path
import re
import subprocess
import sys

ROOT=Path(__file__).resolve().parents[1]
paths=subprocess.check_output(['git','ls-files','--cached','--others','--exclude-standard','-z'],cwd=ROOT).decode().split('\0')
forbidden={'.apk','.apkm','.img','.bin','.mbn','.jks','.keystore','.pem','.key','.jar','.zip'}
patterns=[re.compile(r'gh[pousr]_[A-Za-z0-9]{20,}'),re.compile(r'github_pat_[A-Za-z0-9_]{30,}'),re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'),re.compile(r'/Users/[A-Za-z_][^/\s"\']+/')]
errors=[]
for name in filter(None,paths):
    p=ROOT/name
    if not p.exists():continue
    if p.is_symlink():errors.append(name+': symlink is not publishable');continue
    if Path(name).parts[0] in {'private','work','build'} or p.suffix.lower() in forbidden:errors.append(name+': private artifact');continue
    if p.stat().st_size>25*1024*1024:errors.append(name+': oversized public asset');continue
    try:text=p.read_text()
    except (UnicodeDecodeError,OSError):continue
    if any(regex.search(text) for regex in patterns):errors.append(name+': credential or personal path pattern')
if errors:
    print('\n'.join(errors),file=sys.stderr);raise SystemExit(1)
print('PASS public source audit: no private artifacts, signing keys, known credential tokens or personal host paths')
