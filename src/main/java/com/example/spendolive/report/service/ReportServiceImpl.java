package com.example.spendolive.report.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.report.domain.ReportVO;
import com.example.spendolive.report.repository.ReportRepository;
import java.util.List;
@Service
public class ReportServiceImpl implements ReportService{
    @Autowired
    private ReportRepository reportRepository;
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
    public List<ReportVO> selectReport() throws Exception{
        return reportRepository.selectReport();
    }
    @Override
    @Transactional
    public void updateComment(String comment, Long report_id) throws Exception{
        reportRepository.updateComment(comment, report_id);
    }
}
