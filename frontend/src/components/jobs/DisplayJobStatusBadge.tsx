//displays different colors based on status. used in DisplayJobList

import type { JobStatus } from "@/types/job";

interface Props {
  status: JobStatus;
}

const DisplayJobStatusBadge: React.FC<Props> = ({ status }) => {
  const getBadgeColor = () => {
    switch (status) {      
      case "QUEUED":
        return "bg-yellow-500";
      case "RUNNING":
        return "bg-blue-500";
      case "COMPLETED":
        return "bg-green-500";
      case "FAILED":
        return "bg-red-500";
      default:
        return "bg-gray-500";
    }
  };

  return (
    <span className={`text-white px-2 py-1 rounded ${getBadgeColor()}`}>
      {status}
    </span>
  );
}

export default DisplayJobStatusBadge;