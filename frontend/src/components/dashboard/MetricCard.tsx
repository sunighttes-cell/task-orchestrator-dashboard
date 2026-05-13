import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface MetricCardProps {
  title: string
  value: string | number
  icon?: React.ReactNode
  description?: string
}

export function MetricCard({ title, value, icon, description }: MetricCardProps) {
  return (
    <Card>
        <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle> {title}</CardTitle>
            {icon && <div>{icon}</div>}
        </CardHeader>
        <CardContent>
            <div className="text-3xl font-bold">{value}</div>
            {description && <p className="text-muted-foreground">{description}</p>}
        </CardContent>
    </Card>
  )
}   
