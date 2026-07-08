import { useEffect, useRef, useState } from "react";
import { Upload, X } from "lucide-react";

import Avatar from "./Avatar";
import { PrimaryBtnClass } from "@/lib/constants";
import { useUploadAvatar } from "@/hooks/profile/useUploadAvatar";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";
import { useProfile } from "@/hooks/profile/useProfile";

interface AvatarUploadProps {
    currentAvatar?: string | null;
}

const AvatarUpload: React.FC<AvatarUploadProps> = ({
}) => {
    const inputRef = useRef<HTMLInputElement>(null);

    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [isUploading, setIsUploading] = useState(false);
    const { data: profile, refetch } = useProfile();
    console.log("Current profile in AvatarUpload:", profile);
    const baseUrl = import.meta.env.VITE_API_BASE_URL + "/"
    const avatarSrc = profile?.profilePictureUrl && !profile.profilePictureUrl.startsWith('http') ? `${baseUrl}${profile.profilePictureUrl}` : profile?.profilePictureUrl;
    console.log("Avatar src in AvatarUpload:", avatarSrc);

    const uploading = () => setIsUploading(true);
    const doneUploading = () => setIsUploading(false);

    const uploadMutation = useUploadAvatar();

    useEffect(() => {
        return () => {
            if (previewUrl) {
                URL.revokeObjectURL(previewUrl);
            }
        };
    }, [previewUrl]);

    const handleBrowse = () => {
        inputRef.current?.click();
    };

    const handleFileChange = (
        event: React.ChangeEvent<HTMLInputElement>
    ) => {
        const file = event.target.files?.[0];

        if (!file) return;

        if (!file.type.startsWith("image/")) {
            alert("Please select an image.");
            return;
        }

        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
        }

        setSelectedFile(file);
        setPreviewUrl(URL.createObjectURL(file));
    };

    const handleUpload = () => {
        if (!selectedFile) return;

        uploading();
        uploadMutation.mutate(selectedFile, {
            onSuccess: (updatedProfile) => {
                // Clear preview after successful upload
                reset();
                toast.success("Profile picture updated.");
                console.log("Profile refetched successfully.", updatedProfile);
                // Refetch to ensure profile data is fresh
                refetch();
                doneUploading();
            },
            onError: (error) => {
                console.error("Upload error:", error);
                toast.error("Unable to upload profile picture.");
                doneUploading();
            }
        });
    };

    const reset = () => {
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
        }

        setPreviewUrl(null);
        setSelectedFile(null);

        if (inputRef.current) {
            inputRef.current.value = "";
        }
    };

    return (
        <div className="space-y-4">
            <Avatar src={previewUrl ?? avatarSrc} size="xl" />
            <div>
                <Label>Upload Profile Picture<Upload size={16} onClick={uploading}/></Label>
            </div>
            {isUploading && (
            <div>
            <input
                ref={inputRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleFileChange}
            />

            <div className="flex gap-3">
                <button
                    type="button"
                    className={PrimaryBtnClass}
                    onClick={handleBrowse}>Browse
                </button>

                <button
                    type="button"
                    className={PrimaryBtnClass}
                    disabled={
                        !selectedFile ||
                        uploadMutation.isPending
                    }
                    onClick={handleUpload}
                >
                    {uploadMutation.isPending
                        ? "Uploading..."
                        : "Save"}
                </button>

                {selectedFile && (
                    <button
                        type="button"
                        className={PrimaryBtnClass}
                        onClick={reset}
                    >
                        <X size={16} />
                    </button>
                )}

            </div>
            </div>
            )}
        </div>
    );
};

export default AvatarUpload;