package com.example.spendolive.report.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.report.domain.ReportVO;
import com.example.spendolive.report.domain.WarningVO;
import com.example.spendolive.report.exception.ReportProcessException;
import com.example.spendolive.report.repository.ReportRepository;
import java.util.List;
@Service
public class ReportServiceImpl implements ReportService{
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Override
    @Transactional
    public void insertReport( String reported_member_id,String room_id,String chat_text, MemberVO memberInfo) throws Exception{
        ReportVO reportInfo =new ReportVO();
        String reporter = memberInfo.getId();
        int parsed_room_id = Integer.parseInt(room_id);  
        try{
        reportInfo.setReport_reason(chat_text);
        reportInfo.setReported_member_id(reported_member_id);
        reportInfo.setReporter_id(reporter);
        reportInfo.setRoom_id(parsed_room_id);
        reportRepository.insertReport(reportInfo);
        }catch(Exception e){
            throw new ReportProcessException("REPORT_FAILED", "이미 신고가 완료된 건 입니다.");
        }
    }
    @Override
    @Transactional
    public List<ReportVO> selectReport(String status) throws Exception{
        return reportRepository.selectReport(status);
    }
    @Override
    @Transactional
    public List<ReportVO> selectReportAll() throws Exception{
        return reportRepository.selectReportAll();
    }
    @Override
    @Transactional
    public void updateComment(String comment, int report_id) throws Exception{
        reportRepository.updateComment(comment, report_id);
    }
    @Override
    @Transactional
    public void insertWarning(String comment,String userId,String reportIdstr,String result) throws Exception{
        int report_id = Integer.parseInt(reportIdstr);
        MemberVO user = memberRepository.selectMemberById(userId);
        int count = user.getWarning_count();
        if(result.equals("1")){
            System.err.println("1");
        WarningVO wVo = new WarningVO();
        wVo.setMember_id(userId);
        wVo.setWarning_reason(comment);
        wVo.setStatus("Y");
        wVo.setReport_id(report_id);
        try{
        reportRepository.insertWarning(wVo);
        reportRepository.updateComment(comment, report_id);
        memberRepository.updateWarning(userId,count);
        }catch(Exception e){
            throw new ReportProcessException("REPORT_FAILED", "경고 처리 중 문제가 생겼습니다. 다시 시도 해주세요");
        }
        }else if(result.equals("2")){
            //퇴출
            System.err.println("2");
        }
    }
}
