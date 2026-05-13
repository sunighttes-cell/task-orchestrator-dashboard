import {
    type Cell,
    type ColumnDef,
    type Table
} from "@tanstack/react-table";
import {
    type TDataTableContextMenuProps,
} from "@/types/index";
import {type VirtualItem, Virtualizer} from "@tanstack/virtual-core";

export interface IAdvancedDataTable<T> {
	id: string;
	columns: ColumnDef<T>[];
	data: T[];
	actionProps?: {
		onDelete?: (rows: T[])=> void;
		onUserExport?: (rows: T[])=> void;
	};
	contextMenuProps?: TDataTableContextMenuProps;
	onRowClick?: (prop: T) => void;
	isLoading?: boolean;
}

export interface DataTablePaginationProps<TData> {
	table: Table<TData>
	pageSizeOptions?: number[]
}

export interface IDataTableFloatingBar<T> {
	table: Table<T>;
	onUserExport?: (rows: T[]) => void;
	onDelete?: (rows: T[]) => void;
}

export interface IDataTableExport<T> {
	table:Table<T>;
	onUserExport?: (data: T[])=> void;
}

export interface DataTableViewOptionsProps<TData> {
	table: Table<TData>
}

export interface IDataTableCellEdit<T> {
	cell: Cell<T, unknown>;
}

export interface IDataTableBody<T> {
	table: Table<T>;
	columnOrder: string[];
	onClick?: (prop: T) => void;
	rowVirtualizer: Virtualizer<HTMLDivElement, Element>;
	virtualPaddingLeft: number | undefined;
	virtualPaddingRight: number | undefined;
	virtualColumns: VirtualItem[];
}