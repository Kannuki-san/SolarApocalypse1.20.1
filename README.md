# Solar Apocalypse for Minecraft Forge 1.20.1

## 概要 / Description

**Solar Apocalypse**は、かつて存在した終末MODの雰囲気に触発された、
**Forge 1.20.1対応のサバイバルMOD**です。

日数が進むごとに地表の環境が壊れていきます。

* 草原が枯れ、地表植物が消えていく
* 水源から水が蒸発し、池や川が干上がる
* 木、葉、木材建材、干し草、羊毛などが燃える
* 砂や赤砂がガラスに変わる
* 日光下のエンティティやアイテムが燃える
* 地下探索系チェストに、失われやすい植物の救済lootが少量追加される

太陽は大きくなりません。

## 進行 / Progression

デフォルト設定では以下の順で進行します。設定は `config/solarapocalypse-common.toml` で変更できます。

* **2日目**: 草ブロック、ポドゾル、菌糸、苔などが土へ戻り、草花や苗木が枯れ始めます。
* **3日目**: 空にさらされた水源を起点に、水が塊で蒸発し始めます。
* **4日目**: 木、葉、木材建材、干し草、羊毛などが燃え始めます。
* **5日目**: 日光下のエンティティが燃え、砂や赤砂がガラス化し始めます。

## 特徴 / Features

* Forge 1.20.1専用
* サーバーの演算距離内で、ランダムなチャンクを少しずつ処理
* 水は水源を起点にし、周囲の水流や水没ブロックもまとめて蒸発
* 草ブロックや菌糸の自然拡散を抑制
* 枯れ木は残し、周囲の荒廃表現として利用
* サトウキビやサボテンも枯れ、下の砂はガラス化
* Forge/Minecraftの共通タグを使い、他modの植物や木材系ブロックもできる範囲で対象化
* 廃坑、ダンジョン、要塞、古代都市のチェストに植物救済lootを追加
* 独自ブロックや独自アイテムを追加しないため、基本的にサーバー側導入のみで動作

大量の水流や大規模な森林火災では負荷が出る場合がありますが、処理はランダム化と上限設定で抑えています。

## 救済loot / Preservation Loot

地表が壊滅したあとでも復旧手段が残るように、地下探索系のチェストに以下の候補から少量が追加されます。

対象チェスト生成時、ランダムに1〜3種類、それぞれ1〜5個が追加されます。

* ツタ
* 各種苗木、マングローブの芽
* 竹、サトウキビ、サボテン
* スイレンの葉、コンブ、海草、シーピクルス
* サンゴ、サンゴファン、サンゴブロック

## インストール / Installation

1. Forge 1.20.1を導入します。開発確認はForge `47.4.4` で行っています。
2. `solarapocalypse-2.0.jar` をサーバーの `mods` フォルダに入れます。シングルプレイではクライアント側の `mods` フォルダに入れてください。
3. 新規ワールド、またはバックアップ済みの既存ワールドでプレイしてください。

## 注意事項 / Notices

* 本MODはワールドの地表を大きく変化させます。
* 大切なワールドでは必ずバックアップを取ってください。
* 大規模な水辺や森林では一時的にFPS低下やサーバー負荷が発生する場合があります。
* 設定ファイルはワールドごとではなく、Forgeの共通 `config` フォルダに生成されます。
* マルチプレイでは基本的にサーバー側のみで動作しますが、クライアント側にも入れておくことは問題ありません。

## FAQ

**Q. 地下で木や作物を育てられますか？**  
A. はい。自然光がなくても、松明などの人工光が十分にあれば育てられます。

**Q. Can I grow trees and crops underground?**  
A. Yes. Trees and crops do not require natural sunlight. They can grow with sufficient artificial light.

## クレジット・参考 / Credits

本MODはMinecraft 1.7.x時代のSolar Apocalypse MODに触発されています。
オリジナルの雰囲気をリスペクトしていますが、コードとアセットは自作です。

* finitewater作者様の一部コード・実装アイデアを参考

## ライセンス / License

MIT

## English Short Description

Solar Apocalypse is a hardcore end-of-the-world survival mod for Forge 1.20.1.
As days pass, exposed grass dies, water evaporates, forests burn, sand turns into glass, and entities under open sky catch fire.
Underground exploration chests can contain a small amount of preservation loot, such as saplings, vines, bamboo, and corals, so a ruined world can still be recovered.
The mod is intended to work as a server-side-only mod in multiplayer because it does not add custom blocks, items, menus, or network packets.
