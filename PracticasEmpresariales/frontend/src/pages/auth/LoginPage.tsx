import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

interface CredencialPrueba {
    rol: string
    correo: string
    password: string
}

const CREDENCIALES_PRUEBA: CredencialPrueba[] = [
    { rol: 'Administrador DTI',         correo: 'dti@cue.edu.co',            password: 'Admin2026!' },
    { rol: 'Coordinación Académica',    correo: 'coordinacion@cue.edu.co',   password: 'Test2026!' },
    { rol: 'Secretaría Académica',      correo: 'secretaria@cue.edu.co',     password: 'Test2026!' },
    { rol: 'Coordinador de Prácticas',  correo: 'coordpracticas@cue.edu.co', password: 'Test2026!' },
    { rol: 'Docente Asesor',            correo: 'docente@cue.edu.co',        password: 'Test2026!' },
    { rol: 'Tutor Empresarial',         correo: 'tutor@cue.edu.co',          password: 'Test2026!' },
    { rol: 'Estudiante Practicante',    correo: 'estudiante@cue.edu.co',     password: 'Test2026!' },
    { rol: 'Dirección',                 correo: 'direccion@cue.edu.co',      password: 'Test2026!' },
]

export default function LoginPage() {
    const { login, loading } = useAuth()
    const navigate = useNavigate()
    const [correo, setCorreo] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [mostrarCredenciales, setMostrarCredenciales] = useState(true)

    const usarCredencial = (c: CredencialPrueba) => {
        setCorreo(c.correo)
        setPassword(c.password)
        setError('')
    }

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault()
        setError('')
        try {
            await login(correo, password)
            navigate('/dashboard', { replace: true })
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })
                ?.response?.data?.mensaje
            setError(msg ?? 'Credenciales incorrectas o cuenta inactiva.')
        }
    }

    return (
        <div className="min-h-screen flex">
            {/* Panel izquierdo — branding */}
            <div className="hidden lg:flex lg:w-1/2 bg-cue-primary flex-col justify-between p-12">
                <div>
                    <div className="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center mb-8">
                        <span className="text-white font-bold text-lg">P</span>
                    </div>
                    <h1 className="text-white text-4xl font-bold leading-tight">
                        Sistema de<br />Prácticas<br />Empresariales
                    </h1>
                    <p className="text-blue-300 mt-4 text-base leading-relaxed">
                        Gestión integral de prácticas académicas para estudiantes, coordinadores y empresas.
                    </p>
                </div>
                <div>
                    <p className="text-blue-200 text-sm font-medium">Universidad Alexander Von Humboldt</p>
                    <p className="text-blue-400 text-xs mt-1">© 2025 CUE</p>
                </div>
            </div>

            {/* Panel derecho — formulario */}
            <div className="flex-1 flex items-center justify-center bg-gray-50 p-8">
                <div className="w-full max-w-sm">
                    {/* Título visible solo en móvil */}
                    <div className="lg:hidden mb-8 text-center">
                        <h1 className="text-xl font-bold text-cue-primary">Sistema de Prácticas</h1>
                        <p className="text-gray-500 text-sm mt-1">Universidad Alexander Von Humboldt</p>
                    </div>

                    <h2 className="text-2xl font-bold text-gray-900">Bienvenido</h2>
                    <p className="text-gray-500 text-sm mt-1 mb-8">Ingresa con tu cuenta institucional</p>

                    {error && (
                        <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3 mb-6 text-sm">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit} className="space-y-5">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Correo electrónico
                            </label>
                            <input
                                type="email"
                                value={correo}
                                onChange={(e) => setCorreo(e.target.value)}
                                className="input-field"
                                placeholder="usuario@cue.edu.co"
                                required
                                autoComplete="email"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Contraseña
                            </label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="input-field"
                                placeholder="••••••••"
                                required
                                autoComplete="current-password"
                            />
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full btn-primary py-3 flex items-center justify-center"
                        >
                            {loading ? (
                                <span className="animate-spin mr-2">⟳</span>
                            ) : null}
                            {loading ? 'Verificando...' : 'Ingresar'}
                        </button>
                    </form>

                    <p className="text-xs text-gray-400 mt-8 text-center">
                        Si no recuerdas tu contraseña, contacta al Administrador DTI.
                    </p>

                    {/* Panel de credenciales de prueba */}
                    <div className="mt-8 border-t border-gray-200 pt-6">
                        <button
                            type="button"
                            onClick={() => setMostrarCredenciales(v => !v)}
                            className="w-full flex items-center justify-between text-xs font-semibold text-gray-600 hover:text-cue-primary transition-colors"
                        >
                            <span>Credenciales de prueba (click para usar)</span>
                            <span className={`transition-transform ${mostrarCredenciales ? 'rotate-180' : ''}`}>▾</span>
                        </button>

                        {mostrarCredenciales && (
                            <div className="mt-3 bg-amber-50 border border-amber-200 rounded-lg p-3 text-xs">
                                <p className="text-amber-800 mb-2 leading-snug">
                                    Solo para entornos de desarrollo. Quítalo antes de producción.
                                </p>
                                <ul className="space-y-1.5">
                                    {CREDENCIALES_PRUEBA.map(c => (
                                        <li key={c.correo}>
                                            <button
                                                type="button"
                                                onClick={() => usarCredencial(c)}
                                                className="w-full text-left px-2 py-1.5 rounded hover:bg-amber-100 transition-colors"
                                            >
                                                <span className="font-semibold text-gray-800">{c.rol}</span>
                                                <br />
                                                <span className="text-gray-600 font-mono">
                                                    {c.correo} · {c.password}
                                                </span>
                                            </button>
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
}
