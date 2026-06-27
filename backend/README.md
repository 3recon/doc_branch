# DocBranch Backend

DocBranch MVP1 Spring Boot REST API 서버입니다.

## 기술 스택

- Java 21
- Spring Boot
- Spring Security
- OAuth2 Client
- Spring Data JPA
- QueryDSL
- PostgreSQL
- Flyway
- springdoc-openapi / Swagger UI
- Cloudflare R2 연동용 S3 compatible API

## 주요 경로

- Health Check: `GET /api/health`
- Swagger UI: `/swagger-ui/index.html`

## 실행 전 필요 항목

- PostgreSQL
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- Cloudflare R2 관련 환경 변수는 파일 업로드 구현 시 설정합니다.

## 로컬 DB 기본값

- URL: `jdbc:postgresql://127.0.0.1:15432/docbranch`
- Username: `docbranch`
- Password: `docbranch`

## MVP1 API 문서

이 문서는 현재 구현된 백엔드 코드 기준입니다. 새로운 기능이나 미구현 정책은 포함하지 않습니다.

### 공통 응답 규칙

- 성공 응답은 별도 래퍼 없이 DTO 또는 배열을 그대로 반환합니다.
- 삭제 성공 응답은 `204 No Content`를 반환합니다.
- 에러 응답은 `ErrorResponse`로 통일합니다.

```json
{
  "code": "PROJECT_NOT_FOUND",
  "message": "..."
}
```

### 공통 값

- ID 값은 `UUID` 형식입니다.
- 날짜/시간 값은 `OffsetDateTime` 형식입니다.
- 프로젝트 역할: `PROJECT_ADMIN`, `PARTICIPANT`, `READ_ONLY`
- 프로젝트 상태: `IN_PROGRESS`, `COMPLETED`, `STOPPED`
- 프로젝트 초대 상태: `PENDING`, `ACCEPTED`, `EXPIRED`
- 문서 상태: `DRAFT`, `IN_REVIEW`, `COMPLETED`
- 문서 버전 유형: `INITIAL`, `REVISION`, `BRANCHED`, `MERGED`

### 권한 정책

현재 구현은 인증 토큰이 아니라 요청 body의 사용자 ID로 권한을 확인합니다.

| 구분 | 현재 구현 정책 |
| --- | --- |
| 프로젝트 생성 | `ownerUserId` 사용자가 프로젝트 관리자로 등록됩니다. |
| 프로젝트 수정/삭제 | 요청 사용자가 `PROJECT_ADMIN`이어야 합니다. |
| 프로젝트 멤버 수정/삭제 | 요청 사용자가 `PROJECT_ADMIN`이어야 합니다. |
| 프로젝트 초대 생성 | 요청 사용자가 `PROJECT_ADMIN`이어야 합니다. |
| 프로젝트 초대 수락 | `PENDING` 초대만 수락할 수 있고, 만료되었거나 이미 처리된 초대는 수락할 수 없습니다. 이미 활성 멤버인 사용자는 중복 등록되지 않습니다. |
| 문서 생성/수정/삭제 | 요청 사용자가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 합니다. |
| 문서 버전 생성/수정/삭제 | 요청 사용자가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 합니다. |
| 최종 문서 버전 지정 | 요청 사용자가 `PROJECT_ADMIN`이어야 합니다. |
| 조회 API | 프로젝트 상세, 멤버 목록, 초대 목록, 문서 목록/상세, 문서 버전 목록/단건 조회는 `requesterUserId`가 활성 프로젝트 멤버여야 합니다. |

### MVP1 인증/사용자 임시 정책

현재 MVP1 백엔드는 로그인 세션, JWT, OAuth2 인증 토큰에서 사용자 정보를 꺼내지 않습니다. API 요청 body에 포함된 사용자 UUID를 기준으로 사용자 존재 여부와 프로젝트 권한을 확인합니다.

현재 코드 기준으로 MVP1 사용자 생성/단건 조회 API가 있습니다. 로그인 API는 아직 없으며, 프로젝트와 문서 API에서는 `UserRepository`에 존재하는 사용자 UUID를 요청 body로 전달해야 합니다. 존재하지 않는 사용자는 `USER_NOT_FOUND` 또는 권한 검증 실패로 처리됩니다.

