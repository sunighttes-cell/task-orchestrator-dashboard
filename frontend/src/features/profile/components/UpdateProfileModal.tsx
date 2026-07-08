import { useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { PrimaryBtnClass, SecondaryBtnClass } from "@/lib/constants";
import { useProfileUpdate } from "@/hooks/profile/useProfileUpdate";
import { useAuth } from "@/auth/AuthContext";
import { useNavigate } from 'react-router-dom';
import type { UpdateUserProfileRequest } from "@/types/profile";

const updateProfileSchema = z.object({
  fullName: z.string().min(3, "Name must be at least 3 characters"),
  email: z.string().email("Invalid email address"),
});

type FormData = z.infer<typeof updateProfileSchema>;

const UpdateProfileModal = ({ onClose }: { onClose: () => void}) => {
  const { auth } = useAuth();
  const { user } = auth || {};

  const mutation = useProfileUpdate();
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(updateProfileSchema),
    mode: "onChange",
  });
  
  const onSubmit = (data: FormData) => {
    const updateProfileRequest: UpdateUserProfileRequest = {
        fullName: data.fullName,
        email: data.email,
    };
    try { 
        setIsLoading(true);
        mutation.mutate(updateProfileRequest)
        toast.success("Updated");
        navigate('/profile');
        onClose();}
    catch (error) {
        console.log(error);
        toast.error("Something went wrong");}
    finally {
        setIsLoading(false);
    }};

    return (
      <div className="fixed inset-0 flex items-center justify-center bg-black/50">
        <form onSubmit={handleSubmit(onSubmit)} className="bg-white dark:bg-gray-900 p-6 rounded-lg w-full max-w-md space-y-4">
        <h2 className="text-lg font-bold">Update Profile</h2>
        
        {/* Fullname */}
        <div>
          <input
            {...register("fullName")}
            type="text"
            placeholder="Full Name"
            className="w-full border p-2 rounded"
          />
          {errors.fullName && (
            <p className="text-red-500 text-sm">
              {errors.fullName.message}
            </p>
          )}
        </div>

        {/* Email */}
        <div>
          <input
            {...register("email")}
            type="email"
            placeholder="email"
            className="w-full border p-2 rounded"
          />
          {errors.email && (
            <p className="text-red-500 text-sm">
              {errors.email.message}
            </p>
          )}
        </div>
        <div className="flex justify-end gap-2">
            <button
                type="button"
                onClick={onClose}
                className={SecondaryBtnClass}>Cancel
            </button>
            <button
                type="submit"
                disabled={isLoading}
                className={PrimaryBtnClass}>Update Profile
            </button>
        </div>
        </form>
      </div>
    );
  }

export default UpdateProfileModal;