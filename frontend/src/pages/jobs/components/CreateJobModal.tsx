import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useCreateJob } from "@/hooks/useCreateJob";
import { toast } from "sonner";
import { useQueryClient } from "@tanstack/react-query";

const createJobSchema = z.object({
  name: z.string().min(3, "Name must be at least 3 characters"),
  description: z.string().min(5, "Description is required"),
});

type FormData = z.infer<typeof createJobSchema>;

const CreateJobModal = ({ onClose}: { onClose: () => void}) => {
  const queryClient = useQueryClient();
  const mutation = useCreateJob();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(createJobSchema),
  });

  const onSubmit = (data: FormData) => {
    mutation.mutate(data, {
      onSuccess: () => {
        toast.success("Job created");
        queryClient.invalidateQueries({ queryKey: ["jobs"] });
        onClose();
      },
      onError: () => toast.error("Failed to create job"),
    });
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="bg-white dark:bg-gray-900 p-6 rounded-lg w-full max-w-md space-y-4"
      >
        <h2 className="text-lg font-bold">Create Job</h2>

        {/* Name */}
        <div>
          <input
            {...register("name")}
            placeholder="Job Name"
            className="w-full border p-2 rounded"
          />
          {errors.name && (
            <p className="text-red-500 text-sm">
              {errors.name.message}
            </p>
          )}
        </div>

        {/* Description */}
        <div>
          <textarea
            {...register("description")}
            placeholder="Description"
            className="w-full border p-2 rounded"
          />
          {errors.description && (
            <p className="text-red-500 text-sm">
              {errors.description.message}
            </p>
          )}
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="text-sm text-gray-500"
          >
            Cancel
          </button>

          <button
            type="submit"
            disabled={mutation.isPending}
            className="bg-blue-500 text-white px-4 py-2 rounded disabled:opacity-50">
            {mutation.isPending ? "Creating..." : "Create"}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CreateJobModal;