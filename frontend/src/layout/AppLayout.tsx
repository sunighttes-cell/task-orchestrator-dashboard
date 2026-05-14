import { AppSidebar } from "@/components/sidebar/AppSidebar";
import { MainLayout } from "./MainLayout";

export default function AppLayout() {
  return (
    <div className="flex h-screen w-full bg-white dark:bg-gray-900">
      <AppSidebar />
      <MainLayout />
    </div>
  );
}