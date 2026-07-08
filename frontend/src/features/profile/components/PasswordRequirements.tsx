import type {PasswordValidationResult} from "@/types/passwordValidation"

interface Props {
  rules: PasswordValidationResult["rules"];
}

export function PasswordRequirements({
  rules,
}: Props) {

  const Item = ({
    ok,
    label,
  }: {
    ok: boolean;
    label: string;
  }) => (

    <div
      className={
        ok
          ? "text-green-600"
          : "text-gray-500"
      }
    >
      {ok ? "✓" : "○"} {label}
    </div>

  );

  return (

    <div className="space-y-1 text-sm">

      <Item
        ok={rules.minLength}
        label="At least 8 characters"/>

      <Item
        ok={rules.uppercase}
        label="Uppercase letter"/>

      <Item
        ok={rules.lowercase}
        label="Lowercase letter"/>

      <Item
        ok={rules.number}
        label="Number"/>

      <Item
        ok={rules.special}
        label="Special character"/>

      <Item
        ok={rules.differentFromCurrent}
        label="Different from current password"/>

      <Item
        ok={rules.notTooSimilar}
        label="Not too similar to current password"/>
      
      <Item
        ok={rules.notSimilarToUsername}
        label="Not too similar to username"/>

      <Item
        ok={rules.matchesConfirmation}
        label="Passwords match"/>
    </div>

  );
}