import { DashboardShell } from "@/layouts/DashboardShell";
import { Dashboard } from "@/pages/Dashboard";

//Wire Everything Together

function App() {
  return (
    <DashboardShell>
      <Dashboard />
    </DashboardShell>
  );
}

export default App;