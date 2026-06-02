import { NavLink } from 'react-router-dom'
import { useAuth } from '../../../context/AuthContext'
import { MENUS_POR_ROL } from '../../../constants/Menus'

/**
 * PATRON MEDIATOR — Frontend
 *
 * El Sidebar consulta el rol del usuario al AuthContext (Mediator)
 * y renderiza únicamente los ítems del menú permitidos para ese rol.
 * Ningún rol ve módulos que no le corresponden.
 */
export default function Sidebar() {
    const { user, logout } = useAuth()
    if (!user) return null

    const menuItems = MENUS_POR_ROL[user.rol] ?? []

    return (
        <aside className="w-64 bg-cue-primary text-white flex flex-col shadow-lg">
            {/* Logo / header */}
            <div className="p-5 border-b border-blue-800 flex items-center gap-3">
                <div className="w-8 h-8 bg-cue-accent rounded-md flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                    P
                </div>
                <div>
                    <h1 className="text-sm font-bold leading-tight">Prácticas Empresariales</h1>
                    <p className="text-blue-300 text-xs">Univ. Alexander Von Humboldt</p>
                </div>
            </div>

            {/* Menú dinámico por rol */}
            <nav className="flex-1 overflow-y-auto py-2">
                {menuItems.map((item) => (
                    <NavLink
                        key={item.id}
                        to={item.ruta}
                        className={({ isActive }) =>
                            `flex items-center gap-3 px-4 py-2.5 text-sm transition-colors border-l-4 ${
                                isActive
                                    ? 'border-cue-accent bg-white/10 text-white font-semibold'
                                    : 'border-transparent text-blue-200 hover:bg-white/5 hover:text-white'
                            }`
                        }
                    >
                        <span className="text-base">{item.icono}</span>
                        <span>{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            {/* Logout */}
            <div className="p-4 border-t border-blue-800">
                <button
                    onClick={logout}
                    className="w-full text-sm text-blue-300 hover:text-white transition-colors text-left flex items-center gap-2 py-1"
                >
                    <span>🚪</span> Cerrar sesión
                </button>
            </div>
        </aside>
    )
}
