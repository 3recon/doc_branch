CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    profile_image_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    withdrawn_at TIMESTAMPTZ,
    CONSTRAINT users_email_uq UNIQUE (email),
    CONSTRAINT users_status_chk CHECK (status IN ('ACTIVE', 'WITHDRAWN'))
);

CREATE INDEX users_created_at_idx ON users (created_at);

CREATE TABLE social_accounts (
    social_account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT social_accounts_user_id_fk FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT social_accounts_provider_user_uq UNIQUE (provider, provider_user_id),
    CONSTRAINT social_accounts_provider_chk CHECK (provider IN ('GOOGLE', 'KAKAO', 'NAVER'))
);

CREATE INDEX social_accounts_user_id_idx ON social_accounts (user_id);

CREATE TABLE projects (
    project_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    owner_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    CONSTRAINT projects_owner_user_id_fk FOREIGN KEY (owner_user_id) REFERENCES users (user_id),
    CONSTRAINT projects_deleted_by_fk FOREIGN KEY (deleted_by) REFERENCES users (user_id),
    CONSTRAINT projects_status_chk CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'STOPPED'))
);

CREATE INDEX projects_name_idx ON projects (name);
CREATE INDEX projects_owner_user_id_idx ON projects (owner_user_id);
CREATE INDEX projects_created_at_idx ON projects (created_at);
CREATE INDEX projects_updated_at_idx ON projects (updated_at);
CREATE INDEX projects_deleted_at_idx ON projects (deleted_at);
CREATE INDEX projects_deleted_by_idx ON projects (deleted_by);

CREATE TABLE project_members (
    project_member_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'PARTICIPANT',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    removed_at TIMESTAMPTZ,
    CONSTRAINT project_members_project_id_fk FOREIGN KEY (project_id) REFERENCES projects (project_id),
    CONSTRAINT project_members_user_id_fk FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT project_members_project_user_uq UNIQUE (project_id, user_id),
    CONSTRAINT project_members_role_chk CHECK (role IN ('PROJECT_ADMIN', 'PARTICIPANT', 'READ_ONLY'))
);

CREATE INDEX project_members_project_id_idx ON project_members (project_id);
CREATE INDEX project_members_user_id_idx ON project_members (user_id);

CREATE TABLE project_invitations (
    invitation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    invited_email VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'PARTICIPANT',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT project_invitations_project_id_fk FOREIGN KEY (project_id) REFERENCES projects (project_id),
    CONSTRAINT project_invitations_role_chk CHECK (role IN ('PROJECT_ADMIN', 'PARTICIPANT', 'READ_ONLY')),
    CONSTRAINT project_invitations_status_chk CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED'))
);

CREATE INDEX project_invitations_project_id_idx ON project_invitations (project_id);
CREATE INDEX project_invitations_email_idx ON project_invitations (invited_email);
CREATE INDEX project_invitations_expires_at_idx ON project_invitations (expires_at);

CREATE TABLE document_details (
    document_detail_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    root_version_id UUID,
    final_version_id UUID,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    CONSTRAINT document_details_project_id_fk FOREIGN KEY (project_id) REFERENCES projects (project_id),
    CONSTRAINT document_details_created_by_fk FOREIGN KEY (created_by) REFERENCES users (user_id),
    CONSTRAINT document_details_deleted_by_fk FOREIGN KEY (deleted_by) REFERENCES users (user_id),
    CONSTRAINT document_details_root_version_uq UNIQUE (root_version_id),
    CONSTRAINT document_details_status_chk CHECK (status IN ('DRAFT', 'IN_REVIEW', 'COMPLETED'))
);

CREATE INDEX document_details_project_id_idx ON document_details (project_id);
CREATE INDEX document_details_name_idx ON document_details (name);
CREATE INDEX document_details_final_version_id_idx ON document_details (final_version_id);
CREATE INDEX document_details_created_by_idx ON document_details (created_by);
CREATE INDEX document_details_created_at_idx ON document_details (created_at);
CREATE INDEX document_details_updated_at_idx ON document_details (updated_at);
CREATE INDEX document_details_deleted_at_idx ON document_details (deleted_at);
CREATE INDEX document_details_deleted_by_idx ON document_details (deleted_by);
CREATE INDEX document_details_project_sort_idx ON document_details (project_id, status, updated_at, name);

