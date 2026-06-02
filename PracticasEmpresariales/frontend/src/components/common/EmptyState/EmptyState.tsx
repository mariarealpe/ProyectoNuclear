import { ReactNode } from 'react'

interface EmptyStateProps {
    icon?: string
    title: string
    description?: string
    action?: ReactNode
}

export default function EmptyState({ icon = '📭', title, description, action }: EmptyStateProps) {
    return (
        <div className="flex flex-col items-center justify-center py-12 text-center">
            <span className="text-5xl mb-4">{icon}</span>
            <p className="font-semibold text-gray-600">{title}</p>
            {description && (
                <p className="text-sm text-gray-400 mt-1 max-w-xs">{description}</p>
            )}
            {action && <div className="mt-4">{action}</div>}
        </div>
    )
}
