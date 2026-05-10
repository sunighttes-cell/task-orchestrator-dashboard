//job form
import { useState } from "react";
import CreateJobModal from "./CreateJobModal";

const CreateJobForm: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  return (
    <div>
      <button onClick={openModal}>Create Job</button>
      {isModalOpen && <CreateJobModal onClose={closeModal}/>}
    </div>
  );
};

export default CreateJobForm;