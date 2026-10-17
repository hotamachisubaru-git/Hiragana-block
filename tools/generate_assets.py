#!/usr/bin/env python3
"""Generate exact kana textures and Minecraft resources from the shared catalogue.

Usage: python tools/generate_assets.py --font /path/to/NotoSansJP.ttf
The rendered PNGs and JSON are committed; normal Gradle builds do not need Python.
"""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
COLORS = {"black": (18, 18, 21), "green": (32, 125, 42), "yellow": (177, 144, 24)}
LABELS = {"black": "黒", "green": "緑", "yellow": "黄"}

def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--font", required=True)
    args = parser.parse_args()
    chars = "".join((ROOT / "assets/catalog/kana.txt").read_text(encoding="utf-8").split())
    assert len(chars) == len(set(chars)) == 87
    font = ImageFont.truetype(args.font, 46)
    if hasattr(font, "set_variation_by_axes"):
        font.set_variation_by_axes([600])
    base = ROOT / "assets/generated"
    assets = base / "assets/hiraganablock"
    ja = {"itemGroup.hiraganablock": "ひらがなブロック"}
    en = {"itemGroup.hiraganablock": "Hiragana Blocks"}
    ids = []
    atlas = Image.new("RGB", (1024, 2048), (18,18,21))
    for char in chars:
        for color, background in COLORS.items():
            block_id = f"u{ord(char):x}_{color}"
            ids.append(f"hiraganablock:{block_id}")
            texture = Image.new("RGB", (64, 64), background)
            draw = ImageDraw.Draw(texture)
            draw.rectangle((0, 0, 63, 63), outline=tuple(max(0, c-12) for c in background), width=2)
            box = draw.textbbox((0, 0), char, font=font)
            draw.text(((64-box[2]-box[0])/2, (64-box[3]-box[1])/2), char, font=font, fill=(245,245,245))
            path = assets / "textures/block" / f"{block_id}.png"
            path.parent.mkdir(parents=True, exist_ok=True)
            index = len(ids)-1
            col, row = index % 16, index // 16
            atlas.paste(texture, (col*64,row*64))
            write_json(assets / "blockstates" / f"{block_id}.json", {"variants": {"": {"model": f"hiraganablock:block/{block_id}"}}})
            uv = [col, row/2, col+1, (row+1)/2]
            faces = {face: {"uv": uv, "texture": "#all", "cullface": face} for face in ("north","south","east","west","up","down")}
            write_json(assets / "models/block" / f"{block_id}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "hiraganablock:block/kana_atlas"}, "elements": [{"from": [0,0,0], "to": [16,16,16], "faces": faces}]})
            write_json(assets / "models/item" / f"{block_id}.json", {"parent": f"hiraganablock:block/{block_id}"})
            write_json(assets / "items" / f"{block_id}.json", {"model": {"type": "minecraft:model", "model": f"hiraganablock:block/{block_id}"}})
            loot = {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": f"hiraganablock:{block_id}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]}
            # Resource directory names changed in 1.21. Each build includes only its matching data tree.
            for folder, table in [("modern", "loot_table"), ("legacy", "loot_tables")]:
                write_json(ROOT / f"assets/data-{folder}/data/hiraganablock/{table}/blocks/{block_id}.json", loot)
            ja[f"block.hiraganablock.{block_id}"] = f"{char}（{LABELS[color]}）"
            en[f"block.hiraganablock.{block_id}"] = f"Hiragana {char} ({color.title()})"
    atlas.save(assets / "textures/block/kana_atlas.png", optimize=True)
    for name, dictionary in [("ja_jp", ja), ("en_us", en)]:
        write_json(assets / "lang" / f"{name}.json", dictionary)
    for folder, tag in [("modern", "block"), ("legacy", "blocks")]:
        write_json(ROOT / f"assets/data-{folder}/data/minecraft/tags/{tag}/mineable/pickaxe.json", {"replace": False, "values": ids})
    print(f"Generated {len(ids)} blocks from {len(chars)} kana")

if __name__ == "__main__":
    main()
