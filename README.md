# FFA PvP Plugin for Minecraft 1.8.9

Minecraft 1.8.9用のFree For All（FFA）PvPプラグインです。

## 機能

### 基本機能
- ✅ アリーナ管理システム
- ✅ 参加/退出システム
- ✅ 複数のスポーン地点設定
- ✅ カスタマイズ可能なキット配布
- ✅ 自動リスポーン（遅延設定可能）
- ✅ リアルタイムスコアボード表示

### キルストリークシステム
- ✅ 3, 5, 10連続キルで報酬獲得
- ✅ カスタマイズ可能な報酬設定
- ✅ 全体アナウンス機能
- ✅ 最高連続キル記録保存

### ランキングシステム
- ✅ キル数ランキング
- ✅ K/D比表示
- ✅ 最高連続キル記録
- ✅ SQLiteまたはMySQLでデータ保存

## インストール方法

### 必要なもの
- Java 8以上
- Maven
- Spigot/Paper 1.8.8サーバー

### ビルド手順

1. プロジェクトをダウンロード
```bash
cd FFAPlugin
```

2. Mavenでビルド
```bash
mvn clean package
```

3. 生成されたJARファイルをサーバーの`plugins`フォルダにコピー
```bash
cp target/FFAPlugin-1.0.0.jar /path/to/server/plugins/
```

4. サーバーを起動してプラグインを有効化

## 初期設定

### 1. スポーン地点の設定
```
/ffa setspawn
```
複数のスポーン地点を設定できます。各地点でコマンドを実行してください。

### 2. ロビー地点の設定
```
/ffa setlobby
```

### 3. アリーナを有効化
```
/ffa enable
```

## コマンド

### プレイヤーコマンド
- `/ffa join` - アリーナに参加
- `/ffa leave` - アリーナから退出
- `/ffa stats [プレイヤー]` - 統計を表示
- `/ffa top` - トップ10ランキングを表示

### 管理者コマンド
- `/ffa setspawn` - スポーン地点を設定
- `/ffa setlobby` - ロビー地点を設定
- `/ffa enable` - アリーナを有効化
- `/ffa disable` - アリーナを無効化
- `/ffaadmin reload` - コンフィグをリロード
- `/ffaadmin setkillstreak <プレイヤー> <キル数>` - キルストリークを設定

## 権限

- `ffa.use` - 基本コマンドの使用（デフォルト: true）
- `ffa.admin` - 管理者コマンドの使用（デフォルト: op）

## 設定ファイル (config.yml)

### アリーナ設定
```yaml
arena:
  enabled: false  # アリーナの有効/無効
  world: world    # ワールド名
  spawns: []      # スポーン地点（/ffa setspawnで追加）
  lobby: {}       # ロビー地点（/ffa setlobbyで設定）
```

### キット設定
```yaml
kit:
  helmet: DIAMOND_HELMET
  chestplate: DIAMOND_CHESTPLATE
  leggings: DIAMOND_LEGGINGS
  boots: DIAMOND_BOOTS
  items:
    - type: DIAMOND_SWORD
      slot: 0
      enchantments:
        - DAMAGE_ALL:2
```

### キルストリーク報酬
```yaml
killstreaks:
  enabled: true
  rewards:
    3:
      message: "&e3キル達成！金リンゴ+2を獲得！"
      items:
        - type: GOLDEN_APPLE
          amount: 2
    5:
      message: "&65キル達成！ダイヤモンド+3を獲得！"
      items:
        - type: DIAMOND
          amount: 3
```

### データベース設定
```yaml
database:
  type: sqlite  # sqlite または mysql
  mysql:
    host: localhost
    port: 3306
    database: minecraft
    username: root
    password: password
```

## カスタマイズ

### キットのカスタマイズ
`config.yml`の`kit`セクションで武器や防具を変更できます。
エンチャントも自由に追加可能です。

### キルストリーク報酬のカスタマイズ
`killstreaks.rewards`セクションで、任意のキル数に報酬を設定できます。

### メッセージのカスタマイズ
`messages`セクションで全てのメッセージをカスタマイズできます。
カラーコードは`&`を使用します（例: `&a` = 緑）

## トラブルシューティング

### プレイヤーがアリーナに参加できない
1. `/ffa enable`でアリーナが有効化されているか確認
2. `/ffa setspawn`でスポーン地点が設定されているか確認

### スコアボードが表示されない
`config.yml`で`scoreboard.enabled: true`になっているか確認

### データが保存されない
データベース接続を確認。MySQLを使用する場合は接続情報が正しいか確認

## 開発者向け情報

### プロジェクト構造
```
FFAPlugin/
├── src/main/java/com/yourname/ffa/
│   ├── FFAPlugin.java              # メインクラス
│   ├── commands/
│   │   ├── FFACommand.java         # プレイヤーコマンド
│   │   └── FFAAdminCommand.java    # 管理者コマンド
│   ├── data/
│   │   ├── PlayerStats.java        # プレイヤー統計データ
│   │   └── DataManager.java        # データベース管理
│   ├── listeners/
│   │   └── PlayerListener.java     # イベントリスナー
│   └── managers/
│       ├── ArenaManager.java       # アリーナ管理
│       ├── KillstreakManager.java  # キルストリーク管理
│       └── ScoreboardManager.java  # スコアボード管理
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

### 拡張方法
新しい機能を追加する場合：
1. 適切なマネージャークラスを作成
2. `FFAPlugin.java`で初期化
3. 必要に応じてイベントリスナーを追加
4. コマンドを追加

## ライセンス

このプラグインは自由に使用・改変できます。

## サポート

問題が発生した場合や機能のリクエストがある場合は、GitHubのIssuesで報告してください。

## バージョン履歴

### v1.0.0
- 初回リリース
- 基本的なFFA機能
- キルストリークシステム
- ランキングシステム
