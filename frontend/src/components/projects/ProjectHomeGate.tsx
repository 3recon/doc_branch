"use client";

import { useEffect, useState } from "react";
import { LoginPage } from "@/components/auth/LoginPage";
import { readCurrentUser } from "@/lib/auth/currentUser";
import type { UserResponse } from "@/lib/api/users";
import { ProjectListPage } from "./ProjectListPage";

export const USER_SELECTED_EVENT_NAME = "docbranch:user-selected";

export function ProjectHomeGate() {
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null);
  const [hasCheckedCurrentUser, setHasCheckedCurrentUser] = useState(false);

  useEffect(() => {
    function handleUserSelected(event: Event) {
      setCurrentUser((event as CustomEvent<UserResponse>).detail);
    }

    setCurrentUser(readCurrentUser());
    setHasCheckedCurrentUser(true);

    window.addEventListener(USER_SELECTED_EVENT_NAME, handleUserSelected);

    return () => {
      window.removeEventListener(USER_SELECTED_EVENT_NAME, handleUserSelected);
    };
  }, []);

  if (!hasCheckedCurrentUser) {
    return null;
  }

  return currentUser ? <ProjectListPage currentUser={currentUser} /> : <LoginPage />;
}
