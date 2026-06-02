import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { UsuarioResponse } from '../../types'
import { usuarioService } from '../../services/usuarioService'

/**
 * Vista del Tutor Empresarial — practicantes a su cargo.
 * El backend limita por scope del tutor autenticado (Patrón Proxy).
 */
export default function MisPracticantesPage() {
    const { user } = useAuth()
    const [practicantes, setPracticantes] = useState<UsuarioResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        setLoading(true)
        usuarioService.listar(0, 50)
            .then(pg => {
                const solo = pg.content.filter(u => u.rol === 'ESTUDIANTE')
                setPracticantes(solo)
            })
            .finally(() => setLoading(false))
    }, [])

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Practicantes a mi cargo</h1>
                <p className="text-sm text-gray-500">
                    Tutor: <span className="font-medium">{user?.nombre}</span> — practicantes que supervisas.
                </p>
            </div>

            {loading ? (
                <div className="card animate-pulse h-32 bg-gray-100" />
            ) : practicantes.length === 0 ? (
                <div className="card text-center py-12 text-gray-400">
                    No tienes practicantes asignados por el momento.
                    <p className="text-xs mt-1">El Coordinador de Prácticas realizará las asignaciones en Sprint 3.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {practicantes.map(p => (
                        <div key={p.id} className="card">
                            <div className="flex items-start justify-between">
                                <div>
                                    <p className="font-semibold text-gray-800">{p.nombre}</p>
                                    <p className="text-xs text-gray-500 mt-0.5">{p.correo}</p>
                                    <p className="text-xs text-gray-500">{p.programaNombre ?? '—'}</p>
                                </div>
                                {p.estadoEstudiante === 'APTO'
                                    ? <span className="badge-apto">Apto</span>
                                    : <span className="badge-no-apto">No Apto</span>}
                            </div>
                            {p.promedioAcumulado ? (
                                <p className="text-xs text-gray-500 mt-3">
                                    Promedio: <span className="font-semibold">{p.promedioAcumulado.toFixed(1)}</span>
                                </p>
                            ) : null}
                        </div>
                    ))}
                </div>
            )}

            <div className="card border border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-3">
                    <span className="text-2xl">🗓️</span>
                    <div>
                        <p className="font-semibold text-orange-800">Seguimiento de avances — Sprint 3</p>
                        <p className="text-sm text-orange-700 mt-1">
                            Próximamente podrás registrar avances semanales, aprobar planes de práctica
                            y responder encuestas de satisfacción para cada practicante.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}
