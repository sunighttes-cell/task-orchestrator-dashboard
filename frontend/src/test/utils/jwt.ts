import { jwtDecode } from "jwt-decode";
import type { CurrentUser, JwtPayload } from "@/types/auth";

export function decodeUser(token: string): CurrentUser {
    const payload = jwtDecode<JwtPayload>(token);

    return {
        username: payload.sub,
        role: (payload.role || "USER") as "USER" | "ADMIN"
    };
}