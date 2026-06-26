package com.docbranch.domain.document;

import com.docbranch.domain.project.ProjectMember;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "document_detail_permissions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentDetailPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_detail_permission_id", nullable = false)
    private UUID documentDetailPermissionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_detail_id", nullable = false)
    private DocumentDetail documentDetail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_member_id", nullable = false)
    private ProjectMember projectMember;

    @Column(name = "can_view", nullable = false)
    private boolean canView;

    @Column(name = "can_upload", nullable = false)
    private boolean canUpload;

    @Column(name = "can_download", nullable = false)
    private boolean canDownload;

    @Column(name = "can_manage", nullable = false)
    private boolean canManage;
}
