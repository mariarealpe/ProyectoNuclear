import { useState, useEffect } from 'react'
import { FacultadResponse, ProgramaResponse } from '../../types'
import api from '../../services/api'
import { ApiResponse, Pageable } from '../../types'

// ─── Tipos internos del formulario ────────────────────────────────────────────

interface RequisitoForm {
    numeroPractica: number
    creditosMinimos: number
    promedioMinimo: number
    requierePracticaAnterior: boolean
    documentosRequeridos: string[]
}

interface ProgramaForm {
    nombre: string
    descripcion: string
    facultadId: string
    numeroTotalPracticas: number
    promedioMinimoGeneral: number
    requisitosPorPractica: RequisitoForm[]
}

const requisitoVacio = (n: number): RequisitoForm => ({
    numeroPractica: n,
    creditosMinimos: 0,
    promedioMinimo: 3.0,
    requierePracticaAnterior: n > 1,
    documentosRequeridos: [],
})

const formInicial = (): ProgramaForm => ({
    nombre: '', descripcion: '', facultadId: '',
    numeroTotalPracticas: 1, promedioMinimoGeneral: 3.0,
    requisitosPorPractica: [requisitoVacio(1)],
})

const ajustarRequisitos = (requisitos: RequisitoForm[], total: number): RequisitoForm[] => {
    const actuales = [...requisitos]
    while (actuales.length < total) actuales.push(requisitoVacio(actuales.length + 1))
    return actuales.slice(0, total)
}

/**
 * GPE-140 — Gestión de facultades y programas con Builder.
 *
 * Patrón Builder: la configuración de cada programa se construye paso a paso:
 * datos básicos → número de prácticas → promedio mínimo general →
 * requisitos por número de práctica (créditos, promedio, práctica anterior, documentos).
 * Permite configurar requisitos diferentes para cada número de práctica sin tocar código.
 */
