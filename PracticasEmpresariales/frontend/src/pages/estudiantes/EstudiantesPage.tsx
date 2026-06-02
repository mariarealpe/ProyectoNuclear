import { useState, useEffect, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import {
    EstudianteResponse, EstadoEstudiante, EstadoPractica, EstadoHojaDeVida,
    ProgramaResponse, ApiResponse, Pageable,
} from '../../types'
import {
    estudianteService,
    CrearEstudianteRequest,
    ValidarAptitudRequest,
} from '../../services/estudianteService'
import api from '../../services/api'

// ─── Etiquetas de presentación ────────────────────────────────────────────────

const ESTADO_EST_LABELS: Record<EstadoEstudiante, string> = {
    NO_APTO: 'No Apto',
    APTO: 'Apto',
}

const ESTADO_PRACTICA_LABELS: Record<EstadoPractica, string> = {
    ASIGNADA_PENDIENTE_INICIO: 'Pendiente inicio',
    EN_CURSO: 'En Curso',
    FINALIZADA: 'Finalizada',
    CANCELADA: 'Cancelada',
}

const ESTADO_HV_LABELS: Record<EstadoHojaDeVida, string> = {
    PENDIENTE: 'Pendiente',
    VALIDA: 'Válida',
    RECHAZADA: 'Rechazada',
}

const CLASES_PRACTICA: Record<EstadoPractica, string> = {
    ASIGNADA_PENDIENTE_INICIO: 'bg-yellow-100 text-yellow-700',
    EN_CURSO: 'bg-blue-100 text-blue-700',
    FINALIZADA: 'bg-green-100 text-green-700',
    CANCELADA: 'bg-red-100 text-red-700',
}

// ─── Chain of Responsibility: eslabones de validación de aptitud ──────────────

interface EslabonValidacion {
    id: string
    etiqueta: string
    descripcion: string
    superado: boolean
}

const CADENA_BASE: EslabonValidacion[] = [
    {
        id: 'creditos',
        etiqueta: 'Créditos mínimos aprobados',
        descripcion: 'El estudiante tiene los créditos aprobados requeridos para esta práctica.',
        superado: false,
    },
    {
        id: 'promedio',
        etiqueta: 'Promedio académico mínimo',
        descripcion: 'El promedio del estudiante cumple o supera el mínimo configurado en el programa.',
        superado: false,
    },
    {
        id: 'practica_anterior',
        etiqueta: 'Práctica anterior aprobada (si aplica)',
        descripcion: 'Si no es la primera práctica, la anterior debe estar en estado FINALIZADA.',
        superado: false,
    },
    {
        id: 'documentos',
        etiqueta: 'Documentos base completos',
        descripcion: 'El estudiante ha cargado todos los documentos base requeridos por el programa.',
        superado: false,
    },
    {
        id: 'hoja_de_vida',
        etiqueta: 'Hoja de vida en estado Válida',
        descripcion: 'La hoja de vida del estudiante ha sido validada y tiene estado Válida.',
        superado: false,
    },
]

// ─── Helpers visuales ─────────────────────────────────────────────────────────

const BadgeEstado = ({ estado }: { estado: EstadoEstudiante }) =>
    estado === 'APTO'
        ? <span className="badge-apto">{ESTADO_EST_LABELS.APTO}</span>
        : <span className="badge-no-apto">{ESTADO_EST_LABELS.NO_APTO}</span>

const BadgeHV = ({ estado }: { estado: EstadoHojaDeVida }) => {
    const clases =
        estado === 'VALIDA' ? 'bg-green-100 text-green-700' :
        estado === 'RECHAZADA' ? 'bg-red-100 text-red-700' :
        'bg-yellow-100 text-yellow-700'
    return (
        <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${clases}`}>
            {ESTADO_HV_LABELS[estado]}
        </span>
    )
}

// ─── Componente principal ─────────────────────────────────────────────────────

/**
 * GPE-143 · GPE-145 · GPE-146 · GPE-147
 *
 * Patrones implementados:
 * - Proxy (Protection Proxy): `permisos` centraliza qué acciones puede
 *   realizar cada rol. El backend valida scope en cada petición.
 * - Observer: los useEffect suscriben el listado a cambios de filtro.
 *   cargar() es el manejador de notificación.
 * - Strategy: `permisos` encapsula la estrategia de renderizado por rol.
 *   Abierto a extensión (nuevos roles) sin modificar el render.
 * - Builder: el formulario de creación construye el estudiante paso a paso:
 *   datos personales → datos académicos → NO_APTO asignado automáticamente.
 * - Chain of Responsibility: la validación de aptitud recorre una cadena
 *   secuencial de eslabones; si uno falla los siguientes quedan bloqueados.
 */
export default function EstudiantesPage() {
    const { user } = useAuth()
    const [searchParams] = useSearchParams()

    // ── State principal ───────────────────────────────────────────────────────
    const [estudiantes, setEstudiantes] = useState<EstudianteResponse[]>([])
    const [programas, setProgramas] = useState<ProgramaResponse[]>([])
    const [loading, setLoading] = useState(true)
    // Lee el filtro de estado desde el URL (?estado=APTO | ?estado=NO_APTO)
    // Esto permite que los accesos directos del dashboard pre-apliquen el filtro.
    const [filtroEstado, setFiltroEstado] = useState<EstadoEstudiante | ''>(
        (searchParams.get('estado') as EstadoEstudiante) ?? ''
    )
    const [filtroPrograma, setFiltroPrograma] = useState('')
    const [busqueda, setBusqueda] = useState('')
    const [seleccionados, setSeleccionados] = useState<number[]>([])
    const [error, setError] = useState('')
    const [exito, setExito] = useState('')

    // ── Modales ───────────────────────────────────────────────────────────────
    const [modalCrear, setModalCrear] = useState(false)
    const [modalValidar, setModalValidar] = useState<EstudianteResponse | null>(null)
    const [modalExpediente, setModalExpediente] = useState<EstudianteResponse | null>(null)
    const [confirmEnviar, setConfirmEnviar] = useState(false)

    // ── Builder: formulario de creación de estudiante ─────────────────────────
    const formCrearInicial: CrearEstudianteRequest = {
        nombre: '', identificacion: '', correo: '',
        telefono: '', contactoEmergencia: '',
        programaId: 0, semestre: 1,
    }
    const [formCrear, setFormCrear] = useState<CrearEstudianteRequest>(formCrearInicial)

    // ── Chain of Responsibility: estado de la cadena de validación ────────────
    const [cadena, setCadena] = useState<EslabonValidacion[]>(CADENA_BASE)
    const [motivoRechazo, setMotivoRechazo] = useState('')
    const [decisionAptitud, setDecisionAptitud] = useState<'APTO' | 'NO_APTO' | null>(null)

    // ── Strategy: permisos por rol (Proxy frontend) ───────────────────────────
    const permisos = useMemo(() => {
        const rol = user?.rol ?? ''
        return {
            canCrear: rol === 'ADMIN_DTI',
            canValidarAptitud: rol === 'COORDINACION_ACADEMICA',
            canEnviarAlProceso: rol === 'COORDINACION_ACADEMICA',
            canVerNoAptos: rol === 'ADMIN_DTI' || rol === 'COORDINACION_ACADEMICA',
            canVerExpediente: ['ADMIN_DTI', 'COORDINACION_ACADEMICA', 'COORDINADOR_PRACTICAS', 'DOCENTE_ASESOR'].includes(rol),
            soloLectura: rol === 'DIRECCION',
        }
    }, [user?.rol])

    // ── Data loading ──────────────────────────────────────────────────────────
    const cargar = () => {
        setLoading(true)
        estudianteService
            .listar(
                0, 50,
                filtroEstado || undefined,
                filtroPrograma ? Number(filtroPrograma) : undefined,
                busqueda.trim() || undefined,
            )
            .then(pg => setEstudiantes(pg.content ?? []))
            .catch(() => setEstudiantes([]))
            .finally(() => setLoading(false))
    }

    // Observer: re-carga cuando cambian los filtros de estado o programa
    useEffect(() => { cargar() }, [filtroEstado, filtroPrograma]) // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        api.get<ApiResponse<Pageable<ProgramaResponse>>>('/programas?size=100')
            .then(r => setProgramas(r.data.datos?.content ?? []))
    }, [])

    const handleBuscar = (e: React.FormEvent) => {
        e.preventDefault()
        cargar()
    }

    // ── Builder: crear estudiante ─────────────────────────────────────────────
    const handleCrear = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        try {
            await estudianteService.crear({
                ...formCrear,
                programaId: Number(formCrear.programaId),
                semestre: Number(formCrear.semestre),
                telefono: formCrear.telefono || undefined,
                contactoEmergencia: formCrear.contactoEmergencia || undefined,
            })
            setExito('Estudiante registrado con estado NO_APTO. Se envió la contraseña temporal al correo.')
            setModalCrear(false)
            setFormCrear(formCrearInicial)
            setSeleccionados([])
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al registrar el estudiante.')
        }
    }

    // ── Chain of Responsibility: abrir modal de validación ────────────────────
    const abrirValidar = (est: EstudianteResponse) => {
        setModalValidar(est)
        setCadena(CADENA_BASE.map(e => ({ ...e, superado: false })))
        setDecisionAptitud(null)
        setMotivoRechazo('')
        setError('')
    }

    const toggleEslabon = (id: string) => {
        const idx = cadena.findIndex(e => e.id === id)
        if (idx < 0) return
        const anterioresTodosOk = cadena.slice(0, idx).every(e => e.superado)
        if (!anterioresTodosOk && !cadena[idx].superado) return
        setCadena(cadena.map((e, i) => {
            if (i < idx) return e
            if (i === idx) return { ...e, superado: !e.superado }
            return { ...e, superado: false }
        }))
    }

    const todosEslabonesOk = cadena.every(e => e.superado)

    const aplicarAptitud = async () => {
        if (!modalValidar || !decisionAptitud) return
        if (decisionAptitud === 'NO_APTO' && !motivoRechazo.trim()) {
            setError('El motivo del rechazo es obligatorio.')
            return
        }
        setError('')
        const payload: ValidarAptitudRequest = {
            aprobar: decisionAptitud === 'APTO',
            motivoRechazo: decisionAptitud === 'NO_APTO' ? motivoRechazo.trim() : undefined,
        }
        try {
            await estudianteService.validarAptitud(modalValidar.id, payload)
            setExito(
                decisionAptitud === 'APTO'
                    ? 'Estudiante marcado como APTO. Se creó su instancia de práctica automáticamente.'
                    : 'Estado mantenido como NO_APTO. El motivo quedó registrado en bitácora.'
            )
            setModalValidar(null)
            setSeleccionados([])
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al validar la aptitud.')
        }
    }

    // ── Selección múltiple y envío al proceso ─────────────────────────────────
    const toggleSeleccion = (id: number) => {
        setSeleccionados(prev =>
            prev.includes(id) ? prev.filter(s => s !== id) : [...prev, id]
        )
    }

    const seleccionarTodosAptos = () => {
        setSeleccionados(
            estudiantes
                .filter(e => e.estadoEstudiante === 'APTO' && !e.enviandoAlProceso)
                .map(e => e.id)
        )
    }

    const handleEnviarAlProceso = async () => {
        if (seleccionados.length === 0) return
        setError('')
        try {
            await estudianteService.enviarAlProceso({ estudianteIds: seleccionados })
            setExito(
                `${seleccionados.length} estudiante(s) enviados al proceso. El Coordinador de Prácticas ya puede verlos.`
            )
            setSeleccionados([])
            setConfirmEnviar(false)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al enviar al proceso.')
        }
    }

    const aptosPendientesProceso = estudiantes.filter(
        e => e.estadoEstudiante === 'APTO' && !e.enviandoAlProceso
    )

    const columnCount = permisos.canEnviarAlProceso ? 8 : 7

    // ─────────────────────────────────────────────────────────────────────────
    return (
        <div className="space-y-6">

            {/* ── Header ── */}
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Gestión de Estudiantes</h1>
                    <p className="text-sm text-gray-500">
                        {permisos.canVerNoAptos
                            ? 'Valida requisitos, gestiona la aptitud y envía estudiantes al proceso de práctica.'
                            : 'Estudiantes APTOS asignados a tu programa y enviados al proceso.'}
                    </p>
                </div>
                <div className="flex gap-2 flex-wrap">
                    {permisos.canEnviarAlProceso && aptosPendientesProceso.length > 0 && seleccionados.length === 0 && (
                        <button className="btn-secondary" onClick={seleccionarTodosAptos}>
                            Seleccionar todos APTOS ({aptosPendientesProceso.length})
                        </button>
                    )}
                    {permisos.canEnviarAlProceso && seleccionados.length > 0 && (
                        <button className="btn-primary" onClick={() => setConfirmEnviar(true)}>
                            Enviar {seleccionados.length} al proceso →
                        </button>
                    )}
                    {permisos.canCrear && (
                        <button
                            className="btn-primary"
                            onClick={() => { setModalCrear(true); setError(''); setFormCrear(formCrearInicial) }}
                        >
                            + Nuevo Estudiante
                        </button>
                    )}
                </div>
            </div>

            {/* ── Feedback ── */}
            {exito && (
                <div className="bg-green-50 border border-green-200 text-green-700 rounded-lg px-4 py-3 text-sm flex items-center justify-between">
                    <span>{exito}</span>
                    <button className="text-green-500 hover:text-green-700 ml-2" onClick={() => setExito('')}>✕</button>
                </div>
            )}
            {error && !modalCrear && !modalValidar && !confirmEnviar && (
                <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3 text-sm flex items-center justify-between">
                    <span>{error}</span>
                    <button className="text-red-500 hover:text-red-700 ml-2" onClick={() => setError('')}>✕</button>
                </div>
            )}

            {/* ── Filtros ── */}
            <div className="card">
                <form onSubmit={handleBuscar} className="flex flex-wrap gap-3 items-end">
                    <div className="flex-1 min-w-52">
                        <label className="block text-xs font-medium text-gray-600 mb-1">Buscar</label>
                        <input
                            className="input-field"
                            placeholder="Nombre o número de identificación..."
                            value={busqueda}
                            onChange={e => setBusqueda(e.target.value)}
                        />
                    </div>
                    {permisos.canVerNoAptos && (
                        <div>
                            <label className="block text-xs font-medium text-gray-600 mb-1">Estado</label>
                            <select
                                className="input-field"
                                value={filtroEstado}
                                onChange={e => setFiltroEstado(e.target.value as EstadoEstudiante | '')}
                            >
                                <option value="">Todos</option>
                                <option value="NO_APTO">No Aptos</option>
                                <option value="APTO">Aptos</option>
                            </select>
                        </div>
                    )}
                    <div>
                        <label className="block text-xs font-medium text-gray-600 mb-1">Programa</label>
                        <select
                            className="input-field"
                            value={filtroPrograma}
                            onChange={e => setFiltroPrograma(e.target.value)}
                        >
                            <option value="">Todos</option>
                            {programas.map(p => (
                                <option key={p.id} value={p.id}>{p.nombre}</option>
                            ))}
                        </select>
                    </div>
                    <button type="submit" className="btn-secondary">Buscar</button>
                </form>
            </div>

            {/* ── Tabla de estudiantes ── */}
            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                            {permisos.canEnviarAlProceso && <th className="px-4 py-3 w-10"></th>}
                            {['Estudiante', 'Programa', 'Sem.', 'Estado', 'Práctica actual', 'HV', 'Acciones'].map(h => (
                                <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr>
                                <td colSpan={columnCount} className="text-center py-10 text-gray-400">
                                    Cargando...
                                </td>
                            </tr>
                        ) : estudiantes.length === 0 ? (
                            <tr>
                                <td colSpan={columnCount} className="text-center py-10 text-gray-400">
                                    No hay estudiantes que mostrar con los filtros actuales.
                                </td>
                            </tr>
                        ) : estudiantes.map(est => (
                            <tr
                                key={est.id}
                                className={`border-b border-gray-100 hover:bg-gray-50 transition-colors ${
                                    seleccionados.includes(est.id) ? 'bg-blue-50' : ''
                                }`}
                            >
                                {/* Columna de selección (solo CoordAcad) */}
                                {permisos.canEnviarAlProceso && (
                                    <td className="px-4 py-3">
                                        {est.estadoEstudiante === 'APTO' && !est.enviandoAlProceso ? (
                                            <input
                                                type="checkbox"
                                                checked={seleccionados.includes(est.id)}
                                                onChange={() => toggleSeleccion(est.id)}
                                                className="w-4 h-4"
                                            />
                                        ) : est.enviandoAlProceso ? (
                                            <span className="text-xs text-blue-600 font-semibold" title="Enviado al proceso">✓</span>
                                        ) : null}
                                    </td>
                                )}

                                {/* Datos del estudiante */}
                                <td className="px-4 py-3">
                                    <p className="font-medium text-gray-900">{est.nombre}</p>
                                    <p className="text-xs text-gray-500">{est.identificacion} · {est.correo}</p>
                                </td>

                                {/* Programa y facultad */}
                                <td className="px-4 py-3 text-xs text-gray-600">
                                    <p>{est.programaNombre}</p>
                                    <p className="text-gray-400">{est.facultadNombre}</p>
                                </td>

                                {/* Semestre */}
                                <td className="px-4 py-3 text-center text-gray-700">{est.semestre}°</td>

                                {/* Estado de aptitud */}
                                <td className="px-4 py-3">
                                    <BadgeEstado estado={est.estadoEstudiante} />
                                </td>

                                {/* Práctica actual */}
                                <td className="px-4 py-3 text-xs text-gray-600">
                                    {est.practicaActualNombre && est.practicaActualEstado ? (
                                        <div>
                                            <p>{est.practicaActualNombre}</p>
                                            <span className={`inline-block mt-0.5 px-1.5 py-0.5 rounded text-xs font-medium ${CLASES_PRACTICA[est.practicaActualEstado]}`}>
                                                {ESTADO_PRACTICA_LABELS[est.practicaActualEstado]}
                                            </span>
                                        </div>
                                    ) : (
                                        <span className="text-gray-400">—</span>
                                    )}
                                </td>

                                {/* Estado hoja de vida */}
                                <td className="px-4 py-3">
                                    {est.estadoHojaDeVida
                                        ? <BadgeHV estado={est.estadoHojaDeVida} />
                                        : <span className="text-xs text-gray-400">—</span>}
                                </td>

                                {/* Acciones */}
                                <td className="px-4 py-3 text-right whitespace-nowrap space-x-3">
                                    {permisos.canVerExpediente && (
                                        <button
                                            className="text-xs text-cue-primary hover:underline"
                                            onClick={() => setModalExpediente(est)}
                                        >
                                            Expediente
                                        </button>
                                    )}
                                    {permisos.canValidarAptitud && est.estadoEstudiante === 'NO_APTO' && (
                                        <button
                                            className="text-xs text-green-700 hover:underline"
                                            onClick={() => abrirValidar(est)}
                                        >
                                            Validar
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* ================================================================
                MODAL: Registrar estudiante (Patrón Builder)
                Construye el expediente paso a paso:
                datos personales → datos académicos → NO_APTO automático
            ================================================================ */}
            {modalCrear && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-1">Registrar Estudiante</h2>
                        <p className="text-xs text-gray-500 mb-4">
                            El sistema asigna estado <strong>NO_APTO</strong> automáticamente y envía la contraseña temporal al correo.
                        </p>
                        {error && (
                            <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>
                        )}
                        <form onSubmit={handleCrear} className="space-y-4">

                            {/* Paso 1: datos personales */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Datos personales</legend>
                                <div className="grid grid-cols-2 gap-3">
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo *</label>
                                        <input className="input-field" required
                                               value={formCrear.nombre}
                                               onChange={e => setFormCrear({ ...formCrear, nombre: e.target.value })} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Identificación *</label>
                                        <input className="input-field" required
                                               value={formCrear.identificacion}
                                               onChange={e => setFormCrear({ ...formCrear, identificacion: e.target.value })} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Correo institucional *</label>
                                        <input className="input-field" type="email" required
                                               value={formCrear.correo}
                                               onChange={e => setFormCrear({ ...formCrear, correo: e.target.value })} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                                        <input className="input-field"
                                               value={formCrear.telefono ?? ''}
                                               onChange={e => setFormCrear({ ...formCrear, telefono: e.target.value })} />
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Contacto de emergencia</label>
                                        <input className="input-field" placeholder="Nombre — teléfono"
                                               value={formCrear.contactoEmergencia ?? ''}
                                               onChange={e => setFormCrear({ ...formCrear, contactoEmergencia: e.target.value })} />
                                    </div>
                                </div>
                            </fieldset>

                            {/* Paso 2: datos académicos */}
                            <fieldset className="border border-gray-200 rounded-lg p-4">
                                <legend className="text-xs font-semibold text-gray-600 px-1">Datos académicos</legend>
                                <div className="grid grid-cols-2 gap-3">
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Programa académico *</label>
                                        <select className="input-field" required value={formCrear.programaId || ''}
                                                onChange={e => setFormCrear({ ...formCrear, programaId: Number(e.target.value) })}>
                                            <option value="">— Selecciona un programa —</option>
                                            {programas.filter(p => p.activo).map(p => (
                                                <option key={p.id} value={p.id}>
                                                    {p.nombre} ({p.facultadNombre})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Semestre *</label>
                                        <input className="input-field" type="number" min={1} max={10} required
                                               value={formCrear.semestre}
                                               onChange={e => setFormCrear({ ...formCrear, semestre: Number(e.target.value) })} />
                                    </div>
                                </div>
                            </fieldset>

                            {/* Aviso: estado automático */}
                            <div className="bg-amber-50 border border-amber-200 rounded-lg px-3 py-2.5 text-xs text-amber-800">
                                El sistema asignará <strong>NO_APTO</strong> automáticamente.
                                La Coordinación Académica valida los requisitos para cambiar a APTO.
                            </div>

                            <div className="flex gap-3">
                                <button type="button" className="btn-secondary flex-1"
                                        onClick={() => { setModalCrear(false); setError('') }}>
                                    Cancelar
                                </button>
                                <button type="submit" className="btn-primary flex-1">
                                    Registrar y enviar contraseña
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ================================================================
                MODAL: Validar aptitud (Patrón Chain of Responsibility)
                Cadena secuencial: créditos → promedio → práctica anterior
                → documentos → HV válida. Cada eslabón depende del anterior.
            ================================================================ */}
            {modalValidar && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-0.5">Validar Aptitud</h2>
                        <p className="text-sm text-gray-500">{modalValidar.nombre}</p>
                        <p className="text-xs text-gray-400 mb-5">
                            {modalValidar.programaNombre} — Semestre {modalValidar.semestre}°
                        </p>

                        {error && (
                            <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>
                        )}

                        {/* Cadena de validación */}
                        <div className="mb-5">
                            <p className="text-xs font-bold text-gray-500 uppercase tracking-wide mb-2">
                                Cadena de validación de requisitos
                            </p>
                            <div className="space-y-2">
                                {cadena.map((eslabon, idx) => {
                                    const anteriorOk = cadena.slice(0, idx).every(e => e.superado)
                                    const habilitado = idx === 0 || anteriorOk
                                    return (
                                        <label
                                            key={eslabon.id}
                                            className={`flex items-start gap-3 p-3 rounded-lg border cursor-pointer transition-colors select-none ${
                                                eslabon.superado
                                                    ? 'border-green-300 bg-green-50'
                                                    : habilitado
                                                    ? 'border-gray-200 bg-white hover:bg-gray-50'
                                                    : 'border-gray-100 bg-gray-50 opacity-40 cursor-not-allowed'
                                            }`}
                                        >
                                            <input
                                                type="checkbox"
                                                checked={eslabon.superado}
                                                disabled={!habilitado}
                                                onChange={() => toggleEslabon(eslabon.id)}
                                                className="w-4 h-4 mt-0.5"
                                            />
                                            <div>
                                                <p className="text-sm font-medium text-gray-700">{eslabon.etiqueta}</p>
                                                <p className="text-xs text-gray-500 mt-0.5">{eslabon.descripcion}</p>
                                            </div>
                                        </label>
                                    )
                                })}
                            </div>
                        </div>

                        {/* Decisión */}
                        <div className="mb-4">
                            <p className="text-xs font-bold text-gray-500 uppercase tracking-wide mb-2">
                                Decisión
                            </p>
                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    disabled={!todosEslabonesOk}
                                    onClick={() => { setDecisionAptitud('APTO'); setMotivoRechazo('') }}
                                    className={`flex-1 py-2.5 rounded-lg text-sm font-semibold border transition-colors ${
                                        decisionAptitud === 'APTO'
                                            ? 'bg-green-600 text-white border-green-600'
                                            : 'border-green-300 text-green-700 hover:bg-green-50 disabled:opacity-40 disabled:cursor-not-allowed'
                                    }`}
                                >
                                    ✓ Marcar APTO
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setDecisionAptitud('NO_APTO')}
                                    className={`flex-1 py-2.5 rounded-lg text-sm font-semibold border transition-colors ${
                                        decisionAptitud === 'NO_APTO'
                                            ? 'bg-red-600 text-white border-red-600'
                                            : 'border-red-300 text-red-700 hover:bg-red-50'
                                    }`}
                                >
                                    ✗ Mantener NO_APTO
                                </button>
                            </div>
                            {!todosEslabonesOk && (
                                <p className="text-xs text-gray-400 mt-1.5 text-center">
                                    Completa toda la cadena de validación para habilitar APTO.
                                </p>
                            )}
                        </div>

                        {decisionAptitud === 'NO_APTO' && (
                            <div className="mb-4">
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Motivo del rechazo *
                                </label>
                                <textarea
                                    className="input-field"
                                    rows={3}
                                    placeholder="Especifica qué requisito no cumple y qué debe hacer para subsanarlo..."
                                    value={motivoRechazo}
                                    onChange={e => setMotivoRechazo(e.target.value)}
                                />
                            </div>
                        )}

                        <div className="flex gap-3">
                            <button
                                className="btn-secondary flex-1"
                                onClick={() => { setModalValidar(null); setError('') }}
                            >
                                Cancelar
                            </button>
                            <button
                                className="btn-primary flex-1"
                                disabled={!decisionAptitud}
                                onClick={aplicarAptitud}
                            >
                                Confirmar
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ================================================================
                MODAL: Expediente histórico del estudiante (GPE-146)
                Proxy Protection: prácticas FINALIZADAS/CANCELADAS son solo lectura.
            ================================================================ */}
            {modalExpediente && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl p-6 my-8">
                        <div className="flex items-start justify-between mb-4">
                            <div>
                                <h2 className="text-lg font-bold text-gray-800">{modalExpediente.nombre}</h2>
                                <p className="text-sm text-gray-500">
                                    {modalExpediente.identificacion} · {modalExpediente.correo}
                                </p>
                            </div>
                            <BadgeEstado estado={modalExpediente.estadoEstudiante} />
                        </div>

                        {/* Datos personales y académicos */}
                        <div className="grid grid-cols-2 gap-3 mb-4">
                            <div className="bg-gray-50 rounded-lg p-3 text-sm">
                                <p className="text-xs font-bold text-gray-500 uppercase mb-2">Académico</p>
                                <p><span className="text-gray-500">Programa:</span> {modalExpediente.programaNombre}</p>
                                <p><span className="text-gray-500">Facultad:</span> {modalExpediente.facultadNombre}</p>
                                <p><span className="text-gray-500">Semestre:</span> {modalExpediente.semestre}°</p>
                            </div>
                            <div className="bg-gray-50 rounded-lg p-3 text-sm">
                                <p className="text-xs font-bold text-gray-500 uppercase mb-2">Contacto</p>
                                <p><span className="text-gray-500">Teléfono:</span> {modalExpediente.telefono ?? '—'}</p>
                                <p><span className="text-gray-500">Emergencia:</span> {modalExpediente.contactoEmergencia ?? '—'}</p>
                                <p className="flex items-center gap-2">
                                    <span className="text-gray-500">Hoja de vida:</span>
                                    {modalExpediente.estadoHojaDeVida
                                        ? <BadgeHV estado={modalExpediente.estadoHojaDeVida} />
                                        : '—'}
                                </p>
                            </div>
                        </div>

                        {/* Asignaciones actuales */}
                        {(modalExpediente.docenteAsesorNombre || modalExpediente.tutorEmpresarialNombre) && (
                            <div className="bg-blue-50 rounded-lg p-3 mb-4 text-sm text-blue-800">
                                {modalExpediente.docenteAsesorNombre && (
                                    <p><span className="font-semibold">Docente Asesor:</span> {modalExpediente.docenteAsesorNombre}</p>
                                )}
                                {modalExpediente.tutorEmpresarialNombre && (
                                    <p><span className="font-semibold">Tutor Empresarial:</span> {modalExpediente.tutorEmpresarialNombre}</p>
                                )}
                            </div>
                        )}

                        {/* Historial de prácticas */}
                        <div>
                            <p className="text-xs font-bold text-gray-500 uppercase mb-2">Historial de Prácticas</p>
                            {modalExpediente.historialPracticas?.length > 0 ? (
                                <div className="space-y-2">
                                    {modalExpediente.historialPracticas.map(practica => (
                                        <div key={practica.id}
                                             className={`border rounded-lg p-3 text-sm ${
                                                 practica.estado === 'FINALIZADA' || practica.estado === 'CANCELADA'
                                                     ? 'border-gray-200 bg-gray-50'
                                                     : 'border-cue-light bg-white'
                                             }`}>
                                            <div className="flex items-center justify-between">
                                                <div>
                                                    <span className="font-semibold text-gray-800">
                                                        Práctica {practica.numeroPractica}:
                                                    </span>{' '}
                                                    {practica.nombrePractica}
                                                </div>
                                                <div className="flex items-center gap-2">
                                                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${CLASES_PRACTICA[practica.estado]}`}>
                                                        {ESTADO_PRACTICA_LABELS[practica.estado]}
                                                    </span>
                                                    {(practica.estado === 'FINALIZADA' || practica.estado === 'CANCELADA') && (
                                                        <span className="text-xs text-gray-400 italic">solo lectura</span>
                                                    )}
                                                </div>
                                            </div>
                                            <p className="text-xs text-gray-500 mt-1">
                                                Materia núcleo: {practica.materiaNucleoNombre}{' '}
                                                <span className="text-gray-400">({practica.materiaNucleoCodigo})</span>
                                            </p>
                                            {practica.empresaNombre && (
                                                <p className="text-xs text-gray-500">Empresa: {practica.empresaNombre}</p>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="text-sm text-gray-400 italic py-2">Sin historial de prácticas aún.</p>
                            )}
                        </div>

                        <div className="mt-5 flex justify-end">
                            <button className="btn-secondary" onClick={() => setModalExpediente(null)}>
                                Cerrar
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ================================================================
                MODAL: Confirmar envío al proceso (GPE-147)
                Observer: al confirmar, el backend notifica al Coordinador
                de Prácticas que tiene nuevos candidatos disponibles.
            ================================================================ */}
            {confirmEnviar && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
                        <h2 className="text-lg font-bold text-gray-800 mb-2">Enviar al proceso de práctica</h2>
                        <p className="text-sm text-gray-600 mb-4">
                            Estás a punto de enviar{' '}
                            <strong>{seleccionados.length} estudiante(s) APTOS</strong> al proceso.
                            Una vez enviados quedarán visibles para el Coordinador de Prácticas
                            y no podrán volver al estado NO_APTO.
                        </p>
                        {error && (
                            <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>
                        )}
                        <div className="flex gap-3">
                            <button
                                className="btn-secondary flex-1"
                                onClick={() => { setConfirmEnviar(false); setError('') }}
                            >
                                Cancelar
                            </button>
                            <button className="btn-primary flex-1" onClick={handleEnviarAlProceso}>
                                Confirmar envío
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
