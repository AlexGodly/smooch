import json, os, re, shutil, sys

def clone(src, dst, props, mc_range_fabric=None, mc_range_neo=None, srcdir=None, java=None):
    if os.path.exists(dst): shutil.rmtree(dst)
    shutil.copytree(src, dst, ignore=shutil.ignore_patterns('build', '.gradle'))
    p = os.path.join(dst, 'gradle.properties'); s = open(p).read()
    for k, v in props.items():
        s = re.sub(rf'^{k}=.*$', f'{k}={v}', s, flags=re.M)
    open(p, 'w').write(s)
    b = os.path.join(dst, 'build.gradle'); s = open(b).read()
    if srcdir: s = re.sub(r"\.\./common/v\d+", f"../common/{srcdir}", s)
    if java: s = re.sub(r'JavaLanguageVersion.of\(\d+\)', f'JavaLanguageVersion.of({java})', s).replace('release = 21', f'release = {java}').replace('release = 25', f'release = {java}').replace('VERSION_21', f'VERSION_{java}').replace('VERSION_25', f'VERSION_{java}')
    open(b, 'w').write(s)
    fm = os.path.join(dst, 'src/main/resources/fabric.mod.json')
    if mc_range_fabric and os.path.exists(fm):
        d = json.load(open(fm)); d['depends']['minecraft'] = mc_range_fabric
        if java: d['depends']['java'] = f'>={java}'
        json.dump(d, open(fm, 'w'), indent=2)
    nm = os.path.join(dst, 'src/main/resources/META-INF/neoforge.mods.toml')
    if mc_range_neo and os.path.exists(nm):
        s = open(nm).read()
        s = re.sub(r'(modId = "minecraft"\ntype = "required"\nversionRange = )"[^"]*"', rf'\1"{mc_range_neo}"', s)
        s = re.sub(r'(modId = "neoforge"\ntype = "required"\nversionRange = )"[^"]*"', r'\1"[1,)"', s)
        open(nm, 'w').write(s)
