//job form
import { useState } from "react";
import CreateJobModal from "./CreateJobModal";

interface Props {
  onJobCreated?: () => void;
}

const CreateJobForm: React.FC<Props> = ({ onJobCreated }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  return (
    <div>
      <button onClick={openModal}>Create Job</button>
      {isModalOpen && <CreateJobModal onClose={closeModal} onCreated={onJobCreated}/>}
    </div>
  );
};

export default CreateJobForm;
