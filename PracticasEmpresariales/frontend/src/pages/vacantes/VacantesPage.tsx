import { useEffect, useState } from 'react'
import {
    EmpresaResponse, EstadoVacante, JornadaVacante, ModalidadVacante,
    ProgramaResponse, TutorEmpresarialResponse, VacanteResponse,
} from '../../types'
import { empresaService } from '../../services/empresaService'
import { tutorService } from '../../services/tutorService'
import { vacanteService, CrearVacanteRequest } from '../../services/vacanteService'
import api from '../../services/api'
import { ApiResponse, Pageable } from '../../types'

const ETIQUETAS_ESTADO: Record<EstadoVacante, string> = {
    PENDIENTE: 'Pendiente',
    DISPONIBLE: 'Disponible',
    CERRADA: 'Cerrada',
    RECHAZADA: 'Rechazada',
}

const CLASES_ESTADO: Record<EstadoVacante, string> = {
    PENDIENTE: 'bg-yellow-100 text-yellow-800',
    DISPONIBLE: 'bg-green-100 text-green-800',
    CERRADA: 'bg-gray-200 text-gray-700',
    RECHAZADA: 'bg-red-100 text-red-700',
}

/**
 * GPE-152 + GPE-153 — Gestión y aprobación de vacantes (Persona 2).
 *
 * Combina creación (Builder), filtros (Proxy implícito en backend), y
 * flujo de aprobación (State) en una sola página con tabs por estado.
 */
