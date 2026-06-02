import { ReactNode } from 'react'

type BadgeVariant = 'success' | 'warning' | 'danger' | 'info' | 'default' | 'purple'

interface BadgeProps {
    variant?: BadgeVariant
    children: ReactNode
    className?: string
}

const CLASSES: Record<BadgeVariant, string> = {
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    danger:  'bg-red-100 text-red-700',
    info:    'bg-blue-100 text-blue-800',
    default: 'bg-gray-200 text-gray-700',
    purple:  'bg-purple-100 text-purple-800',
}

export default function Badge({ variant = 'default', children, className = '' }: BadgeProps) {
    return (
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${CLASSES[variant]} ${className}`}>
            {children}
        </span>
    )
}
