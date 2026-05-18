//implement search input
interface Props {
  onChange: (arg) => void, 
  search: string
}

export const FilterSearch: React.FC<Props> = ({search, onChange}) => {
  return (
    <input
      value={search}
      onChange={(e) => onChange(e.target.value)}
      placeholder="Search jobs by name..."
      className="mb-4 p-2 border rounded w-full"
    />
  );
}
