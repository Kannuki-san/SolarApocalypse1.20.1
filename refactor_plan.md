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
