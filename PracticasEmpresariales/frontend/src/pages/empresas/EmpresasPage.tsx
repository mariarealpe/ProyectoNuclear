import { useEffect, useState } from 'react'
import { EmpresaResponse, EstadoEmpresa } from '../../types'
import { empresaService, CrearEmpresaRequest } from '../../services/empresaService'

// ─── Máquina de estados GPE-150 ───────────────────────────────────────────────

const ESTADO_LABELS: Record<EstadoEmpresa, string> = {
    PENDIENTE: 'Pendiente',
    APROBADA: 'Aprobada',
    RECHAZADA: 'Rechazada',
    INACTIVA: 'Inactiva',
}

const ESTADO_CLASES: Record<EstadoEmpresa, string> = {
    PENDIENTE: 'bg-yellow-100 text-yellow-800',
    APROBADA: 'bg-green-100 text-green-800',
    RECHAZADA: 'bg-red-100 text-red-800',
    INACTIVA: 'bg-gray-200 text-gray-600',
}

/**
 * GPE-150 — Gestión de empresas vinculadas.
 *
 * Máquina de estados: Pendiente → Aprobada | Rechazada → Inactiva.
 * Solo empresas APROBADAS pueden tener vacantes ni tutores activos.
 * Eliminar empresa: PROHIBIDO — solo inactivar, conservando historial.
 * Patrón Observer: al inactivar notifica automáticamente a tutores (backend).
 */