| 사용자 ID 필드 | 현재 의미 | 주요 사용처 |
| --- | --- | --- |
| `ownerUserId` | 프로젝트 생성자이자 최초 프로젝트 관리자 | 프로젝트 생성 |
| `requesterUserId` | API를 호출한 사용자로 간주되는 요청자 | 프로젝트 수정, 멤버 관리, 초대 생성, 조회 권한 확인, 문서/버전 수정과 삭제 |
| `createdByUserId` | 문서 또는 문서 버전을 생성한 사용자 | 문서 생성, 문서 버전 생성 |
| `deletedByUserId` | 프로젝트 삭제 요청자 | 프로젝트 삭제 |
| `userId` | 프로젝트 초대를 수락하는 사용자 | 프로젝트 초대 수락 |

MVP1에서는 아래 임시 정책을 유지합니다.

- 클라이언트는 API 호출 시 필요한 사용자 UUID를 요청 body에 포함합니다.
- 서버는 body의 사용자 UUID가 실제 로그인 사용자와 같은지 검증하지 않습니다.
- 권한 검증은 프로젝트 멤버 테이블의 활성 멤버 여부와 역할을 기준으로 처리합니다.
- `PROJECT_ADMIN`, `PARTICIPANT`, `READ_ONLY` 역할 정책은 현재 서비스 로직을 기준으로 적용합니다.
- 프로젝트 목록 조회와 초대 만료 처리 API는 현재 구현 기준으로 요청 사용자 ID를 받지 않습니다.

인증 전환 시에는 로그인한 사용자 정보를 서버가 직접 알아내도록 바꾸고, 아래 API의 사용자 ID 필드를 요청 body에서 제거해야 합니다.

| API | 현재 body 사용자 ID | 인증 전환 시 변경 방향 |
| --- | --- | --- |
| `POST /api/projects` | `ownerUserId` | 로그인 사용자를 프로젝트 소유자로 사용 |
| `GET /api/projects/{projectId}` | `requesterUserId` | 로그인 사용자로 프로젝트 멤버 여부 확인 |
| `PATCH /api/projects/{projectId}` | `requesterUserId` | 로그인 사용자로 관리자 권한 확인 |
| `DELETE /api/projects/{projectId}` | `deletedByUserId` | 로그인 사용자로 관리자 권한 확인 및 삭제자 기록 |
| `GET /api/projects/{projectId}/members` | `requesterUserId` | 로그인 사용자로 프로젝트 멤버 여부 확인 |
| `PATCH /api/projects/{projectId}/members/{projectMemberId}` | `requesterUserId` | 로그인 사용자로 관리자 권한 확인 |
| `DELETE /api/projects/{projectId}/members/{projectMemberId}` | `requesterUserId` | 로그인 사용자로 관리자 권한 확인 |
| `POST /api/projects/{projectId}/invitations` | `requesterUserId` | 로그인 사용자로 관리자 권한 확인 |
| `GET /api/projects/{projectId}/invitations` | `requesterUserId` | 로그인 사용자로 프로젝트 멤버 여부 확인 |
| `POST /api/projects/{projectId}/invitations/{invitationId}/accept` | `userId` | 로그인 사용자를 초대 수락자로 사용 |
| `POST /api/projects/{projectId}/documents` | `createdByUserId` | 로그인 사용자를 문서 생성자로 사용 |
| `GET /api/projects/{projectId}/documents` | `requesterUserId` | 로그인 사용자로 프로젝트 멤버 여부 확인 |
| `GET /api/projects/{projectId}/documents/{documentDetailId}` | `requesterUserId` | 로그인 사용자로 프로젝트 멤버 여부 확인 |
| `PATCH /api/projects/{projectId}/documents/{documentDetailId}` | `requesterUserId` | 로그인 사용자로 문서 수정 권한 확인 |
| `DELETE /api/projects/{projectId}/documents/{documentDetailId}` | `requesterUserId` | 로그인 사용자로 문서 삭제 권한 확인 |
| `POST /api/projects/{projectId}/documents/{documentDetailId}/versions` | `createdByUserId` | 로그인 사용자를 버전 생성자로 사용 |
| `GET /api/projects/{projectId}/documents/{documentDetailId}/versions` | `requesterUserId` | 로그인 사용자로 프로젝트 멤버 여부 확인 |
| `GET /api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}` | `requesterUserId` | 로그인 사용자로 프로젝트 멤버 여부 확인 |
| `PATCH /api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}` | `requesterUserId` | 로그인 사용자로 버전 수정 권한 확인 |
| `PATCH /api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}/final` | `requesterUserId` | 로그인 사용자로 관리자 권한 확인 |
| `DELETE /api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}` | `requesterUserId` | 로그인 사용자로 버전 삭제 권한 확인 |

