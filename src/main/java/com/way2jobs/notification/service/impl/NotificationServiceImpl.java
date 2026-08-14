package com.way2jobs.notification.service.impl;

import com.way2jobs.notification.dto.NotificationResponse;
import com.way2jobs.notification.entity.Notification;
import com.way2jobs.notification.repository.NotificationRepository;
import com.way2jobs.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository repository;


    // ============================================================
    // GET NOTIFICATIONS
    // ============================================================

    @Override
    public Page<NotificationResponse> getNotifications(
            int page,
            int size
    ) {

        return repository
                .findAllByOrderByCreatedAtDesc(
                        PageRequest.of(
                                Math.max(page, 0),
                                Math.max(size, 1)
                        )
                )
                .map(this::map);
    }


    // ============================================================
    // UNREAD COUNT
    // ============================================================

    @Override
    public long unreadCount(
            LocalDateTime since
    ) {

        return repository.countByCreatedAtAfter(
                since
        );
    }


    // ============================================================
    // CREATE NOTIFICATION
    // ============================================================

    @Override
    @Transactional
    public void createNotification(
            String title,
            String body,
            String type
    ) {

        if (title == null
                || title.isBlank()) {

            throw new IllegalArgumentException(
                    "Notification title cannot be empty."
            );
        }

        Notification notification =
                Notification.builder()
                        .title(title.trim())
                        .body(
                                body == null
                                        ? null
                                        : body.trim()
                        )
                        .type(
                                type == null
                                        ? null
                                        : type.trim()
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        repository.save(notification);
    }


    // ============================================================
    // RESPONSE MAPPER
    // ============================================================

    private NotificationResponse map(
            Notification notification
    ) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .jobId(
                        notification.getJob() == null
                                ? null
                                : notification.getJob().getId()
                )
                .type(notification.getType())
                .createdAt(
                        notification.getCreatedAt()
                )
                .build();
    }
}