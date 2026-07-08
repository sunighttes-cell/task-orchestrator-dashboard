
export function validatePasswordStrength(password: string) {
  return {
    minLength: password.length >= 8,
    maxLength: password.length <= 50,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number: /\d/.test(password),
    special: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password),
  };
}
