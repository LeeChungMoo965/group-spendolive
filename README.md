# <img width="50" height="50" alt="logo" src="https://github.com/user-attachments/assets/c146908d-e222-49fe-aac6-66c1bb3b74be" /> SpendOlive (スペンドオリーブ) - 個人資産・支出管理サービス



> **チームプロジェクト (Team Project)** | 開発期間: 2026.06 ~ 2026.08  
> **担当:** フルスタック（アーキテクチャ共同設計、会員・決済コアシステム、管理者バックオフィス、セキュリティ）

---

## 📌 1. プロジェクト概要 (Project Overview)
- **サービス紹介:** (서비스 한 줄 소개: 예 - ユーザーの支出を視覚化し、資産管理をサポートするWebサービス)
- **開発背景:** (개발 배경 1~2줄)

---

## 🛠 2. 使用技術・環境 (Tech Stack)
| 分野 | 技術スタック |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot, Spring Security, Spring JDBC(JdbcTemplate), JWT, Lombok |
| **Frontend** | JavaScript, JSP, JSTL, CSS |
| **Database & Cache** | Oracle 11g, Redis |
| **Tools/DevOps** | Apache Tomcat, Maven, Git, GitHub |
| **External API** | Kakao (Login/Share), Toss Payments, 金融決済院, Solapi, SMTP |
---

## 👨‍💻 3. 担当機能 (My Responsibilities)

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

## 🚨 4. トラブルシューティング (Troubleshooting) - *★最重要*

### 💥 Issue 1: 外部APIと内部DB間のトランザクション不一致の解決および補償トランザクションの実装

- **1. 問題状況 (Problem)**
  - Toss Payments（外部決済API）の承認には成功し、顧客の口座から出金されたにもかかわらず、その後自社サーバーのDBに決済履歴やエスクロー情報を保存（`INSERT`/`UPDATE`）する過程で例外が発生するリスクが存在しました。
  - この場合、顧客はお金を支払ったのにサービス内では「未決済」状態のままになるという、**致命的なデータ不一致（Data Inconsistency）**が発生してしまいます。

- **2. 原因分析 (Cause)**
  - 外部API呼び出し（Network I/O）と内部DBのトランザクションは本質的に分離されています。
  - Springの `@Transactional` を適用してもDBのロールバックが実行されるだけで、すでに完了した外部API（Toss）の決済承認は自動的に取り消されないという、**分散トランザクションの限界**が根本的な原因でした。

- **3. 解決策の検討プロセス (Approach)**
  - 完璧な分散トランザクション制御のために 2PC（Two-Phase Commit）方式を検討しましたが、外部決済サービスと密結合できない構造的な限界がありました。
  - そのため、アプリケーションレベルで**補償トランザクション（Compensating Transaction）**パターンを直接実装し、DB保存に失敗した場合は能動的に外部決済を取り消す「Sagaパターン」の基本概念を適用することに決定しました。

- **4. 適用した解決策 (Solution)**
  - DBへの `INSERT`/`UPDATE` ロジックを `try-catch` ブロックで囲み、`catch` 発生時には即座にToss決済取消API（`cancelApprovedPayment`）を呼び出すように設計しました。
  - 取消の成功・失敗に応じて明確な例外メッセージをスローし、フロントエンドおよびユーザーに正確な状況を認識させるように処理しました。

  ```java
  try {
      // 1. 内部DBトランザクションの実行（決済状態の更新、エスクロー情報の保存など）
      paymentRepository.updatePaymentStatus(paymentInfo);
      paymentRepository.insertEscrow(escrowInfo);
      paymentRepository.insertPlatfoem_Revenue(revenueInfo);
      paymentRepository.updatSettlementroommemberStatus(roomId, userId);
      
  } catch (Exception databaseException) {
      // 🚨 2. DB保存失敗時：すでに承認されたToss決済を即座に取り消し（補償トランザクションの実行）
      boolean cancelled = cancelApprovedPayment(paymentKey);

      String message = cancelled
              ? "決済情報の保存に失敗したため、Toss承認を自動で取り消しました。"
              : "決済情報の保存およびToss承認の取消に失敗しました。管理者の確認が必要です。";

      throw new PaymentProcessException(
              "PAYMENT_SAVE_FAILED", 
              message, 
              databaseException);
  }

---

## 🔗 5. 関連リンク (Links)
- **GitHub Repository:** [Link](https://github.com/...)
- **個人ポートフォリオ:** [Link](https://...)
