import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import {
    CatalogoPracticaResponse, ProgramaResponse,
    ApiResponse, Pageable,
} from '../../types'
import {
    catalogoPracticaService,
    CrearCatalogoPracticaRequest,
    EditarCatalogoPracticaRequest,
} from '../../services/catalogoPracticaService'
import api from '../../services/api'

/**
 * GPE-141 — Catálogo institucional de prácticas.
 *
 * Patrones implementados:
 * - Prototype: cada entrada es la PLANTILLA BASE. El sistema la clona al
 *   marcar un estudiante como APTO, garantizando consistencia institucional.
 *   Los cambios en el catálogo solo afectan instancias futuras.
 * - Builder: la creación de una plantilla se construye paso a paso:
 *   identificación → materia núcleo → configuración → documentos requeridos.
 * - Observer: el listado re-carga automáticamente cuando cambia el filtro
 *   de programa (useEffect suscrito a filtroPrograma).
 */
export default function CatalogoPracticasPage() {
    const { user } = useAuth()

    // ── State ─────────────────────────────────────────────────────────────────
    const [catalogo, setCatalogo] = useState<CatalogoPracticaResponse[]>([])
    const [programas, setProgramas] = useState<ProgramaResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [filtroPrograma, setFiltroPrograma] = useState('')
    const [error, setError] = useState('')
    const [exito, setExito] = useState('')
    const [modalCrear, setModalCrear] = useState(false)
    const [modalEditar, setModalEditar] = useState<CatalogoPracticaResponse | null>(null)
    const [docTemp, setDocTemp] = useState('')

    // Builder: formulario de creación de plantilla
    const formInicial: CrearCatalogoPracticaRequest = {
        numeroPractica: 1,
        nombre: '',
        materiaNucleoNombre: '',
        materiaNucleoCodigo: '',
        programaId: 0,
        numeroCortesSegumiento: 3,
        duracionSemanas: 16,
        documentosRequeridos: [],
    }
    const [form, setForm] = useState<CrearCatalogoPracticaRequest>(formInicial)

    // Formulario de edición
    const [formEdit, setFormEdit] = useState<EditarCatalogoPracticaRequest>({})
    const [docTempEdit, setDocTempEdit] = useState('')

    // ── Permisos (Strategy) ───────────────────────────────────────────────────
    const canGestionar =
        user?.rol === 'COORDINACION_ACADEMICA' || user?.rol === 'ADMIN_DTI'

    // ── Data loading ──────────────────────────────────────────────────────────
    const cargar = () => {
        setLoading(true)
        catalogoPracticaService
            .listar(0, 50, filtroPrograma ? Number(filtroPrograma) : undefined)
            .then(pg => setCatalogo(pg.content ?? []))
            .catch(() => setCatalogo([]))
            .finally(() => setLoading(false))
    }

    // Observer: re-carga cuando cambia el filtro de programa
    useEffect(() => { cargar() }, [filtroPrograma]) // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        api.get<ApiResponse<Pageable<ProgramaResponse>>>('/programas?size=100')
            .then(r => setProgramas(r.data.datos?.content ?? []))
    }, [])

    // ── Builder: gestión de documentos requeridos ─────────────────────────────
    const agregarDoc = () => {
        if (!docTemp.trim()) return
        setForm(prev => ({
            ...prev,
            documentosRequeridos: [...prev.documentosRequeridos, docTemp.trim()],
        }))
        setDocTemp('')
    }

    const quitarDoc = (idx: number) => {
        setForm(prev => ({
            ...prev,
            documentosRequeridos: prev.documentosRequeridos.filter((_, i) => i !== idx),
        }))
    }

    const agregarDocEdit = () => {
        if (!docTempEdit.trim()) return
        setFormEdit(prev => ({
            ...prev,
            documentosRequeridos: [
                ...(prev.documentosRequeridos ?? modalEditar?.documentosRequeridos ?? []),
                docTempEdit.trim(),
            ],
        }))
        setDocTempEdit('')
    }

    const quitarDocEdit = (idx: number) => {
        const base = formEdit.documentosRequeridos ?? modalEditar?.documentosRequeridos ?? []
        setFormEdit(prev => ({
            ...prev,
            documentosRequeridos: base.filter((_, i) => i !== idx),
        }))
    }

    // ── Crear plantilla ───────────────────────────────────────────────────────
    const handleCrear = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!form.nombre || !form.materiaNucleoNombre || !form.materiaNucleoCodigo || !form.programaId) {
            setError('Nombre, materia núcleo (nombre y código) y programa son obligatorios.')
            return
        }
        setError('')
        try {
            await catalogoPracticaService.crear({ ...form, programaId: Number(form.programaId) })
            setExito(
                'Plantilla creada. El sistema la clonará (Prototype) al marcar los próximos estudiantes como APTOS.'
            )
            setModalCrear(false)
            setForm(formInicial)
            setDocTemp('')
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al crear la plantilla.')
        }
    }

    // ── Editar plantilla ──────────────────────────────────────────────────────
    const abrirEditar = (p: CatalogoPracticaResponse) => {
        setModalEditar(p)
        setFormEdit({
            nombre: p.nombre,
            materiaNucleoNombre: p.materiaNucleoNombre,
            materiaNucleoCodigo: p.materiaNucleoCodigo,
            numeroCortesSegumiento: p.numeroCortesSegumiento,
            duracionSemanas: p.duracionSemanas,
            documentosRequeridos: [...p.documentosRequeridos],
        })
        setDocTempEdit('')
        setError('')
    }

    const handleEditar = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!modalEditar) return
        setError('')
        try {
            await catalogoPracticaService.editar(modalEditar.id, formEdit)
            setExito('Plantilla actualizada. Los cambios aplican solo a nuevas instancias.')
            setModalEditar(null)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al actualizar la plantilla.')
        }
    }

    // ── Desactivar ────────────────────────────────────────────────────────────
    const handleDesactivar = async (id: number) => {
        if (!confirm('¿Desactivar esta plantilla? Solo es posible si no hay estudiantes activos en ella.')) return
        try {
            await catalogoPracticaService.desactivar(id)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            alert(msg ?? 'No se puede desactivar: hay estudiantes activos en esta práctica.')
        }
    }

    // ── Helper: lista de documentos en el formulario de edición ──────────────
    const docsEdit = formEdit.documentosRequeridos ?? modalEditar?.documentosRequeridos ?? []

    // ─────────────────────────────────────────────────────────────────────────
    return (
        <div className="space-y-6">

            {/* ── Header ── */}
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Catálogo de Prácticas</h1>
                    <p className="text-sm text-gray-500">
                        Plantillas institucionales (Prototype). El sistema las clona al marcar un estudiante como APTO.
                        Cambios aquí no afectan las prácticas ya en curso.
                    </p>
                </div>
                {canGestionar && (
                    <button
                        className="btn-primary"
                        onClick={() => { setModalCrear(true); setForm(formInicial); setDocTemp(''); setError('') }}
                    >
                        + Nueva Plantilla
                    </button>
                )}
            </div>

            {/* ── Feedback ── */}
            {exito && (
                <div className="bg-green-50 border border-green-200 text-green-700 rounded-lg px-4 py-3 text-sm flex items-center justify-between">
                    <span>{exito}</span>
                    <button className="text-green-500 hover:text-green-700 ml-2" onClick={() => setExito('')}>✕</button>
                </div>
            )}
            {error && !modalCrear && !modalEditar && (
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3 text-sm flex items-center justify-between">
                    <span>{error}</span>
                    <button className="text-red-500 hover:text-red-700 ml-2" onClick={() => setError('')}>✕</button>
                </div>
            )}

            {/* ── Filtro ── */}
            <div className="flex items-center gap-3">
                <label className="text-sm font-medium text-gray-700">Programa:</label>
                <select
                    className="input-field w-auto"
                    value={filtroPrograma}
                    onChange={e => setFiltroPrograma(e.target.value)}
                >
                    <option value="">Todos los programas</option>
                    {programas.map(p => (
                        <option key={p.id} value={p.id}>{p.nombre}</option>
                    ))}
                </select>
            </div>

            {/* ── Tarjetas del catálogo ── */}
            {loading ? (
                <p className="text-gray-400 py-4">Cargando...</p>
            ) : catalogo.length === 0 ? (
                <div className="card text-center py-14 text-gray-400">
                    No hay plantillas configuradas para el filtro seleccionado.
                    {canGestionar && (
                        <p className="text-sm mt-1">
                            Crea la primera plantilla antes de marcar estudiantes como APTOS.
                        </p>
                    )}
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {catalogo.map(p => (
                        <div
                            key={p.id}
                            className={`card border-l-4 transition-opacity ${
                                p.activo ? 'border-l-cue-primary' : 'border-l-gray-300 opacity-60'
                            }`}
                        >
                            {/* Cabecera */}
                            <div className="flex items-start justify-between mb-3">
                                <div>
                                    <p className="text-xs font-bold text-cue-primary uppercase tracking-wide">
                                        Plantilla · Práctica {p.numeroPractica}
                                    </p>
                                    <h3 className="font-semibold text-gray-800 mt-0.5">{p.nombre}</h3>
                                </div>
                                <span className={p.activo ? 'badge-apto' : 'badge-no-apto'}>
                                    {p.activo ? 'Activa' : 'Inactiva'}
                                </span>
                            </div>

                            {/* Datos */}
                            <div className="text-sm text-gray-600 space-y-1.5">
                                <p>
                                    <span className="font-medium">Materia núcleo:</span>{' '}
                                    {p.materiaNucleoNombre}{' '}
                                    <span className="text-gray-400 text-xs">({p.materiaNucleoCodigo})</span>
                                </p>
                                <p>
                                    <span className="font-medium">Programa:</span> {p.programaNombre}
                                </p>
                                <p>
                                    <span className="font-medium">Cortes:</span> {p.numeroCortesSegumiento}
                                    {' · '}
                                    <span className="font-medium">Duración:</span> {p.duracionSemanas} semanas
                                </p>
                                {p.documentosRequeridos.length > 0 && (
                                    <div>
                                        <p className="font-medium">Documentos requeridos:</p>
                                        <ul className="list-disc list-inside text-xs text-gray-500 mt-0.5 space-y-0.5">
                                            {p.documentosRequeridos.map((d, i) => (
                                                <li key={i}>{d}</li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </div>

                            {/* Acciones */}
                            {canGestionar && p.activo && (
                                <div className="flex gap-3 mt-3 pt-3 border-t border-gray-100">
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
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {/* ================================================================
                MODAL: Crear plantilla (Patrón Builder)
                Construye paso a paso:
                identificación → materia núcleo → configuración → documentos
            ================================================================ */}
            {modalCrear && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-1">Nueva Plantilla de Práctica</h2>
                        <p className="text-xs text-gray-500 mb-4">
                            Esta plantilla es el <strong>Prototype</strong> que el sistema clona al marcar un estudiante
                            como APTO. Los cambios posteriores no afectan prácticas ya en curso.
                        </p>
                        {error && (
                            <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>
                        )}
                        <form onSubmit={handleCrear} className="space-y-4">

                            {/* Paso 1: Identificación */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Identificación</legend>
                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">N° de práctica *</label>
                                        <input className="input-field" type="number" min={1} required
                                               value={form.numeroPractica}
                                               onChange={e => setForm({ ...form, numeroPractica: Number(e.target.value) })} />
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
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Nombre de la práctica *</label>
                                        <input className="input-field" required value={form.nombre}
                                               onChange={e => setForm({ ...form, nombre: e.target.value })} />
                                    </div>
                                </div>
                            </fieldset>

                            {/* Paso 2: Materia núcleo */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Materia núcleo obligatoria</legend>
                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Nombre *</label>
                                        <input className="input-field" required value={form.materiaNucleoNombre}
                                               onChange={e => setForm({ ...form, materiaNucleoNombre: e.target.value })} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Código *</label>
                                        <input className="input-field" required value={form.materiaNucleoCodigo}
                                               onChange={e => setForm({ ...form, materiaNucleoCodigo: e.target.value })} />
                                    </div>
                                </div>
                            </fieldset>

                            {/* Paso 3: Configuración */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Configuración</legend>
                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Cortes de seguimiento *
                                        </label>
                                        <input className="input-field" type="number" min={1} required
                                               value={form.numeroCortesSegumiento}
                                               onChange={e => setForm({ ...form, numeroCortesSegumiento: Number(e.target.value) })} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">
                                            Duración (semanas) *
                                        </label>
                                        <input className="input-field" type="number" min={1} required
                                               value={form.duracionSemanas}
                                               onChange={e => setForm({ ...form, duracionSemanas: Number(e.target.value) })} />
                                    </div>
                                </div>
                            </fieldset>

                            {/* Paso 4: Documentos requeridos */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Documentos requeridos</legend>
                                <div className="flex gap-2 mb-2">
                                    <input
                                        className="input-field flex-1"
                                        placeholder="Ej: Carta de intención de la empresa..."
                                        value={docTemp}
                                        onChange={e => setDocTemp(e.target.value)}
                                        onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); agregarDoc() } }}
                                    />
                                    <button type="button" className="btn-secondary px-3" onClick={agregarDoc}>
                                        + Agregar
                                    </button>
                                </div>
                                {form.documentosRequeridos.length > 0 ? (
                                    <ul className="space-y-1">
                                        {form.documentosRequeridos.map((d, i) => (
                                            <li key={i} className="flex items-center justify-between bg-gray-50 rounded px-3 py-1.5 text-sm">
                                                <span className="text-gray-700">{d}</span>
                                                <button type="button" className="text-red-400 hover:text-red-600 text-xs ml-2"
                                                        onClick={() => quitarDoc(i)}>✕</button>
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className="text-xs text-gray-400 italic">Sin documentos definidos aún.</p>
                                )}
                            </fieldset>

                            <div className="flex gap-3">
                                <button type="button" className="btn-secondary flex-1"
                                        onClick={() => { setModalCrear(false); setError('') }}>
                                    Cancelar
                                </button>
                                <button type="submit" className="btn-primary flex-1">Crear Plantilla</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ================================================================
                MODAL: Editar plantilla
                Los cambios aplican solo a nuevas instancias (Prototype: el
                clon ya existente no se modifica retroactivamente).
            ================================================================ */}
            {modalEditar && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-1">Editar Plantilla</h2>
                        <p className="text-xs text-gray-500 mb-4">
                            Práctica {modalEditar.numeroPractica} — {modalEditar.programaNombre}.
                            Los cambios aplican <strong>solo a nuevas instancias</strong>; las prácticas activas conservan la configuración original.
                        </p>
                        {error && (
                            <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>
                        )}
                        <form onSubmit={handleEditar} className="space-y-4">

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre *</label>
                                <input className="input-field" required
                                       value={formEdit.nombre ?? ''}
                                       onChange={e => setFormEdit({ ...formEdit, nombre: e.target.value })} />
                            </div>

                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Materia núcleo</legend>
                                <div className="grid grid-cols-2 gap-3">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Nombre *</label>
                                        <input className="input-field" required
                                               value={formEdit.materiaNucleoNombre ?? ''}
                                               onChange={e => setFormEdit({ ...formEdit, materiaNucleoNombre: e.target.value })} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Código *</label>
                                        <input className="input-field" required
                                               value={formEdit.materiaNucleoCodigo ?? ''}
                                               onChange={e => setFormEdit({ ...formEdit, materiaNucleoCodigo: e.target.value })} />
                                    </div>
                                </div>
                            </fieldset>

                            <div className="grid grid-cols-2 gap-3">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Cortes *</label>
                                    <input className="input-field" type="number" min={1} required
                                           value={formEdit.numeroCortesSegumiento ?? ''}
                                           onChange={e => setFormEdit({ ...formEdit, numeroCortesSegumiento: Number(e.target.value) })} />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Duración (semanas) *</label>
                                    <input className="input-field" type="number" min={1} required
                                           value={formEdit.duracionSemanas ?? ''}
                                           onChange={e => setFormEdit({ ...formEdit, duracionSemanas: Number(e.target.value) })} />
                                </div>
                            </div>

                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Documentos requeridos</legend>
                                <div className="flex gap-2 mb-2">
                                    <input
                                        className="input-field flex-1"
                                        placeholder="Agregar documento..."
                                        value={docTempEdit}
                                        onChange={e => setDocTempEdit(e.target.value)}
                                        onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); agregarDocEdit() } }}
                                    />
                                    <button type="button" className="btn-secondary px-3" onClick={agregarDocEdit}>
                                        + Agregar
                                    </button>
                                </div>
                                {docsEdit.length > 0 ? (
                                    <ul className="space-y-1">
                                        {docsEdit.map((d, i) => (
                                            <li key={i} className="flex items-center justify-between bg-gray-50 rounded px-3 py-1.5 text-sm">
                                                <span className="text-gray-700">{d}</span>
                                                <button type="button" className="text-red-400 hover:text-red-600 text-xs ml-2"
                                                        onClick={() => quitarDocEdit(i)}>✕</button>
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className="text-xs text-gray-400 italic">Sin documentos.</p>
                                )}
                            </fieldset>

                            <div className="flex gap-3">
                                <button type="button" className="btn-secondary flex-1"
                                        onClick={() => { setModalEditar(null); setError('') }}>
                                    Cancelar
                                </button>
                                <button type="submit" className="btn-primary flex-1">Guardar cambios</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
