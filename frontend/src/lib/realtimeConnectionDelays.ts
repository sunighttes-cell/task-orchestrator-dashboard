export function getReconnectDelay(attempt: number): number {
  return Math.min(
    1000 * Math.pow(2, attempt),
    30000,
  );
}

export function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}