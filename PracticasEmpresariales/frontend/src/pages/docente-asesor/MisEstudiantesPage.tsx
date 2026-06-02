import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { UsuarioResponse, EstadoEstudiante } from '../../types'
import { usuarioService } from '../../services/usuarioService'

const ESTADO_BADGE = (estado?: EstadoEstudiante) =>
    estado === 'APTO'
        ? <span className="badge-apto">Apto</span>
        : estado === 'NO_APTO'
        ? <span className="badge-no-apto">No Apto</span>
        : <span className="text-xs text-gray-400">—</span>

/**
 * Vista del Docente Asesor — listado de estudiantes asignados.
 * El backend filtra por scope del docente autenticado.
 * Patrón Proxy: el JWT limita automáticamente qué registros se devuelven.
 */
export default function MisEstudiantesPage() {
    const { user } = useAuth()
    const [estudiantes, setEstudiantes] = useState<UsuarioResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [busqueda, setBusqueda] = useState('')

    const cargar = () => {
        setLoading(true)
        usuarioService.listar(0, 50)
            .then(pg => {
                const solo = pg.content.filter(u => u.rol === 'ESTUDIANTE')
                setEstudiantes(solo)
            })
            .finally(() => setLoading(false))
    }

    useEffect(() => { cargar() }, [])

    const filtrados = estudiantes.filter(e =>
        !busqueda ||
        e.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
        e.correo.toLowerCase().includes(busqueda.toLowerCase())
    )

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Mis Estudiantes</h1>
                    <p className="text-sm text-gray-500">
                        Docente: <span className="font-medium">{user?.nombre}</span> — estudiantes asignados a tu cargo.
                    </p>
                </div>
                <input
                    className="input-field max-w-xs"
                    placeholder="Buscar por nombre o correo..."
                    value={busqueda}
                    onChange={e => setBusqueda(e.target.value)}
                />
            </div>

            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                            {['Estudiante', 'Correo', 'Programa', 'Estado', 'Promedio'].map(h => (
                                <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan={5} className="text-center py-10 text-gray-400">Cargando...</td></tr>
                        ) : filtrados.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="text-center py-10 text-gray-400">
                                    {busqueda ? 'Sin resultados para la búsqueda.' : 'No tienes estudiantes asignados aún.'}
                                </td>
                            </tr>
                        ) : filtrados.map(e => (
                            <tr key={e.id} className="border-b border-gray-100 hover:bg-gray-50">
                                <td className="px-4 py-3 font-medium text-gray-900">{e.nombre}</td>
                                <td className="px-4 py-3 text-gray-500 text-xs">{e.correo}</td>
                                <td className="px-4 py-3 text-gray-600 text-xs">{e.programaNombre ?? '—'}</td>
                                <td className="px-4 py-3">{ESTADO_BADGE(e.estadoEstudiante)}</td>
                                <td className="px-4 py-3 text-gray-700 text-center">
                                    {e.promedioAcumulado ? e.promedioAcumulado.toFixed(1) : '—'}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Sprint 3 note */}
            <div className="card border border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-3">
                    <span className="text-2xl">📝</span>
                    <div>
                        <p className="font-semibold text-orange-800">Seguimientos y evaluaciones — Sprint 3</p>
                        <p className="text-sm text-orange-700 mt-1">
                            Próximamente podrás revisar los seguimientos semanales, registrar observaciones
                            y calificar la práctica de cada estudiante desde esta vista.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}
