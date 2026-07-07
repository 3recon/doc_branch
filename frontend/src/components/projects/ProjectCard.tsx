import type { ProjectListPageProject } from "./ProjectListPage";

type ProjectCardProps = {
  project: ProjectListPageProject;
};

const statusLabels: Record<string, string> = {
  IN_PROGRESS: "진행 중",
  ARCHIVED: "중단",
  DELETED: "삭제됨",
  COMPLETED: "완료"
};

function formatUpdatedAt(updatedAt: string) {
  const date = new Date(updatedAt);

  if (Number.isNaN(date.getTime())) {
    return updatedAt;
  }

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit"
  }).format(date);
}

export function ProjectCard({ project }: ProjectCardProps) {
  const statusLabel = statusLabels[project.status] ?? project.status;
  const statusClassName =
    project.status === "ARCHIVED"
      ? "projectStatusBadge projectStatusBadgePaused"
      : project.status === "COMPLETED"
        ? "projectStatusBadge projectStatusBadgeDone"
        : "projectStatusBadge";

  return (
    <article className="projectCard">
      {project.status === "IN_PROGRESS" ? (
        <span className="projectCardAccent" aria-hidden="true" />
      ) : null}

      <div className="projectCardTop">
        <span className={statusClassName}>{statusLabel}</span>
        <button
          className="projectCardMenu"
          type="button"
          aria-label={`${project.name} 더보기`}
        >
          <span aria-hidden="true">...</span>
        </button>
      </div>

      <div className="projectCardBody">
        <h2>{project.name}</h2>
        <p>{project.description || "프로젝트 설명이 아직 등록되지 않았습니다."}</p>
      </div>

      <dl className="projectCardMeta">
        <div>
          <dt>관리자</dt>
          <dd>{project.ownerName}</dd>
        </div>
        <div>
          <dt>수정일</dt>
          <dd>{formatUpdatedAt(project.updatedAt)}</dd>
        </div>
      </dl>
    </article>
  );
}
