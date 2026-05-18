  export const PrimaryBtnClass = "bg-blue-500 text-white px-4 py-2 rounded disabled:opacity-50";
  export const SecondaryBtnClass = "bg-gray-500 text-white px-4 py-2 rounded disabled:opacity-50";

  export const JobStatusFilterValues = ["ALL", "QUEUED", "RUNNING", "COMPLETED","FAILED"];

  export const StatusColor = {
        "QUEUED":"text-yellow-500",
        "RUNNING": "text-blue-500",
        "COMPLETED": "text-green-500",
        "FAILED": "text-red-500",
    } as const;
    
    export const StatusBgColor = {
        "QUEUED":"bg-yellow-500",
        "RUNNING": "bg-blue-500",
        "COMPLETED": "bg-green-500",
        "FAILED": "bg-red-500",
    } as const;
