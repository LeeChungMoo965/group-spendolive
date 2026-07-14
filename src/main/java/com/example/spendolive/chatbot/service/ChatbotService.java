package com.example.spendolive.chatbot.service;

import com.example.spendolive.chatbot.domain.ChatbotAnswerDTO;

/*
 * 챗봇의 "답변을 찾는다"는 동작 하나만 정의한 인터페이스
 * Controller는 이 인터페이스만 보고 호출하고, 실제 구현(ChatbotServiceImpl)은 몰라도 됨
 * → 나중에 매칭 로직을 완전히 다른 방식(예: AI API 연동)으로 바꿔도 Controller 코드는 안 건드려도 됨
 */
public interface ChatbotService {
    ChatbotAnswerDTO findAnswer(String question);
}