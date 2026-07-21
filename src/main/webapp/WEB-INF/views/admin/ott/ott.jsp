<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<%--
    관리자 OTT 관리 JSP
    적용 위치 예시:
    src/main/webapp/WEB-INF/views/admin/adminOtt.jsp

    원본 ott.html을 SpendOlive JSP 구조에 맞게 변환한 파일입니다.
    주요 변경점:
    1. html/head/body 전체 구조 제거
       - common/layout.jsp 안에서 include되는 body_page 방식에 맞춤
    2. css/js 경로를 contextPath 기준으로 변경
       - /resources/css/admin.css
       - /resources/js/admin.js
    3. 하드코딩된 OTT 카드 목록을 serviceList 기반 c:forEach로 변경
    4. 현재 프로젝트의 ott_service_tb / OttServiceDTO 필드명에 맞춤
       - service_name
       - fixed_plan_name
       - base_price
       - default_price
       - max_member_limit
       - extra_member_fee
       - extra_member_count
       - platform_fee_rate
       - share_yn
       - risk_level
       - block_reason
    5. 추가/수정/삭제 form action은 관리자 OTT Controller를 만들 때 사용할 수 있도록 경로만 잡아둠

    Controller에서 넘기면 좋은 model 이름:
    - serviceList : List<OttServiceDTO>
    - editService : 수정 모드일 때 선택한 OttServiceDTO
    - msg : 성공 메시지
    - errorMsg : 오류 메시지
--%>

<%-- admin.js가 body[data-page]를 기준으로 메뉴 활성화를 처리할 수 있어서 JSP 로딩 시 설정 --%>
<script>
    document.body.setAttribute('data-page', 'ott');
</script>

