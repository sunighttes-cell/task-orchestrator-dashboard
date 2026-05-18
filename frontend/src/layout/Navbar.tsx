// reusable Global Layout dashboard shell for consistent layout & styling

import { SidebarTrigger } from "@/components/sidebar/SidebarTrigger";
import { Toaster } from "sonner";
import { ThemeToggle } from "./ThemeToggle";

export function Navbar() {
  return (
    <header className="flex h-16 items-center justify-between border-b bg-white dark:bg-gray-900 px-4">
      {/* LEFT */}
      <div className="flex items-center gap-3">
        <SidebarTrigger />

        <h1 className="text-lg font-semibold">
          Task Orchestrator
        </h1>
      </div>

      {/* RIGHT */}
      <div className="flex items-center gap-4">
        <Toaster richColors position="top-right" />
        <div className="flex items-center gap-3">
          <ThemeToggle />
        </div>
        <div className="h-8 w-8 rounded-full bg-gray-300" />
      </div>
    </header>
  );
}