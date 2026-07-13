<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<link rel="stylesheet" href="${contextPath}/resources/css/styles.css">

<main>
    <section class="page-hero">
        <div class="container page-hero-grid">
            <div>
                <p class="eyebrow">EXPENSE MANAGER</p>
                <h1>지출관리</h1>
                <p class="hero-text">
                    고정지출, 변동지출, OTT지출을 분류별로 등록하고 월별 지출 내역을 확인합니다.
                </p>
                <div class="hero-buttons">
                    <a href="#expense-form" class="btn btn-primary btn-large">지출 등록</a>
                    <a href="#expense-list" class="btn btn-outline btn-large">월별 내역 보기</a>
                    <a href="${contextPath}/spendolive/calendar.do" class="btn btn-outline btn-large">캘린더</a>
                </div>
            </div>

            <div class="dashboard-preview">
                <div class="preview-header">
                    <span>월별 조회</span>
                    <strong>${selectedYearMonth.substring(5, 7)}월 요약</strong>
                </div>

                <div class="category-list expense-type-summary-list">
                    <div>
                        <span class="dot fixed"></span>
                        <p>고정</p>
                        <strong>
                            <fmt:formatNumber value="${expenseTypeSummary.FIXED}" pattern="#,###" />원
                        </strong>
                    </div>
                    <div>
                        <span class="dot variable"></span>
                        <p>변동</p>
                        <strong>
                            <fmt:formatNumber value="${expenseTypeSummary.VARIABLE}" pattern="#,###" />원
                        </strong>
                    </div>
                    <div>
                        <span class="dot ott"></span>
                        <p>OTT</p>
                        <strong>
                            <fmt:formatNumber value="${expenseTypeSummary.OTT}" pattern="#,###" />원
                        </strong>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section id="expense-form" class="section compact">
        <div class="container">
            <div class="expense-layout">
                <div class="expense-form card">
                    <h3>빠른 지출 등록</h3>

                    <form action="${contextPath}/spendolive/expense/add.do" method="post" class="form-grid">
                        <input type="hidden" name="yearMonth" value="${selectedYearMonth}">
                        <input type="hidden" id="repeatYn" name="repeatYn" value="N">
                        <input type="hidden" id="fixedYn" name="fixedYn" value="N">

                        <label>
                            분류
                            <select id="expenseType" name="expenseType" required>
                                <option value="">분류 선택</option>
                                <option value="FIXED">고정</option>
                                <option value="VARIABLE">변동</option>
                                <option value="OTT">OTT</option>
                            </select>
                        </label>

                        <label>
                            카테고리
                            <select id="categoryId" name="categoryId" required>
                                <option value="">먼저 분류를 선택하세요</option>
                                <c:forEach var="category" items="${categoryList}">
                                    <option value="${category.categoryId}" data-type="${category.expenseType}">
                                        ${category.categoryName}
                                    </option>
                                </c:forEach>
                            </select>
                        </label>

                        <label>
                            지출 제목
                            <input type="text" name="expenseTitle" placeholder="예: 월세, 점심 식사" required>
                        </label>

                        <label>
                            금액
                            <input type="number" name="amount" placeholder="예: 12000" min="0" required>
                        </label>

                        <label>
                            지출 날짜
                            <input type="date" name="expenseDate" required>
                        </label>

                        <label>
                            결제 수단
                            <select name="paymentMethod">
                                <option value="">선택 안함</option>
                                <option value="CARD">카드</option>
                                <option value="CASH">현금</option>
                                <option value="TRANSFER">계좌이체</option>
                                <option value="KAKAO_PAY">카카오페이</option>
                                <option value="NAVER_PAY">네이버페이</option>
                            </select>
                        </label>

                        <label id="repeatCycleArea" style="display:none;">
                            반복 주기
                            <select id="repeatCycle" name="repeatCycle">
                                <option value="">반복 없음</option>
                                <option value="MONTHLY">매월</option>
                                <option value="WEEKLY">매주</option>
                                <option value="YEARLY">매년</option>
                            </select>
                        </label>

                        <label>
                            메모
                            <input type="text" name="memo" placeholder="선택 입력">
                        </label>

                        <button type="submit" class="btn btn-primary full">
                            등록하기
                        </button>
                    </form>
                </div>

                <div id="expense-list" class="expense-table card">
                    <div class="row-title">
                        <div>
                            <h3>최근 지출 내역</h3>
                            <p class="card-desc">선택한 달의 지출만 표시됩니다.</p>
                        </div>

                        <form action="${contextPath}/spendolive/expense/list.do" method="get" class="month-search-form">
                            <input type="month"
                                   name="yearMonth"
                                   value="${selectedYearMonth}"
                                   onchange="this.form.submit()"
                                   class="month-picker-input">
                        </form>
                    </div>

                    <div class="table-wrap">
                        <table>
                            <thead>
                                <tr>
                                    <th>날짜</th>
                                    <th>내용</th>
                                    <th>분류</th>
                                    <th>카테고리</th>
                                    <th>금액</th>
                                    <th>결제수단</th>
                                    <th>반복</th>
                                    <th>관리</th>
                                </tr>
                            </thead>

                            <tbody id="expenseRows">
                                <c:forEach var="expense" items="${expenseList}">
                                    <fmt:formatDate var="expenseDateValue" value="${expense.expenseDate}" pattern="yyyy-MM-dd" />

                                    <tr class="expense-row" data-expense-id="${expense.expenseId}">
                                        <td>
                                            <span class="view-mode">
                                                <fmt:formatDate value="${expense.expenseDate}" pattern="yyyy.MM.dd" />
                                            </span>
                                            <input class="edit-mode" form="editForm${expense.expenseId}" type="date" name="expenseDate" value="${expenseDateValue}" required style="display:none;">
                                        </td>

                                        <td>
                                            <span class="view-mode">
                                                ${expense.expenseTitle}
                                                <c:if test="${expense.autoGeneratedYn == 'Y'}">
                                                    <span class="tag">자동</span>
                                                </c:if>
                                            </span>
                                            <input class="edit-mode" form="editForm${expense.expenseId}" type="text" name="expenseTitle" value="${expense.expenseTitle}" required style="display:none;">
                                        </td>

                                        <td>
                                            <span class="view-mode">
                                                <c:choose>
                                                    <c:when test="${expense.expenseType == 'FIXED'}">고정</c:when>
                                                    <c:when test="${expense.expenseType == 'VARIABLE'}">변동</c:when>
                                                    <c:when test="${expense.expenseType == 'OTT'}">OTT</c:when>
                                                    <c:otherwise>${expense.expenseType}</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <select class="edit-mode edit-expense-type" form="editForm${expense.expenseId}" name="expenseType" data-row-id="${expense.expenseId}" onchange="filterEditCategoriesFromSelect(this)" required style="display:none;">
                                                <option value="FIXED" ${expense.expenseType == 'FIXED' ? 'selected' : ''}>고정</option>
                                                <option value="VARIABLE" ${expense.expenseType == 'VARIABLE' ? 'selected' : ''}>변동</option>
                                                <option value="OTT" ${expense.expenseType == 'OTT' ? 'selected' : ''}>OTT</option>
                                            </select>
                                        </td>

                                        <td>
                                            <span class="view-mode">${expense.categoryName}</span>
                                            <select class="edit-mode edit-category" form="editForm${expense.expenseId}" name="categoryId" data-row-id="${expense.expenseId}" required style="display:none;">
                                                <c:forEach var="category" items="${categoryList}">
                                                    <option value="${category.categoryId}" data-type="${category.expenseType}" ${category.categoryId == expense.categoryId ? 'selected' : ''}>
                                                        ${category.categoryName}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </td>

                                        <td>
                                            <span class="view-mode">
                                                <fmt:formatNumber value="${expense.amount}" pattern="#,###" />원
                                            </span>
                                            <input class="edit-mode" form="editForm${expense.expenseId}" type="number" name="amount" value="${expense.amount}" min="0" required style="display:none;">
                                        </td>

                                        <td>
                                            <span class="view-mode">
                                                <c:choose>
                                                    <c:when test="${expense.paymentMethod == 'CARD'}">카드</c:when>
                                                    <c:when test="${expense.paymentMethod == 'CASH'}">현금</c:when>
                                                    <c:when test="${expense.paymentMethod == 'TRANSFER'}">계좌이체</c:when>
                                                    <c:when test="${expense.paymentMethod == 'KAKAO_PAY'}">카카오페이</c:when>
                                                    <c:when test="${expense.paymentMethod == 'NAVER_PAY'}">네이버페이</c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <select class="edit-mode" form="editForm${expense.expenseId}" name="paymentMethod" style="display:none;">
                                                <option value="" ${empty expense.paymentMethod ? 'selected' : ''}>선택 안함</option>
                                                <option value="CARD" ${expense.paymentMethod == 'CARD' ? 'selected' : ''}>카드</option>
                                                <option value="CASH" ${expense.paymentMethod == 'CASH' ? 'selected' : ''}>현금</option>
                                                <option value="TRANSFER" ${expense.paymentMethod == 'TRANSFER' ? 'selected' : ''}>계좌이체</option>
                                                <option value="KAKAO_PAY" ${expense.paymentMethod == 'KAKAO_PAY' ? 'selected' : ''}>카카오페이</option>
                                                <option value="NAVER_PAY" ${expense.paymentMethod == 'NAVER_PAY' ? 'selected' : ''}>네이버페이</option>
                                            </select>
                                        </td>

                                        <td>
                                            <span class="view-mode">
                                                <c:choose>
                                                    <c:when test="${expense.repeatCycle == 'MONTHLY'}">매월</c:when>
                                                    <c:when test="${expense.repeatCycle == 'WEEKLY'}">매주</c:when>
                                                    <c:when test="${expense.repeatCycle == 'YEARLY'}">매년</c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </span>
                                            <select class="edit-mode edit-repeat-cycle" form="editForm${expense.expenseId}" name="repeatCycle" data-row-id="${expense.expenseId}" onchange="changeEditRepeatYnFromSelect(this)" style="display:none;">
                                                <option value="" ${empty expense.repeatCycle ? 'selected' : ''}>반복 없음</option>
                                                <option value="MONTHLY" ${expense.repeatCycle == 'MONTHLY' ? 'selected' : ''}>매월</option>
                                                <option value="WEEKLY" ${expense.repeatCycle == 'WEEKLY' ? 'selected' : ''}>매주</option>
                                                <option value="YEARLY" ${expense.repeatCycle == 'YEARLY' ? 'selected' : ''}>매년</option>
                                            </select>
                                        </td>

                                        <td>
                                            <c:choose>
                                                <c:when test="${expense.autoGeneratedYn == 'Y'}">
                                                    <span class="tag">원본달에서 삭제</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <form id="editForm${expense.expenseId}" action="${contextPath}/spendolive/expense/modify.do" method="post">
                                                        <input type="hidden" name="expenseId" value="${expense.expenseId}">
                                                        <input type="hidden" name="yearMonth" value="${selectedYearMonth}">
                                                        <input type="hidden" id="editRepeatYn${expense.expenseId}" name="repeatYn" value="${expense.repeatYn}">
                                                        <input type="hidden" id="editFixedYn${expense.expenseId}" name="fixedYn" value="${expense.fixedYn}">
                                                        <input type="hidden" form="editForm${expense.expenseId}" name="memo" value="${expense.memo}">
                                                    </form>

                                                    <div style="display:flex; gap:6px; flex-wrap:wrap;">
                                                        <button type="button" class="btn btn-primary edit-btn" onclick="changeEditMode(this)">수정</button>
                                                        <button type="submit" form="editForm${expense.expenseId}" class="btn btn-primary save-btn" style="display:none;">완료</button>
                                                        <button type="button" class="btn btn-outline cancel-btn" onclick="cancelEditMode(this)" style="display:none;">취소</button>

                                                        <form action="${contextPath}/spendolive/expense/delete.do" method="post" onsubmit="return confirm('이 지출 내역을 삭제하시겠습니까?');">
                                                            <input type="hidden" name="expenseId" value="${expense.expenseId}">
                                                            <input type="hidden" name="yearMonth" value="${selectedYearMonth}">
                                                            <button type="submit" class="btn btn-outline delete-btn">삭제</button>
                                                        </form>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>

                                <c:if test="${empty expenseList}">
                                    <tr>
                                        <td colspan="8" style="text-align:center;">
                                            선택한 달에 등록된 지출 내역이 없습니다.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section id="expense-analytics" class="section compact">
        <div class="container">
            <div class="section-title">
                <p class="eyebrow">EXPENSE INSIGHT</p>
                <h2>지출 분석</h2>
                <p class="section-desc">
                    선택한 달을 기준으로 최근 3개월 지출 흐름, 카테고리별 지출 비중, 이번달 지출 랭킹을 확인합니다.
                </p>
            </div>

            <div class="analytics-grid">
                <div class="analytics-card card">
                    <h3>
                        월별 지출 차트
                        <small>최근 3개월</small>
                    </h3>

                    <div class="bar-chart expense-month-chart">
                        <c:forEach var="monthData" items="${monthChartList}">
                            <div class="bar-item ${monthData.month == selectedYearMonth ? 'active' : ''}">
                                <strong>
                                    <fmt:formatNumber value="${monthData.total}" pattern="#,###" />원
                                </strong>
                                <div class="bar-track">
                                    <div style="height:${monthData.barPercent}%;"></div>
                                </div>
                                <span>${monthData.monthLabel}</span>
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <div class="analytics-card card">
                    <h3>
                        카테고리별 지출 분석
                        <small>이번달 기준</small>
                    </h3>

                    <p class="card-desc">
                        총 지출 <strong><fmt:formatNumber value="${selectedMonthTotal}" pattern="#,###" />원</strong> 기준입니다.
                    </p>

                    <c:choose>
                        <c:when test="${empty categorySummaryList}">
                            <p class="empty-analytics">이번달 카테고리별 지출 데이터가 없습니다.</p>
                        </c:when>
                        <c:otherwise>
                            <ul class="category-analysis-list">
                                <c:forEach var="categoryData" items="${categorySummaryList}">
                                    <li>
                                        <div class="category-analysis-head">
                                            <strong>${categoryData.categoryName}</strong>
                                            <span>${categoryData.percent}%</span>
                                        </div>
                                        <div class="category-progress">
                                            <span style="width:${categoryData.percent}%;"></span>
                                        </div>
                                        <p>
                                            <fmt:formatNumber value="${categoryData.total}" pattern="#,###" />원
                                        </p>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="analytics-card card">
                    <h3>
                        이번달 지출 랭킹
                        <small>최대 10개</small>
                    </h3>

                    <c:choose>
                        <c:when test="${empty rankingList}">
                            <p class="empty-analytics">이번달 지출 랭킹 데이터가 없습니다.</p>
                        </c:when>
                        <c:otherwise>
                            <ol class="ranking-list expense-ranking-list">
                                <c:forEach var="ranking" items="${rankingList}">
                                    <li>
                                        <div>
                                            <strong>${ranking.expenseTitle}</strong>
                                            <span>${ranking.categoryName}</span>
                                        </div>
                                        <em>
                                            <fmt:formatNumber value="${ranking.amount}" pattern="#,###" />원
                                        </em>
                                    </li>
                                </c:forEach>
                            </ol>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </section>

