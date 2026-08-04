# <img width="50" height="50" alt="logo" src="https://github.com/user-attachments/assets/c146908d-e222-49fe-aac6-66c1bb3b74be" /> SpendOlive (スペンドオリーブ) - 個人資産・支出管理サービス



> **チームプロジェクト (Team Project)** | 開発期間: 2026.06 ~ 2026.08  
> **担当:** フルスタック（アーキテクチャ共同設計、会員・決済コアシステム、管理者バックオフィス、セキュリティ）

---

## 1. プロジェクト概要 (Project Overview)
- **サービス紹介:** ユーザーの「固定支出」管理に焦点を当て、サブスクリプション（OTT等）のグループ共有から自動決済・精算までを安全にサポートするパーソナル支出管理サービスです。
- **開発背景:** サブスクリプション時代において、把握しづらくなった定期決済を効率的に管理するプラットフォームを構想しました。その中で、手動送金の煩わしさや金銭トラブルが最も多い「OTTの共有」に着目。単なるユーザーマッチングではなく、システムによる確実な決済と精算コア（エスクロー）を直接実装することで、誰もが安心できるスマートな支出管理の基盤を構築したいと考え企画しました。

---

## 2. 使用技術・環境 (Tech Stack)
| 分野 | 技術スタック |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot, Spring Security, Spring JDBC(JdbcTemplate), JWT, Lombok |
| **Frontend** | JavaScript, JSP, JSTL, CSS |
| **Database & Cache** | Oracle 11g, Redis |
| **Tools/DevOps** | Apache Tomcat, Maven, Git, GitHub |
| **External API** | Kakao (Login/Share), Toss Payments, 金融決済院, Solapi, SMTP |
---

## 3. 担当機能 (My Responsibilities)

- **アーキテクチャ設計およびテックリード協業**
  - テックリードと共にプロジェクト全体のアーキテクチャ設計および技術的な調整を担当
- **コアビジネス・決済システムの構築**
  - 金融決済院・Toss Payments APIを活用した決済および精算（Settlement）システムの実装
  - ユーザーの資産管理（Asset Management）システムの構築
- **管理者用バックオフィス機能の実装**
  - 会員管理、違反申告管理、および自動・手動での警告処理システムの実装
- **フロントエンド設計およびUI/UX最適化**
  - 非同期通信（AJAX/Fetch等）連携モジュールの設計・実装による、コードの再利用性と保守性の向上
  - 共通UIコンポーネント（ボタン等）のCSSモジュール化による、デザインの一貫性確保と開発効率の向上
  - 簡易モードの実装（ユーザーに合わせた文字サイズの調整、フォント設定機能）によるアクセシビリティ改善
- **セキュリティおよびデータ保護**
  - Spring SecurityとJWTを活用したアプリケーション全体のセキュリティ対策および権限管理

---

### 4. トラブルシューティング (Troubleshooting) - *★最重要*

> **主要な技術的課題解決 (Core Problem Solving)**
> ※ 決済・精算コアの信頼性確保に関する代表的な3つの課題を掲載しています。
> その他の詳細なエラー解決ログ（フロント連携、認証、DB最適化など）は 🔗 [トラブルシューティングWiki (Notion)] をご参照ください。

<br>

### Issue 1: インメモリロックとDB悲観的ロック(Atomic Query)を組み合わせた重複決済およびオーバーブッキングの防止

**1. 問題状況 (Problem)**
- 決済の過程で、2つの形態の同時実行（Concurrency）問題が発生するリスクが存在しました。
- **[単一ユーザー]** 決済のローディング中に更新（F5）を押したり、決済ボタンを連続でクリック（連打）した場合、同じPOSTリクエストが重複して送信され、DBに二重決済される問題。
- **[複数ユーザー]** 残り枠が1つの部屋に複数のユーザーが同時に決済を試みた場合、トランザクションの競合（Race Condition）により定員を超過してしまうオーバーブッキング（データの噛み合わせ）の問題。

**2. 原因分析 (Cause)**
- アプリケーション（Java）コード側で `SELECT` を用いて残り枠を確認し、`INSERT` を実行するまでの間に、他のトランザクションが割り込むことができる隙間（Gap）が存在したことが根本的な原因でした。