### 주요 ErrorCode

| ErrorCode | HTTP Status | 주요 상황 |
| --- | --- | --- |
| `INVALID_INPUT_VALUE` | 400 | 요청 body 검증 실패 |
| `USER_NOT_FOUND` | 404 | 사용자를 찾을 수 없음 |
| `PROJECT_NOT_FOUND` | 404 | 프로젝트를 찾을 수 없음 |
| `PROJECT_ACCESS_DENIED` | 403 | 프로젝트 권한 없음 |
| `PROJECT_MEMBER_NOT_FOUND` | 404 | 프로젝트 멤버를 찾을 수 없음 |
| `PROJECT_MEMBER_ALREADY_EXISTS` | 409 | 이미 프로젝트 멤버임 |
| `PROJECT_INVITATION_NOT_FOUND` | 404 | 프로젝트 초대를 찾을 수 없음 |
| `PROJECT_INVITATION_EXPIRED` | 400 | 만료된 프로젝트 초대 |
| `PROJECT_INVITATION_ALREADY_PROCESSED` | 409 | 이미 처리된 프로젝트 초대 |
| `DOCUMENT_DETAIL_NOT_FOUND` | 404 | 문서 상세를 찾을 수 없음 |
| `DOCUMENT_VERSION_NOT_FOUND` | 404 | 문서 버전을 찾을 수 없음 |
| `INTERNAL_SERVER_ERROR` | 500 | 서버 내부 오류 |

## 사용자 API

### 사용자 생성

| 항목 | 내용 |
| --- | --- |
| Method | `POST` |
| Path | `/api/users` |
| Request Body | `name`, `email` |
| Response | `UserResponse` |
| 권한 | 별도 권한 검증 없음 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE` |

`UserResponse`: `userId`, `name`, `email`, `status`, `createdAt`, `updatedAt`

### 사용자 단건 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/users/{userId}` |
| Request Body | 없음 |
| Response | `UserResponse` |
| 권한 | 별도 권한 검증 없음 |
| 주요 ErrorCode | `USER_NOT_FOUND` |

## 프로젝트 API

### 프로젝트 생성

| 항목 | 내용 |
| --- | --- |
| Method | `POST` |
| Path | `/api/projects` |
| Request Body | `ownerUserId`, `name`, `description` |
| Response | `ProjectDetailResponse` |
| 권한 | `ownerUserId` 사용자를 프로젝트 소유자 및 `PROJECT_ADMIN` 멤버로 등록 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `USER_NOT_FOUND` |

`ProjectDetailResponse`: `projectId`, `name`, `description`, `status`, `ownerName`, `createdAt`, `updatedAt`

### 프로젝트 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects` |
| Request Body | 없음 |
| Response | `ProjectSummaryResponse[]` |
| 권한 | 별도 권한 검증 없음 |
| 주요 ErrorCode | `INTERNAL_SERVER_ERROR` |

`ProjectSummaryResponse`: `projectId`, `name`, `status`, `ownerName`, `updatedAt`

### 프로젝트 기본 정보 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects/{projectId}` |
| Request Body | `requesterUserId` |
| Response | `ProjectDetailResponse` |
| 권한 | `requesterUserId`가 활성 프로젝트 멤버여야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

