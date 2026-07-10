    <%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
    <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
    <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
    <c:set var="contextPath" value="${pageContext.request.contextPath}" />
    <link rel="stylesheet" href="${contextPath}/resources/css/admin.css">


    <body data-page="report">
    <div class="admin-shell">

   
    <main class="admin-main">
    
    <section class="hero"><div><div class="hero-kicker">Report Management</div><h1>신고관리</h1>
    <p>신고 리스트에서 신고 항목, 내용, 신고자와 대상자를 확인하고 처리결과 상태를 변경합니다.</p></div></section>
    <br>
    <section class="panel"><div class="panel-header"><div class="panel-title"><div class="section-kicker">Report List</div>
    <h2>신고 리스트(총 ${reportList.size()}건)</h2><p>신고 항목과 내용을 확인한 뒤 처리 상태를 바꿀 수 있습니다.</p></div>
    <div class="filter-pills"><button class="active">전체</button><button>대기</button><button>처리중</button><button>완료</button></div>
    </div><div class="table-wrap">
      
    <c:choose>
                <c:when test="${empty reportList}">
                <div class="panel-title" style="padding: 20px; text-align: center;">
                    <div class="section-kicker">신고 건이 없습니다</div>
                </div>
            </c:when>
                <c:otherwise>
                <table class="admin-table">
                <thead><tr><th>번호</th><th>신고자</th><th>신고대상</th><th>신고내용</th><th>신고 날짜</th><th>상태</th><th>처리</th></tr></thead><tbody>
                        <c:forEach var="report" items="${reportList}" varStatus="s">
                      
    <tr><td>${s.count}</td><td>${report.reporter_id}</td><td>${report.reported_member_id}</td><td><textarea style="margin-top: 12px;" class="form-textarea"  readonly>${report.report_reason}</textarea></td><td>${report.created_at}</td><td><span class="badge yellow">${report.report_status}</span></td>
    <td><button type="button" class="mini-btn" onclick="comment('${report.report_id}', '${report.reported_member_id}','${s.count}','${report.report_reason}')">처리</button></td></tr>
    
                        </c:forEach>
                        </tbody></table>
                </c:otherwise>
        </c:choose>
        </div></section>
        <form action="${contextPath}/admin/report/comment.do" method="post">
 <section id="commentArea" class="panel" style="display:none; margin-top: 20px;">
            
                
                <div class="panel-title">
                    <div class="section-kicker">Process Result</div>
                    <h2>신고 처리결과 작성</h2>
                </div>
                
                <div class="form-grid" style="margin-top:18px">
                    <div class="form-field">
                     
                        <label>처리 상태</label>
                        <select class="form-input" name="result" id="reportResult">
                            <option value="1">경고</option>
                            <option value="2">퇴출</option>
                            <option value="3">반려</option>
                        </select>
                    </div>
           
         
                    <input type="hidden" name="reported_member_id" id="form_reported_member_id" value="">
                    <input type="hidden" name="report_id" id="form_report_id" value="">
                </div>
                <label>코멘트</label>
                <textarea name="admin_comment" class="form-textarea" placeholder="처리 결과를 입력하세요." style="margin-top: 12px;">신고 내용을 확인했고 대상 회원에게 경고 1회를 적용했습니다.</textarea>
                
                <div class="toolbar" style="margin-top:16px">
                    <span></span>
                    <button class="btn primary" type="submit">처리결과 저장</button>
                </div>
                
             </section>
                </form>
    
    </main>

    <footer class="footer">
        <div class="footer-inner">
            <div>SpendOlive Admin UI Preview</div>
            <div>DB 연결 없이 화면 확인용으로 제작된 독립 HTML 프로젝트입니다.</div>
        </div>
    </footer>
    <div class="toast" aria-live="polite"></div>


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
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>

<script src="${contextPath}/resources/js/admin.js"></script>