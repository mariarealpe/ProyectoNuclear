import { ButtonHTMLAttributes, ReactNode } from 'react'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'danger'
    size?: 'sm' | 'md'
    loading?: boolean
    children: ReactNode
}

export default function Button({
    variant = 'primary',
    size = 'md',
    loading = false,
    disabled,
    children,
    className = '',
    ...rest
}: ButtonProps) {
    const base =
        variant === 'primary' ? 'btn-primary' :
        variant === 'danger'  ? 'btn-danger'  :
                                'btn-secondary'
    const sz = size === 'sm' ? '!text-xs !py-1.5 !px-3' : ''

    return (
        <button
            {...rest}
            disabled={disabled || loading}
            className={`${base} ${sz} ${loading ? 'opacity-70 cursor-wait' : ''} ${className}`.trim()}
        >
            {loading ? 'Cargando...' : children}
        </button>
    )
}
