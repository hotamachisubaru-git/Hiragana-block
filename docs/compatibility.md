# 対応状況

## ビルド確認

| ブランチ | MODの対象Minecraft | Paper | Fabric | Forge | NeoForge |
| --- | --- | --- | --- | --- | --- |
| `main` | 26.3 | 成功 | 成功 | 成功 | 成功 |
| `mc/1.21.1` | 1.21.1 | 成功 | 成功 | 成功 | 成功 |
| `mc/1.20.1` | 1.20.1 | 成功 | 成功 | 成功 | 対象外（Forge版を使用） |

共通処理は2,209件の検証、素材は261種類の整合性確認を実施しています。
**Minecraftを起動した実機確認はまだ行っていません。** ビルド成功と実機確認は別です。

- [26.3向けのビルド](https://github.com/hotamachisubaru-git/Hiragana-block/actions/runs/39080537994)
- [1.21.1向けのビルド](https://github.com/hotamachisubaru-git/Hiragana-block/actions/runs/39080654549)
- [1.20.1向けのビルド](https://github.com/hotamachisubaru-git/Hiragana-block/actions/runs/39080634242)

各ページのArtifactsからプラットフォーム別のZIPをダウンロードし、中のJARを取り出してください。
GitHubへのログインが必要です。Actionsの成果物には保存期限があります。現段階では正式Releaseは公開していません。

## バージョン範囲について

1.20.1〜26.3を移植対象とする構成ですが、**現在のMOD実装対象は上記3バージョンです**。
他の中間バージョン（1.20.2、1.21.4、26.1、26.2など）は専用移植・検証が未実施です。
MODのバージョン指定を広げるだけで対応扱いにしません。

Paper版は全ブランチで共通の実装を使い、Paper 1.20.1の公開APIを基準にJava 17のバイトコードで作成しています。
最新Paperを含む個々のサーバーバージョンでの実行は未確認です。NMS・CraftBukkit・内部クラスへのリフレクションは使用しません。
Spigot・Folia・Geyser/統合版の表示互換性は、動作確認対象に含めていません。

## 公式資料

- Paper Display API: https://docs.papermc.io/paper/dev/display-entities/
- Fabric 26.3: https://fabricmc.net/2026/09/15/263.html
- Fabric ブロック登録: https://docs.fabricmc.net/develop/blocks/first-block
- NeoForge MDK: https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- Forge MDK: https://github.com/MinecraftForge/MinecraftForge/tree/26.3/forge-mdk
