"use client";

import { useEffect, useState } from "react";
import { getProjects } from "@/lib/api/projects";
import type { UserResponse } from "@/lib/api/users";
import { ProjectCard } from "./ProjectCard";

export type ProjectListPageProject = {
  projectId: string;
  name: string;
  description?: string | null;
  status: string;
  ownerName: string;
  updatedAt: string;
};

type ProjectListPageProps = {
  currentUser: UserResponse;
  loadProjects?: (userId: string) => Promise<ProjectListPageProject[]>;
};

export function ProjectListPage({
  currentUser,
  loadProjects = getProjects
}: ProjectListPageProps) {
  const [projects, setProjects] = useState<ProjectListPageProject[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let isActive = true;

    async function fetchProjects() {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        const nextProjects = await loadProjects(currentUser.userId);

        if (isActive) {
          setProjects(nextProjects);
        }
      } catch (error) {
        if (isActive) {
          setProjects([]);
          setErrorMessage(
            error instanceof Error
              ? error.message
              : "프로젝트 목록을 불러오지 못했습니다."
          );
        }
      } finally {
        if (isActive) {
          setIsLoading(false);
        }
      }
    }

    void fetchProjects();

    return () => {
      isActive = false;
    };
  }, [currentUser.userId, loadProjects]);

  return (
    <div className="projectWorkspace">
      <aside className="projectSideNav" aria-label="프로젝트 탐색">
        <div className="projectSideHeader">
          <p className="projectBrand">DocBranch</p>
          <button className="projectUserButton" type="button">
            <span className="projectAvatar" aria-hidden="true">
              {currentUser.name.slice(0, 1)}
            </span>
            <span>
              <strong>{currentUser.name}</strong>
              <small>{currentUser.email}</small>
            </span>
          </button>
        </div>

        <nav className="projectNavLinks" aria-label="프로젝트 메뉴">
          <p>프로젝트</p>
          <a className="active" href="#project-list">
            내 프로젝트
          </a>
          <a href="#joined-projects">참여 프로젝트</a>
          <a href="#trash">휴지통</a>
        </nav>
      </aside>

      <main className="projectListShell" id="project-list">
        <header className="projectMobileHeader">
          <p className="projectBrand">DocBranch</p>
          <button type="button" aria-label="메뉴 열기">
            <span aria-hidden="true">☰</span>
          </button>
        </header>

        <section className="projectListContent" aria-labelledby="project-list-title">
          <div className="projectListHeader">
            <div>
              <h1 id="project-list-title">내 프로젝트</h1>
              <p>진행 중인 프로젝트를 확인하고 문서 작업 공간을 선택합니다.</p>
            </div>
            <button className="projectCreateButton" type="button">
              <span aria-hidden="true">+</span>
              새 프로젝트
            </button>
          </div>

          <div className="projectListTools" aria-label="프로젝트 검색 및 필터">
            <label className="projectSearch">
              <span aria-hidden="true">⌕</span>
              <input
                type="search"
                placeholder="프로젝트명 또는 관리자 검색"
                aria-label="프로젝트 검색"
              />
            </label>

            <div className="projectFilters">
              <label>
                <span className="srOnly">상태 필터</span>
                <select defaultValue="">
                  <option value="">상태: 전체</option>
                  <option value="IN_PROGRESS">진행 중</option>
                  <option value="ARCHIVED">중단</option>
                  <option value="COMPLETED">완료</option>
                </select>
              </label>
              <button type="button" aria-label="필터 열기">
                <span aria-hidden="true">≡</span>
              </button>
            </div>
          </div>

          {isLoading ? (
            <div className="projectListState" role="status" aria-live="polite">
              <span className="projectStateMark" aria-hidden="true" />
              <h2>프로젝트 목록을 불러오는 중입니다</h2>
              <p>참여 중인 프로젝트 작업 공간을 확인하고 있습니다.</p>
            </div>
          ) : errorMessage ? (
            <div className="projectListState projectListStateError" role="alert">
              <span className="projectStateMark" aria-hidden="true">
                !
              </span>
              <h2>프로젝트 목록을 불러오지 못했습니다</h2>
              <p>{errorMessage}</p>
            </div>
          ) : projects.length > 0 ? (
            <div className="projectGrid">
              {projects.map((project) => (
                <ProjectCard key={project.projectId} project={project} />
              ))}

              <button className="projectAddCard" type="button">
                <span aria-hidden="true">+</span>
                <strong>새 프로젝트 만들기</strong>
              </button>
            </div>
          ) : (
            <div className="projectEmptyState">
              <span aria-hidden="true">+</span>
              <h2>아직 프로젝트가 없습니다</h2>
              <p>새 프로젝트를 만들면 이 목록에서 작업 공간을 확인할 수 있습니다.</p>
              <button className="projectCreateButton" type="button">
                새 프로젝트
              </button>
            </div>
          )}
        </section>

        <nav className="projectBottomNav" aria-label="모바일 메뉴">
          <a className="active" href="#project-list">
            홈
          </a>
          <a href="#documents">문서</a>
          <a href="#joined-projects">공유</a>
          <a href="#settings">설정</a>
        </nav>
      </main>
    </div>
  );
}