**3. 解決策の検討プロセス (Approach)**
- 複雑な分散ロック（Redissonなど）を導入するとシステムのオーバーヘッドが増加すると判断しました。
- そのため、**単一ユーザーの重複リクエストはアプリケーションレベル（インメモリ）**で素早く弾き返し、**複数ユーザーのオーバーブッキングはRedisのアトミック演算（decrement）**を活用して、ロックなしで高速かつ安全に同時実行を制御するハイブリッド方式を採用しました。

**4. 適用した解決策 (Solution)**
- **単一ユーザー制御 (Application Level Lock):** `ConcurrentHashMap` を活用して現在処理中の決済キーをインメモリで管理し、重複アクセスを遮断しました。
- **複数ユーザー制御 (Redis Atomic Counter):** Redisに部屋の残り枠（Seats）を保存し、決済リクエストが来るたびに `decrement()` を使用してアトミックに枠を減らします。結果が0未満になった場合は、即座に `increment()` でロールバックし例外を発生させることで、物理的に定員超過を防ぎました。
```java
// 1. 単一ユーザーの重複決済(連打)遮断 (Java インメモリロック)
String processingKey = createProcessingKey(userId, roomId);
if (!processingPayments.add(processingKey)) {
    throw new PaymentProcessException("PAYMENT_PROCESSING", "すでに決済を処理中です。");
}

try {
    // 2. 複数ユーザーのオーバーブッキング完全遮断 (Redis Atomic Counter 活用)
    String redisKey = "room:" + roomId + ":seats";

    // 初回アクセス時、残り枠を初期化（TTL 1時間）
    if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
        redisKey, String.valueOf(Math.max(limit - currentMembers, 0))))) {
            redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
    }

    // アトミック演算(decrement)による残り枠のマイナス処理
    Long remainingSeats = redisTemplate.opsForValue().decrement(redisKey);
    if (remainingSeats == null || remainingSeats < 0) {
        // 枠がないため、マイナスした1を元に戻して(increment)例外処理
        redisTemplate.opsForValue().increment(redisKey);
        throw new PaymentProcessException("ROOM_FULL", "すでに定員に達した部屋です。");
    }

    try {
        // 決済および部屋入室ロジックの実行 (省略)
    } catch (Exception e) {
        // 決済失敗時、減らした枠を元に戻す(ロールバック)
        redisTemplate.opsForValue().increment(redisKey);
        throw e;
    }
} finally {
    // 3. 全ての処理終了後、インメモリロックを解除
    processingPayments.remove(processingKey);
}
-- 2. 複数ユーザーのオーバーブッキング完全遮断 (DB 条件付き INSERT クエリ)
INSERT INTO ott_room_member_tb (room_id, member_login_id, status, ...)
SELECT ?, ?, 'ACTIVE', ...
FROM ott_room_tb
WHERE room_id = ?
  AND (SELECT COUNT(*) 
       FROM ott_room_member_tb 
       WHERE room_id = ? AND status = 'ACTIVE') < member_limit;
```
**5. 成果および学び (Result)**
- 単一ユーザーの重複クリックおよび複数ユーザーのオーバーブッキング問題を100%遮断し、決済データの無欠性を確保しました。
- 重い排他ロックをかけることなく、Redisのシングルスレッド特性（アトミック演算）を正確に理解して適用することで、高いパフォーマンスとデータ整合性の両方を満たすアーキテクチャを設計しました。

<br>

### Issue 2: 外部APIと内部DB間のトランザクション不一致の解決および補償トランザクションの実装

**1. 問題状況 (Problem)**
- Toss Payments（外部決済API）の承認には成功し、顧客の口座から出金されたにもかかわらず、その後自社サーバーのDBに決済履歴やエスクロー情報を保存（`INSERT`/`UPDATE`）する過程で例外が発生するリスクが存在しました。
- この場合、顧客はお金を支払ったのにサービス内では「未決済」状態のままになるという、**致命的なデータ不一致（Data Inconsistency）**が発生してしまいます。

