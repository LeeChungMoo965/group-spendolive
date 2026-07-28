    document.addEventListener('DOMContentLoaded', function () {
        // JSP에서는 퍼센트 값만 data-* 속성으로 전달하고 실제 스타일은 CSS 변수로 적용한다.
        document.querySelectorAll('[data-bar-height]').forEach(element => {
            const percent = Math.max(0, Math.min(100, Number(element.dataset.barHeight) || 0));
            element.style.setProperty('--expense-bar-height', `${percent}%`);
        });

        document.querySelectorAll('[data-progress-width]').forEach(element => {
            const percent = Math.max(0, Math.min(100, Number(element.dataset.progressWidth) || 0));
            element.style.setProperty('--expense-progress-width', `${percent}%`);
        });

        const expenseTypeSelect = document.getElementById('expense_type');
        const categorySelect = document.getElementById('category_id');
        const repeatCycleArea = document.getElementById('repeatCycleArea');
        const repeatCycleSelect = document.getElementById('repeat_cycle');
        const repeatYnInput = document.getElementById('repeat_yn');
        const fixedYnInput = document.getElementById('fixed_yn');

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
            // 자동 반복 행의 화면 키와 실제 DB 지출 ID를 분리한다.
            const expense_id = row.dataset.sourceExpenseId;
            const typeSelect = row.querySelector('.edit-expense-type');
            const editRepeatCycleSelect = row.querySelector('.edit-repeat-cycle');
            const editRepeatYnInput = document.getElementById(`editRepeatYn${expense_id}`);
            const editFixedYnInput = document.getElementById(`editFixedYn${expense_id}`);

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

    const typeFilter = document.getElementById('expenseTypeFilter');
    const categoryFilter = document.getElementById('expenseCategoryFilter');
    const amountSort = document.getElementById('expenseAmountSort');
    const expenseRows = Array.from(document.querySelectorAll('.expense-row'));
    const expenseTbody = document.getElementById('expenseRows');
    const categoryFilterMasterList = categoryFilter
        ? Array.from(categoryFilter.querySelectorAll('option[data-type]')).map(option => ({
            value: option.value,
            type: (option.dataset.type || '').trim(),
            text: option.textContent.trim()
        }))
        : [];

    // 목록의 분류를 선택하면 해당 분류에 속한 카테고리만 필터 선택창에 표시한다.
    function syncExpenseCategoryFilter() {
        if (!typeFilter || !categoryFilter) {
            return;
        }

        const selectedType = typeFilter.value;
        const previousCategory = categoryFilter.value;
        const visibleCategories = selectedType
            ? categoryFilterMasterList.filter(category => category.type === selectedType)
            : categoryFilterMasterList;

        categoryFilter.innerHTML = '';

        const allOption = document.createElement('option');
        allOption.value = '';
        allOption.textContent = selectedType ? '해당 분류 전체 카테고리' : '전체 카테고리';
        categoryFilter.appendChild(allOption);

        visibleCategories.forEach(function (category) {
            const option = document.createElement('option');
            option.value = category.value;
            option.dataset.type = category.type;
            option.textContent = category.text;
            categoryFilter.appendChild(option);
        });

        const canKeepPreviousCategory = visibleCategories.some(
            category => category.value === previousCategory
        );
        categoryFilter.value = canKeepPreviousCategory ? previousCategory : '';
    }

    function filterExpenseRows() {
        const selectedType = typeFilter.value;
        const selectedCategory = categoryFilter.value;
        const selectedSort = amountSort.value;

        const sortedRows = [...expenseRows].sort((a, b) => {
            if (selectedSort === 'DESC') {
                return Number(b.dataset.amount) - Number(a.dataset.amount);
            }

            if (selectedSort === 'ASC') {
                return Number(a.dataset.amount) - Number(b.dataset.amount);
            }

            return Number(a.dataset.order) - Number(b.dataset.order);
        });

        sortedRows.forEach(row => {
            const typeMatch =
                !selectedType || row.dataset.type === selectedType;

            const categoryMatch =
                !selectedCategory || row.dataset.category === selectedCategory;

            row.style.display =
                typeMatch && categoryMatch ? '' : 'none';

            expenseTbody.appendChild(row);
        });

        // sortedRows는 이 함수 안에서 만든 지역변수이므로 빈 목록 계산도 함수 안에서 처리한다.
        const visibleCount = sortedRows.filter(row => row.style.display !== 'none').length;
        const emptyMessage = document.getElementById('expenseFilterEmpty');

        if (emptyMessage) {
            // CSS에서 초기 숨김 처리했으므로 class를 토글해 필터 결과가 없을 때만 표시한다.
            emptyMessage.classList.toggle('expense-hidden', visibleCount !== 0);
        }
    }

    if (typeFilter) {
        typeFilter.addEventListener('change', function () {
            syncExpenseCategoryFilter();
            filterExpenseRows();
        });
    }

    [categoryFilter, amountSort].forEach(filter => {
        if (filter) {
            filter.addEventListener('change', filterExpenseRows);
        }
    });

    // 최초 진입 시에도 분류에 맞는 카테고리와 현재 필터 조건을 적용한다.
    if (typeFilter && categoryFilter && amountSort && expenseTbody) {
        syncExpenseCategoryFilter();
        filterExpenseRows();
    }
