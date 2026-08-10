package com.way2jobs.notification.service;
import com.way2jobs.notification.dto.NotificationResponse; import org.springframework.data.domain.Page; import java.time.LocalDateTime;
public interface NotificationService { Page<NotificationResponse> getNotifications(int page,int size); long unreadCount(LocalDateTime since); }
