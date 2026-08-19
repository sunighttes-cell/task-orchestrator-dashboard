import type { JobStatus } from "@/types/job";

//implement dropdown filter for job status
interface Props { 
  status: JobStatus,
  onChange: (status: JobStatus) => void,
  optionValues: string[]
}

export const FilterDropdown: React.FC<Props> = ({ status, onChange, optionValues}) =>  {
  return (
    <>
      {optionValues && 
        <select
        value={status}
        onChange={(e) => onChange(e.target.value as JobStatus)}
        className="border rounded px-3 py-2"
      >
       {optionValues.map((option, index) =>(<option key={index} value={option}>{option}</option>))}
      </select>
    }
    </>
  );
}