import React, { useState } from "react";
import { useProfile } from "@/hooks/profile/useProfile";
import { PageHeader } from "@/layout/PageHeader";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyData } from "@/components/EmptyData";
import { PrimaryBtnClass } from "@/lib/constants";
import UpdateProfileModal from "@/features/profile/components/UpdateProfileModal";
import UpdatePasswordModal from "@/features/profile/components/UpdatePasswordModal";
import AvatarUpload from "./components/AvatarUpload";
import { Card, CardTitle } from "@/components/ui/card";

const ProfilePage: React.FC = () => {
  const { data: profile, isLoading, isError } = useProfile();
  //modal actions for updating profile 
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
  const openUpdateModal = () => setIsUpdateModalOpen(true);
  const closeUpdateModal = () => setIsUpdateModalOpen(false)
  
  //modal actions for updating password
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const openPasswordModal = () => setIsPasswordModalOpen(true);
  const closePasswordModal = () => setIsPasswordModalOpen(false);

  return (
    <div className="p-6 rounded dark:bg-gray-900 space-y-4">
    {isLoading ? (
        <div role="status" aria-label="Loading">
            <Skeleton />
            <span className="sr-only">Loading...</span>
        </div>
        ) : isError ? (
            <div role="alert">Error loading jobs</div>
        ) : !profile  || Object.keys(profile).length === 0 ? (<EmptyData/>
    ) : (
    <>
      <PageHeader title="Profile Page" description="Displays User Profile Information" />
      <Card className="bg-white dark:bg-gray-900 p-6 rounded-lg w-full max-w-md space-y-4">
        <div className="flex items-left justify-left p-4 space-x-4">
            <CardTitle className="p-4 space-x-4">
              <div className="p-4 space-x-4">
                <AvatarUpload />
              </div>
              <div className="p-4 space-x-4">
                <div className="text-lg font-semibold">{profile.fullName}</div>
                <div className="text-sm text-blue-500">{profile.username}</div>
              </div>
              <div className="p-4 space-x-4">
                <p><span className="font-medium">Email:</span> {profile.email}</p>
                <p><span className="font-medium">Role:</span> {profile.role}</p>
              </div>
              <div className="flex justify-end gap-2">
                <button
                    type="button"
                    onClick={openPasswordModal}
                    className={PrimaryBtnClass}>Update Password
                </button>
                <button
                    type="button"
                    onClick={openUpdateModal}
                    className={PrimaryBtnClass}>Update Profile
                </button>
              </div>
            {isUpdateModalOpen && 
            <UpdateProfileModal onClose={closeUpdateModal}/>}
            {isPasswordModalOpen &&
             <UpdatePasswordModal onClose={closePasswordModal}/>}
          </CardTitle>
        </div>
      </Card>
    </> 
    )}
    </div>
  );
};

export default ProfilePage;
