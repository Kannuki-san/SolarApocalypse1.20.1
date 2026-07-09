# Refactor Plan

## 2026-07-09 IDE warning cleanup

VS Codeの問題一覧に残っていた警告を整理した。

* JDTのnull safety警告はForge/Minecraft側のNonnull注釈との相性によるIDE警告だったため、ローカル `.vscode/settings.json` で `java.compile.nullAnalysis.mode` を `disabled` に変更
* `ModLoadingContext.get()` の非推奨警告は、Forge 1.20.1の `FMLJavaModLoadingContext` コンストラクタ注入に変更して解消
* `.vscode/` はignore済みなので、ローカルのJava 17絶対パスやIDE設定はGit管理に含めない

検証。

```text
./gradlew build
BUILD SUCCESSFUL
```

## 2026-07-09 Simulation distance processing radius

演算距離12チャンクに対して、終末処理の範囲が狭く見えることを確認した。
原因は `chunkRadius` config のデフォルトが8で、実際の処理範囲が `min(config, simulationDistance)` になっていたため。

対応。

* 処理範囲configのキーを `maxProcessingChunkRadius` に変更
* デフォルトを32にし、通常はサーバー演算距離が実際の上限になるよう変更
* 既存server configの古い `chunkRadius=8` に引きずられないようにした

補足。

1tickに選ぶチャンク数は変えていないため、遠方は低頻度で進む。
軽さを維持しつつ、候補範囲だけ演算距離いっぱいに広げる方針にした。

## 2026-07-09 Global config file location

設定ファイルがワールドごとの `serverconfig` に生成されていた。
Solar Apocalypseの調整値はワールドごとではなく、Forgeの `config` フォルダ側で扱いたいため登録タイプを変更した。

対応。

* config登録を `ModConfig.Type.SERVER` から `ModConfig.Type.COMMON` に変更
* `SolarApocalypseConfig.SERVER_SPEC` を `COMMON_SPEC` にリネーム
* 生成先を `run/saves/<world>/serverconfig/solarapocalypse-server.toml` ではなく `run/config/solarapocalypse-common.toml` 側に変更

## 2026-07-09 Grass spread suppression and config comments

草ブロックが土になっても、近くの草ブロックや菌糸ブロックからすぐ再拡散していた。
水源化とは違いForge側に専用キャンセルイベントが見当たらないため、既存のMixin設定を使ってvanillaの拡散処理へ直接差し込む方針にした。

対応。

* `SpreadingSnowyDirtBlock#randomTick` にMixinを追加
* 終末の草劣化日以降、オーバーワールドかつ空が見える場所では草ブロック/菌糸ブロックの自然拡散random tickをキャンセル
* config説明コメントを英語行の下に日本語行が出る形へ変更

## 2026-07-09 Water evaporation radius tuning

水の蒸発がまだ遅く見えたため、1回の水処理をブロック数指定ではなく半径指定に寄せた。

対応。

* 水の塊処理を `2〜5個` のブロック数指定から、半径 `2〜3` の範囲指定へ変更
* 半径内の候補をシャッフルし、予算が許す範囲でランダムに蒸発させるよう変更
* 水のまとまりが進みやすいよう、1tickのブロック更新上限デフォルトを12へ変更
* configキーを `minWaterClusterRadius` / `maxWaterClusterRadius` に変更し、古い水ブロック数指定に引きずられないようにした

## 2026-07-09 Water source evaporation origins

水流だけの場所から水蒸発クラスタが始まらないようにした。

対応。

* 水蒸発クラスタの起点判定を水源ブロックだけに限定
* 起点が見つかった後の半径内巻き込みは、水流や水没ブロックも引き続き対象

## 2026-07-09 Dead bush preservation and scarce plant notes

枯れ木が荒廃した地表の雰囲気に合っていたため、壊す対象ではなく残す対象へ寄せた。

対応。

* 枯れ木は消さず、下の草ブロックなどを土へ戻す対象に変更
* 下のブロック更新で枯れ木が壊れた場合は、可能なら枯れ木を置き直すよう変更
* 草、花、苗木などの地表植物は低確率で枯れ木へ変化するよう変更
* 竹本体、竹の子、羊毛、カーペットを燃焼対象に追加
* 後々の入手救済を考える候補を、Git管理外の `future_gameplay_preservation_notes.md` に記録

## 2026-07-09 Faster water clusters and arid plant decay

水の蒸発がまだ遅かったため、起点の水だけを通常のブロック更新上限に数え、周囲の巻き込み水は予算外でまとめて蒸発するようにした。

対応。

* 水蒸発クラスタのデフォルト半径を `3〜5` に変更
* 水蒸発でブロック更新上限に数えるのは、選ばれた起点ブロックだけに変更
* 周囲の水、水流、水没ブロックは、半径内の巻き込みとして予算外で蒸発
* 周囲巻き込みを予算外にしたため、1tickのブロック更新上限デフォルトを8へ変更
* サトウキビとサボテンを地表植物と同じ枯れ処理へ追加
* サトウキビとサボテンの根元をたどり、下の砂や赤砂をガラス化する処理を追加

## 2026-07-09 Broader mod tag support

