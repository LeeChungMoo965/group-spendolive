package com.example.spendolive.faq.service;

import java.util.List;
import java.util.Map;

import com.example.spendolive.faq.domain.FaqVO;

public interface FaqService {

    // 사용자 화면 (faqList.jsp) — 노출(use_yn='Y')인 것만, 카테고리별로 묶어서 반환
    Map<String, List<FaqVO>> getVisibleFaqGroupedByCategory();

    // 관리자 화면
    List<FaqVO> getAllFaqList();
    Map<String, List<FaqVO>> getAllFaqGroupedByCategory(); // 관리자 목록도 카테고리별로 묶어서 보여주기 위함(숨김 포함)
    FaqVO getFaqDetail(int faq_id);
    int insertFaq(FaqVO faq);
    void updateFaq(FaqVO faq);
    void deleteFaq(int faq_id);

    // 새 FAQ가 들어갈 다음 순서 (해당 카테고리 맨 뒤)
    int getNextSortOrder(String category);

    // 목록에서 ▲▼ 버튼으로 순서 바꾸기 (같은 카테고리 안에서만 이동)
    void moveFaqUp(int faq_id);
    void moveFaqDown(int faq_id);
}
