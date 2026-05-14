//Summary Card Component
import { Card, CardContent} from "@/components/ui/card";
import type { StatusSummaryResponse } from "@/types/job";

interface Props {
  summary: StatusSummaryResponse;
  isSelected?: boolean;
  onClick?: () => void;
}

const SummaryCard: React.FC<Props> = ({
  summary,
  isSelected = false,
  onClick,
}) => {
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
    <Card
      onClick={onClick}
      className={`
        border p-4 rounded cursor-pointer transition
        dark:bg-gray-900
        ${isSelected ? "ring-2 ring-blue-500 bg-blue-50" : "hover:bg-gray-100"}
      `}
    >
      <CardContent className="flex flex-col items-start">
        <h1 className="text-2xl font-bold">{summary.count}</h1>
        <p className={`text-sm font-medium ${getStatusColor()}`}>
          {summary.status}
        </p>
      </CardContent>
    </Card>
  );
};

export default SummaryCard; 

