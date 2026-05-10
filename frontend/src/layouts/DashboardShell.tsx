import { Navbar } from "@/components/layout/Navbar";
import type { ReactNode } from "react";

// reusable Global Layout dashboard shell for consistent layout & styling

interface Props {
  children: ReactNode;
}

export function DashboardShell({ children }: Props) {
  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />
      <main className="p-6">
        {children}
      </main>
    </div>
  );
}