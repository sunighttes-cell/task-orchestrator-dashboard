import { useAuthLogin } from "@/hooks/auth/useAuthLogin";
import { PrimaryBtnClass } from "@/lib/constants";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useNavigate } from 'react-router-dom';
import { useForm } from "react-hook-form";
import { useState } from "react";

const loginDataSchema = z.object({
  username: z.string().min(4, "Username must be at least 4 characters"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

type FormData = z.infer<typeof loginDataSchema>;

export default function LoginPage() {
  const mutation = useAuthLogin();
  const navigate = useNavigate();
  // const [loginErr, setLoginErr] = useState(null);
  const [loginErr, setLoginErr] = useState<string | null>(null);
  
const {
  register,
  handleSubmit,
  formState: { errors, isValid },
} = useForm<FormData>({
  resolver: zodResolver(loginDataSchema),
  mode: "onChange",
});

  console.log("errors", errors);
  console.log("isValid", isValid);

  const onSubmit = (data: FormData) => {
    // Extract username and password from form inputs
    const credentials = {
      username: data.username,
      password: data.password,
    };

    console.log("Login credentials:", credentials);
    mutation.mutate(credentials, {
    onSettled: () => {
      if(!mutation.data) { 
        setLoginErr("Unable to Login, Please Try Again!");
      }} 
    });
  };

  console.log("Login Page: token:", mutation.data, "Login error:", mutation.error);
  
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50">
      <form
        onSubmit={handleSubmit(onSubmit)}
        className="bg-white dark:bg-gray-900 p-6 rounded-lg w-full max-w-md space-y-4">
        <h2 className="text-lg font-bold">Login</h2>
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

        {/* Actions */}
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={() => navigate('/register')}
            className={PrimaryBtnClass}
          >
            Register new user
          </button>
          <button
            type="submit"
            disabled={mutation.isPending}
            className={PrimaryBtnClass}>
            {mutation.isPending ? "Logging in..." : "Login"}
          </button>
        </div>
        {loginErr && (
        <div className="flex justify-end gap-2">
          <p className="text-red-500 text-sm">{loginErr}</p>
        </div>
        )}
      </form>
    </div>
  );
}