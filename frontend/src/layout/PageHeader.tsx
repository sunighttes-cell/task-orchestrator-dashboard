
interface Props {
   title: string
   status?: string
   description?: string
}

export function PageHeader({ title, description, status }: Props) {
  return (
      <div className="space-y-1">
        <h1 className="text-2xl font-bold tracking-tight">
          {title}
        </h1>

        {description && (
          <p className="text-muted-foreground">
            {description}
          </p>
        )}
        {status === "CONNECTED" && (
        <span>Live updates connected</span>
        )}

        {status === "RECONNECTING" && (
        <span>Reconnecting...</span>
        )}

        {status === "ERROR" && (
        <span>Live updates unavailable</span>
        )}
      </div>
  )
}