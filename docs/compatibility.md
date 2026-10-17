# 対応状況

「実装済み」「ビルド確認済み」「Minecraftを起動して確認済み」を区別します。
バージョン番号や依存範囲を広げただけで互換性を保証しません。

| ブランチ | Minecraft | Paper | Fabric | Forge | NeoForge |
| --- | --- | --- | --- | --- | --- |
| `main` | MOD: 26.3 / Paper: 1.20.1 API基準 | 実装済み・検証中 | 実装済み・検証中 | 実装済み・検証中 | 実装済み・検証中 |

1.20.1や1.21.1などの旧版では、その世代のブランチでソース・ビルドツール・データ形式を移植します。
中間バージョンの対応は、該当バージョンでビルドと実機確認を行ってから追記します。
Paper版は公開APIのみを使い、NMS・CraftBukkit・内部クラスへのリフレクションを使いません。
Spigot・Folia・Geyser/統合版の表示互換性は、この実装の動作確認対象に含めていません。

## 使用した公式資料

- Paper Display API: https://docs.papermc.io/paper/dev/display-entities/
- Fabric 26.3: https://fabricmc.net/2026/09/15/263.html
- Fabric ブロック登録: https://docs.fabricmc.net/develop/blocks/first-block
- NeoForge MDK: https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- Forge MDK: https://github.com/MinecraftForge/MinecraftForge/tree/26.3/forge-mdk
