package com.way2jobs.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.way2jobs.service.FirebaseMessagingService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class FirebaseMessagingServiceImpl implements FirebaseMessagingService {

    @Value("${firebase.service.account.path:}")
    private String serviceAccountPath;

    @Value("${firebase.topic:jobs}")
    private String topic;

    @PostConstruct
    public void initialize() {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.error("FCM is disabled: firebase.service.account.path is not configured. Bulk-imported jobs cannot send Android notifications.");
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase using the configured service account", e);
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }

    @Override
    public void sendNotification(String title, String body, Long jobId, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.error("FCM notification was not sent for job {} because Firebase is not initialized", jobId);
            return;
        }

        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setNotification(AndroidNotification.builder().build())
                        .build())
                .setTopic(topic)
                .build();

        try {
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("FCM notification sent for job {} to topic '{}' with message ID {}", jobId, topic, messageId);
        } catch (Exception e) {
            log.error("Failed to send FCM notification for job {} to topic '{}'", jobId, topic, e);
            throw new RuntimeException("Failed to send FCM notification", e);
        }
    }
}
