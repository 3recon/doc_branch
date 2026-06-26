# DocBranch Backend

DocBranch MVP1 Spring Boot REST API 서버다.

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
- Cloudflare R2 관련 환경 변수는 파일 업로드 구현 시 설정한다.

## 로컬 DB 기본값

- URL: `jdbc:postgresql://127.0.0.1:15432/docbranch`
- Username: `docbranch`
- Password: `docbranch`
