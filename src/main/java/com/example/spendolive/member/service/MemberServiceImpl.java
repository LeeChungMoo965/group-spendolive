package com.example.spendolive.member.service;

import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;

import jakarta.mail.internet.MimeMessage;
@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
	private MemberRepository memberRepository;
    @Autowired
    private JavaMailSender mailSender;
    
    @Override
    public MemberVO login(Map loginMap) throws Exception {
        
        return memberRepository.login(loginMap);
    }

    @Override
    public void addMember(MemberVO memberVO) throws Exception {
        memberRepository.insertNewMember(memberVO);
	}

    @Override
    public String overlapped(String id) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'overlapped'");
    }

    @Override
    public String sendVerificationEmail(String toEmail) throws Exception {
        String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));
        
        String title = "[SpendOlive] 회원가입 이메일 인증번호입니다.";
        String content = "<h3>안녕하세요. SpendOlive입니다.</h3>"
                    + "<p>회원가입 화면에서 아래의 인증번호를 입력해 주세요.</p>"
                    + "<h2 style='color: #4CAF50;'>" + verificationCode + "</h2>"
                    + "<p>감사합니다.</p>";

        // 포털 사이트의 기기 보안 차단을 원천 봉쇄하는 개발자 전용 발송 서버 세팅
        org.springframework.mail.javamail.JavaMailSenderImpl customSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
        customSender.setHost("sandbox.smtp.mailtrap.io"); // ◀ 메일트랩 호스트
        customSender.setPort(2525); // ◀ 보안 충돌 없는 포트
        customSender.setUsername("e05077ac67ba67"); // ◀ 임시 공용 테스트 아이디
        customSender.setPassword("a9a2d54c235102"); // ◀ 임시 공용 테스트 비밀번호

        java.util.Properties props = customSender.getJavaMailProperties();
// ◀ [이 줄을 반드시 추가!] 윈도우 한글 이름으로 인한 501 에러를 원천 차단합니다.
        props.put("mail.smtp.localhost", "127.0.0.1"); 
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); 
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        try {
            MimeMessage message = customSender.createMimeMessage(); 
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("admin@spendolive.com");
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(content, true);

            // 실제 발송 전송 로직 작동
            customSender.send(message); 
            System.out.println("★ SMTP 이메일 발송 최종 성공! 인증번호: " + verificationCode);
            
            return verificationCode;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.");
        }
    }
//추후에 시연 할 때 실계정 전환 후 시연 할 예정  ()
    @Override
    public String sendSmsVerification(String toNumber) throws Exception {
        // 1. 진짜 통신사 망을 탈 때와 똑같이 6자리 랜덤 인증번호 생성
        String verificationCode = String.valueOf(100000 + new Random().nextInt(900000));

        // 2. 외부 쿨에스엠에스 서버를 거치지 않고, 성공 패킷을 백엔드가 직접 조립
        try {
            System.out.println("=========================================");
            System.out.println("★ [CoolSMS 가상 시뮬레이터 작동 중]");
            System.out.println("★ 수신자 번호: " + toNumber);
            System.out.println("★ 발송 메세지: [SpendOlive] 가입 인증번호는 [" + verificationCode + "] 입니다.");
            System.out.println("★ 상태 코드: 200 (Success) - 가상 발송 완료");
            System.out.println("=========================================");
            
            // 3. 화면(프론트엔드)과 세션 검증 로직으로 인증번호를 그대로 리턴
            return verificationCode;
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("문자 가상 전송 중 오류 발생");
        }
    }
    
}
