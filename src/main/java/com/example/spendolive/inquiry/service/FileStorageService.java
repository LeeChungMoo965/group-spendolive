package com.example.spendolive.inquiry.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spendolive.inquiry.domain.InquiryFileVO;

/*
 * 문의 첨부파일을 실제 디스크에 저장하는 서비스.
 * 저장 경로는 application.properties의 app.upload.inquiry-dir 로 설정 가능
 * (기본값: 프로젝트 루트 기준 uploads/inquiry)
 *
 * 예) application.properties
 *   app.upload.inquiry-dir=/data/spendolive/uploads/inquiry
 *   spring.servlet.multipart.max-file-size=5MB
 *   spring.servlet.multipart.max-request-size=15MB
 */
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXT = List.of("png", "jpg", "jpeg", "gif", "pdf");
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB, inquiryWrite.jsp 안내 문구와 동일
    private static final int MAX_FILE_COUNT = 3;                // inquiryWrite.jsp 안내 문구와 동일

    private final String uploadDir;

    public FileStorageService(@Value("${app.upload.inquiry-dir:uploads/inquiry}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    /**
     * 첨부파일들을 디스크에 저장하고, DB insert에 쓸 InquiryFileVO 목록을 만들어 반환한다.
     * DB insert 자체는 호출부(InquiryService)에서 트랜잭션 안에서 처리한다.
     *
     * @param inquiryId  이미 생성된 문의 번호 (inquiry_tb PK)
     * @param attachments 폼에서 넘어온 첨부파일 배열 (null/빈 파일 섞여 있어도 됨)
     */
    public List<InquiryFileVO> storeFiles(int inquiryId, MultipartFile[] attachments) {
        List<InquiryFileVO> result = new ArrayList<>();
        if (attachments == null || attachments.length == 0) {
            return result;
        }

        List<MultipartFile> validFiles = new ArrayList<>();
        for (MultipartFile file : attachments) {
            if (file != null && !file.isEmpty()) {
                validFiles.add(file);
            }
        }
        if (validFiles.isEmpty()) {
            return result;
        }
        if (validFiles.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("첨부파일은 최대 " + MAX_FILE_COUNT + "개까지 업로드할 수 있습니다.");
        }

        // 저장 전에 전체 파일 먼저 검증 (하나라도 규격 위반이면 아무 것도 저장하지 않음)
        for (MultipartFile file : validFiles) {
            validateFile(file);
        }

        try {
            Path targetDir = Path.of(uploadDir, String.valueOf(inquiryId));
            Files.createDirectories(targetDir);

            for (MultipartFile file : validFiles) {
                String originName = file.getOriginalFilename();
                String ext = extractExtension(originName);
                String savedName = UUID.randomUUID() + "." + ext;

                Path targetPath = targetDir.resolve(savedName);
                file.transferTo(targetPath);

                InquiryFileVO fileVO = new InquiryFileVO();
                fileVO.setInquiryId(inquiryId);
                fileVO.setOriginName(originName);
                fileVO.setSavedName(savedName);
                fileVO.setFilePath(targetPath.toString());
                fileVO.setFileSize(file.getSize());
                result.add(fileVO);
            }
        } catch (IOException e) {
            throw new RuntimeException("첨부파일 저장 중 오류가 발생했습니다.", e);
        }

        return result;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 \"" + file.getOriginalFilename() + "\"의 용량이 5MB를 초과합니다.");
        }
        String ext = extractExtension(file.getOriginalFilename());
        if (ext.isEmpty() || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + file.getOriginalFilename());
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
