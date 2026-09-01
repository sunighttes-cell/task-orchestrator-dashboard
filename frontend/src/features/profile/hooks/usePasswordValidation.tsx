import { useMemo } from "react";

import { validatePasswordStrength } from "../utils/passwordStrength";
import { isTooSimilar } from "../utils/passwordSimilarity";

export function usePasswordValidation(
  currentPassword: string,
  newPassword: string,
  confirmPassword: string, 
  username: string,
) {

  return useMemo(() => {

    const strength = validatePasswordStrength(newPassword);

    const rules = {

      ...strength,

      differentFromCurrent:
        currentPassword !== newPassword,

      notTooSimilar:
        !isTooSimilar(currentPassword, newPassword),
        
      notSimilarToUsername:
        !isTooSimilar(username, newPassword),

      matchesConfirmation:
        newPassword === confirmPassword

    };

    const valid = Object.values(rules)?.every(Boolean);
    const score =
      Object.values(rules)?.filter(Boolean)?.length;

    return {
      valid,
      score,
      rules
    };

  }, [
    currentPassword,
    newPassword,
    confirmPassword
  ]);
}