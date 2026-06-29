type SocialProvider = "google" | "kakao" | "naver";

type SocialLoginButtonProps = {
  provider: SocialProvider;
  children: React.ReactNode;
};

function GoogleIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="socialIcon">
      <path
        fill="#4285F4"
        d="M22.6 12.2c0-.8-.1-1.5-.2-2.2H12v4.2h5.9c-.3 1.3-1 2.4-2.1 3.1v2.6h3.4c2-1.8 3.4-4.4 3.4-7.7Z"
      />
      <path
        fill="#34A853"
        d="M12 23c3 0 5.5-1 7.3-2.7l-3.4-2.6c-.9.6-2.2 1-3.9 1-3 0-5.5-2-6.4-4.8H2.1v2.7C3.9 20.4 7.6 23 12 23Z"
      />
      <path
        fill="#FBBC05"
        d="M5.6 13.9c-.2-.6-.4-1.2-.4-1.9s.1-1.3.4-1.9V7.4H2.1C1.4 8.8 1 10.4 1 12s.4 3.2 1.1 4.6l3.5-2.7Z"
      />
      <path
        fill="#EA4335"
        d="M12 5.3c1.6 0 3.1.6 4.2 1.7l3.1-3.1C17.5 2.1 15 1 12 1 7.6 1 3.9 3.6 2.1 7.4l3.5 2.7C6.5 7.3 9 5.3 12 5.3Z"
      />
    </svg>
  );
}

function KakaoIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" className="socialIcon">
      <path
        fill="currentColor"
        d="M12 4C6.9 4 2.8 7.2 2.8 11.1c0 2.5 1.7 4.7 4.2 5.9l-.8 2.9c-.1.4.3.7.6.5l3.6-2.4c.5.1 1.1.1 1.6.1 5.1 0 9.2-3.2 9.2-7.1S17.1 4 12 4Z"
      />
    </svg>
  );
}

function NaverIcon() {
  return (
    <span aria-hidden="true" className="naverIcon">
      N
    </span>
  );
}

function getIcon(provider: SocialProvider) {
  if (provider === "google") return <GoogleIcon />;
  if (provider === "kakao") return <KakaoIcon />;
  return <NaverIcon />;
}

export function SocialLoginButton({
  provider,
  children
}: SocialLoginButtonProps) {
  return (
    <button
      className={`socialButton socialButton-${provider}`}
      type="button"
      onClick={() => {
        window.alert(
          "소셜 로그인은 이후 인증 단계에서 연결합니다. MVP1 테스트는 오른쪽 아래 사용자 선택을 사용하세요."
        );
      }}
    >
      {getIcon(provider)}
      <span>{children}</span>
    </button>
  );
}
