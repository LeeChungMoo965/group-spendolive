SpendOlive SQL 분리 실행 안내
==============================

현재 실행 기준
- 이 폴더의 번호별 SQL이 최신 기준입니다.
- 상위 폴더의 spendolive_schema_final*.sql은 과거 통합본이므로 새 DB 구성에 사용하지 않습니다.

파일 구성
1. 00_reset_all_objects.sql
   - 기존 테이블/시퀀스를 모두 삭제하는 초기화 파일입니다.
   - 기존 데이터가 필요하면 실행하지 마세요.

2. 01_member_schema.sql
   - 회원가입/로그인에 필요한 member_tb, seq_member, trg_member_bi입니다.
   - 거의 모든 기능의 기준 테이블이므로 가장 먼저 실행합니다.

3. 02_expense_calendar_schema.sql
   - 지출 카테고리, 지출 내역 테이블입니다.
   - 지출관리/캘린더 화면에 필요합니다.

4. 03_ott_schema.sql
   - OTT 서비스, 공유방, 참여자, 채팅, 월별 정산 기본 테이블입니다.
   - 01번 실행 후 실행합니다.
   - 실제 OTT 신청/승인/정산/방삭제 요청 흐름은 알림을 생성하므로 04번도 함께 실행하는 것을 권장합니다.
   - 결제/환불 테이블은 다음 03-1_payment.sql에서 생성하므로 반드시 이어서 실행하세요.

5. 03-1_payment.sql
   - settlement_payment_tb와 settlement_refund_tb를 현재 Java 결제 코드 컬럼으로 다시 구성합니다.
   - escrow_payout_tb, platform_revenue_tb, seller_account_tb도 생성합니다.
   - 반드시 03_ott_schema.sql 다음에 실행하세요.

6. 04_notice_notification_inquiry_faq.sql
   - 공지사항, FAQ, 문의, 문의답변, 알림, 공지 즐겨찾기 테이블입니다.
   - OTT 기능에서 notification_tb를 사용하므로 OTT 페이지를 테스트할 때도 실행하는 것이 좋습니다.

7. 05_admin_report_warning_schema.sql
   - 신고/경고 테이블입니다.
   - report_tb가 ott_room_tb를 참조하므로 03번 실행 후 실행하세요.

8. 06_seed_member_sample.sql
   - 로그인 테스트용 회원 데이터입니다.

9. 07_seed_expense_categories_and_samples.sql
   - 지출 카테고리와 테스트 지출 데이터입니다.

10. 08_seed_ott_services.sql
   - OTT 서비스 기본 데이터입니다.
   - OTT 페이지에서 서비스 목록을 확인하려면 실행하는 것이 좋습니다.

11. 09_seed_alert_sample.sql
    - 테스트 알림 데이터입니다.

12. 10_seed_all_default_data.sql
    - 기본 데이터를 한 번에 넣는 파일입니다.
    - 06~09번을 개별 실행하지 않고 전체 기본 데이터를 넣고 싶을 때 사용하세요.

13. 11_reference_queries.sql
    - 참고용 조회 SQL입니다. 필수 실행 파일이 아닙니다.

추천 실행 순서
-------------

전체 새로 만들기:
00_reset_all_objects.sql
01_member_schema.sql
02_expense_calendar_schema.sql
03_ott_schema.sql
03-1_payment.sql
04_notice_notification_inquiry_faq.sql
05_admin_report_warning_schema.sql
10_seed_all_default_data.sql

OTT 페이지만 먼저 확인:
00_reset_all_objects.sql  (기존 데이터 삭제 필요할 때만)
01_member_schema.sql
03_ott_schema.sql
03-1_payment.sql
04_notice_notification_inquiry_faq.sql
06_seed_member_sample.sql
08_seed_ott_services.sql

지출/캘린더만 확인:
00_reset_all_objects.sql  (기존 데이터 삭제 필요할 때만)
01_member_schema.sql
02_expense_calendar_schema.sql
06_seed_member_sample.sql
07_seed_expense_categories_and_samples.sql

주의
----
- 00번은 삭제용 파일입니다. 기존 데이터를 살릴 때는 실행하지 마세요.
- 10번 전체 기본 데이터를 실행했다면 06~09번을 중복 실행하지 마세요.
- 각 SQL 파일 상단에 실행 순서와 필요한 선행 파일을 적어두었습니다.