CREATE TABLE document_detail_permissions (
    document_detail_permission_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_detail_id UUID NOT NULL,
    project_member_id UUID NOT NULL,
    can_view BOOLEAN NOT NULL DEFAULT FALSE,
    can_upload BOOLEAN NOT NULL DEFAULT FALSE,
    can_download BOOLEAN NOT NULL DEFAULT FALSE,
    can_manage BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT dd_permissions_detail_id_fk FOREIGN KEY (document_detail_id) REFERENCES document_details (document_detail_id),
    CONSTRAINT dd_permissions_member_id_fk FOREIGN KEY (project_member_id) REFERENCES project_members (project_member_id),
    CONSTRAINT dd_permissions_detail_member_uq UNIQUE (document_detail_id, project_member_id)
);

CREATE INDEX dd_permissions_detail_id_idx ON document_detail_permissions (document_detail_id);
CREATE INDEX dd_permissions_member_id_idx ON document_detail_permissions (project_member_id);

CREATE TABLE document_versions (
    document_version_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_detail_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    version_type VARCHAR(30) NOT NULL DEFAULT 'REVISION',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    change_note TEXT,
    uploaded_by UUID NOT NULL,
    last_modified_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ,
    deleted_by UUID,
    CONSTRAINT document_versions_detail_id_fk FOREIGN KEY (document_detail_id) REFERENCES document_details (document_detail_id),
    CONSTRAINT document_versions_uploaded_by_fk FOREIGN KEY (uploaded_by) REFERENCES users (user_id),
    CONSTRAINT document_versions_modified_by_fk FOREIGN KEY (last_modified_by) REFERENCES users (user_id),
    CONSTRAINT document_versions_deleted_by_fk FOREIGN KEY (deleted_by) REFERENCES users (user_id),
    CONSTRAINT document_versions_detail_number_uq UNIQUE (document_detail_id, version_number),
    CONSTRAINT document_versions_type_chk CHECK (version_type IN ('INITIAL', 'REVISION', 'BRANCHED', 'MERGED')),
    CONSTRAINT document_versions_status_chk CHECK (status IN ('DRAFT', 'IN_REVIEW', 'COMPLETED'))
);

CREATE INDEX document_versions_detail_id_idx ON document_versions (document_detail_id);
CREATE INDEX document_versions_uploaded_by_idx ON document_versions (uploaded_by);
CREATE INDEX document_versions_modified_by_idx ON document_versions (last_modified_by);
CREATE INDEX document_versions_created_at_idx ON document_versions (created_at);
CREATE INDEX document_versions_deleted_at_idx ON document_versions (deleted_at);
CREATE INDEX document_versions_deleted_by_idx ON document_versions (deleted_by);

ALTER TABLE document_details
    ADD CONSTRAINT document_details_root_version_id_fk
        FOREIGN KEY (root_version_id) REFERENCES document_versions (document_version_id),
    ADD CONSTRAINT document_details_final_version_id_fk
        FOREIGN KEY (final_version_id) REFERENCES document_versions (document_version_id);

