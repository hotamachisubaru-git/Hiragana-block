# Hiragana Block / ひらがなブロック

白いひらがなを表示する、黒・緑・黄の文字ブロックです。
Javaのパッケージは **`org.hotamachisubaru.hiraganablock`**、Gradleのgroupは **`org.hotamachisubaru`** です。

## 文字と判定

- 基本の「あ〜ん」46文字、濁音・半濁音、小書き、旧仮名「ゐ・ゑ」、長音「ー」の計87文字。
- 3色で計261種類。黒＝未判定または不一致、緑＝文字と位置が一致、黄＝答えに含まれるが位置が違う。
- 同じ文字を複数使う答えでは、緑を先に確定し、残った文字数の分だけ黄色にします。
- 「て」＋結合濁点のような入力もUnicode正規化で「で」として扱います。小書き「ゃ」と通常の「や」は区別します。

## 導入先を選ぶ

| 版 | 導入先 | 参加プレイヤー側 |
| --- | --- | --- |
| Paper | サーバーの `plugins/` | MOD不要。既存のコンクリート＋TextDisplayで表示 |
| Fabric | Fabricの `mods/` | 同じMinecraft向けの本MOD＋Fabric APIが必要 |
| Forge | Forgeの `mods/` | 同じMinecraft向けの本MODが必要 |
| NeoForge | NeoForgeの `mods/` | 同じMinecraft向けの本MODが必要 |

**全版のJARを同時に入れるものではありません。** Minecraftバージョンとローダーが合う1種類を選んでください。
MOD版では専用ブロックを追加し、テクスチャもJARに含めます。
Paper版ではインベントリ内は通常の色付きコンクリート＋文字のアイテム名です。置いたブロックの6面に文字を表示します。

## 対応バージョン

1.20.1〜26.3を移植対象としていますが、**全バージョンの動作確認が済んだという意味ではありません**。
mainは26.3向けMOD実装と、1.20.1 APIを基準にしたPaper実装を含みます。
実際のビルド・実機確認の状況は [docs/compatibility.md](docs/compatibility.md) を参照してください。

## 操作

コマンドは管理者用です。Paperは `hiraganablock.admin`（既定OP）、MOD版はゲームマスター権限が必要です。
コマンド名は `/hiragana`、短縮名は `/hb` です。

```text
/hb give あ
/hb give で black 64
/hb give ぶ green 1
/hb give ゃ yellow 1
/hb answer ありがとう
/hb check 100 64 200 east
/hb judge あいう うあい
```

1. `/hb give` で黒い文字ブロックを入手し、1列に並べます。
2. 出題者が `/hb answer <答え>` で正解を設定します（出題者ごとに保存）。
3. 同じ出題者が `/hb check <先頭のx> <y> <z> <方向>` で判定します。
4. 指定方向に答えと同じ文字数だけ読み取り、正解なら緑、位置違いなら黄、不一致なら黒に変えます。

方向は `east`（+X）、`west`（-X）、`south`（+Z）、`north`（-Z）。判定前に列全体を検査し、文字不足・未読み込みチャンクでは変更しません。
答えは1〜32文字です。正解設定コマンドはサーバーログや管理用プラグインに記録される可能性があります。

Paper版には `/hb menu` の文字選択画面もあります。色切替・ページ切替に対応し、クリックで1個、Shiftクリックで64個入手できます。
MOD版ではクリエイティブの建築ブロック欄、またはコマンドから入手できます。サバイバル用クラフトレシピはありません。

## 保存・ブロックの扱い

- Paper: `plugins/HiraganaBlock/blocks.yml` に配置と答えを保存。チャンクの読み込み時に文字表示を復元します。
- MOD: ブロックは通常のワールドデータに保存。答えはワールド内の `hiraganablock-answers.properties` に保存します。
- Paperの文字ブロックはピストン移動・爆発から保護します。通常破壊時は文字と色を保持したアイテムを落とします。
- WorldEditなど他プラグインによる直接置換・コピーは専用連携の対象外です。文字の管理データはコピーされません。
- `/hb check` は管理者による直接更新です。領域保護プラグインの権限チェックを代行しません。
- Paper版とMOD版のワールド間の自動変換は実装していません。

## ビルド

mainはJDK 25を使用します。Windowsでは `./gradlew` を `gradlew.bat` に置き換えてください。

```sh
./gradlew -Pplatform=common :common:check
./gradlew -Pplatform=paper :paper:build
./gradlew -Pplatform=fabric :fabric:build
./gradlew -Pplatform=forge :forge:build
./gradlew -Pplatform=neoforge :neoforge:build
```

プラットフォームごとに開発ツールが異なるため、1回のビルドでは1つを選びます。既定値は `paper` です。
生成物は各フォルダーの `build/libs/` に出ます。通常のビルドではPythonやフォントのインストールは不要です。
GitHub Actionsも各版を個別にビルドし、成功したジョブからJARをダウンロードできます。未検証のJARを正式リリースとして自動公開はしません。

## フォルダー

| 場所 | 内容 |
| --- | --- |
| `common/src/main/java/` | Minecraftに依存しない文字一覧・色・判定 |
| `common/src/selfTest/java/` | 文字数・重複文字・不正入力などの回帰テスト |
| `common/src/minecraft/java/` | MOD版共通のMinecraft向けコマンドとブロック参照 |
| `paper/` | Paperプラグイン |
| `fabric/`, `forge/`, `neoforge/` | 各MODローダー向け登録処理 |
| `assets/` | 共通文字一覧と生成済み素材 |
| `tools/` | 素材生成・整合性確認 |
| `docs/` | 対応状況・実機確認手順 |

## ライセンス

コードはMIT。文字画像はNoto Sans JPを使用して生成しています。フォントのライセンスと出典は `assets/` に同梱しています。
参考動画から画像・音声・テクスチャを抽出していません。