export default function EmpresasPage() {
    const [empresas, setEmpresas] = useState<EmpresaResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [filtro, setFiltro] = useState('')
    const [modal, setModal] = useState(false)
    const [editandoId, setEditandoId] = useState<number | null>(null)
    const [error, setError] = useState('')

    // Modal de decisión (aprobar/rechazar)
    const [decision, setDecision] = useState<{
        empresa: EmpresaResponse
        accion: 'rechazar' | 'inactivar'
    } | null>(null)
    const [motivoDecision, setMotivoDecision] = useState('')

    const formInicial: CrearEmpresaRequest = {
        nit: '', razonSocial: '', nombreComercial: '', sector: '',
        direccion: '', ciudad: '', correoContacto: '', telefono: '',
        sitioWeb: '', representanteLegal: '', descripcion: '',
    }
    const [form, setForm] = useState<CrearEmpresaRequest>(formInicial)

    const cargar = () => {
        setLoading(true)
        empresaService.listar(0, 50, filtro || undefined)
            .then(pg => setEmpresas(pg.content ?? []))
            .finally(() => setLoading(false))
    }

    useEffect(() => { cargar() }, []) // eslint-disable-line

    const abrirNuevo = () => {
        setForm(formInicial)
        setEditandoId(null)
        setError('')
        setModal(true)
    }

    const abrirEditar = (e: EmpresaResponse) => {
        setForm({
            nit: e.nit,
            razonSocial: e.razonSocial,
            nombreComercial: e.nombreComercial ?? '',
            sector: e.sector ?? '',
            direccion: e.direccion ?? '',
            ciudad: e.ciudad ?? '',
            correoContacto: e.correoContacto,
            telefono: e.telefono ?? '',
            sitioWeb: e.sitioWeb ?? '',
            representanteLegal: e.representanteLegal ?? '',
            descripcion: e.descripcion ?? '',
        })
        setEditandoId(e.id)
        setError('')
        setModal(true)
    }

    const guardar = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        try {
            if (editandoId) {
                await empresaService.editar(editandoId, form)
            } else {
                await empresaService.crear(form)
            }
            setModal(false)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'No se pudo guardar la empresa.')
        }
    }

    // ── Transiciones de estado ────────────────────────────────────────────────

    const aprobar = async (empresa: EmpresaResponse) => {
        try {
            await empresaService.aprobar(empresa.id)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            alert(msg ?? 'Error al aprobar la empresa.')
        }
    }

    const aplicarDecision = async () => {
        if (!decision) return
        if (!motivoDecision.trim()) {
            setError('El motivo es obligatorio.')
            return
        }
        setError('')
        try {
            if (decision.accion === 'rechazar') {
                await empresaService.rechazar(decision.empresa.id, motivoDecision)
            } else {
                await empresaService.inactivar(decision.empresa.id)
            }
            setDecision(null)
            setMotivoDecision('')
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al procesar la acción.')
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Empresas</h1>
                    <p className="text-sm text-gray-500">
                        Solo empresas <strong>APROBADAS</strong> pueden tener vacantes y tutores activos.
                        Flujo: Pendiente → Aprobada | Rechazada → Inactiva.
                    </p>
                </div>
                <div className="flex gap-2">
                    <input
                        className="input-field max-w-xs"
                        placeholder="Buscar por razón social..."
                        value={filtro}
                        onChange={e => setFiltro(e.target.value)}
                        onKeyDown={e => { if (e.key === 'Enter') cargar() }}
                    />
                    <button className="btn-secondary" onClick={cargar}>Buscar</button>
                    <button className="btn-primary" onClick={abrirNuevo}>+ Nueva Empresa</button>
                </div>
            </div>

            {/* Tabla */}
            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                            {['NIT', 'Razón Social', 'Sector', 'Ciudad', 'Contacto', 'Estado', 'Acciones'].map(h => (
                                <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan={7} className="text-center py-8 text-gray-400">Cargando...</td></tr>
                        ) : empresas.length === 0 ? (
                            <tr><td colSpan={7} className="text-center py-8 text-gray-400">No hay empresas registradas.</td></tr>
                        ) : empresas.map(emp => (
                            <tr key={emp.id} className="border-b border-gray-100 hover:bg-gray-50">
                                <td className="px-4 py-3 font-mono text-xs">{emp.nit}</td>
                                <td className="px-4 py-3">
                                    <div className="font-medium text-gray-900">{emp.razonSocial}</div>
                                    {emp.nombreComercial && (
                                        <div className="text-xs text-gray-500">{emp.nombreComercial}</div>
                                    )}
                                </td>
                                <td className="px-4 py-3 text-gray-500">{emp.sector ?? '—'}</td>
                                <td className="px-4 py-3 text-gray-500">{emp.ciudad ?? '—'}</td>
                                <td className="px-4 py-3 text-gray-500 text-xs">
                                    <div>{emp.correoContacto}</div>
                                    {emp.telefono && <div>{emp.telefono}</div>}
                                </td>
                                <td className="px-4 py-3">
                                    <div>
                                        <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${ESTADO_CLASES[emp.estadoEmpresa ?? (emp.activo ? 'APROBADA' : 'INACTIVA')]}`}>
                                            {ESTADO_LABELS[emp.estadoEmpresa ?? (emp.activo ? 'APROBADA' : 'INACTIVA')]}
                                        </span>
                                        {emp.motivoRechazo && (
                                            <p className="text-xs text-red-500 mt-0.5 max-w-32 truncate" title={emp.motivoRechazo}>
                                                {emp.motivoRechazo}
                                            </p>
                                        )}
                                    </div>
                                </td>
                                <td className="px-4 py-3 text-right whitespace-nowrap space-x-2">
                                    {/* Transiciones de estado según máquina */}
                                    {(emp.estadoEmpresa === 'PENDIENTE' || (!emp.estadoEmpresa && emp.activo)) && (
                                        <>
                                            <button
                                                className="text-xs text-green-700 hover:underline"
                                                onClick={() => aprobar(emp)}
                                            >
                                                Aprobar
                                            </button>
                                            <button
                                                className="text-xs text-red-600 hover:underline"
                                                onClick={() => { setDecision({ empresa: emp, accion: 'rechazar' }); setMotivoDecision(''); setError('') }}
                                            >
                                                Rechazar
                                            </button>
                                        </>
                                    )}
                                    {emp.estadoEmpresa === 'APROBADA' && (
                                        <button
                                            className="text-xs text-gray-600 hover:underline"
                                            onClick={() => { setDecision({ empresa: emp, accion: 'inactivar' }); setMotivoDecision('inactivación por solicitud'); setError('') }}
                                        >
                                            Inactivar
                                        </button>
                                    )}
                                    {/* Editar siempre disponible excepto inactiva */}
                                    {emp.estadoEmpresa !== 'INACTIVA' && emp.activo && (
                                        <button
                                            className="text-xs text-cue-primary hover:underline"
                                            onClick={() => abrirEditar(emp)}
                                        >
                                            Editar
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* ── Modal: crear / editar empresa ─────────────────────────────── */}
            {modal && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-4">
                            {editandoId ? 'Editar Empresa' : 'Nueva Empresa'}
                        </h2>
                        {error && <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>}
                        <form onSubmit={guardar} className="grid grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">NIT *</label>
                                <input className="input-field" required value={form.nit}
                                       onChange={e => setForm({ ...form, nit: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Razón Social *</label>
                                <input className="input-field" required value={form.razonSocial}
                                       onChange={e => setForm({ ...form, razonSocial: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre Comercial</label>
                                <input className="input-field" value={form.nombreComercial ?? ''}
                                       onChange={e => setForm({ ...form, nombreComercial: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Sector</label>
                                <input className="input-field" value={form.sector ?? ''}
                                       onChange={e => setForm({ ...form, sector: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Correo de contacto *</label>
                                <input className="input-field" type="email" required value={form.correoContacto}
                                       onChange={e => setForm({ ...form, correoContacto: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                                <input className="input-field" value={form.telefono ?? ''}
                                       onChange={e => setForm({ ...form, telefono: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Ciudad</label>
                                <input className="input-field" value={form.ciudad ?? ''}
                                       onChange={e => setForm({ ...form, ciudad: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Sitio Web</label>
                                <input className="input-field" value={form.sitioWeb ?? ''}
                                       onChange={e => setForm({ ...form, sitioWeb: e.target.value })} />
                            </div>
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Dirección</label>
                                <input className="input-field" value={form.direccion ?? ''}
                                       onChange={e => setForm({ ...form, direccion: e.target.value })} />
                            </div>
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Representante legal</label>
                                <input className="input-field" value={form.representanteLegal ?? ''}
                                       onChange={e => setForm({ ...form, representanteLegal: e.target.value })} />
                            </div>
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                                <textarea className="input-field" rows={3} value={form.descripcion ?? ''}
                                          onChange={e => setForm({ ...form, descripcion: e.target.value })} />
                            </div>
                            {!editandoId && (
                                <div className="col-span-2 bg-blue-50 border border-blue-200 rounded-lg px-3 py-2 text-xs text-blue-800">
                                    La empresa se creará en estado <strong>Pendiente</strong>.
                                    Deberás aprobarla antes de crear vacantes o tutores.
                                </div>
                            )}
                            <div className="col-span-2 flex gap-3 pt-2">
                                <button type="button" className="btn-secondary flex-1"
                                        onClick={() => { setModal(false); setError('') }}>Cancelar</button>
                                <button type="submit" className="btn-primary flex-1">
                                    {editandoId ? 'Guardar cambios' : 'Crear empresa'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ── Modal: rechazar / inactivar ────────────────────────────────── */}
            {decision && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
                        <h2 className="text-lg font-bold text-gray-800 mb-1">
                            {decision.accion === 'rechazar' ? 'Rechazar empresa' : 'Inactivar empresa'}
                        </h2>
                        <p className="text-sm text-gray-500 mb-4">{decision.empresa.razonSocial}</p>
                        {error && <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>}
                        <div className="mb-4">
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                {decision.accion === 'rechazar' ? 'Motivo del rechazo *' : 'Motivo (obligatorio) *'}
                            </label>
                            <textarea
                                className="input-field"
                                rows={3}
                                value={motivoDecision}
                                onChange={e => setMotivoDecision(e.target.value)}
                                placeholder={
                                    decision.accion === 'rechazar'
                                        ? 'Indica por qué no cumple los requisitos...'
                                        : 'Motivo de inactivación...'
                                }
                            />
                        </div>
                        <div className="flex gap-3">
                            <button className="btn-secondary flex-1"
                                    onClick={() => { setDecision(null); setMotivoDecision(''); setError('') }}>
                                Cancelar
                            </button>
                            <button
                                className={`flex-1 py-2 rounded-lg text-white font-semibold ${
                                    decision.accion === 'rechazar'
                                        ? 'bg-red-600 hover:bg-red-700'
                                        : 'bg-gray-700 hover:bg-gray-800'
                                }`}
                                onClick={aplicarDecision}
                            >
                                Confirmar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
