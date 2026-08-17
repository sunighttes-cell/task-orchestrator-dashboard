import { useRegisterUser } from "@/hooks/profile/useRegisterUser";
import { PrimaryBtnClass, SecondaryBtnClass } from "@/lib/constants";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate } from 'react-router-dom';
import { useForm } from "react-hook-form";
import type {RegisterUserRequest} from "@/types/auth";
import { usePasswordValidation } from "@/features/profile/hooks/usePasswordValidation"
import { PasswordRequirements } from "@/features/profile/components/PasswordRequirements";

const registerUserDataSchema = z.object({
  username: z.string().min(5, "Username must be at least 5 characters"),
  email: z.string().email("Invalid email address"),
  fullName: z.string().min(3, "Full name must be at least 3 characters"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  confirmPassword: z.string().min(8, "Please confirm password"),
});

type FormData = z.infer<typeof registerUserDataSchema>;

export default function RegisterForm() {
  const mutation = useRegisterUser();
  const navigate = useNavigate();
  
const {
  register,
  watch,
  handleSubmit,
  formState: { errors, isValid },
} = useForm<FormData>({
  resolver: zodResolver(registerUserDataSchema),
  mode: "onSubmit",
});

console.log("errors", errors);
console.log("isValid", isValid);

const username = watch("username") || "";
const newPassword = watch("password") || "";
const confirmPassword = watch("confirmPassword") || "";

console.log("newPassword: ", newPassword, "confirmPassword: ", confirmPassword, "username", username);

const validation = usePasswordValidation("", newPassword, confirmPassword, username);

console.log("Password validation on change", validation);

  const onSubmit = (data: FormData) => {
    console.log("Submitting Register form");
    console.log("Form data:", data);
    console.log("Form data:", data.username, data.email, data.fullName, data.password, data.confirmPassword);

    // Extract username datafrom form inputs
    const registerUserRequest: RegisterUserRequest = {
      username: data.username,
      email: data.email, 
      fullName: data.fullName,
      password: data.password,
    };

    console.log("Password validation on submit: ", validation);

    if (!validation?.valid) return;

    //console.log("Register credentials:", registerUserRequest);
    console.log("UI: Mutating user with request:", registerUserRequest);
    mutation.mutate(registerUserRequest);
  };

  console.log("Register Page: token:", mutation.data, "Register error:", mutation.error);
  
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="bg-white dark:bg-gray-900 p-6 rounded-lg w-full max-w-md space-y-4">
        <h2 className="text-lg font-bold">Register as a new user</h2>
        {/* Username */}
        <div>
          <input
            {...register("username")}
            type="text"
            placeholder="Username"
            className="w-full border p-2 rounded"
          />
          {errors.username && (
            <p className="text-red-500 text-sm">
              {errors.username.message}
            </p>
          )}
        </div>

       {/* Email */}
        <div>
          <input
            {...register("email")}
            type="text"
            placeholder="email"
            className="w-full border p-2 rounded"
          />
          {errors.email && (
            <p className="text-red-500 text-sm">
              {errors.email.message}
            </p>
          )}
        </div>

        {/* Full Name */}
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

        {/* Password */}
        <div>
          <input
            {...register("password")}
            type="password"
            placeholder="Password"
            className="w-full border p-2 rounded"
          />
          {errors.password && (
            <p className="text-red-500 text-sm">
              {errors.password.message}
            </p>
          )}
        </div>

        {/* Confirm Password */}
        <div>
          <input
            {...register("confirmPassword")}
            type="password"
            placeholder="Confirm Password"
            className="w-full border p-2 rounded"
          />
          {errors.confirmPassword && (
            <p className="text-red-500 text-sm">
              {errors.confirmPassword.message}
            </p>
          )}
        </div>

        {/* Actions */}
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/login')}
            className={SecondaryBtnClass}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={!validation?.valid || mutation.isPending}
            className={PrimaryBtnClass}>
            {mutation.isPending ? "Registering User..." : "Register"}
          </button>
        </div>
        <div className="flex justify-end"> <PasswordRequirements rules={validation.rules}/> </div>
      </form>
    </div>
  );
}