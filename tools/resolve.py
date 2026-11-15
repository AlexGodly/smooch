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
                sys.exit(f"no {project} for {mc}/{loader}")
            if project == "cloth-config":
                v = v.split("+")[0]
            text = re.sub(rf"^{key}=auto$", f"{key}={v}", text, flags=re.M)
            print(f"{key} -> {v}")
    open(path, "w").write(text)


if __name__ == "__main__":
    main(sys.argv[1])
