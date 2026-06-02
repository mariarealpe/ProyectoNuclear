import { useState, useEffect } from 'react'
import api from '../../services/api'
import { ApiResponse, Pageable, EmpresaResponse, VacanteResponse, TutorEmpresarialResponse, UsuarioResponse } from '../../types'

interface KPI {
    titulo: string
    valor: number
    descripcion: string
    color: string
    icono: string
}

/**
 * GPE-131 / Épica DIRECCIÓN — Panel de indicadores globales (solo lectura).
 * Patrón Proxy: ninguna acción de escritura disponible para este rol.
 * Agrega datos reales de los endpoints existentes.
 */
export default function IndicadoresPage() {
    const [kpis, setKpis] = useState<KPI[]>([])
    const [vacantesPorEstado, setVacantesPorEstado] = useState<Record<string, number>>({})
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const fetchCount = async <T,>(url: string): Promise<number> => {
            try {
                const res = await api.get<ApiResponse<Pageable<T>>>(url)
                return res.data.datos?.totalElements ?? 0
            } catch {
                return 0
            }
        }

        const cargar = async () => {
            setLoading(true)
            const [
                totalUsuarios,
                totalEmpresas,
                totalTutores,
                vacantesDisponibles,
                vacantesPendientes,
                vacantesCerradas,
                vacantesRechazadas,
            ] = await Promise.all([
                fetchCount<UsuarioResponse>('/usuarios?size=1'),
                fetchCount<EmpresaResponse>('/empresas?size=1'),
                fetchCount<TutorEmpresarialResponse>('/tutores?size=1'),
                fetchCount<VacanteResponse>('/vacantes?estado=DISPONIBLE&size=1'),
                fetchCount<VacanteResponse>('/vacantes?estado=PENDIENTE&size=1'),
                fetchCount<VacanteResponse>('/vacantes?estado=CERRADA&size=1'),
                fetchCount<VacanteResponse>('/vacantes?estado=RECHAZADA&size=1'),
            ])

            const totalVacantes = vacantesDisponibles + vacantesPendientes + vacantesCerradas + vacantesRechazadas

            setKpis([
                { titulo: 'Usuarios registrados',   valor: totalUsuarios,         descripcion: 'Docentes, tutores, coordinadores, estudiantes y administrativos', color: 'bg-blue-50 border-blue-200',   icono: '👥' },
                { titulo: 'Empresas vinculadas',     valor: totalEmpresas,         descripcion: 'Empresas del sector productivo y de servicios aliadas',           color: 'bg-green-50 border-green-200', icono: '🏢' },
                { titulo: 'Tutores empresariales',   valor: totalTutores,          descripcion: 'Tutores activos que supervisan practicantes',                     color: 'bg-purple-50 border-purple-200', icono: '🤝' },
                { titulo: 'Vacantes disponibles',    valor: vacantesDisponibles,   descripcion: 'Vacantes aprobadas y disponibles para asignación',               color: 'bg-emerald-50 border-emerald-200', icono: '✅' },
                { titulo: 'Vacantes en revisión',    valor: vacantesPendientes,    descripcion: 'Vacantes pendientes de aprobación por coordinador',              color: 'bg-yellow-50 border-yellow-200', icono: '⏳' },
                { titulo: 'Total vacantes',          valor: totalVacantes,         descripcion: 'Vacantes registradas en todos los estados',                       color: 'bg-gray-50 border-gray-200',   icono: '💼' },
            ])

            setVacantesPorEstado({
                DISPONIBLE: vacantesDisponibles,
                PENDIENTE: vacantesPendientes,
                CERRADA: vacantesCerradas,
                RECHAZADA: vacantesRechazadas,
            })

            setLoading(false)
        }

        cargar()
    }, [])

    const totalVac = Object.values(vacantesPorEstado).reduce((a, b) => a + b, 0) || 1

    const barras = [
        { estado: 'Disponibles', valor: vacantesPorEstado['DISPONIBLE'] ?? 0, color: 'bg-green-500' },
        { estado: 'Pendientes',  valor: vacantesPorEstado['PENDIENTE'] ?? 0,  color: 'bg-yellow-400' },
        { estado: 'Cerradas',    valor: vacantesPorEstado['CERRADA'] ?? 0,    color: 'bg-gray-400' },
        { estado: 'Rechazadas',  valor: vacantesPorEstado['RECHAZADA'] ?? 0,  color: 'bg-red-400' },
    ]

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Indicadores Institucionales</h1>
                    <p className="text-sm text-gray-500 mt-0.5">
                        Vista gerencial de solo lectura — datos en tiempo real del sistema.
                    </p>
                </div>
                <span className="bg-amber-100 text-amber-800 text-xs font-semibold px-3 py-1.5 rounded-full">
                    Solo lectura — Dirección
                </span>
            </div>

            {loading ? (
                <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
                    {Array.from({ length: 6 }).map((_, i) => (
                        <div key={i} className="card animate-pulse h-28 bg-gray-100" />
                    ))}
                </div>
            ) : (
                <>
                    {/* KPI cards */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                        {kpis.map(k => (
                            <div key={k.titulo} className={`card border ${k.color}`}>
                                <div className="flex items-start justify-between">
                                    <div>
                                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">{k.titulo}</p>
                                        <p className="text-4xl font-bold text-gray-900 mt-2">{k.valor}</p>
                                        <p className="text-xs text-gray-500 mt-1">{k.descripcion}</p>
                                    </div>
                                    <span className="text-3xl opacity-60">{k.icono}</span>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Distribución de vacantes */}
                    <div className="card">
                        <h2 className="text-sm font-bold text-gray-700 uppercase tracking-wide mb-4">
                            Distribución de vacantes por estado
                        </h2>
                        <div className="space-y-3">
                            {barras.map(b => (
                                <div key={b.estado} className="flex items-center gap-3">
                                    <span className="text-xs text-gray-600 w-24 shrink-0">{b.estado}</span>
                                    <div className="flex-1 bg-gray-100 rounded-full h-5 overflow-hidden">
                                        <div
                                            className={`h-full rounded-full ${b.color} transition-all duration-700`}
                                            style={{ width: `${(b.valor / totalVac) * 100}%` }}
                                        />
                                    </div>
                                    <span className="text-xs font-semibold text-gray-700 w-6 text-right">{b.valor}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Sprint 3 note */}
                    <div className="card border border-dashed border-blue-300 bg-blue-50">
                        <div className="flex items-start gap-3">
                            <span className="text-2xl">📊</span>
                            <div>
                                <p className="font-semibold text-blue-800">Indicadores avanzados — Sprint 3</p>
                                <p className="text-sm text-blue-700 mt-1">
                                    En Sprint 3 se agregarán: tasa de ocupación de vacantes, promedio de prácticas por programa,
                                    tiempo promedio de validación académica, índice de satisfacción empresarial y evolución mensual.
                                </p>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}
