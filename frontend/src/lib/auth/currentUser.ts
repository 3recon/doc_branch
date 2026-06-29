import type { UserResponse } from "@/lib/api/users";

export const CURRENT_USER_STORAGE_KEY = "docbranch.currentUser";

export function readCurrentUser(): UserResponse | null {
  try {
    const stored = window.localStorage.getItem(CURRENT_USER_STORAGE_KEY);
    return stored ? (JSON.parse(stored) as UserResponse) : null;
  } catch {
    return null;
  }
}

export function storeCurrentUser(user: UserResponse) {
  window.localStorage.setItem(CURRENT_USER_STORAGE_KEY, JSON.stringify(user));
}
