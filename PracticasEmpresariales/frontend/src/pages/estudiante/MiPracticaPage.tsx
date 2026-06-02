import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { UsuarioResponse, VacanteResponse } from '../../types'
import { usuarioService } from '../../services/usuarioService'
import { vacanteService } from '../../services/vacanteService'

/**
 * Vista del Estudiante — estado de su práctica y vacantes disponibles.
 * El backend restringe toda información al scope del estudiante autenticado.
 */
export default function MiPracticaPage() {
    const { user } = useAuth()
    const [perfil, setPerfil] = useState<UsuarioResponse | null>(null)
    const [vacantes, setVacantes] = useState<VacanteResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        if (!user) return
        setLoading(true)
        Promise.all([
            usuarioService.obtener(user.usuarioId),
            vacanteService.listar(0, 10, 'DISPONIBLE'),
        ]).then(([u, pg]) => {
            setPerfil(u)
            setVacantes(pg.content ?? [])
        }).finally(() => setLoading(false))
    }, [user])

    if (loading) {
        return (
            <div className="space-y-4">
                {[1, 2, 3].map(i => <div key={i} className="card animate-pulse h-24 bg-gray-100" />)}
            </div>
        )
    }

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Mi Práctica</h1>
                <p className="text-sm text-gray-500">Hola, <span className="font-medium">{perfil?.nombre}</span>. Este es el estado de tu práctica empresarial.</p>
            </div>

            {/* Estado del estudiante */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="card border-l-4 border-l-cue-primary">
                    <p className="text-xs font-bold text-gray-500 uppercase mb-1">Mi estado</p>
                    {perfil?.estadoEstudiante === 'APTO'
                        ? <span className="badge-apto text-sm">Apto para práctica</span>
                        : <span className="badge-no-apto text-sm">No Apto aún</span>}
                    {perfil?.estadoEstudiante === 'NO_APTO' && perfil.motivoNoApto && (
                        <p className="text-xs text-red-600 mt-2">{perfil.motivoNoApto}</p>
                    )}
                </div>
                <div className="card border-l-4 border-l-blue-400">
                    <p className="text-xs font-bold text-gray-500 uppercase mb-1">Programa</p>
                    <p className="font-semibold text-gray-800">{perfil?.programaNombre ?? '—'}</p>
                </div>
                <div className="card border-l-4 border-l-green-400">
                    <p className="text-xs font-bold text-gray-500 uppercase mb-1">Promedio acumulado</p>
                    <p className="text-3xl font-bold text-gray-900">
                        {perfil?.promedioAcumulado ? perfil.promedioAcumulado.toFixed(1) : '—'}
                    </p>
                </div>
            </div>

            {/* Práctica actual */}
            <div className="card">
                <p className="text-xs font-bold text-gray-500 uppercase mb-3">Práctica asignada</p>
                <div className="flex items-center gap-3 py-6 text-gray-400 justify-center">
                    <span className="text-3xl">🎯</span>
                    <div className="text-left">
                        <p className="font-medium text-gray-600">Aún sin asignación</p>
                        <p className="text-xs">El Coordinador de Prácticas realizará la asignación en Sprint 3.</p>
                    </div>
                </div>
            </div>

            {/* Vacantes disponibles para su programa */}
            {vacantes.length > 0 && (
                <div className="card">
                    <p className="text-xs font-bold text-gray-500 uppercase mb-3">
                        Vacantes disponibles ({vacantes.length})
                    </p>
                    <div className="space-y-3">
                        {vacantes.map(v => (
                            <div key={v.id} className="border border-gray-200 rounded-lg p-3 hover:bg-gray-50">
                                <div className="flex items-start justify-between">
                                    <div>
                                        <p className="font-medium text-gray-800">{v.titulo}</p>
                                        <p className="text-xs text-gray-500">{v.empresaRazonSocial} · {v.ciudad ?? 'Sin ciudad'}</p>
                                    </div>
                                    <span className="text-xs bg-green-100 text-green-700 font-semibold px-2 py-0.5 rounded-full">
                                        {v.cupos - v.cuposOcupados} cupo{v.cupos - v.cuposOcupados !== 1 ? 's' : ''}
                                    </span>
                                </div>
                                <div className="flex gap-3 mt-2 text-xs text-gray-500">
                                    <span>{v.modalidad}</span>
                                    <span>·</span>
                                    <span>{v.jornada.replace('_', ' ')}</span>
                                    {v.remunerada && <><span>·</span><span className="text-green-600 font-medium">Remunerada</span></>}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Sprint 3 */}
            <div className="card border border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-3">
                    <span className="text-2xl">🚀</span>
                    <div>
                        <p className="font-semibold text-orange-800">Funcionalidades completas — Sprint 3</p>
                        <ul className="text-sm text-orange-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Carga y gestión del plan de práctica</li>
                            <li>Registro de seguimientos semanales</li>
                            <li>Visualización de evaluaciones del docente</li>
                            <li>Programación y actas de sustentación</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    )
}
