import { DashboardShell } from "@/layouts/DashboardShell";
import { Dashboard } from "@/pages/Dashboard";
import { Toaster } from "sonner";

//Wire Everything Together

function App() {
  return (
    <DashboardShell>
      <Toaster richColors position="top-right" />
      <Dashboard />
    </DashboardShell>
  );
}

export default App;
