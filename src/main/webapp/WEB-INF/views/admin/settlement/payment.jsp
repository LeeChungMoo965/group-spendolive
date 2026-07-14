    <%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
    <c:set var="contextPath" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${contextPath}/resources/css/admin.css">


    <body data-page="report">
    <div class="admin-shell">

   
    <main class="admin-main">
    
    <section class="hero"><div><div class="hero-kicker">Report Management</div><h1>정산관리</h1>
    <p>금일 이용료 보관액 방장 정산</p></div></section>
    <br>
    <section class="panel"><div class="panel-header"><div class="panel-title"><div class="section-kicker">payment List</div>
    <h2>금일 정산 리스트(총 ${paymentList.size()}건)</h2></div>
    <div class="filter-pills"><button onclick="location.href='${contextPath}/admin/settlement/paymentlist.do'">전체</button><button onclick="location.href='${contextPath}/admin/settlement/paymentlist.do?status=READY'" >대기</button><button onclick="location.href='${contextPath}/admin/settlement/paymentlist.do?status=DONE'">완료</button></div>
    </div><div class="table-wrap">
      
    <c:choose>
                <c:when test="${empty paymentList}">
                <div class="panel-title" style="padding: 20px; text-align: center;">
                    <div class="section-kicker">정산 건이 없습니다</div>
                </div>
            </c:when>
                <c:otherwise>
                <table class="admin-table">
                <thead><tr><th>번호</th><th>방 ID</th><th>방장 ID</th><th>정산 금액</th><th>결제일</th><th>상태</th><th>처리</th></tr></thead><tbody>
                        <c:forEach var="member" items="${paymentList}" varStatus="s">
                      
    <tr><td>${s.count}</td><td>${member.roomId}</td><td>${member.memberId}</td><td><fmt:formatNumber value="${member.payAmount}" type="number" />원</td><td>${member.pay_day} + 연체일 ${member.pay_late_day} </td><td><span class="badge yellow">${member.settlement_status}</span></td>
    <td>
       
    <c:choose>
    <c:when test="${member.settlement_status == 'DONE'}">
        <button class="mini-btn">정산 완료</button>
    </c:when>
     <c:otherwise>
     <form action="${contextPath}/payment/paymenting.do" method="post"><input id="roomId" type="hidden" name="roomId" value="${room.roomId}">
    <button class="mini-btn warning">정산금 받기</button></form>
      </c:otherwise>
    </c:choose>
    </td></tr>
                        </c:forEach>
                        </tbody></table>
                </c:otherwise>
        </c:choose>
        </tbody></table></div></section>


    </div>
    </body>
    </html>
    <script>
        // 컨트롤러가 보낸 일회성 메시지(msg)가 있다면 alert을 띄운다
        var msg = "${msg}";
        if(msg && msg !== "") {
            alert(msg);
        }
    </script>
    <script src="${contextPath}/resources/js/admin.js"></script>
