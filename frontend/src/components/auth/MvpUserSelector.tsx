"use client";

import { useEffect, useState } from "react";
import {
  createUser,
  getUser,
  type UserResponse
} from "@/lib/api/users";
import {
  readCurrentUser,
  storeCurrentUser
} from "@/lib/auth/currentUser";

type Mode = "create" | "select";
type Status = {
  type: "success" | "error";
  message: string;
} | null;

export function MvpUserSelector() {
  const [isOpen, setIsOpen] = useState(false);
  const [mode, setMode] = useState<Mode>("create");
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null);
  const [status, setStatus] = useState<Status>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setCurrentUser(readCurrentUser());
  }, []);

  function saveUser(user: UserResponse) {
    storeCurrentUser(user);
    setCurrentUser(user);
    window.dispatchEvent(
      new CustomEvent("docbranch:user-selected", { detail: user })
    );
  }

  async function handleCreate(formData: FormData) {
    setStatus(null);
    setIsSubmitting(true);

    const name = String(formData.get("name") ?? "").trim();
    const email = String(formData.get("email") ?? "").trim();

    try {
      const user = await createUser({ name, email });
      saveUser(user);
      setStatus({
        type: "success",
        message: "사용자를 생성하고 현재 사용자로 저장했습니다."
      });
    } catch (error) {
      setStatus({
        type: "error",
        message: error instanceof Error ? error.message : "요청에 실패했습니다."
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleSelect(formData: FormData) {
    setStatus(null);
    setIsSubmitting(true);

    const userId = String(formData.get("userId") ?? "").trim();

    try {
      const user = await getUser(userId);
      saveUser(user);
      setStatus({
        type: "success",
        message: "기존 사용자를 현재 사용자로 저장했습니다."
      });
    } catch (error) {
      setStatus({
        type: "error",
        message: error instanceof Error ? error.message : "요청에 실패했습니다."
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <>
      <button
        className="mvpLauncher"
        type="button"
        aria-haspopup="dialog"
        aria-controls="mvp-user-dialog"
        onClick={() => setIsOpen(true)}
      >
        MVP 사용자 선택
      </button>

      {isOpen ? (
        <section
          className="mvpOverlay"
          id="mvp-user-dialog"
          role="dialog"
          aria-modal="true"
          aria-labelledby="mvp-user-title"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              setIsOpen(false);
            }
          }}
        >
          <div className="mvpPanel">
            <div className="mvpPanelHeader">
              <div>
                <h2 className="mvpPanelTitle" id="mvp-user-title">
                  MVP 사용자 선택
                </h2>
                <p className="mvpPanelCopy">
                  소셜 로그인 연결 전까지 API 테스트용 사용자 ID를 저장합니다.
                </p>
              </div>
              <button
                className="mvpClose"
                type="button"
                aria-label="닫기"
                onClick={() => setIsOpen(false)}
              >
                ×
              </button>
            </div>

            <div className="mvpPanelBody">
              <div className="mvpTabs" role="tablist" aria-label="사용자 선택 방식">
                <button
                  className="mvpTab"
                  type="button"
                  role="tab"
                  aria-selected={mode === "create"}
                  onClick={() => {
                    setMode("create");
                    setStatus(null);
                  }}
                >
                  새 사용자
                </button>
                <button
                  className="mvpTab"
                  type="button"
                  role="tab"
                  aria-selected={mode === "select"}
                  onClick={() => {
                    setMode("select");
                    setStatus(null);
                  }}
                >
                  기존 사용자
                </button>
              </div>

              {mode === "create" ? (
                <form action={handleCreate} className="mvpForm">
                  <label className="mvpField">
                    <span>이름</span>
                    <input
                      name="name"
                      type="text"
                      maxLength={100}
                      autoComplete="name"
                      placeholder="홍길동"
                      required
                    />
                  </label>
                  <label className="mvpField">
                    <span>이메일</span>
                    <input
                      name="email"
                      type="email"
                      maxLength={255}
                      autoComplete="email"
                      placeholder="name@example.com"
                      required
                    />
                  </label>
                  <button
                    className="mvpAction"
                    type="submit"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "처리 중..." : "사용자 생성 후 저장"}
                  </button>
                </form>
              ) : (
                <form action={handleSelect} className="mvpForm">
                  <label className="mvpField">
                    <span>사용자 ID</span>
                    <input
                      name="userId"
                      type="text"
                      inputMode="text"
                      placeholder="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
                      required
                    />
                  </label>
                  <button
                    className="mvpAction"
                    type="submit"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "처리 중..." : "이 사용자로 저장"}
                  </button>
                </form>
              )}

              {status ? (
                <div className={`mvpStatus ${status.type}`} role="status">
                  {status.message}
                </div>
              ) : null}

              {currentUser ? (
                <aside className="mvpCurrent" aria-live="polite">
                  <p className="mvpCurrentName">{currentUser.name}</p>
                  <p className="mvpCurrentMeta">{currentUser.email}</p>
                  <p className="mvpCurrentMeta">{currentUser.userId}</p>
                </aside>
              ) : null}

              <p className="mvpHelper">
                저장된 userId는 프로젝트 API 요청의 ownerUserId 또는
                requesterUserId로 사용할 수 있습니다.
              </p>
            </div>
          </div>
        </section>
      ) : null}
    </>
  );
}
