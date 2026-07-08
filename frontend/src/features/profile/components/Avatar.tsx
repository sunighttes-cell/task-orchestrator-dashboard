import { User } from "lucide-react";

interface AvatarProps {
    src?: string | null;
    alt?: string;
    size?: "sm" | "md" | "lg" | "xl";
    className?: string;
}

const sizeClasses = {
    sm: "h-8 w-8",
    md: "h-12 w-12",
    lg: "h-16 w-16",
    xl: "h-24 w-24",
};

const Avatar: React.FC<AvatarProps> = ({
    src,
    alt = "Profile picture",
    size = "lg",
    className = "",
}) => {
    return (
        <div
            className={`
                ${sizeClasses[size]}
                rounded-full
                overflow-hidden
                bg-gray-100
                border
                flex
                items-center
                justify-center
                ${className}
            `}
        >{src ? (
                <img
                    src={src}
                    alt={alt}
                    className="h-full w-full object-cover"
                />
            ) : (
                <User className="text-gray-400" size={28} />
            )}
        </div>
    );
};

export default Avatar;