import os
import json

with open("./run/config/illicitblocks.json", "r") as file:
    data = json.load(file)

base_path = "./src/main/resources/assets"

template_json = """
{{
  "model": {{
    "type": "minecraft:model",
    "model": "{}:block/{}"
  }}
}}
"""

for identifier in data["static_list"]:
    namespace, item_id = identifier.split(":")
    content = template_json.format(namespace, item_id)

    dir_path = os.path.join(base_path, namespace, "items")
    os.makedirs(dir_path, exist_ok=True)

    file_path = os.path.join(dir_path, f"{item_id}.json")

    with open(file_path, "w") as f:
        f.write(content)

    print(f"Written {file_path}")