他modの植物や木材系ブロックも終末処理に巻き込みやすくするため、バニラ個別ブロックだけでなく共通タグを見る対象を増やした。

対応。

* Forge系の `flowers`、`crops`、`mushrooms`、`plants`、`saplings`、`vines` などのブロックタグを地表植物の枯れ対象へ追加
* Forge系の `sugar_cane`、`cacti` タグをサトウキビ・サボテン系の枯れ対象へ追加
* 砂のガラス化判定をForgeの `sand` タグへ変更し、他modの砂も対象に入りやすくした
* 木製の樽、本棚、チェスト、フェンスゲート系のForgeタグを燃焼対象へ追加

## 2026-07-09 Preservation loot in underground chests

地表の植物が終末処理で失われても復旧手段が残るように、地下探索系のチェストlootへ少量の植物救済枠を追加した。

対応。

* Global Loot Modifierを追加
* 廃坑、ダンジョン、要塞、古代都市のloot tableを対象に設定
* 対象チェスト生成時、候補からランダムに1〜3種類を選び、それぞれ1〜5個追加
* 候補にツタ、各種苗木とマングローブの芽、竹、サトウキビ、サボテン、スイレンの葉、コンブ、海草、シーピクルス、サンゴとサンゴファンを追加

## 2026-07-09 Version 2.0 release preparation

一通りの終末進行、負荷対策、植物救済lootが揃ったため、2.0リリース向けに整えた。

対応。

* 救済loot候補へサンゴブロック5種を追加
* `mod_version` を `2.0` に変更
* mod説明文を現在の内容に合わせて更新
* READMEを現在の進行、config、負荷注意、救済loot、インストール手順に合わせて更新

## 2026-07-09 Release notes draft

GitHub ReleaseとCurseForgeのページへ貼るため、2.0の更新ポイントをローカルメモとして整理した。

対応。

* `release_notes_2.0.md` をGit管理外のリリース文面メモとして追加
* `.gitignore` に `release_notes_2.0.md` を追加
* 日本語の主な更新点、救済loot、注意事項、英語サマリーを記載

## 2026-07-09 Server-side multiplayer compatibility

独自ブロック、独自アイテム、GUI、独自パケットを追加していないため、マルチプレイではクライアント未導入でも動く形に寄せた。

対応。

* `mods.toml` の `displayTest` を `IGNORE_SERVER_VERSION` に設定
* READMEにサーバー側導入のみで動作する旨を追記
* リリースノート草案にもサーバー側導入の注意を追記

## 2026-07-09 VS Code server launch duplicate module workaround

VS CodeのJava Debug構成で `runServer` を起動した際、Java拡張の通常出力 `bin/main` とForgeGradleの `MOD_CLASSES` が同じパッケージを二重に載せ、専用サーバー起動前にモジュール衝突した。

対応。

* `./gradlew runServer` ではmod本体の起動が進み、EULA未同意で通常停止することを確認
* ローカルの `.vscode/launch.json` に `classPaths: []` と `modulePaths: []` を追加し、Java拡張が `bin/main` を自動追加しないよう調整
* `.vscode` はGit管理外のため、必要に応じて `genVSCodeRuns` 後に同じ設定を入れ直す

## 2026-07-09 VS Code classpath diagnostics

VS Code上で `net.minecraftforge.fml.*` が存在しないという診断が再発した。
Gradle build自体は成功していたため、IDE側がForgeGradleのclasspathを拾えていない状態と判断した。

対応。

* `./gradlew eclipse` を実行し、JDT/Eclipse系language server向けの `.classpath`、`.project`、`.settings/` を生成
* `.classpath` には `~/.gradle` 配下のForge/FML依存が入ることを確認
* 生成物にはローカル絶対パスが含まれるため、`.gitignore` に `.classpath`、`.project`、`.settings/` を追加

補足。

VS Code側は生成後にウィンドウリロード、またはJava language serverのワークスペースクリーンを行うと反映されやすい。

## 2026-07-09 Apocalypse surface target expansion

プレイ確認で軽さは良好だったため、ランダム処理のまま対象と進行量を広げた。

対応。

* 水の蒸発を、見つけた塊ごとに2〜5個を目標に進めるよう変更
* 水の2〜5個処理が働くよう、1tickのブロック更新上限デフォルトを6へ変更
* 草ブロックに加えてポドゾル、菌糸、土の道、苔ブロックを土へ戻す対象に追加
* 草、シダ、花、苗木、枯れ木などの地表植物を少しずつ消す対象に追加
* 地表植物を消したあと、予算が残っていれば下の地表ブロックも同時に変換するよう変更
* 原木、葉、木材、階段、ハーフブロック、フェンス、ドア、トラップドア、看板、干し草などを燃焼対象に追加
* 木材系ブロックは火の演出だけでなく、低確率でブロック自体も崩れるよう変更
* 火は対象の上だけでなく、周囲の置ける場所にも置くよう変更

補足。

草ブロックの自然拡散はForge側に水源化のような専用キャンセルイベントが見当たらないため、拡散した地表ブロックをランダム処理で土へ戻す方針にした。

検証。

```text
./gradlew build
BUILD SUCCESSFUL
```
