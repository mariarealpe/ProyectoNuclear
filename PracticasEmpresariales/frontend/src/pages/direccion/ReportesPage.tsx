import { useState, useEffect } from 'react'
import api from '../../services/api'
import { ApiResponse, Pageable, VacanteResponse, EmpresaResponse } from '../../types'

interface FilaReporte {
    concepto: string
    valor: string | number
    nota?: string
}

/**
 * GPE-131 / Épica DIRECCIÓN — Reportes gerenciales (solo lectura).
 * Patrón Proxy: vista de solo lectura, sin botones de acción.
 */
export default function ReportesPage() {
    const [resumenVacantes, setResumenVacantes] = useState<FilaReporte[]>([])
    const [resumenEmpresas, setResumenEmpresas] = useState<FilaReporte[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const getTotal = async <T,>(url: string): Promise<number> => {
            try {
                const res = await api.get<ApiResponse<Pageable<T>>>(url)
                return res.data.datos?.totalElements ?? 0
            } catch {
                return 0
            }
        }

        const cargar = async () => {
            setLoading(true)
            const [disponibles, pendientes, cerradas, rechazadas, empresas] = await Promise.all([
                getTotal<VacanteResponse>('/vacantes?estado=DISPONIBLE&size=1'),
                getTotal<VacanteResponse>('/vacantes?estado=PENDIENTE&size=1'),
                getTotal<VacanteResponse>('/vacantes?estado=CERRADA&size=1'),
                getTotal<VacanteResponse>('/vacantes?estado=RECHAZADA&size=1'),
                getTotal<EmpresaResponse>('/empresas?size=1'),
            ])
            const total = disponibles + pendientes + cerradas + rechazadas
            const pctDisponible = total > 0 ? ((disponibles / total) * 100).toFixed(1) : '0'

            setResumenVacantes([
                { concepto: 'Total de vacantes registradas',         valor: total },
                { concepto: 'Vacantes disponibles para asignación',  valor: disponibles },
                { concepto: 'Vacantes pendientes de revisión',        valor: pendientes },
                { concepto: 'Vacantes cerradas',                     valor: cerradas },
                { concepto: 'Vacantes rechazadas',                   valor: rechazadas },
                { concepto: 'Tasa de aprobación',                    valor: `${pctDisponible}%`, nota: 'Disponibles / Total' },
            ])

            setResumenEmpresas([
                { concepto: 'Total de empresas vinculadas',  valor: empresas },
                { concepto: 'Empresas activas',              valor: empresas, nota: 'Dato del sistema' },
            ])

            setLoading(false)
        }
        cargar()
    }, [])

    const exportarCSV = (filas: FilaReporte[], titulo: string) => {
        const csv = [
            ['Concepto', 'Valor', 'Nota'],
            ...filas.map(f => [f.concepto, String(f.valor), f.nota ?? '']),
        ].map(row => row.map(c => `"${c}"`).join(',')).join('\n')
        const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `reporte_${titulo}_${new Date().toISOString().split('T')[0]}.csv`
        a.click()
        URL.revokeObjectURL(url)
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Reportes Gerenciales</h1>
                    <p className="text-sm text-gray-500">Solo lectura — exportables en CSV.</p>
                </div>
                <span className="bg-amber-100 text-amber-800 text-xs font-semibold px-3 py-1.5 rounded-full">
                    Solo lectura — Dirección
                </span>
            </div>

            {loading ? (
                <div className="space-y-4">
                    {[1, 2].map(i => <div key={i} className="card animate-pulse h-40 bg-gray-100" />)}
                </div>
            ) : (
                <>
                    {/* Reporte vacantes */}
                    <div className="card">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="font-bold text-gray-800">Reporte de Vacantes</h2>
                            <button
                                className="btn-secondary text-xs py-1.5 px-3"
                                onClick={() => exportarCSV(resumenVacantes, 'vacantes')}
                            >
                                ↓ Exportar CSV
                            </button>
                        </div>
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 border-b border-gray-200">
                                <tr>
                                    <th className="text-left px-4 py-2 text-gray-600 font-semibold">Concepto</th>
                                    <th className="text-right px-4 py-2 text-gray-600 font-semibold">Valor</th>
                                    <th className="text-right px-4 py-2 text-gray-400 font-normal text-xs">Nota</th>
                                </tr>
                            </thead>
                            <tbody>
                                {resumenVacantes.map((f, i) => (
                                    <tr key={i} className="border-b border-gray-100 hover:bg-gray-50">
                                        <td className="px-4 py-2.5 text-gray-700">{f.concepto}</td>
                                        <td className="px-4 py-2.5 text-right font-semibold text-gray-900">{f.valor}</td>
                                        <td className="px-4 py-2.5 text-right text-xs text-gray-400">{f.nota ?? ''}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Reporte empresas */}
                    <div className="card">
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="font-bold text-gray-800">Reporte de Empresas</h2>
                            <button
                                className="btn-secondary text-xs py-1.5 px-3"
                                onClick={() => exportarCSV(resumenEmpresas, 'empresas')}
                            >
                                ↓ Exportar CSV
                            </button>
                        </div>
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 border-b border-gray-200">
                                <tr>
                                    <th className="text-left px-4 py-2 text-gray-600 font-semibold">Concepto</th>
                                    <th className="text-right px-4 py-2 text-gray-600 font-semibold">Valor</th>
                                    <th className="text-right px-4 py-2 text-gray-400 font-normal text-xs">Nota</th>
                                </tr>
                            </thead>
                            <tbody>
                                {resumenEmpresas.map((f, i) => (
                                    <tr key={i} className="border-b border-gray-100 hover:bg-gray-50">
                                        <td className="px-4 py-2.5 text-gray-700">{f.concepto}</td>
                                        <td className="px-4 py-2.5 text-right font-semibold text-gray-900">{f.valor}</td>
                                        <td className="px-4 py-2.5 text-right text-xs text-gray-400">{f.nota ?? ''}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Próximos reportes */}
                    <div className="card border border-dashed border-blue-300 bg-blue-50">
                        <div className="flex items-start gap-3">
                            <span className="text-2xl">📈</span>
                            <div>
                                <p className="font-semibold text-blue-800">Reportes avanzados — Sprint 4</p>
                                <ul className="text-sm text-blue-700 mt-1 space-y-0.5 list-disc list-inside">
                                    <li>Reporte de prácticas por programa y periodo</li>
                                    <li>Rendimiento académico de practicantes</li>
                                    <li>Tasa de satisfacción empresarial</li>
                                    <li>Ranking de empresas formadoras</li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </>
            )}
        </div>
    )
}
