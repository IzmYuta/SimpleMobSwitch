# Minecraft Mod開発用Makefile

# デフォルトのターゲット
.PHONY: help
help:
	@echo "使用可能なコマンド:"
	@echo "  make build          - プロジェクトをビルドする"
	@echo "  make run            - 開発環境でマインクラフトを実行する"
	@echo "  make runClient      - クライアント環境でマインクラフトを実行する"
	@echo "  make runServer      - サーバー環境でマインクラフトを実行する"
	@echo "  make clean          - ビルド成果物を削除する"
	@echo "  make decompileJar   - 特定のJARファイルをデコンパイルする"
	@echo "  make decompileModJars - Fabric API JARファイルをデコンパイルする"
	@echo "  make decompileAllJars - すべてのJARファイルをデコンパイルする"
	@echo "  make publish        - MavenリポジトリにMODを公開する"
	@echo "  make tasks          - 利用可能なすべてのGradleタスクを表示する"

# ビルドコマンド
.PHONY: build
build:
	./gradlew build

# 実行コマンド
.PHONY: run
run:
	./gradlew run

.PHONY: runClient
runClient:
	./gradlew runClient

.PHONY: runServer
runServer:
	./gradlew runServer

# クリーンアップコマンド
.PHONY: clean
clean:
	./gradlew clean

# デコンパイルコマンド
.PHONY: decompileJar
decompileJar:
	./gradlew decompileJar

.PHONY: decompileModJars
decompileModJars:
	./gradlew decompileModJars

.PHONY: decompileAllJars
decompileAllJars:
	./gradlew decompileAllJars

# 公開コマンド
.PHONY: publish
publish:
	./gradlew publish

# タスク一覧表示
.PHONY: tasks
tasks:
	./gradlew tasks
