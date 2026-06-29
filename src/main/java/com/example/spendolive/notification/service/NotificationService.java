package com.example.spendolive.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spendolive.notification.domain.NotificationDTO;
import org.springframework.dao.DataAccessException;
import com.example.spendolive.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationDTO> getNotificationList(String id) {
        return notificationRepository.findById(id);
    }

    public int getUnreadCount(String id) {
        return notificationRepository.countUnread(id);
    }

    public void readNotification(int notificationId, String id) {
        notificationRepository.updateReadYn(notificationId, id);
    }

    public void toggleStar(int notificationId, String id) {
        notificationRepository.toggleStar(notificationId, id);
    }

    public NotificationDTO getNotificationDetail(int notificationId, String id) {
        return notificationRepository.findByNotificationId(notificationId, id);
    }
}