package com.example.spendolive.member.service;

import com.example.spendolive.member.domain.MyPageDTO;

public interface MyPageService {
    MyPageDTO getMyPage(String loginId) throws Exception;
}
