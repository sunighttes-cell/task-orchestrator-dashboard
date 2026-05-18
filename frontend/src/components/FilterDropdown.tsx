//implement dropdown filter for job status
interface Props { 
  status: string,
  onChange: (arg) => void,
  optionValues: string []
}

export const FilterDropdown: React.FC<Props> = ({ status, onChange, optionValues}) =>  {
  return (
    <>
      {optionValues && 
        <select
        value={status}
        onChange={(e) => onChange(e.target.value)}
        className="border rounded px-3 py-2"
      >
       {optionValues.map((option, index) =>(<option key={index} value={option}>{option}</option>))}
      </select>
    }
    </>
  );
}