CREATE TABLE document_files (
    document_file_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_version_id UUID NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_extension VARCHAR(20) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_key TEXT NOT NULL,
    checksum VARCHAR(128),
    upload_status VARCHAR(30) NOT NULL DEFAULT 'UPLOADING',
    upload_error_code VARCHAR(50),
    upload_error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT document_files_version_id_fk FOREIGN KEY (document_version_id) REFERENCES document_versions (document_version_id),
    CONSTRAINT document_files_version_id_uq UNIQUE (document_version_id),
    CONSTRAINT document_files_storage_key_uq UNIQUE (storage_key),
    CONSTRAINT document_files_extension_chk CHECK (file_extension IN ('HWP', 'HWPX', 'DOC', 'DOCX', 'PDF', 'XLS', 'XLSX', 'PPT', 'PPTX')),
    CONSTRAINT document_files_size_chk CHECK (file_size > 0),
    CONSTRAINT document_files_upload_status_chk CHECK (upload_status IN ('UPLOADING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX document_files_name_idx ON document_files (original_file_name);
CREATE INDEX document_files_checksum_idx ON document_files (checksum);
CREATE INDEX document_files_upload_error_idx ON document_files (upload_error_code);

CREATE TABLE document_version_relations (
    relation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_version_id UUID NOT NULL,
    child_version_id UUID NOT NULL,
    relation_type VARCHAR(30) NOT NULL DEFAULT 'REVISION',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT dv_relations_parent_fk FOREIGN KEY (parent_version_id) REFERENCES document_versions (document_version_id),
    CONSTRAINT dv_relations_child_fk FOREIGN KEY (child_version_id) REFERENCES document_versions (document_version_id),
    CONSTRAINT dv_relations_parent_child_uq UNIQUE (parent_version_id, child_version_id),
    CONSTRAINT dv_relations_type_chk CHECK (relation_type IN ('REVISION', 'BRANCH', 'MERGE'))
);

CREATE INDEX dv_relations_parent_idx ON document_version_relations (parent_version_id);
CREATE INDEX dv_relations_child_idx ON document_version_relations (child_version_id);

CREATE TABLE activity_logs (
    activity_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    document_detail_id UUID,
    document_version_id UUID,
    actor_user_id UUID NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    target_type VARCHAR(30) NOT NULL,
    target_name VARCHAR(255),
    detail_json JSONB,
    CONSTRAINT activity_logs_project_id_fk FOREIGN KEY (project_id) REFERENCES projects (project_id),
    CONSTRAINT activity_logs_detail_id_fk FOREIGN KEY (document_detail_id) REFERENCES document_details (document_detail_id),
    CONSTRAINT activity_logs_version_id_fk FOREIGN KEY (document_version_id) REFERENCES document_versions (document_version_id),
    CONSTRAINT activity_logs_actor_id_fk FOREIGN KEY (actor_user_id) REFERENCES users (user_id),
    CONSTRAINT activity_logs_action_type_chk CHECK (action_type IN ('UPLOAD', 'BRANCH', 'MERGE', 'FINALIZE', 'DELETE', 'RESTORE')),
    CONSTRAINT activity_logs_target_type_chk CHECK (target_type IN ('PROJECT', 'DOCUMENT_DETAIL', 'DOCUMENT_VERSION'))
);

CREATE INDEX activity_logs_project_created_idx ON activity_logs (project_id);
CREATE INDEX activity_logs_detail_id_idx ON activity_logs (document_detail_id);
CREATE INDEX activity_logs_version_id_idx ON activity_logs (document_version_id);
CREATE INDEX activity_logs_actor_id_idx ON activity_logs (actor_user_id);
CREATE INDEX activity_logs_created_at_idx ON activity_logs (created_at);
CREATE INDEX activity_logs_target_name_idx ON activity_logs (target_name);
CREATE INDEX activity_logs_project_filter_idx ON activity_logs (project_id, document_detail_id, actor_user_id, action_type, created_at);

CREATE TABLE notification_channels (
    notification_channel_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    channel_type VARCHAR(30) NOT NULL,
    webhook_url TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT notification_channels_project_id_fk FOREIGN KEY (project_id) REFERENCES projects (project_id),
    CONSTRAINT notification_channels_created_by_fk FOREIGN KEY (created_by) REFERENCES users (user_id),
    CONSTRAINT notification_channels_type_chk CHECK (channel_type IN ('SLACK', 'DISCORD'))
);

CREATE INDEX notification_channels_project_id_idx ON notification_channels (project_id);
CREATE INDEX notification_channels_active_idx ON notification_channels (is_active);
CREATE INDEX notification_channels_created_by_idx ON notification_channels (created_by);

CREATE TABLE notification_events (
    notification_event_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_channel_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMPTZ,
    project_id UUID NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    failure_reason TEXT,
    CONSTRAINT notification_events_channel_id_fk FOREIGN KEY (notification_channel_id) REFERENCES notification_channels (notification_channel_id),
    CONSTRAINT notification_events_project_id_fk FOREIGN KEY (project_id) REFERENCES projects (project_id),
    CONSTRAINT notification_events_event_type_chk CHECK (event_type IN ('VERSION_UPLOADED', 'FINAL_VERSION_SET')),
    CONSTRAINT notification_events_status_chk CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    CONSTRAINT notification_events_target_type_chk CHECK (target_type IN ('DOCUMENT_VERSION', 'DOCUMENT_DETAIL'))
);

CREATE INDEX notification_events_channel_id_idx ON notification_events (notification_channel_id);
CREATE INDEX notification_events_project_id_idx ON notification_events (project_id);
CREATE INDEX notification_events_target_idx ON notification_events (target_id);

CREATE TABLE trash_items (
    trash_item_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    deleted_by UUID NOT NULL,
    deleted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    restored_at TIMESTAMPTZ,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT trash_items_project_id_fk FOREIGN KEY (project_id) REFERENCES projects (project_id),
    CONSTRAINT trash_items_deleted_by_fk FOREIGN KEY (deleted_by) REFERENCES users (user_id),
    CONSTRAINT trash_items_target_type_chk CHECK (target_type IN ('PROJECT', 'DOCUMENT_DETAIL', 'DOCUMENT_VERSION'))
);

CREATE INDEX trash_items_project_id_idx ON trash_items (project_id);
CREATE INDEX trash_items_target_idx ON trash_items (target_id);
CREATE INDEX trash_items_deleted_by_idx ON trash_items (deleted_by);
CREATE INDEX trash_items_deleted_at_idx ON trash_items (deleted_at);
CREATE INDEX trash_items_expires_at_idx ON trash_items (expires_at);
