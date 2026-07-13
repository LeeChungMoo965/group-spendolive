package com.example.spendolive.chatbot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.spendolive.chatbot.domain.ChatbotAnswerDTO;
import com.example.spendolive.chatbot.domain.ChatbotAskDTO;
import com.example.spendolive.chatbot.service.ChatbotService;

/*
 * 챗봇 위젯(chatbot.js)이 fetch()로 호출하는 API의 진입점
 * @RestController = 여기서 리턴하는 객체를 JSP 화면이 아니라 JSON으로 바로 응답함
 */
@RestController
@RequestMapping("/spendolive/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * POST /spendolive/chatbot/ask.do
     * 요청 body 예시: { "question": "정산은 언제 되나요?" }
     * 응답 예시: { "matched": true, "answer": "...", "faqId": 12, "category": "expense" }
     *
     * 컨트롤러는 요청을 받아서 서비스에 그대로 넘기고, 서비스가 만든 결과를 그대로 돌려줄 뿐
     * → 실제 "생각하는" 로직은 전부 ChatbotServiceImpl에 있음 (관심사 분리)
     */
    @PostMapping("/ask.do")
    @ResponseBody
    public ChatbotAnswerDTO ask(@RequestBody ChatbotAskDTO request) {
        return chatbotService.findAnswer(request.getQuestion());
    }
}