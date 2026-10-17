#!/usr/bin/env python3
"""Verify that every registered kana/color has models, translations and self-drop data."""
import json
from pathlib import Path
from PIL import Image

root = Path(__file__).resolve().parents[1]
chars = ''.join((root/'assets/catalog/kana.txt').read_text(encoding='utf-8').split())
assets = root/'assets/generated/assets/hiraganablock'
ja = json.loads((assets/'lang/ja_jp.json').read_text(encoding='utf-8'))
count = 0
atlas = Image.open(assets/'textures/block/kana_atlas.png')
assert atlas.size == (1024,2048)
for char in chars:
    for color in ('black','green','yellow'):
        bid = f'u{ord(char):x}_{color}'
        assert f'block.hiraganablock.{bid}' in ja
        x,y = (count%16)*64, (count//16)*64
        im = atlas.crop((x,y,x+64,y+64))
        assert len(im.getcolors(65536)) > 3, f'Blank glyph: {bid}'
        model = json.loads((assets/f'models/block/{bid}.json').read_text())
        expected = [count%16, (count//16)/2, count%16+1, (count//16+1)/2]
        assert all(face['uv'] == expected for face in model['elements'][0]['faces'].values())
        for folder in ('blockstates','models/block','models/item','items'):
            json.loads((assets/f'{folder}/{bid}.json').read_text(encoding='utf-8'))
        for version, table in [('modern','loot_table'),('legacy','loot_tables')]:
            loot = json.loads((root/f'assets/data-{version}/data/hiraganablock/{table}/blocks/{bid}.json').read_text())
            assert loot['pools'][0]['entries'][0]['name'] == f'hiraganablock:{bid}'
        count += 1
assert count == 261
print(f'Verified {count} complete block resources')
