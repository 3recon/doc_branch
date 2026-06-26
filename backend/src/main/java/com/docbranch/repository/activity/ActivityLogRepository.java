package com.docbranch.repository.activity;

import com.docbranch.domain.activity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
}
