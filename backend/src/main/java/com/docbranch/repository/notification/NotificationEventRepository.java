package com.docbranch.repository.notification;

import com.docbranch.domain.notification.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {
}
