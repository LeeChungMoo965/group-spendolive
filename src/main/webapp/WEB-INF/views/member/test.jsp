    <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <style>
        :root { --olive: #5e6b36; --bg: #fdfdf5; --gray: #b0b0b0; }
        body { margin: 0; font-family: sans-serif; background-color: var(--bg); }
        
        /* 카드 디자인 */
        .settlement-card {
            max-width: 600px; margin: 50px auto; padding: 40px;
            background: white; border-radius: 30px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            display: flex; justify-content: space-between; align-items: center;
        }
        
        /* 버튼 그룹 */
        .btn-group { display: flex; justify-content: center; gap: 20px; margin-top: 30px; }
        .btn { padding: 15px 40px; border-radius: 50px; border: none; cursor: pointer; color: white; font-weight: bold; }
        .btn-green { background-color: var(--olive); }
        .btn-gray { background-color: var(--gray); }

        /* 하단 푸터 */
        footer { background-color: var(--olive); color: white; padding: 30px; margin-top: 50px; font-size: 0.8rem; }
    </style>
    <link rel="stylesheet" href="${contextPath}/resources/css/styles.css">
<div class="settlement-card">
        <div>
            <h1>(공유방 이름)</h1>
            <p>공유방 가격: 15,000원<br>세부 가격: 3,750원<br>OTT: 넷플릭스<br>정보: 프리미엄 / 4인</p>
        </div>
        <div style="width: 100px; height: 100px; background: #E50914; border-radius: 10px;"></div>
    </div>

    <!-- 하단 액션 버튼 -->
    <div class="btn-group">
        <button class="btn btn-green">정산 하기</button>
        <button class="btn btn-gray">돌아가기</button>
    </div>
