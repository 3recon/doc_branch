import { requestJson } from "@/lib/api/client";

export type UserStatus = "ACTIVE" | "INACTIVE" | "DELETED" | string;

export type UserResponse = {
  userId: string;
  name: string;
  email: string;
  status?: UserStatus;
};

export type UserCreateRequest = {
  name: string;
  email: string;
};

export function createUser(request: UserCreateRequest) {
  return requestJson<UserResponse>("/api/users", {
    method: "POST",
    body: JSON.stringify(request)
  });
}

export function getUser(userId: string) {
  return requestJson<UserResponse>(`/api/users/${encodeURIComponent(userId)}`);
}