export default function VacantesPage() {
    const [vacantes, setVacantes] = useState<VacanteResponse[]>([])
    const [empresas, setEmpresas] = useState<EmpresaResponse[]>([])
    const [programas, setProgramas] = useState<ProgramaResponse[]>([])
    const [tutores, setTutores] = useState<TutorEmpresarialResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [estadoFiltro, setEstadoFiltro] = useState<EstadoVacante | ''>('')
    const [modal, setModal] = useState(false)
    const [error, setError] = useState('')
    const [decision, setDecision] = useState<{ vacante: VacanteResponse, accion: 'aprobar' | 'rechazar' | 'cerrar' } | null>(null)
    const [motivo, setMotivo] = useState('')

    const formInicial: CrearVacanteRequest = {
        titulo: '', descripcion: '',
        empresaId: 0, tutorId: undefined, programaId: 0,
        modalidad: 'PRESENCIAL' as ModalidadVacante,
        jornada: 'TIEMPO_COMPLETO' as JornadaVacante,
        ciudad: '', cupos: 1, duracionMeses: 6,
        remunerada: false, valorRemuneracion: undefined,
        fechaInicio: '', fechaFin: '',
        requisitos: '', responsabilidades: '',
    }
    const [form, setForm] = useState<CrearVacanteRequest>(formInicial)

    const cargar = () => {
        setLoading(true)
        vacanteService.listar(0, 50, (estadoFiltro || undefined) as EstadoVacante | undefined)
            .then(pg => setVacantes(pg.content ?? []))
            .finally(() => setLoading(false))
    }

    useEffect(() => { cargar() /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, [estadoFiltro])

    useEffect(() => {
        // Carga inicial de selectores para el modal de creación.
        empresaService.listar(0, 100).then(pg => setEmpresas(pg.content ?? []))
        api.get<ApiResponse<Pageable<ProgramaResponse>>>('/programas?size=100')
            .then(r => setProgramas(r.data.datos?.content ?? []))
    }, [])

    // Cuando cambia la empresa seleccionada, recargamos sus tutores activos.
    useEffect(() => {
        if (form.empresaId) {
            tutorService.listar(0, 50, form.empresaId).then(pg => setTutores(pg.content ?? []))
        } else {
            setTutores([])
        }
    }, [form.empresaId])

    const abrirNuevo = () => {
        setForm(formInicial)
        setError('')
        setModal(true)
    }

    const crear = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        try {
            await vacanteService.crear({
                ...form,
                empresaId: Number(form.empresaId),
                programaId: Number(form.programaId),
                tutorId: form.tutorId ? Number(form.tutorId) : undefined,
                valorRemuneracion: form.remunerada && form.valorRemuneracion ? Number(form.valorRemuneracion) : undefined,
                fechaInicio: form.fechaInicio || undefined,
                fechaFin: form.fechaFin || undefined,
            })
            setModal(false)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'No se pudo registrar la vacante.')
        }
    }

    const aplicarDecision = async () => {
        if (!decision) return
        try {
            if (decision.accion === 'aprobar') {
                await vacanteService.aprobar(decision.vacante.id, motivo || undefined)
            } else if (decision.accion === 'rechazar') {
                if (!motivo.trim()) {
                    setError('El motivo del rechazo es obligatorio.')
                    return
                }
                await vacanteService.rechazar(decision.vacante.id, motivo)
            } else {
                await vacanteService.cerrar(decision.vacante.id, motivo || undefined)
            }
            setDecision(null)
            setMotivo('')
            setError('')
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'No se pudo procesar la acción.')
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Vacantes de Práctica</h1>
                    <p className="text-sm text-gray-500">Flujo de aprobación: las vacantes se publican una vez el Coordinador de Prácticas las aprueba.</p>
                </div>
                <button className="btn-primary" onClick={abrirNuevo}>+ Nueva Vacante</button>
            </div>

            {/* Filtro por estado */}
            <div className="flex gap-2 flex-wrap">
                {(['', 'PENDIENTE', 'DISPONIBLE', 'CERRADA', 'RECHAZADA'] as const).map(s => (
                    <button
                        key={s || 'todos'}
                        className={`text-sm px-3 py-1.5 rounded-lg border ${
                            estadoFiltro === s
                                ? 'bg-cue-primary text-white border-cue-primary'
                                : 'bg-white text-gray-600 border-gray-300 hover:border-cue-primary'
                        }`}
                        onClick={() => setEstadoFiltro(s as EstadoVacante | '')}
                    >
                        {s ? ETIQUETAS_ESTADO[s as EstadoVacante] : 'Todos'}
                    </button>
                ))}
            </div>

            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                        {['Título', 'Empresa', 'Programa', 'Modalidad', 'Cupos', 'Estado', 'Acciones'].map(h => (
                            <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {loading ? (
                        <tr><td colSpan={7} className="text-center py-8 text-gray-400">Cargando...</td></tr>
                    ) : vacantes.length === 0 ? (
                        <tr><td colSpan={7} className="text-center py-8 text-gray-400">No hay vacantes que mostrar.</td></tr>
                    ) : vacantes.map(v => (
                        <tr key={v.id} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="px-4 py-3">
                                <div className="font-medium text-gray-900">{v.titulo}</div>
                                <div className="text-xs text-gray-500">{v.ciudad ?? '—'} · {v.duracionMeses} meses</div>
                            </td>
                            <td className="px-4 py-3 text-gray-600">{v.empresaRazonSocial}</td>
                            <td className="px-4 py-3 text-gray-600">{v.programaNombre}</td>
                            <td className="px-4 py-3 text-gray-600">{v.modalidad} · {v.jornada}</td>
                            <td className="px-4 py-3 text-center text-gray-700">{v.cuposOcupados}/{v.cupos}</td>
                            <td className="px-4 py-3">
                                <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${CLASES_ESTADO[v.estado]}`}>
                                    {ETIQUETAS_ESTADO[v.estado]}
                                </span>
                            </td>
                            <td className="px-4 py-3 text-right whitespace-nowrap">
                                {v.estado === 'PENDIENTE' && (
                                    <>
                                        <button
                                            className="text-xs text-green-700 hover:underline mr-3"
                                            onClick={() => { setDecision({ vacante: v, accion: 'aprobar' }); setMotivo(''); setError('') }}
                                        >Aprobar</button>
                                        <button
                                            className="text-xs text-red-600 hover:underline"
                                            onClick={() => { setDecision({ vacante: v, accion: 'rechazar' }); setMotivo(''); setError('') }}
                                        >Rechazar</button>
                                    </>
                                )}
                                {v.estado === 'DISPONIBLE' && (
                                    <button
                                        className="text-xs text-gray-700 hover:underline"
                                        onClick={() => { setDecision({ vacante: v, accion: 'cerrar' }); setMotivo(''); setError('') }}
                                    >Cerrar</button>
                                )}
                                {v.estado === 'RECHAZADA' && v.motivoRechazo && (
                                    <span className="text-xs text-gray-500 italic" title={v.motivoRechazo}>
                                        Motivo: {v.motivoRechazo.length > 30 ? v.motivoRechazo.slice(0, 30) + '…' : v.motivoRechazo}
                                    </span>
                                )}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {/* Modal: nueva vacante */}
            {modal && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-3xl p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-2">Nueva Vacante</h2>
                        <p className="text-xs text-gray-500 mb-4">
                            Patrón Builder: la vacante se construye paso a paso y queda en estado PENDIENTE hasta aprobación.
                        </p>
                        {error && <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>}
                        <form onSubmit={crear} className="grid grid-cols-2 gap-4">
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Título *</label>
                                <input className="input-field" required value={form.titulo}
                                       onChange={e => setForm({ ...form, titulo: e.target.value })} />
                            </div>
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción *</label>
                                <textarea className="input-field" rows={3} required value={form.descripcion}
                                          onChange={e => setForm({ ...form, descripcion: e.target.value })} />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Empresa *</label>
                                <select className="input-field" required value={form.empresaId || ''}
                                        onChange={e => setForm({ ...form, empresaId: Number(e.target.value), tutorId: undefined })}>
                                    <option value="">— Selecciona —</option>
                                    {empresas.filter(e => e.activo).map(e => (
                                        <option key={e.id} value={e.id}>{e.razonSocial}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Programa *</label>
                                <select className="input-field" required value={form.programaId || ''}
                                        onChange={e => setForm({ ...form, programaId: Number(e.target.value) })}>
                                    <option value="">— Selecciona —</option>
                                    {programas.filter(p => p.activo).map(p => (
                                        <option key={p.id} value={p.id}>{p.nombre}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Tutor empresarial (opcional)</label>
                                <select className="input-field" value={form.tutorId ?? ''}
                                        onChange={e => setForm({ ...form, tutorId: e.target.value ? Number(e.target.value) : undefined })}>
                                    <option value="">— Sin asignar —</option>
                                    {tutores.filter(t => t.activo).map(t => (
                                        <option key={t.id} value={t.id}>{t.nombre} ({t.cargo})</option>
                                    ))}
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Modalidad *</label>
                                <select className="input-field" required value={form.modalidad}
                                        onChange={e => setForm({ ...form, modalidad: e.target.value as ModalidadVacante })}>
                                    <option value="PRESENCIAL">Presencial</option>
                                    <option value="REMOTO">Remoto</option>
                                    <option value="HIBRIDO">Híbrido</option>
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Jornada *</label>
                                <select className="input-field" required value={form.jornada}
                                        onChange={e => setForm({ ...form, jornada: e.target.value as JornadaVacante })}>
                                    <option value="TIEMPO_COMPLETO">Tiempo Completo</option>
                                    <option value="MEDIO_TIEMPO">Medio Tiempo</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Ciudad</label>
                                <input className="input-field" value={form.ciudad ?? ''}
                                       onChange={e => setForm({ ...form, ciudad: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Cupos *</label>
                                <input className="input-field" type="number" min={1} required value={form.cupos}
                                       onChange={e => setForm({ ...form, cupos: Number(e.target.value) })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Duración (meses) *</label>
                                <input className="input-field" type="number" min={1} required value={form.duracionMeses}
                                       onChange={e => setForm({ ...form, duracionMeses: Number(e.target.value) })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Remunerada</label>
                                <select className="input-field"
                                        value={form.remunerada ? 'si' : 'no'}
                                        onChange={e => setForm({ ...form, remunerada: e.target.value === 'si' })}>
                                    <option value="no">No</option>
                                    <option value="si">Sí</option>
                                </select>
                            </div>
                            {form.remunerada && (
                                <div className="col-span-2">
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Valor mensual (COP)</label>
                                    <input className="input-field" type="number" min={0}
                                           value={form.valorRemuneracion ?? ''}
                                           onChange={e => setForm({ ...form, valorRemuneracion: e.target.value ? Number(e.target.value) : undefined })} />
                                </div>
                            )}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Fecha inicio</label>
                                <input className="input-field" type="date" value={form.fechaInicio ?? ''}
                                       onChange={e => setForm({ ...form, fechaInicio: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Fecha fin</label>
                                <input className="input-field" type="date" value={form.fechaFin ?? ''}
                                       onChange={e => setForm({ ...form, fechaFin: e.target.value })} />
                            </div>
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Requisitos</label>
                                <textarea className="input-field" rows={2} value={form.requisitos ?? ''}
                                          onChange={e => setForm({ ...form, requisitos: e.target.value })} />
                            </div>
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Responsabilidades</label>
                                <textarea className="input-field" rows={2} value={form.responsabilidades ?? ''}
                                          onChange={e => setForm({ ...form, responsabilidades: e.target.value })} />
                            </div>

                            <div className="col-span-2 flex gap-3 pt-2">
                                <button type="button" className="btn-secondary flex-1" onClick={() => setModal(false)}>Cancelar</button>
                                <button type="submit" className="btn-primary flex-1">Registrar Vacante</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Modal: aprobación / rechazo / cierre */}
            {decision && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
                        <h2 className="text-lg font-bold text-gray-800 mb-2">
                            {decision.accion === 'aprobar' ? 'Aprobar vacante' :
                                decision.accion === 'rechazar' ? 'Rechazar vacante' : 'Cerrar vacante'}
                        </h2>
                        <p className="text-sm text-gray-500 mb-4">{decision.vacante.titulo}</p>
                        {error && <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>}
                        <div className="space-y-3">
                            <label className="block text-sm font-medium text-gray-700">
                                {decision.accion === 'rechazar' ? 'Motivo del rechazo *' : 'Observación (opcional)'}
                            </label>
                            <textarea
                                className="input-field"
                                rows={4}
                                value={motivo}
                                onChange={e => setMotivo(e.target.value)}
                                placeholder={decision.accion === 'rechazar'
                                    ? 'Indica claramente qué debe corregirse...'
                                    : 'Comentario para la bitácora...'}
                            />
                        </div>
                        <div className="flex gap-3 pt-4">
                            <button className="btn-secondary flex-1" onClick={() => { setDecision(null); setMotivo(''); setError('') }}>
                                Cancelar
                            </button>
                            <button
                                className={`flex-1 py-2 rounded-lg text-white font-semibold transition-colors ${
                                    decision.accion === 'aprobar' ? 'bg-green-600 hover:bg-green-700' :
                                        decision.accion === 'rechazar' ? 'bg-red-600 hover:bg-red-700' :
                                            'bg-gray-700 hover:bg-gray-800'
                                }`}
                                onClick={aplicarDecision}
                            >Confirmar</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
