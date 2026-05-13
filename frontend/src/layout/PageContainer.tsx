
interface Props {
  children: React.ReactNode
}
export function PageContainer({children}: Props) {
  return (
    <div className="mx-auto w-full max-w-7xl p-6">
      {children}
    </div>
  );
}
