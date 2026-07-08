//job form
import { useState } from "react";
import CreateJobModal from "./CreateJobModal";
import { PrimaryBtnClass } from "@/lib/constants";

const CreateJobForm = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  return (
    <div>
      <button type="button" onClick={openModal}
      className="bg-blue-500 text-white px-4 py-2 rounded disabled:opacity-50">
        Create Job
      </button>
      {isModalOpen && <CreateJobModal onClose={closeModal}/>}
    </div>
  );
};

export default CreateJobForm;
