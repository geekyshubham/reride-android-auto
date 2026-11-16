#!/usr/bin/env python3
"""Build an experimental API22 managed APK on the host. Never installs or flashes."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import secrets
import struct
import shutil
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from reride import load_project, save, plan, ABIS


def execute(argv, **kwargs):
    subprocess.run([str(a) for a in argv], check=True, **kwargs)


def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--project', required=True);p.add_argument('--upstream-apk',required=True)
    p.add_argument('--sdk', default=os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT'))
    p.add_argument('--java-home',default=os.environ.get('JAVA_HOME'))
    p.add_argument('--upstream-source',help='Optional local Git clone at the pinned commit')
    p.add_argument('--host-test',action='store_true',help='Compile an unbound synthetic profile; no device acceptance claim')
    args=p.parse_args();project,profile=load_project(args.project)
    if profile['deployment']['mode']!='managed':raise ValueError('Select managed mode for this experimental build; app mode uses upstream Open Headunit')
    if not args.host_test and not profile['deployment']['identity_confirmed']:raise ValueError('Bind the exact observed phone model first')
    inventory_path=project/'inventory.json';inventory=json.loads(inventory_path.read_text()) if inventory_path.exists() else profile['phone']
    if not args.host_test:
        eligibility=plan(profile,inventory)
        if eligibility["status"]!="READY_FOR_APP_PREPARATION":raise ValueError("; ".join(eligibility["blockers"]))
    if inventory.get('sdk')!=22:raise ValueError('Managed port currently targets API22 only; use app-first setup or add a reviewed SDK port')
    abi=next((a for a in inventory.get('abis',[]) if a in ABIS),None)
    if not abi:raise ValueError('No supported native ABI')
    if not args.sdk or not args.java_home:raise ValueError('Set ANDROID_HOME and JAVA_HOME (JDK21)')
    sdk=Path(args.sdk).resolve();jdk=Path(args.java_home).resolve();tools=sdk/'build-tools/36.0.0'
    for path in [tools/'zipalign',tools/'apksigner',jdk/'bin/java']:
        if not path.is_file():raise ValueError('Required build tool missing: '+str(path))
    accent=profile['brand']['accent']
    if not re.fullmatch(r'#[0-9a-fA-F]{6}',accent):raise ValueError('Accent must be #RRGGBB')
    def linear(n):
        n=n/255;return n/12.92 if n<=.04045 else ((n+.055)/1.055)**2.4
    def light(s):return sum(w*linear(int(s[i:i+2],16)) for i,w in [(1,.2126),(3,.7152),(5,.0722)])
    if (light(accent)+.05)/(light('#151b20')+.05)<4.5:raise ValueError('Choose a lighter accent for readable instrument text (contrast >=4.5)')
    spec=json.loads((ROOT/'integrations/open-headunit/upstream.json').read_text());apk=Path(args.upstream_apk).resolve()
    with apk.open('rb') as f:
        if hashlib.file_digest(f,'sha256').hexdigest()!=spec['sha256']:raise ValueError('Official upstream APK checksum mismatch')
    build=ROOT/'build'/project.name;build.mkdir(parents=True,exist_ok=True);source=build/'upstream'
    if not source.exists():execute(['git','clone','--no-hardlinks',args.upstream_source or spec['repository'],source])
    commit=subprocess.check_output(['git','-C',str(source),'rev-parse','HEAD'],text=True).strip()
    if commit!=spec['commit']:
        if subprocess.check_output(['git','-C',str(source),'status','--porcelain'],text=True).strip():raise ValueError('Upstream worktree has edits; do not overwrite')
        execute(['git','-C',source,'checkout',spec['commit']])
    marker=source/'.reride-patch'
    if not marker.exists():execute(['git','-C',source,'apply',ROOT/'integrations/open-headunit/managed.patch']);marker.write_text('applied\n')
    gradle=source/'app/build.gradle.kts';s=gradle.read_text();s=re.sub(r'ndk \{ abiFilters \+= "[^"]+" \}', 'ndk { abiFilters += "'+abi+'" }',s);gradle.write_text(s)
    dest=source/'app/src/main/java/org/reride/headunit';dest.mkdir(parents=True,exist_ok=True)
    for f in (ROOT/'android/kiosk/src/org/reride/headunit').glob('*.java'):shutil.copyfile(f,dest/f.name)
    for name in ['admin','accessibility']:shutil.copyfile(ROOT/f'android/kiosk/res/xml/{name}.xml',source/f'app/src/main/res/xml/reride_{name}.xml')
    assets=source/'app/src/main/assets';assets.mkdir(parents=True,exist_ok=True)
    public={k:profile[k] for k in ['brand','navigation','policy']};public['brand']=dict(public['brand']);public['brand']['logo']=None
    save(assets/'reride-device.json',public)
    defaults=json.loads((ROOT/'integrations/open-headunit/defaults.json').read_text());defaults['settings'].update({'vehicle-make':profile['brand']['name'],'vehicle-display-name':profile['brand']['unit_name']})
    save(assets/'reride-profile.json',defaults)
    logo=profile['brand'].get('logo')
    if logo:
        f=Path(logo).resolve()
        if f.suffix.lower()!='.png' or f.stat().st_size>4*1024*1024:raise ValueError('Use a PNG logo under 4MiB, supplied with permission')
        with f.open('rb') as stream:header=stream.read(24)
        if len(header)!=24 or header[:8]!=b'\x89PNG\r\n\x1a\n' or header[12:16]!=b'IHDR':raise ValueError('Invalid PNG logo header')
        width,height=struct.unpack('>II',header[16:24])
        if not (1<=width<=2048 and 1<=height<=2048 and width*height<=2097152):raise ValueError('Logo pixel dimensions exceed the managed UI memory budget')
        shutil.copyfile(f,assets/'brand-logo.png')
    else:(assets/'brand-logo.png').unlink(missing_ok=True)
    libdir=source/f'app/src/main/jniLibs/{abi}';libdir.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(apk) as z:
        libs=[n for n in z.namelist() if n.startswith('lib/'+abi+'/') and n.endswith('.so')]
        if not libs:raise ValueError('Missing upstream ABI payload')
        for n in libs:
            if 'conscrypt' not in n:(libdir/Path(n).name).write_bytes(z.read(n))
    (source/'local.properties').write_text('sdk.dir='+sdk.as_posix()+'\n')
    env=dict(os.environ,JAVA_HOME=str(jdk));execute([source/'gradlew','-p',source,':app:assembleGithubRelease','--console=plain'],env=env)
    built=list((source/'app/build/outputs/apk/github/release').glob('*.apk'))
    if len(built)!=1:raise ValueError('Expected exactly one release APK')
    unsigned=build/'unsigned.apk'
    with zipfile.ZipFile(built[0]) as z,zipfile.ZipFile(apk) as official,zipfile.ZipFile(unsigned,'w') as out:
        for entry in z.infolist():
            data=official.read(entry.filename) if entry.filename in libs else z.read(entry.filename)
            out.writestr(entry,data)
    aligned=build/'aligned.apk';execute([tools/'zipalign','-f','4',unsigned,aligned])
    keys=project/'signing';keys.mkdir(exist_ok=True);key=keys/'release.jks';password=keys/'password'
    if key.exists()!=password.exists():raise ValueError('Restore the existing signing key/password pair; do not silently change identity')
    if not key.exists():
        password.write_text(secrets.token_urlsafe(32));password.chmod(0o600)
        env['RERIDE_SIGNING_PASSWORD']=password.read_text()
        execute([jdk/'bin/keytool','-genkeypair','-keystore',key,'-storepass:env','RERIDE_SIGNING_PASSWORD','-keypass:env','RERIDE_SIGNING_PASSWORD','-alias','reride','-keyalg','RSA','-keysize','3072','-validity','10000','-dname','CN=ReRide Local Build','-noprompt'],env=env);key.chmod(0o600)
    env['RERIDE_SIGNING_PASSWORD']=password.read_text();output=build/'ReRide.apk'
    execute([tools/'apksigner','sign','--ks',key,'--ks-key-alias','reride','--ks-pass','env:RERIDE_SIGNING_PASSWORD','--key-pass','env:RERIDE_SIGNING_PASSWORD','--min-sdk-version','22','--out',output,aligned],env=env)
    execute([tools/'apksigner','verify','--min-sdk-version','22',output],env=env)
    with zipfile.ZipFile(output) as z,zipfile.ZipFile(apk) as official:
        assert all(z.read(n)==official.read(n) for n in libs if n in z.namelist())
    with output.open('rb') as f:digest=hashlib.file_digest(f,'sha256').hexdigest()
    save(build/'manifest.json',{'apk':output.name,'sha256':digest,'abi':abi,'upstream_commit':spec['commit'],'original_jni_preserved':True,'synthetic_host_test':args.host_test,'runtime_tested':False,'device_writes':False})
    print('Built host candidate:',output)

if __name__=='__main__':
    try:main()
    except (ValueError,OSError,subprocess.SubprocessError) as e:raise SystemExit(str(e))
