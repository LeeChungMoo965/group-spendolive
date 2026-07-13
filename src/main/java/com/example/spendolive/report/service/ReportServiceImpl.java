package com.example.spendolive.report.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.member.repository.MemberRepository;
import com.example.spendolive.member.service.MemberService;
import com.example.spendolive.report.domain.ReportVO;
import com.example.spendolive.report.domain.WarningVO;
import com.example.spendolive.report.repository.ReportRepository;

import java.lang.reflect.Member;
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
        int roomId = Integer.parseInt(room_id);  
        reportInfo.setReport_reason(chat_text);
        reportInfo.setReported_member_id(reported_member_id);
        reportInfo.setReporter_id(reporter);
        reportInfo.setRoom_id(roomId);
        reportRepository.insertReport(reportInfo);
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
        int reportId = Integer.parseInt(reportIdstr);
        MemberVO user = memberRepository.selectMemberById(userId);
        int count = user.getWarning_count();
        if(result.equals("1")){
            System.err.println("1");
        WarningVO wVo = new WarningVO();
        wVo.setMember_id(userId);
        wVo.setWarning_reason(comment);
        wVo.setStatus("Y");
        wVo.setReport_id(reportId);
        try{
        reportRepository.insertWarning(wVo);
        reportRepository.updateComment(comment, reportId);
        memberRepository.updateWarning(userId,count);
        }catch(Exception e){
            System.err.println("🚨 [시스템 에러]: " + e.getMessage());
        }
        }else if(result.equals("2")){
            //퇴출
            System.err.println("2");
        }
    }
}
