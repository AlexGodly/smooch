"""Upload jars from the builds branch to Modrinth."""
import json
import os
import sys
import urllib.request
import uuid

API = "https://api.modrinth.com/v2"
TOKEN = os.environ["MODRINTH_TOKEN"]
SLUG = os.environ.get("MODRINTH_PROJECT", "smooch")
VERSION = os.environ.get("MOD_VERSION", "1.0.0")
CHANGELOG = os.environ.get("CHANGELOG") or "First release."
HEADERS = {"Authorization": TOKEN, "User-Agent": "AlexGodly/smooch-publisher"}

FABRIC_API, CLOTH, MODMENU = "P7dR8mSH", "9s6osm5g", "mOgUt4GM"


def call(method, path, body=None, content_type=None):
    req = urllib.request.Request(API + path, data=body, method=method, headers=dict(HEADERS))
    if content_type:
        req.add_header("Content-Type", content_type)
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read() or b"null")


def main(targets):
    meta = json.load(open(os.path.join(os.path.dirname(__file__), "targets.json")))
    project = call("GET", f"/project/{SLUG}")
    existing = {v["version_number"] for v in call("GET", f"/project/{project['id']}/version")}

    for t in targets:
        number = f"{VERSION}+{t}"
        if number in existing:
            print(f"skip {number}, already on Modrinth")
            continue
        m = meta[t]
        jar = f"smooch-{number}.jar"
        deps = [{"project_id": CLOTH, "dependency_type": "optional"}]
        if "fabric" in m["loaders"]:
            deps += [{"project_id": FABRIC_API, "dependency_type": "required"},
                     {"project_id": MODMENU, "dependency_type": "optional"}]
        data = {
            "project_id": project["id"], "name": f"Smooch {VERSION} ({m['name']})",
            "version_number": number, "changelog": CHANGELOG, "dependencies": deps,
            "game_versions": m["game_versions"], "loaders": m["loaders"],
            "version_type": "release", "featured": True, "status": "listed",
            "file_parts": ["file"], "primary_file": "file",
        }
        boundary = uuid.uuid4().hex
        body = (f"--{boundary}\r\nContent-Disposition: form-data; name=\"data\"\r\n"
                f"Content-Type: application/json\r\n\r\n{json.dumps(data)}\r\n"
                f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"{jar}\"\r\n"
                f"Content-Type: application/java-archive\r\n\r\n").encode()
        body += open(jar, "rb").read() + f"\r\n--{boundary}--\r\n".encode()
        try:
            v = call("POST", "/version", body, f"multipart/form-data; boundary={boundary}")
            print(f"published {number} -> {v['id']}")
        except urllib.error.HTTPError as e:
            print(f"FAILED {number}: {e.code} {e.read().decode()}")
            sys.exit(1)


if __name__ == "__main__":
    main(sys.argv[1:])
