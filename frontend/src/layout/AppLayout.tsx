import { AppSidebar } from "@/components/sidebar/AppSidebar";
import { MainLayout } from "./MainLayout";
import { Toaster } from "sonner";

export default function AppLayout() {
  return (
    <div className="flex h-screen overflow-hidden">
        <Toaster richColors position="top-right" />
        <AppSidebar />

        <MainLayout />
    </div>
  );
}