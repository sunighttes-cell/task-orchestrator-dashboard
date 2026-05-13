//Summary Card Component
import { Card, CardContent} from "@/components/ui/card";
import type { StatusSummaryResponse } from "@/types/job";

interface Props {
  summary: StatusSummaryResponse;
}

const SummaryCard: React.FC<Props> = ({ summary }) => {
  const getStatusColor = () => {
    switch (summary.status) {      
      case "QUEUED":
        return "text-yellow-500";
      case "RUNNING":
        return "text-blue-500";
      case "COMPLETED":
        return "text-green-500";
      case "FAILED":
        return "text-red-500";
      default:
        return "text-gray-500";
    }
  };

  return (
    <Card className="border p-4 rounded">
      <CardContent>
        <h1 className="text-lg font-bold">{summary.count}</h1>
        <p className={`font-bold ${getStatusColor()}`}>{summary.status}</p>
      </CardContent>
    </Card>
  );
}

export default SummaryCard;     

