import { Outlet } from "react-router-dom";

export default function AuthLayout() {
  return (
    <div className="flex h-screen w-full bg-white dark:bg-gray-900">
         <Outlet /> 
    </div>
  );
}