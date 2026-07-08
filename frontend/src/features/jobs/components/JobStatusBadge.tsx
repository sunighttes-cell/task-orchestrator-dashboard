//displays different colors based on status. used in DisplayJobList

import { Badge } from "@/components/ui/badge"
import { Label } from "@/components/ui/label"


interface Props {
  status: string | null;
}

const JobStatusBadge: React.FC<Props> = ({ status }) => {

  return (
    <Label>Status: <Badge>{status}</Badge></Label>
  );
}

export default JobStatusBadge;