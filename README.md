# 💳 SpendOlive (スペンドオリーブ) - 個人資産・支出管理サービス
<img width="168" height="168" alt="logo" src="https://github.com/user-attachments/assets/c146908d-e222-49fe-aac6-66c1bb3b74be" />


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

### 💥 Issue 1: 非同期パラメータの型不一致による `NumberFormatException` の発生
- **問題 (Problem):** 管理者モーダルで申告処理を実行する際、IDの型不一致によりバックエンドで `NumberFormatException` が発生し処理が失敗。
- **原因 (Cause):** フロントエンドの `data-*` 属性から値を取得する際、`report_id` と `reported_member_id`（文字列ID）のデータが逆に設定され、数値型 (`int`) のパラメータに文字列が送信されていた。
- **解決策 (Solution):** 
  - フロントエンドの `dataset` バインディング順序を修正。
  - バックエンドでの例外発生時に、明確なエラーメッセージを返す `@ExceptionHandler` レスポンス構造の設計。
- **成果・学び (Result):** フロント-バック間のデータフローを明確化し、パラメータ検証の重要性を再確認。

---

## 🔗 5. 関連リンク (Links)
- **GitHub Repository:** [Link](https://github.com/...)
- **個人ポートフォリオ:** [Link](https://...)
