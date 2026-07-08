
export function isTooSimilar(
  current: string,
  replacement: string
): boolean {

  if (!current || !replacement) {
    return false;
  }

  const oldPassword = current.toLowerCase();
  const newPassword = replacement.toLowerCase();

  if (
    newPassword.includes(oldPassword) ||
    oldPassword.includes(newPassword)
  ) {
    return true;
  }

  for (let length = 4; length <= oldPassword.length; length++) {
    for (let start = 0; start <= oldPassword.length - length; start++) {
      const fragment = oldPassword.substring(start, start + length);

      if (newPassword.includes(fragment)) {
        return true;
      }
    }
  }

  return false;
}