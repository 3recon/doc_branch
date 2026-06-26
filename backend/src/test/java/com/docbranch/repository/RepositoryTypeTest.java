package com.docbranch.repository;

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
import com.docbranch.repository.activity.ActivityLogRepository;
import com.docbranch.repository.document.DocumentDetailPermissionRepository;
import com.docbranch.repository.document.DocumentDetailRepository;
import com.docbranch.repository.document.DocumentFileRepository;
import com.docbranch.repository.document.DocumentVersionRelationRepository;
import com.docbranch.repository.document.DocumentVersionRepository;
import com.docbranch.repository.notification.NotificationChannelRepository;
import com.docbranch.repository.notification.NotificationEventRepository;
import com.docbranch.repository.project.ProjectInvitationRepository;
import com.docbranch.repository.project.ProjectMemberRepository;
import com.docbranch.repository.project.ProjectRepository;
import com.docbranch.repository.trash.TrashItemRepository;
import com.docbranch.repository.user.SocialAccountRepository;
import com.docbranch.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTypeTest {

    @Test
    void mvp1RepositoriesExtendJpaRepository() {
        Map<Class<?>, Class<?>> repositories = Map.ofEntries(
                Map.entry(UserRepository.class, User.class),
                Map.entry(SocialAccountRepository.class, SocialAccount.class),
                Map.entry(ProjectRepository.class, Project.class),
                Map.entry(ProjectMemberRepository.class, ProjectMember.class),
                Map.entry(ProjectInvitationRepository.class, ProjectInvitation.class),
                Map.entry(DocumentDetailRepository.class, DocumentDetail.class),
                Map.entry(DocumentDetailPermissionRepository.class, DocumentDetailPermission.class),
                Map.entry(DocumentVersionRepository.class, DocumentVersion.class),
                Map.entry(DocumentFileRepository.class, DocumentFile.class),
                Map.entry(DocumentVersionRelationRepository.class, DocumentVersionRelation.class),
                Map.entry(ActivityLogRepository.class, ActivityLog.class),
                Map.entry(NotificationChannelRepository.class, NotificationChannel.class),
                Map.entry(NotificationEventRepository.class, NotificationEvent.class),
                Map.entry(TrashItemRepository.class, TrashItem.class)
        );

        repositories.forEach((repositoryType, entityType) -> {
            assertThat(JpaRepository.class).isAssignableFrom(repositoryType);
            assertThat(repositoryType.getGenericInterfaces()[0].getTypeName())
                    .contains(entityType.getName())
                    .contains("java.util.UUID");
        });
    }
}
