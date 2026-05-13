import * as React from "react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

function SidebarProvider({ children }: { children: React.ReactNode }) {
  return <>{children}</>
}

function SidebarTrigger() {
  return (
    <Button type="button" variant="ghost" size="sm" className="md:hidden">
      Menu
    </Button>
  )
}

function Sidebar({ className, ...props }: React.ComponentProps<"aside">) {
  return (
    <aside
      className={cn(
        "fixed inset-y-0 left-0 hidden w-64 border-r bg-card px-4 py-6 md:block",
        className
      )}
      {...props}
    />
  )
}

function SidebarHeader({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("mb-6 text-lg font-semibold", className)}
      {...props}
    />
  )
}

function SidebarContent({ className, ...props }: React.ComponentProps<"div">) {
  return <div className={cn("space-y-4", className)} {...props} />
}

function SidebarGroup({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("rounded-lg border bg-background/60 p-3", className)}
      {...props}
    />
  )
}

function SidebarMenuItem({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn(
        "flex items-center rounded-md px-3 py-2 text-sm text-muted-foreground hover:bg-muted hover:text-foreground",
        className
      )}
      {...props}
    />
  )
}

function SidebarFooter({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("absolute inset-x-4 bottom-6 text-sm text-muted-foreground", className)}
      {...props}
    />
  )
}

export {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarHeader,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
}
