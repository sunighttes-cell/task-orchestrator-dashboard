import { Link, useLocation } from "react-router-dom";
import { navItems } from "./NavItems";
import { useSidebarStore } from "@/store/useSidebarStore";

export function AppSidebar() {
  const location = useLocation();
  const isOpen = useSidebarStore(
    (state) => state.isOpen
  );

  return (
    <>
      {/* Overlay */}
      {isOpen && (
        <div className="fixed inset-0 z-40 bg-black/50 lg:hidden" />
      )}
    <aside className=
        {`
          fixed left-0 top-0 z-50 h-full w-64
          border-r bg-white transition-transform
          lg:static lg:translate-x-0
          ${isOpen ? "translate-x-0" : "-translate-x-full"}
        `}
      >
      <div className="p-4 text-xl font-bold">
        Task Orchestrator
      </div>

      <nav className="space-y-1 p-2">
        {navItems.map((item) => {
          const Icon = item.icon;

          const isActive =
            location.pathname === item.href;

          return (
            <Link
              key={item.href}
              to={item.href}
              className={`
                flex items-center gap-3 rounded-lg px-3 py-2 transition-colors
                ${
                  isActive
                    ? "bg-black text-white"
                    : "hover:bg-gray-100"
                }
              `}
            >
              <Icon className="h-4 w-4" />

              <span>{item.title}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
    </>
  );
}