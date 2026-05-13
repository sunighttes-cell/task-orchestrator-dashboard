import type {PropsWithChildren} from "react";
import {useRef} from "react";
import {createDataTableStore, DataTableProvider, type DataTableStoreType, type IDataTableStore} from "@/store/useDataTableStore";

export const DataTableStoreProvider = ({children, ...props}:PropsWithChildren<IDataTableStore>) => {
    const storeRef = useRef<DataTableStoreType>({} as DataTableStoreType);
    if (!storeRef.current) {
        storeRef.current = createDataTableStore({...props});
    }
    return <DataTableProvider value={storeRef.current}>{children}</DataTableProvider>;
};