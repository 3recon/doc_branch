package com.docbranch.domain;

import com.docbranch.domain.activity.ActivityLog;
import com.docbranch.domain.document.DocumentDetail;
import com.docbranch.domain.document.DocumentDetailPermission;
import com.docbranch.domain.document.DocumentFile;
import com.docbranch.domain.document.DocumentVersion;
import com.docbranch.domain.document.DocumentVersionRelation;
import com.docbranch.domain.notification.NotificationChannel;
import com.docbranch.domain.notification.NotificationEvent;
import com.docbranch.domain.project.Project;
import com.docbranch.domain.project.ProjectInvitation;
import com.docbranch.domain.project.ProjectMember;
import com.docbranch.domain.trash.TrashItem;
import com.docbranch.domain.user.SocialAccount;
import com.docbranch.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMappingTest {

    @Test
    void mvp1EntitiesUsePhysicalModelTableNames() {
        Map<Class<?>, String> entities = Map.ofEntries(
                Map.entry(User.class, "users"),
                Map.entry(SocialAccount.class, "social_accounts"),
                Map.entry(Project.class, "projects"),
                Map.entry(ProjectMember.class, "project_members"),
                Map.entry(ProjectInvitation.class, "project_invitations"),
                Map.entry(DocumentDetail.class, "document_details"),
                Map.entry(DocumentDetailPermission.class, "document_detail_permissions"),
                Map.entry(DocumentVersion.class, "document_versions"),
                Map.entry(DocumentFile.class, "document_files"),
                Map.entry(DocumentVersionRelation.class, "document_version_relations"),
                Map.entry(ActivityLog.class, "activity_logs"),
                Map.entry(NotificationChannel.class, "notification_channels"),
                Map.entry(NotificationEvent.class, "notification_events"),
                Map.entry(TrashItem.class, "trash_items")
        );

        entities.forEach((entityType, tableName) -> {
            assertThat(entityType.isAnnotationPresent(Entity.class)).isTrue();
            assertThat(entityType.getAnnotation(Table.class).name()).isEqualTo(tableName);
        });
    }
}
