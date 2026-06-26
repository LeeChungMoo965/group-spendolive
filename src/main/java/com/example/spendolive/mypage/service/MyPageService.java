package com.example.spendolive.mypage.service;

import com.example.spendolive.mypage.domain.MyPageDTO;

public interface MyPageService {
    MyPageDTO getMyPage(String loginId) throws Exception;
}
