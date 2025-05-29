# SimpleMobSwitch Mod 技術仕様詳細

## 概要

SimpleMobSwitchは、Minecraft 1.20.x Fabric環境で動作するモブスポーン制御Modです。プレイヤー数に応じて動的にダミーモブ（シュルカー）をスポーンさせ、モブキャップメカニズムを利用してサーバー全体のモブスポーンを制御します。

## 主要機能と実装詳細

### 1. 動的モブキャップ制御

- **基本計算式**: `プレイヤー数 × 70体 = 必要ダミーモブ数`
- **配置位置**: ワールドスポーン地点から3チャンク離れた位置（Y=1000）
- **エンティティタイプ**: シュルカー（`EntityType.SHULKER`）

### 2. ダミーモブの特性設定

```java
// 各ダミーモブに適用される設定
shulker.setCustomName(Text.literal("MobSwitchDummy"));
shulker.setCustomNameVisible(false);  // 名前非表示
shulker.setAiDisabled(true);          // AI無効化
shulker.setSilent(true);              // 音無効化
shulker.setInvulnerable(true);        // 無敵化
shulker.setNoGravity(true);           // 重力無効化
shulker.setInvisible(true);           // 透明化
```

### 3. プレイヤー数連動システム

- **参加時**: プレイヤー参加時に必要モブ数を再計算し、不足分を自動スポーン
- **退出時**: プレイヤー退出時に過剰分を自動削除
- **リアルタイム調整**: `adjustDummyMobCount()`メソッドによる動的調整

### 4. 永続化システム

**MobSwitchState.java**による状態管理：

- **保存データ**:
  - `isActive`: モブスイッチの有効/無効状態
  - `dummyMobUUIDs`: スポーンしたダミーモブのUUIDリスト
- **保存方式**: Minecraft標準の`PersistentState`システム使用
- **データ形式**: NBTCompound形式でワールドデータに保存

### 5. サーバーライフサイクル管理

- **起動時**: 保存されたUUIDでダミーモブを再スポーン
- **終了時**: ダミーモブを削除（UUIDは保持）
- **自動復旧**: サーバー再起動後の状態復元

## コマンドシステム

### 権限要件

- **必要権限**: OP権限（権限レベル2以上）
- **実装**: `source.hasPermissionLevel(2)`による制御

### コマンド構文

```bash
/mobswitch on   # モブスイッチ有効化
/mobswitch off  # モブスイッチ無効化
```

## パフォーマンス最適化

### 1. 効率的な配置戦略

- **配置場所**: スポーンチャンクから3チャンク離れた単一地点
- **Y座標**: 1000（地上から離れた位置）
- **集約配置**: 全ダミーモブを同一座標に配置

### 2. AI処理最適化

- **AI無効化**: `setAiDisabled(true)`によるAI処理停止
- **物理演算無効化**: `setNoGravity(true)`による重力計算停止
- **音響処理無効化**: `setSilent(true)`による音響処理停止

### 3. メモリ効率化

- **UUID管理**: ダミーモブのUUIDのみを保持
- **遅延実行**: プレイヤー退出時の調整を`server.execute()`で遅延実行

## エラーハンドリング

### 1. スポーン失敗対応

```java
if (overworld.spawnEntity(shulker)) {
    state.addDummyMob(shulker.getUuid());
    successCount++;
}
```

### 2. 状態整合性チェック

- エンティティタイプ確認
- カスタム名確認
- 生存状態確認

### 3. ログ出力

- 詳細な動作ログ（INFO、ERROR レベル）
- プレイヤー数とモブ数の追跡
- 成功/失敗カウントの記録

## 技術的制約と注意点

### 1. ディメンション制限

- **対象**: オーバーワールドのみ
- **理由**: スポーンチャンクの概念がオーバーワールド固有

### 2. モブキャップの仕様

- **影響範囲**: サーバー全体のモブスポーン
- **例外**: プレイヤーが直接スポーンさせたモブ（スポーンエッグ等）

### 3. パフォーマンス影響

- **エンティティ数**: プレイヤー数 × 70体の常駐エンティティ
- **メモリ使用量**: 最小限（AI無効化により）
- **CPU負荷**: 軽微（物理演算無効化により）

## Mixin使用

**SimpleMobSwitchMixin.java**:

- MinecraftServerクラスへの軽微な介入
- ワールドロード時の初期化ログ出力のみ
- 非侵襲的な実装

この実装により、SimpleMobSwitchは効率的かつ安全にモブスポーン制御を実現し、サーバーパフォーマンスへの影響を最小限に抑えながら、プレイヤー数に応じた動的な制御を提供します。
