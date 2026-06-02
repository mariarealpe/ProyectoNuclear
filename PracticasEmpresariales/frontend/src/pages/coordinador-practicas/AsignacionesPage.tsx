import { useState, useEffect } from 'react'
import { VacanteResponse } from '../../types'
import { vacanteService } from '../../services/vacanteService'

/**
 * GPE — Asignación de estudiantes APTOS a vacantes DISPONIBLES.
 * Patrón Observer: al asignar, notifica automáticamente al estudiante y al tutor.
 * Estado: Sprint 3 — el endpoint de asignación aún no existe en el backend.
 * Esta vista ya muestra las vacantes disponibles como preparación.
 */
export default function AsignacionesPage() {
    const [vacantes, setVacantes] = useState<VacanteResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        vacanteService.listar(0, 20, 'DISPONIBLE')
            .then(pg => setVacantes(pg.content ?? []))
            .finally(() => setLoading(false))
    }, [])

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Asignaciones de Práctica</h1>
                <p className="text-sm text-gray-500">
                    Vincula estudiantes APTOS con vacantes DISPONIBLES para iniciar su práctica empresarial.
                </p>
            </div>

            {/* Sprint 3 Banner */}
            <div className="card border-2 border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-4">
                    <span className="text-3xl">🔗</span>
                    <div>
                        <p className="font-bold text-orange-800">Módulo de Asignaciones — Sprint 3</p>
                        <p className="text-sm text-orange-700 mt-1">
                            El flujo completo de asignación estará disponible en Sprint 3. Incluirá:
                        </p>
                        <ul className="text-sm text-orange-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Selección de estudiante APTO disponible</li>
                            <li>Vinculación con vacante DISPONIBLE con cupos</li>
                            <li>Asignación de docente asesor</li>
                            <li>Generación automática de la práctica con estado EN_CURSO</li>
                            <li>Notificación al estudiante, empresa y docente</li>
                        </ul>
                    </div>
                </div>
            </div>

            {/* Vacantes disponibles (preview del módulo) */}
            <div className="card">
                <p className="text-xs font-bold text-gray-500 uppercase mb-3">
                    Vacantes disponibles para asignación ({vacantes.length})
                </p>
                {loading ? (
                    <p className="text-gray-400 py-4 text-center">Cargando...</p>
                ) : vacantes.length === 0 ? (
                    <p className="text-gray-400 py-4 text-center text-sm">
                        No hay vacantes disponibles aún. Aprueba vacantes desde el módulo de Vacantes.
                    </p>
                ) : (
                    <div className="space-y-2">
                        {vacantes.map(v => (
                            <div key={v.id} className="flex items-center justify-between border border-gray-200 rounded-lg px-4 py-3 hover:bg-gray-50">
                                <div>
                                    <p className="font-medium text-gray-800 text-sm">{v.titulo}</p>
                                    <p className="text-xs text-gray-500">{v.empresaRazonSocial} · {v.programaNombre}</p>
                                </div>
                                <div className="text-right">
                                    <span className="text-xs bg-green-100 text-green-700 font-semibold px-2 py-0.5 rounded-full">
                                        {v.cupos - v.cuposOcupados} cupo{v.cupos - v.cuposOcupados !== 1 ? 's' : ''} libres
                                    </span>
                                    <p className="text-xs text-gray-400 mt-0.5">{v.modalidad} · {v.duracionMeses} meses</p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}