**2. 原因分析 (Cause)**
- 外部API呼び出し（Network I/O）と内部DBのトランザクションは本質的に分離されています。
- Springの `@Transactional` を適用してもDBのロールバックが実行されるだけで、すでに完了した外部API（Toss）の決済承認は自動的に取り消されないという、**分散トランザクションの限界**が根本的な原因でした。

**3. 解決策の検討プロセス (Approach)**
- 完璧な分散トランザクション制御のために 2PC（Two-Phase Commit）方式を検討しましたが、外部決済サービスと密結合できない構造的な限界がありました。
- そのため、アプリケーションレベルで**補償トランザクション（Compensating Transaction）**パターンを直接実装し、DB保存に失敗した場合は能動的に外部決済を取り消す「Sagaパターン」の基本概念を適用することに決定しました。

**4. 適用した解決策 (Solution)**
- DBへの `INSERT`/`UPDATE` ロジックを `try-catch` ブロックで囲み、`catch` 発生時には即座にToss決済取消API（`cancelApprovedPayment`）を呼び出すように設計しました。
- 取消の成功・失敗に応じて明確な例外メッセージをスローし、フロントエンドおよびユーザーに正確な状況を認識させるように処理しました。
```java
try {
    // 1. 内部DBトランザクションの実行（決済状態の更新、エスクロー情報の保存など）
    paymentRepository.updatePaymentStatus(paymentInfo);
    paymentRepository.insertEscrow(escrowInfo);
    // ... その他の関連データ保存処理
    
} catch (Exception databaseException) {
    // 2. DB保存失敗時：すでに承認されたToss決済を即座に取り消し（補償トランザクションの実行）
    boolean cancelled = cancelApprovedPayment(paymentKey);

    String message = cancelled
            ? "決済情報の保存に失敗したため、Toss承認を自動で取り消しました。"
            : "決済情報の保存およびToss承認の取消に失敗しました。管理者の確認が必要です。";

    throw new PaymentProcessException(
            "PAYMENT_SAVE_FAILED", 
            message, 
            databaseException);
}
```
**5. 成果および学び (Result)**
- 決済システムにおける最も致命的な問題である「顧客の金銭的被害（ファントム決済）」を根本から防ぎ、決済データの整合性を100%保証しました。
- 外部サービスと内部システム間のエラー伝播（Error Propagation）の過程を理解し、安全なフェイルセーフ（Fail-safe）メカニズムを自ら設計するアーキテクチャ設計のスキルを身につけました。

<br>

### Issue 3: 決済・精算コアのライフサイクル(State Machine)設計および返金処理のデータ整合性確保

**1. 問題状況 (Problem)**
- OTT相乗りサービスの自動決済・精算をスケジューラー（バッチ処理）で運用する中で、2つの重大なビジネスロジックの欠陥（Edge Case）が発見されました。
- **[過剰請求リスク]** 「OTT開始日の10日前」を自動決済日としたため、部屋が満室になりサービスが開始される「前」に自動決済日が到来し、最初の月にユーザーへ**二重決済（Double Billing）**が発生する危険性がありました。
- **[返金による精算プールの汚染]** ユーザーが途中で抜けた場合、プラットフォームの損失に直結するため、単純な返金処理を行うと、エスクロー（保管金）やプラットフォーム収益データと不整合が起き、ホストへ誤った金額が送金されるリスクがありました。

**2. 原因分析 (Cause)**
- 決済と精算のフローが「日付（Date）」ベースの単純なスケジューラーに依存しており、各データ行の「状態（Status）」による厳格な制御（State Machine）が欠如していたことが根本原因でした。

**3. 解決策の検討プロセス (Approach)**
- スケジューラー内に複雑な `if-else` の日付判定ロジックを入れることは、技術的負債になると判断しました。
- 代わりに、**状態遷移モデル（State Machine）**を導入して決済・精算のライフサイクルを明確に定義し、返金処理はシステム自動化ではなく「管理者承認ベースのトランザクション」として分離する安全な設計を選択しました。

