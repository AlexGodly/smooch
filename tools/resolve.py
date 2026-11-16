"""Fill in 'auto' dependency versions in gradle.properties from Modrinth before a build."""
import json
import re
import sys
import urllib.parse
import urllib.request


def latest(project, mc, loader):
    q = urllib.parse.urlencode({"game_versions": json.dumps([mc]), "loaders": json.dumps([loader])})
    req = urllib.request.Request(f"https://api.modrinth.com/v2/project/{project}/version?{q}",
                                 headers={"User-Agent": "AlexGodly/smooch-build"})
    versions = json.load(urllib.request.urlopen(req))
    releases = [v for v in versions if v["version_type"] == "release"] or versions
    return releases[0]["version_number"] if releases else None


def main(path):
    text = open(path).read()
    props = dict(re.findall(r"^(\w+)=(.*)$", text, re.M))
    mc = props["minecraft_version"]
    lookups = {"fabric_version": ("fabric-api", "fabric"),
               "cloth_config_version": ("cloth-config", props.get("cloth_loader", "fabric")),
               "modmenu_version": ("modmenu", "fabric")}
    for key, (project, loader) in lookups.items():
        if props.get(key) == "auto":
            v = latest(project, mc, loader)
            if v is None:
                if project == "cloth-config":
                    text = re.sub(r"^cloth_config_version=auto$", "cloth_config_version=none", text, flags=re.M)
                    print("cloth_config_version -> none")
                    continue
                sys.exit(f"no {project} for {mc}/{loader}")
            if project == "cloth-config":
                v = v.split("+")[0]
            text = re.sub(rf"^{key}=auto$", f"{key}={v}", text, flags=re.M)
            print(f"{key} -> {v}")
    if props.get("forge_version") == "auto":
        req = urllib.request.Request("https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json",
                                     headers={"User-Agent": "AlexGodly/smooch-build"})
        promos = json.load(urllib.request.urlopen(req))["promos"]
        v = promos.get(f"{mc}-recommended") or promos.get(f"{mc}-latest")
        if v is None:
            sys.exit(f"no forge for {mc}")
        text = re.sub(r"^forge_version=auto$", f"forge_version={v}", text, flags=re.M)
        print(f"forge_version -> {v}")
        import os
        toml = os.path.join(os.path.dirname(path), "src/main/resources/META-INF/mods.toml")
        if os.path.exists(toml):
            t = open(toml).read().replace("@MAJOR@", v.split(".")[0])
            open(toml, "w").write(t)
    open(path, "w").write(text)


if __name__ == "__main__":
    main(sys.argv[1])
