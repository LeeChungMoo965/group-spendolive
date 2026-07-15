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

    public int getUnread_count(String id) {
        return notificationRepository.countUnread(id);
    }

    public void readNotification(int notification_id, String id) {
        notificationRepository.updateReadYn(notification_id, id);
    }

    public void toggleStar(int notification_id, String id) {
        notificationRepository.toggleStar(notification_id, id);
    }

    public NotificationDTO getNotificationDetail(int notification_id, String id) {
        return notificationRepository.findByNotificationId(notification_id, id);
    }
}