**4. 適用した解決策 (Solution)**
- **[初回過剰請求の防止 - `FIRST`状態の導入]** 満室になった部屋のステータスを一時的に `FIRST` に設定。スケジューラーが自動決済対象を抽出する際、`FIRST` 状態の部屋はスキップさせることで、初月の二重決済を完全に遮断。その後、安全なタイミングで `ACTIVE` へ遷移させました。
- **[精算ライフサイクルの確立とカスケード返金処理]** 精算状態を `YET(待機)` -> `READY(送金準備)` -> `DONE(完了)` と厳格に遷移させました。返金は管理者が状況を判断した上で実行し、Tossの決済取消APIを呼び出すと同時に、決済履歴（`payment_tb`）、エスクロー保管金（`escrow_payout_tb`）、プラットフォーム収益（`revenue_tb`）の**全ての関連ステータスを同一トランザクション内で `REFUNDED` に一括更新**し、精算プールから完全に隔離しました。
// 管理者による返金トランザクション（状態の完全隔離）
```java
@Transactional(rollbackFor = Exception.class)
public void executeRoomRefund(SettlementPaymentVO payment) throws Exception {
    // 1. 外部決済API (Toss) の承認取消を実行
    if(!cancelApprovedPayment(payment.getPaymentKey())) {
        throw new PaymentProcessException("Toss決済の取消に失敗しました。");
    }
    
    // 2. 内部DBのカスケード状態更新 (REFUNDED に変更して精算対象から除外)
    paymentRepository.updatePaymentstatusRefund(payment.getPayment_id()); // 決済状態更新
    
    SettlementRefundVO refund = new SettlementRefundVO();
    refund.setPayment_id(payment.getPayment_id());
    refund.setRefund_status("COMPLETED");
    // ... (その他の返金メタデータセット)
    
    paymentRepository.insertRefund(refund); // 返金履歴の分離保存
}
```
**5. 成果および学び (Result)**
- `FIRST` 状態の導入により、複雑な日付計算なしで過剰請求（二重決済）バグを100%解決しました。
- エスクロー基盤の複雑な資金移動において、返金や途中退出などのエッジケースが発生してもデータが矛盾しない、堅牢なコアー精算システム（State Machine）を設計するドメインモデリングの能力を身につけました。

---

## 5. 関連リンク (Links)
- **GitHub Repository:** [Link](https://github.com/LeeChungMoo965/group-spendolive/tree/master/src/main)
- **画面設計 Figma :**[Link](https://www.figma.com/design/jXBp0uN1p2c65oKGgrZjmq/%EB%B0%B1%EC%97%94%EB%93%9C-%EC%B5%9C%EC%A2%85-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-UI?node-id=0-1&t=va79lufa0lhW4jKj-1)
## 6. 環境構築 (Getting Started)

### 必須要件 (Prerequisites)
- Java 21
- Spring Boot (WAR Packaging)
- Apache Maven 3.8.x 以上 (または IDE 内蔵 Maven)
- Oracle Database 11g
- Redis

### 環境変数の設定 (Environment Variables)
セキュリティ保護のため、DBパスワードおよび各種外部APIキーが含まれる `application.properties` はリポジトリから除外されています。
ローカルで実行する際は、`src/main/resources/` ディレクトリ配下に `application.properties` を作成し、リポジトリ内の `application-template.properties` を参考に以下の外部APIキーを設定してください。

**[連携が必要な外部API]**
- Google SMTP (メール認証用)
- Kakao Developers (ソーシャルログイン用)
- 金融決済院 OpenBanking API (口座連携用)
- Toss Payments API (決済処理用)
- Solapi (SMS送信処理用)

### 実行手順 (How to Run)
1. 本リポジトリをクローンします。
   `git clone https://github.com/LeeChungMoo965/group-spendolive.git`
2. IDE（IntelliJ IDEA, Eclipse等）を開き、Mavenプロジェクトとしてインポートします。
3. ローカル環境の Oracle DB および Redis サーバーを起動します。
4. プロジェクト内に含まれる DDL スクリプトを実行し、データベースのテーブルを生成します。
5. `application.properties` の設定（DB接続情報および各種APIキー）を完了させます。
6. `SpendoliveApplication.java` を実行します。（※ 本プロジェクトはJSPを使用しているため、WARパッケージングで動作します）


## ERD (Entity Relationship Diagram)
<img width="500" height="240" alt="Relational_1" src="https://github.com/user-attachments/assets/a03ccd1a-473a-465a-992c-94973c2b2ebc" />


