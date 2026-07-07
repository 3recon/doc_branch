"use client";

import { useEffect, useId, useState, type FormEvent } from "react";
import {
  createProject,
  type ProjectDetailResponse
} from "@/lib/api/projects";

type CreateProjectModalProps = {
  ownerUserId: string;
  onClose: () => void;
  onCreated: (project: ProjectDetailResponse) => void;
};

export function CreateProjectModal({
  ownerUserId,
  onClose,
  onCreated
}: CreateProjectModalProps) {
  const titleId = useId();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [isCreating, setIsCreating] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const trimmedName = name.trim();
  const canCreate = trimmedName.length > 0 && !isCreating;

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape" && !isCreating) {
        onClose();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isCreating, onClose]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!canCreate) {
      return;
    }

    setErrorMessage(null);
    setIsCreating(true);

    try {
      const project = await createProject({
        ownerUserId,
        name: trimmedName,
        description: description.trim() || null
      });
      onCreated(project);
    } catch (error) {
      setErrorMessage(
        error instanceof Error ? error.message : "프로젝트 생성에 실패했습니다."
      );
    } finally {
      setIsCreating(false);
    }
  }

  return (
    <section
      className="projectModalOverlay"
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !isCreating) {
          onClose();
        }
      }}
    >
      <form className="projectModal" onSubmit={handleSubmit}>
        <h2 className="projectModalTitle" id={titleId}>
          새 프로젝트 생성
        </h2>

        <div className="projectModalFields">
          <label className="projectModalField">
            <span>
              프로젝트 이름 <strong aria-hidden="true">*</strong>
            </span>
            <input
              type="text"
              value={name}
              maxLength={100}
              autoFocus
              placeholder="이름을 입력하세요"
              onChange={(event) => setName(event.target.value)}
            />
          </label>

          <label className="projectModalField">
            <span>설명 (선택 사항)</span>
            <textarea
              value={description}
              maxLength={1000}
              placeholder="프로젝트에 대한 간단한 설명"
              onChange={(event) => setDescription(event.target.value)}
            />
          </label>
        </div>

        {errorMessage ? (
          <p className="projectModalError" role="alert">
            {errorMessage}
          </p>
        ) : null}

        <div className="projectModalActions">
          <button
            className="projectModalSecondary"
            type="button"
            disabled={isCreating}
            onClick={onClose}
          >
            취소
          </button>
          <button
            className="projectModalPrimary"
            type="submit"
            disabled={!canCreate}
          >
            {isCreating ? "생성 중..." : "프로젝트 생성"}
          </button>
        </div>
      </form>
    </section>
  );
}
