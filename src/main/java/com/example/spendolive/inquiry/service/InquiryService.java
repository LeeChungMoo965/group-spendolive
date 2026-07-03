package com.example.spendolive.inquiry.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.inquiry.domain.InquiryVO;
import com.example.spendolive.inquiry.repository.InquiryRepository;

@Service
public class InquiryService {

    private static final int PAGE_SIZE = 5;
    private static final int PAGINATION_THRESHOLD = 10; // 이 개수 이하면 페이지네이션 없이 전부 표시

    private final InquiryRepository inquiryRepository;

    public InquiryService(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    public void writeInquiry(InquiryVO inquiry) {
        inquiryRepository.insertInquiry(inquiry);
    }

    public List<InquiryVO> getMyInquiryList(String id, int page) {
        int totalCount = inquiryRepository.countByMemberId(id);
        if (totalCount == 0) {
            return Collections.emptyList();
        }
        if (totalCount <= PAGINATION_THRESHOLD) {
            // 10개 이하면 페이지 나누지 않고 전부 반환
            return inquiryRepository.findByMemberId(id, 0, totalCount);
        }
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;
        return inquiryRepository.findByMemberId(id, offset, PAGE_SIZE);
    }

    public int getMyInquiryTotalPages(String id) {
        int totalCount = inquiryRepository.countByMemberId(id);
        if (totalCount <= PAGINATION_THRESHOLD) {
            return 1; // 10개 이하면 페이지네이션 UI 자체를 숨김 (inquiryList.jsp의 totalPages > 1 조건)
        }
        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    public InquiryVO getInquiryDetail(int inquiryId) {
        return inquiryRepository.findById(inquiryId);
    }
}