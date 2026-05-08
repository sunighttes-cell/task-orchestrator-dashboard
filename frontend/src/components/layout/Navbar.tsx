// Architecture decision - creating a reusable dashboard shell component for consistent layout and styling across all pages.
//Create Global Layout Shell
export function Navbar() {
  return (
    <nav className="border-b p-4">
      <h1 className="text-xl font-bold">
        Task Orchestrator Dashboard
      </h1>
    </nav>
  );
}