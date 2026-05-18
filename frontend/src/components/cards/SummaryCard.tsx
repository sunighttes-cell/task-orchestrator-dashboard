//Summary Card Component
import { Card, CardContent} from "@/components/ui/card";
import {StatusColor} from "@/lib/constants";

interface Props {
  summary: {count: number, status: string};
  isSelected?: boolean;
  onClick?: (status: string) => void;
}

const SummaryCard: React.FC<Props> = ({
  summary,
  isSelected = false,
  onClick,
}) => {

  return (
    <Card
      onClick={() => onClick?.(summary.status)}
      className={`
        border p-4 rounded cursor-pointer transition
        dark:bg-gray-900
        ${isSelected ? "ring-2 ring-blue-500 bg-blue-50" : "hover:bg-gray-100"}
      `}
    >
      <CardContent className="flex flex-col items-start">
        <h1 className="text-2xl font-bold">{summary.count}</h1>
        <p className={`text-sm font-medium ${StatusColor[summary.status]}`}>
          {summary.status}
        </p>
      </CardContent>
    </Card>
  );
};

export default SummaryCard; 

