export const buildQueryParams = (filters: Record<string, any>) => {
  return new URLSearchParams(
    Object.fromEntries(
      Object.entries(filters).filter(
        ([_, v]) => v !== undefined && v !== null && v !== ""
      )
    )
  ).toString();
};