import { ReactNode } from 'react'

interface PageHeaderProps {
    title: string
    description?: string
    action?: ReactNode
    badge?: ReactNode
}

export default function PageHeader({ title, description, action, badge }: PageHeaderProps) {
    return (
        <div className="flex items-center justify-between gap-4 flex-wrap">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
                {description && (
                    <p className="text-sm text-gray-500 mt-0.5">{description}</p>
                )}
            </div>
            {(action || badge) && (
                <div className="flex items-center gap-3 flex-wrap">
                    {badge}
                    {action}
                </div>
            )}
        </div>
    )
}
