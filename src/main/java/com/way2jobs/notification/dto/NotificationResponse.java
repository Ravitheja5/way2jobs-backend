package com.way2jobs.notification.dto;
import lombok.*; import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class NotificationResponse {private Long id,jobId; private String title,body,type; private LocalDateTime createdAt;}
