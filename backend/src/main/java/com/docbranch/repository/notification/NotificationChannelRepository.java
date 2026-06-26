package com.docbranch.repository.notification;

import com.docbranch.domain.notification.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, UUID> {
}
