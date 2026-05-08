import { Navbar } from "@/components/layout/Navbar";
import type { ReactNode } from "react";

// Architecture decision - creating a reusable dashboard shell component for consistent layout and styling across all pages. 
//Create Global Layout Shell

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