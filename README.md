# 💳 SpendOlive (スペンドオリーブ) - 個人資産・支出管理サービス

> **チームプロジェクト (Team Project)** | 開発期間: 202X.XX ~ 202X.XX  
> **担当:** バックエンド / フロントエンド（管理機能・通報処理）

---

## 📌 1. プロジェクト概要 (Project Overview)
- **サービス紹介:** (서비스 한 줄 소개: 예 - ユーザーの支出を視覚化し、資産管理をサポートするWebサービス)
- **開発背景:** (개발 배경 1~2줄)

---

## 🛠 2. 使用技術・環境 (Tech Stack)
| 分野 | 技術スタック |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot, MyBatis |
| **Frontend** | JavaScript (ES6+), JSP, CSS3 |
| **Database** | Oracle / MySQL |
| **Tools/DevOps** | Git, GitHub, Eclipse / IntelliJ |

---

## 👨‍💻 3. 担当機能 (My Responsibilities)
*(네가 직접 개발한 핵심 기능들)*
- **管理者 違反申告・警告処理システム開発**
  - 非同期通信（AJAX/Fetch API）を活用したモーダルUIおよびステータス更新機能の実装
  - 入力値バリデーションおよび例外処理（Exception Handling）の統一

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
