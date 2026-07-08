import { useState } from "react";
import { useProfile } from "@/hooks/profile/useProfile";
import { useAuth } from "@/auth/AuthContext";
import Avatar from "./Avatar";
import { Link } from "react-router-dom";
import { LogOut, User } from "lucide-react";

interface ProfileAvatarProps {
  size?: "sm" | "md" | "lg" | "xl";
}

const ProfileAvatar: React.FC<ProfileAvatarProps> = ({size = "md" }) => {
  const { data: profile, refetch } = useProfile();
  const { logout } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const baseUrl = import.meta.env.VITE_API_BASE_URL + "/"
  const avatarSrc = profile?.profilePictureUrl && !profile.profilePictureUrl.startsWith('http') ? `${baseUrl}${profile.profilePictureUrl}` : profile?.profilePictureUrl;
  
  console.log("Current profile in profileAvatar:", profile);
  console.log("Avatar src in profileAvatar:", avatarSrc);

  const handleLogout = () => {
    const confirmed = window.confirm("Are you sure you want to logout?");
    if (confirmed) {
      logout();
    }
  };

  if (!profile) {
    return null;
  }

  return (
    <div className="relative">
      <div className="flex items-center gap-2">
      <span className="text-sm font-semibold text-gray-900 dark:text-white">
        {profile && (
          <div className="flex items-center gap-2">
            <span>Welcome, {profile.fullName}</span>
            {profile.role === "ADMIN" && (
              <span className="rounded-full bg-blue-600 px-2 py-0.5 text-xs font-semibold text-white">
                ADMIN
              </span>
            )}
          </div>
        )}
      </span>
      <span>
      <button
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Open profile menu"
      >
        <Avatar
          src={avatarSrc}
          alt={profile.fullName}
          size={size}
        />
      </button>
      </span>
      </div>

      {isOpen && (
        <div className="absolute right-0 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-50">
          <div className="px-4 py-3 border-b border-gray-200 dark:border-gray-700">
            <div className="text-sm font-semibold text-gray-900 dark:text-white">
              {profile.fullName}
            </div>
            <div className="text-xs text-gray-500 dark:text-gray-400">
              @{profile.username}
            </div>
          </div>

          <Link
            to="/profile"
            onClick={() => setIsOpen(false)}
            className="flex items-center gap-2 w-full px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
          >
            <User size={16} />
            View Profile
          </Link>

          <button
            onClick={() => {
              handleLogout();
              setIsOpen(false);
            }}
            className="flex items-center gap-2 w-full px-4 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-gray-100 dark:hover:bg-gray-700 border-t border-gray-200 dark:border-gray-700"
          >
            <LogOut size={16} />
            Logout
          </button>
        </div>
      )}

      {isOpen && (
        <div
          className="fixed inset-0 z-40"
          onClick={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

export default ProfileAvatar;
