// reusable Global Layout dashboard shell for consistent layout & styling

//import { Bell } from "lucide-react";
import { ThemeToggle } from "./ThemeToggle";
import { SidebarTrigger } from "@/components/sidebar/SidebarTrigger";

export function Navbar() {
  return (
    <header className="flex h-16 items-center justify-between border-b bg-white px-4">
      {/* LEFT */}
      <div className="flex items-center gap-3">
        <SidebarTrigger />

        <h1 className="text-lg font-semibold">
          Task Orchestrator
        </h1>
      </div>

      {/* RIGHT */}
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-3">
          <ThemeToggle />
        </div>
        {/* <button className="rounded-md p-2 hover:bg-gray-100">
          <Bell className="h-5 w-5" />
        </button> */}
        <div className="h-8 w-8 rounded-full bg-gray-300" />
      </div>
    </header>
  );
}

// import { ThemeToggle } from "./ThemeToggle";
// export function Navbar() {
//   return (
//     <header className="h-16 border-b bg-background sticky top-0 z-50 backdrop-blur">
//       <div className="flex h-full items-center justify-between px-6">
        
//         <div>
//           <h1 className="text-xl font-semibold">
//             Jobs
//           </h1>
//         </div>

//         <div className="flex items-center gap-3">
//           <ThemeToggle />
//         </div>

//       </div>
//     </header>
//   )
// }