import { InputHTMLAttributes } from 'react'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    label?: string
    error?: string
}

export default function Input({ label, error, id, className = '', ...rest }: InputProps) {
    return (
        <div className="flex flex-col gap-1">
            {label && (
                <label htmlFor={id} className="text-sm font-medium text-gray-700">
                    {label}
                </label>
            )}
            <input
                id={id}
                className={`input-field ${error ? 'border-red-400 ring-1 ring-red-300' : ''} ${className}`.trim()}
                {...rest}
            />
            {error && <p className="text-xs text-red-600">{error}</p>}
        </div>
    )
}
