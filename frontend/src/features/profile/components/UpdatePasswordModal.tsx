import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { PrimaryBtnClass, SecondaryBtnClass } from "@/lib/constants";
import { useProfile } from "@/hooks/profile/useProfile";
import { useNavigate } from 'react-router-dom';
import { usePasswordValidation } from "../hooks/usePasswordValidation"
import type { UpdatePasswordRequest } from "@/types/profile";
import { PasswordRequirements } from "./PasswordRequirements";
import { usePasswordUpdate } from "@/hooks/profile/usePasswordUpdate";


const updatePasswordSchema = z.object({
  currentPassword: z.string().min(8, "Password must be at least 8 characters"),
  newPassword: z.string().min(8, "Password must be at least 8 characters"),
  confirmNewPassword: z.string().min(8, { message: 'Please confirm your password' }),
}); 

type FormData = z.infer<typeof updatePasswordSchema>;

const UpdatePasswordModal = ({ onClose }: { onClose: () => void}) => {
  const mutation = usePasswordUpdate();
  const navigate = useNavigate();

  const { data: profile, refetch } = useProfile();

  const {
    register,
    watch,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(updatePasswordSchema),
    mode: "onSubmit",
  });

const currentPassword = watch("currentPassword") || "";
const newPassword = watch("newPassword") || "";
const confirmNewPassword = watch("confirmNewPassword") || "";
const profileUsername = profile?.username || "";

const validation = usePasswordValidation(currentPassword, newPassword, confirmNewPassword, profileUsername);
  
const onSubmit = (data: FormData) => {
  const updatePasswordRequest: UpdatePasswordRequest = {
      currentPassword: data.currentPassword,
      newPassword: data.newPassword,
  };
  
  if (!validation?.valid) return;
  mutation.mutate(updatePasswordRequest, {
  onSuccess: () => {
      toast.success("Profile password updated.");
      // Refetch to ensure profile data is fresh
      refetch();
      navigate('/profile');
      onClose();
    },
    onError: (error) => {
        console.error("Password error:", error);
        toast.error("Error Updating password");
    }});
};

  return (
      <div className="fixed inset-0 flex items-center justify-center bg-black/50">
        <form onSubmit={handleSubmit(onSubmit)} className="bg-white dark:bg-gray-900 p-6 rounded-lg w-full max-w-md space-y-4">
        <h2 className="text-lg font-bold">Update Password</h2>
        
        {/* Current Password */}
        <div>
          <input
            {...register("currentPassword")}
            type="password"
            placeholder="Current Password"
            className="w-full border p-2 rounded"
          />
          {errors.currentPassword && (
            <p className="text-red-500 text-sm">
              {errors.currentPassword.message}
            </p>
          )}
        </div>

        {/* New Password */}
        <div>
          <input
            {...register("newPassword")}
            type="password"
            placeholder="New Password"
            className="w-full border p-2 rounded"
          />
          {errors.newPassword && (
            <p className="text-red-500 text-sm">
              {errors.newPassword.message}
            </p>
          )}
        </div>

        {/* Confirm New Password */}
        <div>
          <input
            {...register("confirmNewPassword")}
            type="password"
            placeholder="Confirm New Password"
            className="w-full border p-2 rounded"
          />
          {errors.confirmNewPassword && (
            <p className="text-red-500 text-sm">
              {errors.confirmNewPassword.message}
            </p>
          )}
        </div>
        {/* Actions*/}
        <div className="flex justify-end gap-2">
            <button
                type="button"
                onClick={onClose}
                className={SecondaryBtnClass}>Cancel
            </button>
            <button
                type="submit"
                disabled={!validation?.valid || mutation.isPending}
                className={PrimaryBtnClass}>Update Password
            </button>
        </div>
        <div className="flex justify-end"> <PasswordRequirements rules={validation.rules}/> </div>
        </form>
      </div>
    );
  }

export default UpdatePasswordModal;