### 프로젝트 수정

| 항목 | 내용 |
| --- | --- |
| Method | `PATCH` |
| Path | `/api/projects/{projectId}` |
| Request Body | `requesterUserId`, `name`, `description` |
| Response | `ProjectDetailResponse` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

### 프로젝트 삭제

| 항목 | 내용 |
| --- | --- |
| Method | `DELETE` |
| Path | `/api/projects/{projectId}` |
| Request Body | `deletedByUserId` |
| Response | `204 No Content` |
| 권한 | `deletedByUserId`가 `PROJECT_ADMIN`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `USER_NOT_FOUND`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

## 프로젝트 멤버 API

### 프로젝트 멤버 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects/{projectId}/members` |
| Request Body | `requesterUserId` |
| Response | `ProjectMemberResponse[]` |
| 권한 | `requesterUserId`가 활성 프로젝트 멤버여야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

`ProjectMemberResponse`: `projectMemberId`, `userId`, `name`, `email`, `role`, `joinedAt`

### 프로젝트 멤버 역할 수정

| 항목 | 내용 |
| --- | --- |
| Method | `PATCH` |
| Path | `/api/projects/{projectId}/members/{projectMemberId}` |
| Request Body | `requesterUserId`, `role` |
| Response | `ProjectMemberResponse` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED`, `PROJECT_MEMBER_NOT_FOUND` |

### 프로젝트 멤버 제거

| 항목 | 내용 |
| --- | --- |
| Method | `DELETE` |
| Path | `/api/projects/{projectId}/members/{projectMemberId}` |
| Request Body | `requesterUserId` |
| Response | `204 No Content` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED`, `PROJECT_MEMBER_NOT_FOUND` |

## 프로젝트 초대 API

### 프로젝트 초대 생성

| 항목 | 내용 |
| --- | --- |
| Method | `POST` |
| Path | `/api/projects/{projectId}/invitations` |
| Request Body | `requesterUserId`, `invitedEmail`, `role`, `expiresAt` |
| Response | `ProjectInvitationResponse` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

`ProjectInvitationResponse`: `invitationId`, `projectId`, `invitedEmail`, `role`, `status`, `expiresAt`

### 프로젝트 초대 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects/{projectId}/invitations` |
| Request Body | `requesterUserId` |
| Response | `ProjectInvitationResponse[]` |
| 권한 | `requesterUserId`가 활성 프로젝트 멤버여야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

### 프로젝트 초대 수락

| 항목 | 내용 |
| --- | --- |
| Method | `POST` |
| Path | `/api/projects/{projectId}/invitations/{invitationId}/accept` |
| Request Body | `userId` |
| Response | `ProjectInvitationResponse` |
| 권한 | 초대 상태가 `PENDING`이고 만료되지 않아야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `USER_NOT_FOUND`, `PROJECT_NOT_FOUND`, `PROJECT_INVITATION_NOT_FOUND`, `PROJECT_INVITATION_EXPIRED`, `PROJECT_INVITATION_ALREADY_PROCESSED`, `PROJECT_MEMBER_ALREADY_EXISTS` |

### 프로젝트 초대 만료 처리

| 항목 | 내용 |
| --- | --- |
| Method | `POST` |
| Path | `/api/projects/{projectId}/invitations/expire` |
| Request Body | 없음 |
| Response | `ProjectInvitationExpireResponse` |
| 권한 | 프로젝트 존재 여부 확인 |
| 주요 ErrorCode | `PROJECT_NOT_FOUND` |

`ProjectInvitationExpireResponse`: `expiredCount`

## 문서 API

### 문서 생성

| 항목 | 내용 |
| --- | --- |
| Method | `POST` |
| Path | `/api/projects/{projectId}/documents` |
| Request Body | `name`, `description`, `createdByUserId` |
| Response | `DocumentResponse` |
| 권한 | `createdByUserId`가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `USER_NOT_FOUND`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

`DocumentResponse`: `documentDetailId`, `projectId`, `name`, `description`, `status`, `createdByUserId`, `createdByName`, `createdAt`, `updatedAt`

