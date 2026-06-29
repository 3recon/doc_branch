import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "DocBranch",
  description: "DocBranch MVP1 frontend"
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
