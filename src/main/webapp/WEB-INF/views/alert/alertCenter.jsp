<section class="section">

    <div class="container">

        <div class="section-title">
            <h2>내 알림</h2>
            <p class="section-desc">
                수신된 알림 목록입니다.
            </p>
        </div>

        <div class="alert-list">

            <c:forEach var="alert"
                       items="${alertList}">

                <a class="alert-card"
                   href="${contextPath}/spendolive/alert/detail.do?alertId=${alert.alertId}">

                    <div>

                        <c:if test="${alert.readYn eq 'N'}">
                            <span class="alert-badge">
                                NEW
                            </span>
                        </c:if>

                        <strong>
                            ${alert.title}
                        </strong>

                    </div>

                    <span class="alert-date">
                        ${alert.createdAt}
                    </span>

                </a>

            </c:forEach>

        </div>

    </div>

</section>