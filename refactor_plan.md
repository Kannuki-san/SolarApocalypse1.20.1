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
