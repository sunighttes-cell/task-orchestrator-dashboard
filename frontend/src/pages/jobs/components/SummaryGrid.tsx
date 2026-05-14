//Summary Grid Component
import SummaryCard from "@/pages/jobs/components/SummaryCard";
import type { StatusSummaryResponse } from "@/types/job";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

//queued //running //completed //failed
interface Props {
  datasummary: StatusSummaryResponse[] | undefined;
}



{/* <SummaryCard
  summary={item}
  onClick={() => navigate(`/jobs?status=${item.status}`)}
/> */}

const SummaryGrid: React.FC<Props> = ({ datasummary }) => {
  // const [statusFilter, setStatusFilter] = useState<string | null>(null);
  const navigate = useNavigate();
  
  if (!datasummary || datasummary.length === 0) return <div>No jobs found</div>;
  return (
    <div className="flex gap-4 mb-4">
      {datasummary.map((item) => (
        <SummaryCard
          key={item.status}
          summary={item}
          // isSelected={statusFilter === item.status}
          onClick={() => navigate(`/jobs?status=${item.status}`)}
          // { () => 
          //   setStatusFilter((prev) =>
          //     prev === item.status ? null : item.status
          //   )
          // }
        />
      ))}
    </div>
  );
};

export default SummaryGrid;     


