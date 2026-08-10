package com.way2jobs.notification.repository;
import com.way2jobs.notification.entity.Notification; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.time.LocalDateTime;
public interface NotificationRepository extends JpaRepository<Notification,Long>{Page<Notification> findAllByOrderByCreatedAtDesc(Pageable p); long countByCreatedAtAfter(LocalDateTime since);}