</main>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        const expenseTypeSelect = document.getElementById('expenseType');
        const categorySelect = document.getElementById('categoryId');
        const repeatCycleArea = document.getElementById('repeatCycleArea');
        const repeatCycleSelect = document.getElementById('repeatCycle');
        const repeatYnInput = document.getElementById('repeatYn');
        const fixedYnInput = document.getElementById('fixedYn');

        const categoryMasterList = categorySelect
            ? Array.from(categorySelect.querySelectorAll('option[data-type]')).map(option => ({
                value: option.value,
                type: (option.dataset.type || '').trim(),
                text: option.textContent.trim()
            }))
            : [];

        function isRepeatTargetType(type) {
            return type === 'FIXED' || type === 'OTT';
        }

        function makeCategoryOption(category) {
            const option = document.createElement('option');
            option.value = category.value;
            option.dataset.type = category.type;
            option.textContent = category.text;
            return option;
        }

        function renderCategoryOptions(select, selectedType, selectedValue, placeholderText) {
            if (!select) {
                return;
            }

            select.innerHTML = '';

            const placeholder = document.createElement('option');
            placeholder.value = '';
            placeholder.textContent = placeholderText || '카테고리 선택';
            select.appendChild(placeholder);

            if (!selectedType) {
                select.value = '';
                return;
            }

            const filteredList = categoryMasterList.filter(category => category.type === selectedType);

            if (filteredList.length === 0) {
                placeholder.textContent = '해당 분류의 카테고리가 없습니다';
                select.value = '';
                return;
            }

            filteredList.forEach(category => {
                select.appendChild(makeCategoryOption(category));
            });

            const hasSelectedValue = filteredList.some(category => category.value === String(selectedValue || ''));

            if (hasSelectedValue) {
                select.value = selectedValue;
            } else {
                select.value = '';
            }
        }

        function filterCategories() {
            if (!expenseTypeSelect || !categorySelect) {
                return;
            }

            const selectedType = expenseTypeSelect.value;

            if (!selectedType) {
                renderCategoryOptions(categorySelect, '', '', '먼저 분류를 선택하세요');
            } else {
                renderCategoryOptions(categorySelect, selectedType, categorySelect.value, '카테고리 선택');
            }

            if (isRepeatTargetType(selectedType)) {
                if (repeatCycleArea) {
                    repeatCycleArea.style.display = 'block';
                }

                if (fixedYnInput) {
                    fixedYnInput.value = 'Y';
                }
            } else {
                if (repeatCycleArea) {
                    repeatCycleArea.style.display = 'none';
                }

                if (repeatCycleSelect) {
                    repeatCycleSelect.value = '';
                }

                if (repeatYnInput) {
                    repeatYnInput.value = 'N';
                }

                if (fixedYnInput) {
                    fixedYnInput.value = 'N';
                }
            }

            changeRepeatYn();
        }

        function changeRepeatYn() {
            if (!expenseTypeSelect) {
                return;
            }

            if (isRepeatTargetType(expenseTypeSelect.value) && repeatCycleSelect && repeatCycleSelect.value !== '') {
                if (repeatYnInput) {
                    repeatYnInput.value = 'Y';
                }

                if (fixedYnInput) {
                    fixedYnInput.value = 'Y';
                }
            } else if (isRepeatTargetType(expenseTypeSelect.value)) {
                if (repeatYnInput) {
                    repeatYnInput.value = 'N';
                }

                if (fixedYnInput) {
                    fixedYnInput.value = 'Y';
                }
            } else {
                if (repeatYnInput) {
                    repeatYnInput.value = 'N';
                }

                if (fixedYnInput) {
                    fixedYnInput.value = 'N';
                }
            }
        }

        function changeEditMode(button) {
            const row = button.closest('tr');

            if (!row) {
                console.error('수정할 행을 찾을 수 없습니다.');
                return;
            }

            row.querySelectorAll('.view-mode').forEach(element => {
                element.style.display = 'none';
            });

            row.querySelectorAll('.edit-mode').forEach(element => {
                element.style.display = 'inline-block';
            });

            const editButton = row.querySelector('.edit-btn');
            const saveButton = row.querySelector('.save-btn');
            const cancelButton = row.querySelector('.cancel-btn');
            const deleteButton = row.querySelector('.delete-btn');

            if (editButton) {
                editButton.style.display = 'none';
            }

            if (saveButton) {
                saveButton.style.display = 'inline-block';
            }

            if (cancelButton) {
                cancelButton.style.display = 'inline-block';
            }

            if (deleteButton) {
                deleteButton.style.display = 'none';
            }

            filterEditCategoriesByRow(row);
            changeEditRepeatYnByRow(row);
        }

        function cancelEditMode(button) {
            const row = button.closest('tr');

            if (!row) {
                return;
            }

            row.querySelectorAll('.view-mode').forEach(element => {
                element.style.display = 'inline';
            });

            row.querySelectorAll('.edit-mode').forEach(element => {
                element.style.display = 'none';
            });

            const editButton = row.querySelector('.edit-btn');
            const saveButton = row.querySelector('.save-btn');
            const cancelButton = row.querySelector('.cancel-btn');
            const deleteButton = row.querySelector('.delete-btn');

            if (editButton) {
                editButton.style.display = 'inline-block';
            }

            if (saveButton) {
                saveButton.style.display = 'none';
            }

            if (cancelButton) {
                cancelButton.style.display = 'none';
            }

            if (deleteButton) {
                deleteButton.style.display = 'inline-block';
            }
        }

        function filterEditCategoriesFromSelect(select) {
            const row = select.closest('tr');

            if (!row) {
                return;
            }

            filterEditCategoriesByRow(row);
            changeEditRepeatYnByRow(row);
        }

        function changeEditRepeatYnFromSelect(select) {
            const row = select.closest('tr');

            if (!row) {
                return;
            }

            changeEditRepeatYnByRow(row);
        }

        function filterEditCategoriesByRow(row) {
            const typeSelect = row.querySelector('.edit-expense-type');
            const editCategorySelect = row.querySelector('.edit-category');

            if (!typeSelect || !editCategorySelect) {
                return;
            }

            renderCategoryOptions(
                editCategorySelect,
                typeSelect.value,
                editCategorySelect.value,
                '카테고리 선택'
            );
        }

        function changeEditRepeatYnByRow(row) {
            const expenseId = row.dataset.expenseId;
            const typeSelect = row.querySelector('.edit-expense-type');
            const editRepeatCycleSelect = row.querySelector('.edit-repeat-cycle');
            const editRepeatYnInput = document.getElementById(`editRepeatYn${expenseId}`);
            const editFixedYnInput = document.getElementById(`editFixedYn${expenseId}`);

            if (!typeSelect || !editRepeatCycleSelect || !editRepeatYnInput || !editFixedYnInput) {
                return;
            }

            if (isRepeatTargetType(typeSelect.value)) {
                editFixedYnInput.value = 'Y';

                if (editRepeatCycleSelect.value !== '') {
                    editRepeatYnInput.value = 'Y';
                } else {
                    editRepeatYnInput.value = 'N';
                }
            } else {
                editFixedYnInput.value = 'N';
                editRepeatYnInput.value = 'N';
                editRepeatCycleSelect.value = '';
            }
        }

        window.changeEditMode = changeEditMode;
        window.cancelEditMode = cancelEditMode;
        window.filterEditCategoriesFromSelect = filterEditCategoriesFromSelect;
        window.changeEditRepeatYnFromSelect = changeEditRepeatYnFromSelect;

        if (expenseTypeSelect) {
            expenseTypeSelect.addEventListener('change', filterCategories);
        }

        if (repeatCycleSelect) {
            repeatCycleSelect.addEventListener('change', changeRepeatYn);
        }

        filterCategories();
    });
</script>
