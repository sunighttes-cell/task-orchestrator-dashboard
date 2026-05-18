//Summary Grid Component
import SummaryCard from "@/components/cards/SummaryCard";
import { useNavigate } from "react-router-dom";
import {Skeleton} from "@/components/ui/skeleton"
import { EmptyData } from "../EmptyData";

//queued //running //completed //failed
interface Props {
  datasummary: any | undefined;
  navigateBaseURL: string;
  isLoading: boolean;
}

const SummaryGrid: React.FC<Props> = ({ datasummary, navigateBaseURL, isLoading }) => {
  const navigate = useNavigate();
  
  return (
    <>
    {isLoading ? ( <div role="status" aria-label="Loading"><Skeleton /><span className="sr-only">Loading...</span></div>) :
    ! datasummary|| datasummary.length === 0 ? (<EmptyData/>) :
        (
          <div>
            <h2 className="text-lg font-semibold text-center mb-4">Summary</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {datasummary.map((item) => (
                <SummaryCard
                  key={item.status}
                  summary={item}
                  onClick={() => navigate(navigateBaseURL + `${item.status}`)}
                />
              ))}
            </div>
          </div>
        )
      }
    </>
  );
};

export default SummaryGrid;     


