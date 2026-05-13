"use client";

import {TooltipProvider} from "@/components/ui/tooltip";
import {DataTableStoreProvider} from "@/store/dataTableStoreProvider";

export function DataTableProvider({children}: { children: React.ReactNode }) {
    return (
        <TooltipProvider>
            <DataTableStoreProvider isSelecting={false}>
                {children}
            </DataTableStoreProvider>
        </TooltipProvider>
    );
}