package com.example.spendolive.faq.service;

import java.util.List;
import java.util.Map;

import com.example.spendolive.faq.domain.FaqVO;

public interface FaqService {

    // 사용자 화면 (faqList.jsp) — 노출(use_yn='Y')인 것만, 카테고리별로 묶어서 반환
    Map<String, List<FaqVO>> getVisibleFaqGroupedByCategory();

    // 관리자 화면
    List<FaqVO> getAllFaqList();
    FaqVO getFaqDetail(int faq_id);
    int insertFaq(FaqVO faq);
    void updateFaq(FaqVO faq);
    void deleteFaq(int faq_id);
}
