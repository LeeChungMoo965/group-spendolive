SpendOlive CSS 1차 정리본

적용 방법
1. 압축을 프로젝트 루트에서 풀어주세요.
2. 기존 src/main/webapp/resources/css/styles.css 파일이 목차 파일로 바뀝니다.
3. 실제 CSS는 src/main/webapp/resources/css/styles/ 폴더로 분리됩니다.
4. 적용 후 브라우저에서 Ctrl + F5로 강력 새로고침하세요.

정리 방향
- 기존 기능 CSS는 삭제하지 않고 보존했습니다.
- styles.css를 짧은 목차 파일로 만들었습니다.
- 페이지/기능별 CSS를 styles 폴더로 분리했습니다.
- 버튼, 카드, 탭, 폼은 99-ui-unified-overrides.css에서 한 번 더 통일합니다.

주요 수정 위치
- 공통 버튼/카드/탭/폼: styles/99-ui-unified-overrides.css
- 지출관리: styles/02-expense.css
- OTT: styles/03-ott-base-chat.css ~ styles/07-ott-family-recruit.css
- 마이페이지: styles/08-mypage.css, styles/09-mypage-withdraw.css
- 메인 슬롯머신/그래프: styles/10-main-slot-dashboard.css
- 빠른 참가: styles/11-quick-join.css

주의
- 기존 기능을 살리기 위해 selector 이름은 거의 바꾸지 않았습니다.
- JSP class명을 대량으로 바꾸는 작업은 2차 정리에서 하는 것이 안전합니다.