### 문서 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects/{projectId}/documents` |
| Request Body | `requesterUserId` |
| Response | `DocumentResponse[]` |
| 권한 | `requesterUserId`가 활성 프로젝트 멤버여야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

### 문서 기본 정보 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}` |
| Request Body | `requesterUserId` |
| Response | `DocumentResponse` |
| 권한 | `requesterUserId`가 활성 프로젝트 멤버여야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED`, `DOCUMENT_DETAIL_NOT_FOUND` |

### 문서 수정

| 항목 | 내용 |
| --- | --- |
| Method | `PATCH` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}` |
| Request Body | `requesterUserId`, `name`, `description` |
| Response | `DocumentResponse` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `DOCUMENT_DETAIL_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

### 문서 삭제

| 항목 | 내용 |
| --- | --- |
| Method | `DELETE` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}` |
| Request Body | `requesterUserId` |
| Response | `204 No Content` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `DOCUMENT_DETAIL_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

## 문서 버전 API

### 문서 버전 생성

| 항목 | 내용 |
| --- | --- |
| Method | `POST` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}/versions` |
| Request Body | `title`, `content`, `createdByUserId` |
| Response | `DocumentVersionResponse` |
| 권한 | `createdByUserId`가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `USER_NOT_FOUND`, `PROJECT_NOT_FOUND`, `DOCUMENT_DETAIL_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

`DocumentVersionResponse`: `documentVersionId`, `documentDetailId`, `versionNumber`, `title`, `content`, `versionType`, `status`, `createdByUserId`, `createdByName`, `createdAt`, `updatedAt`

생성 시 `versionNumber`는 자동 증가합니다. 첫 버전은 `INITIAL`, 이후 버전은 `REVISION`으로 저장됩니다. 문서 상세의 `rootVersion`이 없으면 생성한 버전으로 설정하고, `finalVersion`은 생성한 버전으로 갱신합니다.

### 문서 버전 목록 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}/versions` |
| Request Body | `requesterUserId` |
| Response | `DocumentVersionResponse[]` |
| 권한 | `requesterUserId`가 활성 프로젝트 멤버여야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED`, `DOCUMENT_DETAIL_NOT_FOUND` |

### 문서 버전 단건 조회

| 항목 | 내용 |
| --- | --- |
| Method | `GET` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}` |
| Request Body | `requesterUserId` |
| Response | `DocumentVersionResponse` |
| 권한 | `requesterUserId`가 활성 프로젝트 멤버여야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `PROJECT_ACCESS_DENIED`, `DOCUMENT_DETAIL_NOT_FOUND`, `DOCUMENT_VERSION_NOT_FOUND` |

### 문서 버전 수정

| 항목 | 내용 |
| --- | --- |
| Method | `PATCH` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}` |
| Request Body | `requesterUserId`, `title`, `content` |
| Response | `DocumentVersionResponse` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `DOCUMENT_DETAIL_NOT_FOUND`, `DOCUMENT_VERSION_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

### 최종 문서 버전 지정

| 항목 | 내용 |
| --- | --- |
| Method | `PATCH` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}/final` |
| Request Body | `requesterUserId` |
| Response | `DocumentVersionResponse` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `DOCUMENT_DETAIL_NOT_FOUND`, `DOCUMENT_VERSION_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |

### 문서 버전 삭제

| 항목 | 내용 |
| --- | --- |
| Method | `DELETE` |
| Path | `/api/projects/{projectId}/documents/{documentDetailId}/versions/{documentVersionId}` |
| Request Body | `requesterUserId` |
| Response | `204 No Content` |
| 권한 | `requesterUserId`가 `PROJECT_ADMIN` 또는 `PARTICIPANT`이어야 함 |
| 주요 ErrorCode | `INVALID_INPUT_VALUE`, `PROJECT_NOT_FOUND`, `DOCUMENT_DETAIL_NOT_FOUND`, `DOCUMENT_VERSION_NOT_FOUND`, `PROJECT_ACCESS_DENIED` |
