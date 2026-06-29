"use client";

import { MvpUserSelector } from "@/components/auth/MvpUserSelector";
import { SocialLoginButton } from "@/components/auth/SocialLoginButton";

export function LoginPage() {
  return (
    <main className="loginPage">
      <section className="loginGrid" aria-label="DocBranch 시작 화면">
        <div className="loginColumn">
          <div className="loginCard">
            <div className="brandBlock">
              <div className="brandMark" aria-hidden="true">
                <span />
                <span />
                <span />
              </div>
              <p className="brandName">DocBranch</p>
            </div>

            <h1>소셜 계정으로 간편하게 시작하세요</h1>

            <div className="socialStack" aria-label="소셜 로그인">
              <SocialLoginButton provider="google">
                Google 계정으로 계속하기
              </SocialLoginButton>
              <SocialLoginButton provider="kakao">
                카카오로 계속하기
              </SocialLoginButton>
              <SocialLoginButton provider="naver">
                네이버로 계속하기
              </SocialLoginButton>
            </div>

          </div>

          <nav className="loginLinks" aria-label="보조 링크">
            <a href="#">개인정보 처리방침</a>
            <a href="#">이용약관</a>
            <a href="#">고객센터</a>
          </nav>
        </div>
      </section>

      <MvpUserSelector />
    </main>
  );
}
