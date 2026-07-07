"use client";

import { useEffect, useState } from "react";
import { LoginPage } from "@/components/auth/LoginPage";
import { readCurrentUser } from "@/lib/auth/currentUser";
import type { UserResponse } from "@/lib/api/users";
import { ProjectListPage } from "./ProjectListPage";

export function ProjectHomeGate() {
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null);
  const [hasCheckedCurrentUser, setHasCheckedCurrentUser] = useState(false);

  useEffect(() => {
    setCurrentUser(readCurrentUser());
    setHasCheckedCurrentUser(true);
  }, []);

  if (!hasCheckedCurrentUser) {
    return null;
  }

  return currentUser ? <ProjectListPage currentUser={currentUser} /> : <LoginPage />;
}
