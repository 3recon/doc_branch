import { requestJson } from "@/lib/api/client";

export type ProjectStatus = "IN_PROGRESS" | "ARCHIVED" | "DELETED" | string;

export type ProjectSummaryResponse = {
  projectId: string;
  name: string;
  status: ProjectStatus;
  ownerName: string;
  updatedAt: string;
};

export type ProjectDetailResponse = {
  projectId: string;
  name: string;
  description: string | null;
  status: ProjectStatus;
  ownerName: string;
  createdAt: string;
  updatedAt: string;
};

export type ProjectCreateRequest = {
  ownerUserId: string;
  name: string;
  description?: string | null;
};

export function getProjects(userId: string) {
  const query = new URLSearchParams({ userId });
  return requestJson<ProjectSummaryResponse[]>(`/api/projects?${query}`);
}

export function createProject(request: ProjectCreateRequest) {
  return requestJson<ProjectDetailResponse>("/api/projects", {
    method: "POST",
    body: JSON.stringify(request)
  });
}
