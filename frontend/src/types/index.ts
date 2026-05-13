
export type TDataTableContextMenuProps = {
	enableEdit: boolean;
	enableDelete: boolean;
	onDelete: (prop: any) => void;
	extra?: { [menuName: string]: (prop: any)=> void; }
}