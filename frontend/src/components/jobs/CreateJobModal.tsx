import { useState } from "react";
import { useCreateJob } from "@/hooks/useCreateJob";

//controlled inputs // loading spinner //disabled submit during mutation
interface CreateJobModalProps {
  onClose: () => void;
}

const CreateJobModal: React.FC<CreateJobModalProps> = ({ onClose }) => {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const createJobMutation = useCreateJob();

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    createJobMutation.mutate(
      { name, description },
      {
        onSuccess: () => {
          onClose();
        },
      }
    );
  };

  return (
    <div className="modal">
      <form onSubmit={handleSubmit}>
        <label>
          Job Name:
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </label>
        <label>
          Description:
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
          />
        </label>
        <button type="submit" disabled={createJobMutation.isPending}>
          Create Job
        </button>
      </form>
    </div>
  );
};

export default CreateJobModal;      