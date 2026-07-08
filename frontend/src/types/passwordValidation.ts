// Password Validation

export interface PasswordValidationResult {
  valid: boolean;
  score: number;

  rules: {
    minLength: boolean;
    maxLength: boolean;
    uppercase: boolean;
    lowercase: boolean;
    number: boolean;
    special: boolean;
    differentFromCurrent: boolean;
    notTooSimilar: boolean;
    notSimilarToUsername: boolean;
    matchesConfirmation: boolean;
  };
  
}