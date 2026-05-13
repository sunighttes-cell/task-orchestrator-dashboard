import { Menu } from "lucide-react";

import { useSidebarStore } from "@/store/useSidebarStore";

export function SidebarTrigger() {
  const toggle = useSidebarStore(
    (state) => state.toggle
  );

  return (
    <button
      onClick={toggle}
      className="rounded-md border p-2 lg:hidden"
    >
      <Menu className="h-5 w-5" />
    </button>
  );
}