<main class="admin-main">

    <section class="hero">
        <div>
            <div class="hero-kicker">OTT Management</div>
            <h1>OTT 관리</h1>
            <p>
                Netflix, Disney+, TVING, Wavve, Watcha, Laftel 등 SpendOlive에서 제공하는
                OTT 서비스 항목의 요금, 최대 인원, 추가 멤버 비용, 공유 가능 여부를 관리합니다.
            </p>
        </div>
    </section>
    <br>
    <%-- 성공/실패 메시지 출력 --%>
    <c:if test="${not empty msg}">
        <script>
            alert("${fn:escapeXml(msg)}");
        </script>
    </c:if>
    <c:if test="${not empty errorMsg}">
        <script>
            alert("${fn:escapeXml(errorMsg)}");
        </script>
    </c:if>

    <section class="panel">
        <div class="panel-header">
            <div class="panel-title">
                <div class="section-kicker">OTT Services</div>
                <h2>OTT 항목 관리</h2>
                <p>
                    최고 멤버십 가격, 최종 기준금액, 최대 인원, 추가 멤버 비용, 플랫폼 수수료를 관리합니다.
                </p>
            </div>

            <a href="#adminOttForm" class="btn primary">
                OTT 항목 추가
            </a>
        </div>

        <c:choose>
            <c:when test="${empty serviceList}">
                <div class="panel-title" style="padding: 28px; text-align: center;">
                    <div class="section-kicker">No Data</div>
                    <h2>등록된 OTT 서비스가 없습니다.</h2>
                    <p>아래 추가·수정 폼에서 OTT 항목을 먼저 등록해 주세요.</p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="card-grid">
                    <c:forEach var="service" items="${serviceList}">
                        <%-- 서비스명에 따라 로고 색상 클래스 지정 --%>
                        <c:set var="logoClass" value="" />
                        <c:choose>
                            <c:when test="${fn:contains(fn:toLowerCase(service.service_name), 'netflix')}">
                                <c:set var="logoClass" value="netflix" />
                            </c:when>
                            <c:when test="${fn:contains(fn:toLowerCase(service.service_name), 'disney')}">
                                <c:set var="logoClass" value="disney" />
                            </c:when>
                            <c:when test="${fn:contains(fn:toLowerCase(service.service_name), 'tving')}">
                                <c:set var="logoClass" value="tving" />
                            </c:when>
                            <c:when test="${fn:contains(fn:toLowerCase(service.service_name), 'wavve')}">
                                <c:set var="logoClass" value="wavve" />
                            </c:when>
                            <c:when test="${fn:contains(fn:toLowerCase(service.service_name), 'watcha')}">
                                <c:set var="logoClass" value="watcha" />
                            </c:when>
                            <c:when test="${fn:contains(fn:toLowerCase(service.service_name), 'laftel')}">
                                <c:set var="logoClass" value="laftel" />
                            </c:when>
                        </c:choose>

                        <article class="manage-card">
                            <div class="manage-card-head">
                                <div class="ott-cell">
                                    <div class="ott-logo ${logoClass}">
                                        ${fn:substring(service.service_name, 0, 1)}
                                    </div>
                                    <div>
                                        <h3>${service.service_name}</h3>
                                        <p>${empty service.fixed_plan_name ? '프리미엄' : service.fixed_plan_name}</p>
                                    </div>
                                </div>

                                <c:choose>
                                    <c:when test="${service.share_yn eq 'N'}">
                                        <span class="badge gray">숨김</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge green">사용중</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="meta-grid">
                                <div class="meta-box">
                                    <small>최고 멤버십</small>
                                    <strong>
                                        <fmt:formatNumber value="${service.base_price}" type="number" />원
                                    </strong>
                                </div>

                                <div class="meta-box">
                                    <small>최종 기준금액</small>
                                    <strong>
                                        <fmt:formatNumber value="${service.default_price}" type="number" />원
                                    </strong>
                                </div>

                                <div class="meta-box">
                                    <small>최대 인원</small>
                                    <strong>${service.max_member_limit}명</strong>
                                </div>

                                <div class="meta-box">
                                    <small>추가 멤버 비용</small>
                                    <strong>
                                        <fmt:formatNumber value="${service.extra_member_fee}" type="number" />원
                                        <c:if test="${not empty service.extra_member_count}">
                                            × ${service.extra_member_count}명
                                        </c:if>
                                    </strong>
                                </div>

                                <div class="meta-box">
                                    <small>수수료</small>
                                    <strong>
                                        <fmt:formatNumber value="${service.platform_fee_rate}" pattern="0.##" />%
                                    </strong>
                                </div>

                                <div class="meta-box">
                                    <small>공유 위험도</small>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${empty service.risk_level}">-</c:when>
                                            <c:otherwise>${service.risk_level}</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                            </div>

                            <c:if test="${not empty service.block_reason}">
                                <p style="font-size:13px;">
                                    차단/주의 사유: ${service.block_reason}
                                </p>
                            </c:if>

                            <div class="table-actions">
                                <%--
                                    수정 버튼:
                                    AdminOttController에서 /spendolive/admin/ott/edit.do를 만들고
                                    editService를 model에 담아 adminOtt.jsp로 다시 보내면 아래 폼에 값이 채워집니다.
                                --%>
                                <a class="btn"
                                   href="${contextPath}/admin/ott/edit.do?ott_service_id=${service.ott_service_id}#adminOttForm">
                                    수정
                                </a>

                                <%--
                                    삭제 버튼:
                                    실제 삭제보다는 share_yn = 'N' 처리로 숨김 처리하는 방식을 추천합니다.
                                --%>
                                <form action="${contextPath}/admin/ott/delete.do"
                                      method="post"
                                      style="display:inline;"
                                      onsubmit="return confirm('해당 OTT 항목을 삭제 또는 숨김 처리하시겠습니까?');">
                                    <input type="hidden" name="ott_service_id" value="${service.ott_service_id}">
                                    <button type="submit" class="btn danger">
                                        삭제
                                    </button>
                                </form>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- 추가/수정 폼 --%>
    <c:set var="isEdit" value="${not empty editService}" />

    <section class="panel" id="adminOttForm">
        <div class="panel-title">
            <div class="section-kicker">Add / Edit</div>
            <h2>
                <c:choose>
                    <c:when test="${isEdit}">OTT 항목 수정</c:when>
                    <c:otherwise>OTT 항목 추가</c:otherwise>
                </c:choose>
            </h2>
            <p>
                현재 SpendOlive의 ott_service_tb 구조에 맞춰 서비스명, 요금, 최대 인원, 수수료, 공유 여부를 입력합니다.
            </p>
        </div>

        <form action="${contextPath}/admin/ott/${isEdit ? 'update' : 'insert'}.do"
              method="post"
              style="margin-top:18px;">

            <c:if test="${isEdit}">
                <input type="hidden" name="ott_service_id" value="${editService.ott_service_id}">
            </c:if>

            <div class="form-grid">
                <div class="form-field">
                    <label>OTT 이름</label>
                    <input class="form-input"
                           type="text"
                           name="service_name"
                           value="${isEdit ? editService.service_name : ''}"
                           placeholder="예: Netflix"
                           required>
                </div>

                <div class="form-field">
                    <label>고정 멤버십명</label>
                    <input class="form-input"
                           type="text"
                           name="fixed_plan_name"
                           value="${isEdit ? editService.fixed_plan_name : '프리미엄'}"
                           placeholder="예: 프리미엄">
                </div>

                <div class="form-field">
                    <label>최고 멤버십 가격</label>
                    <input class="form-input"
                           type="number"
                           name="base_price"
                           value="${isEdit ? editService.base_price : ''}"
                           placeholder="예: 17000"
                           min="0"
                           required>
                </div>

                <div class="form-field">
                    <label>최종 기준금액</label>
                    <input class="form-input"
                           type="number"
                           name="default_price"
                           value="${isEdit ? editService.default_price : ''}"
                           placeholder="예: 27000"
                           min="0">
                </div>

                <div class="form-field">
                    <label>최대 인원</label>
                    <input class="form-input"
                           type="number"
                           name="max_member_limit"
                           value="${isEdit ? editService.max_member_limit : 4}"
                           placeholder="예: 4"
                           min="1"
                           max="6"
                           required>
                </div>

                <div class="form-field">
                    <label>추가 멤버 비용</label>
                    <input class="form-input"
                           type="number"
                           name="extra_member_fee"
                           value="${isEdit ? editService.extra_member_fee : 0}"
                           placeholder="예: 4000"
                           min="0">
                </div>

                <div class="form-field">
                    <label>추가 멤버 수</label>
                    <input class="form-input"
                           type="number"
                           name="extra_member_count"
                           value="${isEdit ? editService.extra_member_count : 0}"
                           placeholder="예: 2"
                           min="0">
                </div>

                <div class="form-field">
                    <label>플랫폼 수수료율(%)</label>
                    <input class="form-input"
                           type="number"
                           step="0.01"
                           name="platform_fee_rate"
                           value="${isEdit ? editService.platform_fee_rate : 3}"
                           placeholder="예: 3"
                           min="0">
                </div>

                <div class="form-field">
                    <label>공유 가능 여부</label>
                    <select class="form-input" name="share_yn">
                        <option value="Y" ${isEdit and editService.share_yn eq 'Y' ? 'selected' : ''}>사용중</option>
                        <option value="N" ${isEdit and editService.share_yn eq 'N' ? 'selected' : ''}>숨김</option>
                    </select>
                </div>

                <div class="form-field">
                    <label>위험도</label>
                    <select class="form-input" name="risk_level">
                        <option value="" ${empty editService.risk_level ? 'selected' : ''}>선택 안 함</option>
                        <option value="LOW" ${isEdit and editService.risk_level eq 'LOW' ? 'selected' : ''}>LOW</option>
                        <option value="MEDIUM" ${isEdit and editService.risk_level eq 'MEDIUM' ? 'selected' : ''}>MEDIUM</option>
                        <option value="HIGH" ${isEdit and editService.risk_level eq 'HIGH' ? 'selected' : ''}>HIGH</option>
                    </select>
                </div>
            </div>

            <div class="form-field" style="margin-top:16px;">
                <label>차단/주의 사유</label>
                <textarea class="form-textarea"
                          name="block_reason"
                          placeholder="공유 불가 또는 주의 사유가 있다면 입력하세요.">${isEdit ? editService.block_reason : ''}</textarea>
            </div>

            <div class="toolbar" style="margin-top:16px;">
                <span>
                    <c:choose>
                        <c:when test="${isEdit}">
                            수정 중인 항목 ID: ${editService.ott_service_id}
                        </c:when>
                    </c:choose>
                </span>

                <div class="table-actions">
                    <c:if test="${isEdit}">
                        <a href="${contextPath}/admin/ott/list.do" class="btn">
                            수정 취소
                        </a>
                    </c:if>
                    <button class="btn primary" type="submit">
                        저장하기
                    </button>
                </div>
            </div>
        </form>
    </section>
</main>

<div class="toast" aria-live="polite"></div>
<script src="${contextPath}/resources/js/admin.js"></script>
