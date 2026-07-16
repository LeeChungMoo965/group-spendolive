package com.example.spendolive.faq.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.spendolive.faq.domain.FaqVO;
import com.example.spendolive.faq.repository.FaqRepository;

@Service
public class FaqServiceImpl implements FaqService {

    // faqList.jsp에 보여줄 카테고리 고정 순서. 여기 없는 카테고리 값이 들어오면 그냥 안 보임(방어적으로 무시).
    private static final String[] CATEGORY_ORDER = {"account", "expense", "ott", "notice", "etc"};

    private final FaqRepository faqRepository;

    public FaqServiceImpl(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @Override
    public Map<String, List<FaqVO>> getVisibleFaqGroupedByCategory() {
        return groupByCategory(faqRepository.findAllVisible());
    }

    @Override
    public List<FaqVO> getAllFaqList() {
        return faqRepository.findAll();
    }

    @Override
    public Map<String, List<FaqVO>> getAllFaqGroupedByCategory() {
        return groupByCategory(faqRepository.findAll()); // 숨김(N) 포함 전체
    }

    @Override
    public FaqVO getFaqDetail(int faq_id) {
        return faqRepository.findById(faq_id);
    }

    @Override
    public int insertFaq(FaqVO faq) {
        return faqRepository.insertFaq(faq);
    }

    @Override
    public void updateFaq(FaqVO faq) {
        faqRepository.updateFaq(faq);
    }

    @Override
    public void deleteFaq(int faq_id) {
        faqRepository.deleteFaq(faq_id);
    }

    @Override
    public int getNextSortOrder(String category) {
        return faqRepository.getNextSortOrder(category);
    }

    @Override
    public void moveFaqUp(int faq_id) {
        moveFaq(faq_id, true);
    }

    @Override
    public void moveFaqDown(int faq_id) {
        moveFaq(faq_id, false);
    }

    /* FAQ 리스트를 CATEGORY_ORDER 순서대로 그룹핑.
       항목이 하나도 없는 카테고리는 아예 map에 안 넣음
       → jsp에서 그 카테고리 섹션 자체가 안 뜨고, 필터 버튼 눌러도 "등록된 FAQ 없음"으로 자연스럽게 처리됨 */
    private Map<String, List<FaqVO>> groupByCategory(List<FaqVO> all) {
        Map<String, List<FaqVO>> grouped = new LinkedHashMap<>();
        for (String cat : CATEGORY_ORDER) {
            List<FaqVO> inCat = all.stream()
                    .filter(f -> cat.equals(f.getCategory()))
                    .toList();
            if (!inCat.isEmpty()) {
                grouped.put(cat, inCat);
            }
        }
        return grouped;
    }

    /* 같은 카테고리 안에서 바로 이웃한 항목과 순서를 바꿈.
       클릭할 때마다 그 카테고리 전체를 0,1,2...로 먼저 정규화해서
       예전에 값이 겹치거나 건너뛴 데이터가 있어도 자동으로 깨끗해지게 함 */
    private void moveFaq(int faq_id, boolean up) {
        List<FaqVO> all = faqRepository.findAll(); // 카테고리 고정순서 → sort_order asc → faq_id asc

        String category = all.stream()
                .filter(f -> f.getFaqId() == faq_id)
                .map(FaqVO::getCategory)
                .findFirst()
                .orElse(null);
        if (category == null) return;

        List<FaqVO> sameCat = all.stream()
                .filter(f -> category.equals(f.getCategory()))
                .toList();

        int idx = -1;
        for (int i = 0; i < sameCat.size(); i++) {
            if (sameCat.get(i).getFaqId() == faq_id) { idx = i; break; }
        }
        if (idx < 0) return;

        int neighborIdx = up ? idx - 1 : idx + 1;
        if (neighborIdx < 0 || neighborIdx >= sameCat.size()) return; // 카테고리 맨 위/맨 아래면 무시

        // 1) 카테고리 내 전체를 현재 화면 순서 그대로 0,1,2...로 정규화
        for (int i = 0; i < sameCat.size(); i++) {
            faqRepository.updateSortOrder(sameCat.get(i).getFaqId(), i);
        }
        // 2) 정규화된 값 기준으로 두 항목만 swap
        faqRepository.updateSortOrder(sameCat.get(idx).getFaqId(), neighborIdx);
        faqRepository.updateSortOrder(sameCat.get(neighborIdx).getFaqId(), idx);
    }
}