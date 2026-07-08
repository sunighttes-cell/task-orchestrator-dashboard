export interface UserProfile {
    id: number;
    username: string;
    role: "USER" | "ADMIN";
    fullName: string;
    email: string;
    profilePictureUrl?: string;
}

export interface UpdateUserProfileRequest {
    fullName: string;
    email: string;
}

export interface UploadProfilePictureRequest {
    profilePictureUrl: File;
}

export interface UpdatePasswordRequest {
    currentPassword: string;
    newPassword: string;
}

export interface UpdatePasswordRequestSchema {
    currentPassword: string;
    newPassword: string;
    confirmNewPassword: string;
}

export interface UserTextInputType {
    name: string;
    placeholder: string;
    errors: object;
}

export interface PrimaryBtnType {
    title: string;
    isLoading: boolean;
}

export interface SecondaryBtnType {
    title: string;
    onClick: () => void;
}

