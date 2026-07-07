import { ProjectCard } from "./ProjectCard";
import { ProjectListPage, type ProjectListPageProject } from "./ProjectListPage";
import type { UserResponse } from "@/lib/api/users";

const currentUser: UserResponse = {
  userId: "user-1",
  name: "김진수",
  email: "jinsoo.kim@docbranch.com"
};

const projects: ProjectListPageProject[] = [
  {
    projectId: "project-1",
    name: "2024 신규 브랜드 가이드라인",
    description: "브랜드 리뉴얼에 따른 문서 작업 공간입니다.",
    status: "IN_PROGRESS",
    ownerName: "김진수",
    updatedAt: "2026-07-07T09:00:00Z"
  }
];

export function ProjectListPageTypeContract() {
  return (
    <ProjectListPage
      currentUser={currentUser}
      loadProjects={async (userId) => {
        if (userId !== currentUser.userId) {
          throw new Error("Unexpected user");
        }

        return projects;
      }}
    />
  );
}

export function ProjectCardTypeContract() {
  return <ProjectCard project={projects[0]} />;
}
