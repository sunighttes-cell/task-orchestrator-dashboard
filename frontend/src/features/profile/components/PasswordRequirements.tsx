import type {PasswordValidationResult} from "@/types/passwordValidation"

interface Props {
  rules: PasswordValidationResult["rules"];
}

interface ItemProps {
  ok: boolean;
  label: string;
}

export function PasswordRequirementItem ({ok, label}: ItemProps){
  return (
    <div className={ ok
          ? "text-green-600"
          : "text-gray-500"
      }>
      {ok ? "✓" : "○"} {label}
    </div>
  );
}

export function PasswordRequirements({rules}: Props) {
  return (
    <div className="space-y-1 text-sm">
      <PasswordRequirementItem
        ok={rules.minLength}
        label="At least 8 characters"/>

      <PasswordRequirementItem
        ok={rules.uppercase}
        label="Uppercase letter"/>

      <PasswordRequirementItem
        ok={rules.lowercase}
        label="Lowercase letter"/>

      <PasswordRequirementItem
        ok={rules.number}
        label="Number"/>

      <PasswordRequirementItem
        ok={rules.special}
        label="Special character"/>

      <PasswordRequirementItem
        ok={rules.differentFromCurrent}
        label="Different from current password"/>

      <PasswordRequirementItem
        ok={rules.notTooSimilar}
        label="Not too similar to current password"/>
      
      <PasswordRequirementItem
        ok={rules.notSimilarToUsername}
        label="Not too similar to username"/>

      <PasswordRequirementItem
        ok={rules.matchesConfirmation}
        label="Passwords match"/>
    </div>
  );
}