export default function ProgramasPage() {
    const [programas, setProgramas] = useState<ProgramaResponse[]>([])
    const [facultades, setFacultades] = useState<FacultadResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [modal, setModal] = useState(false)
    const [editandoId, setEditandoId] = useState<number | null>(null)
    const [form, setForm] = useState<ProgramaForm>(formInicial())
    const [error, setError] = useState('')
    const [exito, setExito] = useState('')
    const [docTemps, setDocTemps] = useState<string[]>([''])

    const cargar = () => {
        setLoading(true)
        api.get<ApiResponse<Pageable<ProgramaResponse>>>('/programas')
            .then(r => setProgramas(r.data.datos?.content ?? []))
            .finally(() => setLoading(false))
    }

    useEffect(() => {
        cargar()
        api.get<ApiResponse<Pageable<FacultadResponse>>>('/facultades?size=100')
            .then(r => setFacultades(r.data.datos?.content ?? []))
    }, [])

    // Observer: ajusta la lista de requisitos cuando cambia el número de prácticas
    const handleNumPracticasChange = (n: number) => {
        setForm(prev => ({
            ...prev,
            numeroTotalPracticas: n,
            requisitosPorPractica: ajustarRequisitos(prev.requisitosPorPractica, n),
        }))
        setDocTemps(Array(n).fill(''))
    }

    const updateRequisito = (idx: number, campo: keyof RequisitoForm, valor: unknown) => {
        setForm(prev => {
            const arr = [...prev.requisitosPorPractica]
            arr[idx] = { ...arr[idx], [campo]: valor }
            return { ...prev, requisitosPorPractica: arr }
        })
    }

    const agregarDoc = (idx: number) => {
        const doc = docTemps[idx]?.trim()
        if (!doc) return
        updateRequisito(idx, 'documentosRequeridos', [
            ...form.requisitosPorPractica[idx].documentosRequeridos, doc,
        ])
        setDocTemps(prev => { const a = [...prev]; a[idx] = ''; return a })
    }

    const quitarDoc = (pracIdx: number, docIdx: number) => {
        const arr = form.requisitosPorPractica[pracIdx].documentosRequeridos
        updateRequisito(pracIdx, 'documentosRequeridos', arr.filter((_, i) => i !== docIdx))
    }

    const abrirNuevo = () => {
        setForm(formInicial())
        setDocTemps([''])
        setEditandoId(null)
        setError('')
        setModal(true)
    }

    const abrirEditar = (p: ProgramaResponse) => {
        const req: RequisitoForm[] = (p.requisitosPorPractica ?? []).map(r => ({
            numeroPractica: r.numeroPractica,
            creditosMinimos: r.creditosMinimos,
            promedioMinimo: r.promedioMinimo,
            requierePracticaAnterior: r.requierePracticaAnterior,
            documentosRequeridos: [...r.documentosRequeridos],
        }))
        setForm({
            nombre: p.nombre,
            descripcion: p.descripcion ?? '',
            facultadId: String(p.facultadId),
            numeroTotalPracticas: p.numeroTotalPracticas,
            promedioMinimoGeneral: p.promedioMinimoGeneral,
            requisitosPorPractica: ajustarRequisitos(req, p.numeroTotalPracticas),
        })
        setDocTemps(Array(p.numeroTotalPracticas).fill(''))
        setEditandoId(p.id)
        setError('')
        setModal(true)
    }

    const guardar = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        const payload = {
            ...form,
            facultadId: Number(form.facultadId),
        }
        try {
            if (editandoId) {
                await api.put(`/programas/${editandoId}`, payload)
                setExito('Programa actualizado.')
            } else {
                await api.post('/programas', payload)
                setExito('Programa creado y disponible para asignar coordinadores.')
            }
            setModal(false)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al guardar el programa.')
        }
    }

    const handleDesactivar = async (id: number) => {
        if (!confirm('¿Desactivar este programa? Solo es posible si no tiene estudiantes ni prácticas activas.')) return
        try {
            await api.patch(`/programas/${id}/desactivar`)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            alert(msg ?? 'No se puede desactivar.')
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-gray-900">Programas Académicos</h1>
                <button className="btn-primary" onClick={abrirNuevo}>+ Nuevo Programa</button>
            </div>

            {exito && (
                <div className="bg-green-50 border border-green-200 text-green-700 rounded-lg px-4 py-3 text-sm flex items-center justify-between">
                    <span>{exito}</span>
                    <button className="text-green-500 hover:text-green-700 ml-2" onClick={() => setExito('')}>✕</button>
                </div>
            )}

            {/* Tabla de programas */}
            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                            {['Programa', 'Facultad', 'N° Prácticas', 'Promedio Mín.', 'Estado', 'Acciones'].map(h => (
                                <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan={6} className="text-center py-8 text-gray-400">Cargando...</td></tr>
                        ) : programas.length === 0 ? (
                            <tr><td colSpan={6} className="text-center py-8 text-gray-400">No hay programas registrados.</td></tr>
                        ) : programas.map(p => (
                            <tr key={p.id} className="border-b border-gray-100 hover:bg-gray-50">
                                <td className="px-4 py-3 font-medium">{p.nombre}</td>
                                <td className="px-4 py-3 text-gray-500">{p.facultadNombre}</td>
                                <td className="px-4 py-3 text-center">{p.numeroTotalPracticas}</td>
                                <td className="px-4 py-3 text-center">{p.promedioMinimoGeneral.toFixed(1)}</td>
                                <td className="px-4 py-3">
                                    <span className={p.activo ? 'badge-apto' : 'badge-no-apto'}>
                                        {p.activo ? 'Activo' : 'Inactivo'}
                                    </span>
                                </td>
                                <td className="px-4 py-3 text-right whitespace-nowrap space-x-3">
                                    {p.activo && (
                                        <>
                                            <button
                                                className="text-xs text-cue-primary hover:underline"
                                                onClick={() => abrirEditar(p)}
                                            >
                                                Editar
                                            </button>
                                            <button
                                                className="text-xs text-red-500 hover:text-red-700"
                                                onClick={() => handleDesactivar(p.id)}
                                            >
                                                Desactivar
                                            </button>
                                        </>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* ================================================================
                MODAL: Crear / Editar programa (Patrón Builder)
                Construye paso a paso:
                Paso 1 — Datos básicos
                Paso 2 — Requisitos por número de práctica (configurables)
            ================================================================ */}
            {modal && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-1">
                            {editandoId ? 'Editar Programa' : 'Nuevo Programa'}
                        </h2>
                        <p className="text-xs text-gray-500 mb-4">
                            Builder: configura paso a paso los datos básicos y los requisitos
                            individuales por número de práctica.
                        </p>
                        {error && (
                            <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>
                        )}
                        <form onSubmit={guardar} className="space-y-5">

                            {/* Paso 1: Datos básicos */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Paso 1 — Datos básicos</legend>
                                <div className="grid grid-cols-2 gap-3">
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Nombre *</label>
                                        <input className="input-field" required value={form.nombre}
                                               onChange={e => setForm({ ...form, nombre: e.target.value })} />
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Descripción</label>
                                        <textarea className="input-field" rows={2} value={form.descripcion}
                                                  onChange={e => setForm({ ...form, descripcion: e.target.value })} />
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Facultad *</label>
                                        <select className="input-field" required value={form.facultadId}
                                                onChange={e => setForm({ ...form, facultadId: e.target.value })}>
                                            <option value="">— Selecciona una facultad —</option>
                                            {facultades.map(f => (
                                                <option key={f.id} value={f.id}>{f.nombre}</option>
                                            ))}
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">N° total de prácticas *</label>
                                        <input className="input-field" type="number" min={1} max={10} required
                                               value={form.numeroTotalPracticas}
                                               onChange={e => handleNumPracticasChange(Number(e.target.value))} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Promedio mínimo general</label>
                                        <input className="input-field" type="number" step="0.1" min={0} max={5}
                                               value={form.promedioMinimoGeneral}
                                               onChange={e => setForm({ ...form, promedioMinimoGeneral: Number(e.target.value) })} />
                                    </div>
                                </div>
                            </fieldset>

                            {/* Paso 2: Requisitos por número de práctica */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">
                                    Paso 2 — Requisitos por práctica (configurables sin tocar código)
                                </legend>
                                <div className="space-y-4 mt-2">
                                    {form.requisitosPorPractica.map((req, idx) => (
                                        <div key={req.numeroPractica}
                                             className="border border-gray-100 rounded-lg p-3 bg-gray-50">
                                            <p className="text-xs font-bold text-cue-primary mb-2 uppercase">
                                                Práctica {req.numeroPractica}
                                            </p>
                                            <div className="grid grid-cols-2 gap-2 mb-2">
                                                <div>
                                                    <label className="block text-xs font-medium text-gray-600 mb-1">
                                                        Créditos mínimos
                                                    </label>
                                                    <input className="input-field py-1.5 text-sm"
                                                           type="number" min={0}
                                                           value={req.creditosMinimos}
                                                           onChange={e => updateRequisito(idx, 'creditosMinimos', Number(e.target.value))} />
                                                </div>
                                                <div>
                                                    <label className="block text-xs font-medium text-gray-600 mb-1">
                                                        Promedio mínimo
                                                    </label>
                                                    <input className="input-field py-1.5 text-sm"
                                                           type="number" step="0.1" min={0} max={5}
                                                           value={req.promedioMinimo}
                                                           onChange={e => updateRequisito(idx, 'promedioMinimo', Number(e.target.value))} />
                                                </div>
                                            </div>
                                            <label className="flex items-center gap-2 text-sm text-gray-700 mb-2">
                                                <input
                                                    type="checkbox"
                                                    checked={req.requierePracticaAnterior}
                                                    disabled={idx === 0}
                                                    onChange={e => updateRequisito(idx, 'requierePracticaAnterior', e.target.checked)}
                                                    className="w-4 h-4"
                                                />
                                                Requiere práctica anterior aprobada
                                                {idx === 0 && <span className="text-xs text-gray-400">(no aplica para Práctica 1)</span>}
                                            </label>
                                            <div>
                                                <label className="block text-xs font-medium text-gray-600 mb-1">Documentos requeridos</label>
                                                <div className="flex gap-2 mb-1">
                                                    <input
                                                        className="input-field py-1.5 text-sm flex-1"
                                                        placeholder="Nombre del documento..."
                                                        value={docTemps[idx] ?? ''}
                                                        onChange={e => setDocTemps(prev => { const a = [...prev]; a[idx] = e.target.value; return a })}
                                                        onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); agregarDoc(idx) } }}
                                                    />
                                                    <button type="button" className="btn-secondary px-2 py-1 text-xs"
                                                            onClick={() => agregarDoc(idx)}>+ Agregar</button>
                                                </div>
                                                {req.documentosRequeridos.length > 0 && (
                                                    <div className="flex flex-wrap gap-1">
                                                        {req.documentosRequeridos.map((d, di) => (
                                                            <span key={di}
                                                                  className="bg-white border border-gray-200 rounded px-2 py-0.5 text-xs text-gray-700 flex items-center gap-1">
                                                                {d}
                                                                <button type="button" className="text-red-400 hover:text-red-600 ml-0.5"
                                                                        onClick={() => quitarDoc(idx, di)}>✕</button>
                                                            </span>
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </fieldset>

                            <div className="flex gap-3">
                                <button type="button" className="btn-secondary flex-1"
                                        onClick={() => { setModal(false); setError('') }}>Cancelar</button>
                                <button type="submit" className="btn-primary flex-1">
                                    {editandoId ? 'Guardar cambios' : 'Crear Programa'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
