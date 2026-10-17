# 共通素材

- `catalog/kana.txt`: 全環境共通の文字一覧。通常46文字・濁音21文字・半濁音5文字・小書き12文字・旧仮名2文字・長音1文字、計87文字。
- `generated/assets/hiraganablock/`: 261種類のテクスチャ、モデル、blockstate、日本語・英語名。
- `data-modern/`: 1.21以降のデータファイル名。
- `data-legacy/`: 1.20.1向けのデータファイル名。

通常ビルドでは生成済みのファイルを利用します。素材の再生成時のみ Python 3 と Pillow が必要です。

```sh
python -m pip install Pillow
python tools/generate_assets.py --font /path/to/NotoSansJP.ttf
python tools/verify_assets.py
```

文字は Noto Sans JP（SIL Open Font License 1.1）を使って描画しています。
フォントの取得元: https://github.com/google/fonts/tree/main/ofl/notosansjp
フォント本体は同梱していません。ライセンスは `OFL-NotoSansJP.txt` に保存しています。
動画から画像・音声・テクスチャを抽出していません。
