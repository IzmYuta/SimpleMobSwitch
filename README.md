# Minecraft Fabric MOD開発テンプレート

このリポジトリはMinecraft Fabric MOD開発のためのテンプレートプロジェクトです。Fabric APIを使用してMinecraft 1.21.5向けのMODを開発するための基本的な構成が含まれています。

## 機能

- Fabric Loader と Fabric API を使用したMOD開発環境
- Java 21対応
- クライアントとサーバーの分離された環境設定
- JARファイルのデコンパイル機能

## 必要条件

- Java Development Kit (JDK) 21
- Gradle 8.x以上（Wrapper同梱）

## セットアップ

1. このリポジトリをクローンまたはダウンロードします
2. 以下のコマンドでプロジェクトをビルドします：

```bash
make build
```

## 使用方法

このプロジェクトには、開発を容易にするための様々なMakeコマンドが用意されています：

### 基本コマンド

- `make build` - プロジェクトをビルドする
- `make run` - 開発環境でマインクラフトを実行する
- `make runClient` - クライアント環境でマインクラフトを実行する
- `make runServer` - サーバー環境でマインクラフトを実行する
- `make clean` - ビルド成果物を削除する

### デコンパイル関連コマンド

- `make decompileJar` - 特定のJARファイルをデコンパイルする
- `make decompileModJars` - Fabric API JARファイルをデコンパイルする
- `make decompileAllJars` - すべてのJARファイルをデコンパイルする

### その他のコマンド

- `make publish` - MavenリポジトリにMODを公開する
- `make tasks` - 利用可能なすべてのGradleタスクを表示する

## プロジェクト構成

このテンプレートは以下の構成になっています：

- `src/main` - MODのメインソースコード
- `src/client` - クライアント専用のソースコード
- `decompiled-output` - デコンパイルされたコードの出力先

## カスタマイズ

MODの基本情報は`gradle.properties`ファイルで設定できます。以下の項目を変更してください：

- `mod_version` - MODのバージョン
- `maven_group` - Mavenグループ ID
- `archives_base_name` - MODのアーカイブ名

## ライセンス

このプロジェクトには`LICENSE`ファイルが含まれています。MODの配布前に適切なライセンスを選択してください。

## 貢献

バグ報告や機能リクエストは、Issueトラッカーを通じてお願いします。プルリクエストも歓迎します。

## 参考リンク

- [Fabric Wiki](https://fabricmc.net/wiki/start)
- [Fabric GitHub](https://github.com/FabricMC)
- [Minecraft Developer Documentation](https://minecraft.fandom.com/wiki/Tutorials/Creating_Fabric_mods)
