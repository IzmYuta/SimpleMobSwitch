# マインクラフトFabric Modテンプレートのコード構造

## プロジェクト構造の概要

このリポジトリはマインクラフトのFabric Mod開発用テンプレートです。ソースコードは主に`src`ディレクトリに配置されており、クライアント側とサーバー/共通側のコードが分離されています。

## 主要なディレクトリ構造

```
/
├── src/                           # ソースコード
│   ├── main/                      # メインコード（サーバー/共通）
│   │   ├── java/                  # Javaソースコード
│   │   │   └── com/example/       # パッケージ構造
│   │   │       ├── Template.java  # メインModクラス
│   │   │       └── mixin/         # Mixinクラス
│   │   └── resources/             # リソースファイル
│   │       ├── assets/template/   # テクスチャなどのアセット
│   │       ├── fabric.mod.json    # Mod設定ファイル
│   │       └── template.mixins.json # Mixin設定
│   └── client/                    # クライアント専用コード
│       ├── java/                  # クライアント側Javaコード
│       │   └── com/example/       
│       │       ├── TemplateClient.java # クライアント側エントリポイント
│       │       └── mixin/client/  # クライアント側Mixin
│       └── resources/             # クライアント側リソース
│           └── template.client.mixins.json # クライアント側Mixin設定
├── gradle/                        # Gradleラッパー設定
├── build.gradle                   # ビルド設定
├── gradle.properties              # Gradleプロパティ
└── settings.gradle                # Gradleプロジェクト設定
```

## 重要な設定ファイル

### 1. `fabric.mod.json`

**最も重要な設定ファイル**で、このファイルには以下の情報が含まれています：

- Modの基本情報（ID、名前、バージョン）
- 作者情報
- 依存関係
- エントリーポイント（Modの実行開始点）
- 必要なMinecraft/Fabricのバージョン
- Mixinの設定ファイルへの参照

### 2. `template.mixins.json` と `template.client.mixins.json`

Mixinの設定ファイルで、バニラのマインクラフトコードに介入するためのクラスを定義します。サーバー/共通側とクライアント側で分離されています。

### 3. `gradle.properties`

Fabricの開発に必要なバージョン情報やプロジェクト設定が含まれています：

- マインクラフトのバージョン
- Yarn（マッピング）のバージョン
- Loaderのバージョン
- Fabric APIのバージョン
- Modのバージョン

### 4. `build.gradle`

Modのビルド設定、依存関係、Mavenリポジトリなどが定義されています。

## 主要なJavaファイル

### 1. `Template.java`

メインModクラスで、サーバー側および共通の初期化コードが含まれています。通常、`onInitialize`メソッドでアイテムやブロックの登録を行います。

### 2. `TemplateClient.java`

クライアント側のエントリーポイントで、クライアント専用の初期化コードが含まれています。レンダラーの登録などを行います。

### 3. Mixinクラス

`ExampleMixin.java`と`ExampleClientMixin.java`は、バニラのマインクラフトコードに介入するためのクラスです。これらを使用して既存のゲーム機能を変更できます。

## 開発時の注意点

- `fabric.mod.json`の設定は非常に重要で、エントリーポイントやModのIDが正しく設定されていないとModが動作しません
- クライアント側とサーバー/共通側のコードは明確に分離されています
- Mixinを使用する場合は、対応するMixin設定ファイルに正しく登録する必要があります
- アセットは`assets/[mod_id]/`ディレクトリに配置します
