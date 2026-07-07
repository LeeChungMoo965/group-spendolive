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
        List<FaqVO> all = faqRepository.findAllVisible();

        Map<String, List<FaqVO>> grouped = new LinkedHashMap<>();
        for (String cat : CATEGORY_ORDER) {
            List<FaqVO> inCat = all.stream()
                    .filter(f -> cat.equals(f.getCategory()))
                    .toList();
            // 항목이 하나도 없는 카테고리는 아예 map에 안 넣음
            // → faqList.jsp에서 그 카테고리 섹션 자체가 안 뜨고, 필터 버튼 눌러도 "등록된 FAQ 없음"으로 자연스럽게 처리됨
            if (!inCat.isEmpty()) {
                grouped.put(cat, inCat);
            }
        }
        return grouped;
    }

    @Override
    public List<FaqVO> getAllFaqList() {
        return faqRepository.findAll();
    }

    @Override
    public FaqVO getFaqDetail(int faqId) {
        return faqRepository.findById(faqId);
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
    public void deleteFaq(int faqId) {
        faqRepository.deleteFaq(faqId);
    }
}
