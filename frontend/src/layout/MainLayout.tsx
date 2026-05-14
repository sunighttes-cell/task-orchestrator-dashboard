import { Outlet } from "react-router-dom";
import { Navbar } from "./Navbar";
import { PageContainer } from "./PageContainer";

export function MainLayout() {
  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <Navbar />

      <main className="flex-1 overflow-y-auto bg-gray-50 dark:bg-gray-900">        
        <PageContainer>
          <Outlet />
        </PageContainer>
      </main>
    </div>
  );
}