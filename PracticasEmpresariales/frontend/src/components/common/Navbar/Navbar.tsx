import { useAuth } from '../../../context/AuthContext'
import { ROL_LABELS } from '../../../constants/roles'

export default function Navbar() {
    const { user } = useAuth()
    if (!user) return null

    return (
        <header className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between shadow-sm">
            <span className="text-sm font-semibold text-cue-primary tracking-wide">
                Prácticas Empresariales · CUE
            </span>

            <div className="flex items-center gap-3">
                {user.rol === 'DIRECCION' && (
                    <span className="text-xs bg-amber-100 text-amber-800 px-3 py-1 rounded-full font-medium">
                        Solo lectura
                    </span>
                )}
                <div className="flex items-center gap-2.5 bg-gray-50 border border-gray-200 rounded-full px-3 py-1.5">
                    <div className="text-right">
                        <p className="text-xs font-semibold text-gray-800 leading-none">{user.nombre}</p>
                        <p className="text-xs text-gray-500 mt-0.5">{ROL_LABELS[user.rol]}</p>
                    </div>
                    <div className="w-7 h-7 rounded-full bg-cue-primary text-white flex items-center justify-center text-xs font-bold flex-shrink-0">
                        {user.nombre.charAt(0).toUpperCase()}
                    </div>
                </div>
            </div>
        </header>
    )
}
