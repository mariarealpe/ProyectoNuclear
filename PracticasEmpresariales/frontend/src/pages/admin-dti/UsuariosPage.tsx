import { useState, useEffect } from 'react'
import { UsuarioResponse, Rol, EtiquetaCargo, FacultadResponse, ProgramaResponse } from '../../types'
import { usuarioService } from '../../services/usuarioService'
import { ROL_LABELS } from '../../constants/roles'
import api from '../../services/api'
import { ApiResponse, Pageable } from '../../types'

const ROL_OPTIONS: Rol[] = [
    'ADMIN_DTI', 'COORDINACION_ACADEMICA', 'COORDINADOR_PRACTICAS',
    'DOCENTE_ASESOR', 'TUTOR_EMPRESARIAL', 'ESTUDIANTE', 'DIRECCION',
]

const ESTADO_BADGE = (activo: boolean) =>
    activo
        ? <span className="badge-apto">Activo</span>
        : <span className="badge-no-apto">Inactivo</span>

/**
 * GPE-136 — Gestión de usuarios por rol.
 *
 * Cumple el criterio: crear, editar, activar, inactivar y restablecer contraseña.
 * OCL: puedeGestionarUsuarios, puedeRestablecerContrasena, minimoUnDTIActivo.
 * Patrón Singleton: usuarioService centraliza todas las operaciones de usuarios.
 */
export default function UsuariosPage() {
    const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([])
    const [facultades, setFacultades] = useState<FacultadResponse[]>([])
    const [programas, setProgramas] = useState<ProgramaResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [exito, setExito] = useState('')

    // ── Modales ───────────────────────────────────────────────────────────────
    const [modalCrear, setModalCrear] = useState(false)
    const [modalEditar, setModalEditar] = useState<UsuarioResponse | null>(null)
    const [confirmReset, setConfirmReset] = useState<UsuarioResponse | null>(null)

    // ── Formulario de creación ────────────────────────────────────────────────
    const formCrearInicial = {
        nombre: '', correo: '', rol: '' as Rol,
        etiquetaCargo: '' as EtiquetaCargo | '',
        telefono: '', facultadId: '', programaId: '',
    }
    const [formCrear, setFormCrear] = useState(formCrearInicial)

    // ── Formulario de edición ─────────────────────────────────────────────────
    const [formEditar, setFormEditar] = useState({
        nombre: '', telefono: '', facultadId: '', programaId: '',
    })

    const cargar = () => {
        setLoading(true)
        usuarioService.listar().then(p => setUsuarios(p.content)).finally(() => setLoading(false))
    }

    const cargarOpciones = () => {
        api.get<ApiResponse<Pageable<FacultadResponse>>>('/facultades?size=100')
            .then(r => setFacultades(r.data.datos?.content ?? []))
        api.get<ApiResponse<Pageable<ProgramaResponse>>>('/programas?size=100')
            .then(r => setProgramas(r.data.datos?.content ?? []))
    }

    useEffect(() => { cargar(); cargarOpciones() }, [])

    const handleRolChange = (rol: Rol) => {
        setFormCrear({ ...formCrear, rol, etiquetaCargo: '', facultadId: '', programaId: '' })
    }

    // ── Crear usuario ─────────────────────────────────────────────────────────
    const handleCrear = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        try {
            await usuarioService.crear({
                nombre: formCrear.nombre,
                correo: formCrear.correo,
                rol: formCrear.rol,
                etiquetaCargo: formCrear.etiquetaCargo || undefined,
                telefono: formCrear.telefono || undefined,
                facultadId: formCrear.facultadId ? Number(formCrear.facultadId) : undefined,
                programaId: formCrear.programaId ? Number(formCrear.programaId) : undefined,
            })
            setExito('Usuario creado. Se envió la contraseña temporal al correo.')
            setModalCrear(false)
            setFormCrear(formCrearInicial)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al crear el usuario.')
        }
    }

    // ── Editar usuario ────────────────────────────────────────────────────────
    const abrirEditar = (u: UsuarioResponse) => {
        setModalEditar(u)
        setFormEditar({
            nombre: u.nombre,
            telefono: u.telefono ?? '',
            facultadId: u.facultadId ? String(u.facultadId) : '',
            programaId: u.programaId ? String(u.programaId) : '',
        })
        setError('')
    }

    const handleEditar = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!modalEditar) return
        setError('')
        try {
            await usuarioService.editar(modalEditar.id, {
                nombre: formEditar.nombre,
                telefono: formEditar.telefono || undefined,
                facultadId: formEditar.facultadId ? Number(formEditar.facultadId) : undefined,
                programaId: formEditar.programaId ? Number(formEditar.programaId) : undefined,
            })
            setExito('Usuario actualizado.')
            setModalEditar(null)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'Error al editar el usuario.')
        }
    }

    // ── Activar / Desactivar ──────────────────────────────────────────────────
    const handleToggle = async (u: UsuarioResponse) => {
        try {
            if (u.activo) await usuarioService.desactivar(u.id)
            else await usuarioService.activar(u.id)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            alert(msg ?? 'Error al cambiar estado.')
        }
    }

    // ── Restablecer contraseña ────────────────────────────────────────────────
    const handleRestablecerPassword = async () => {
        if (!confirmReset) return
        try {
            await usuarioService.restablecerPassword(confirmReset.id)
            setExito(`Contraseña restablecida para ${confirmReset.nombre}. Se envió una nueva contraseña temporal al correo.`)
            setConfirmReset(null)
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            alert(msg ?? 'Error al restablecer la contraseña.')
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-gray-900">Gestión de Usuarios</h1>
                <button className="btn-primary" onClick={() => { setModalCrear(true); setError('') }}>
                    + Crear Usuario
                </button>
            </div>

            {exito && (
                <div className="bg-green-50 border border-green-200 text-green-700 rounded-lg px-4 py-3 text-sm flex items-center justify-between">
                    <span>{exito}</span>
                    <button className="text-green-500 hover:text-green-700 ml-2" onClick={() => setExito('')}>✕</button>
                </div>
            )}

            {/* Tabla de usuarios */}
            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                        <tr>
                            {['Nombre', 'Correo', 'Rol', 'Cargo', 'Scope', 'Estado', 'Acciones'].map(h => (
                                <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan={7} className="text-center py-8 text-gray-400">Cargando...</td></tr>
                        ) : usuarios.length === 0 ? (
                            <tr><td colSpan={7} className="text-center py-8 text-gray-400">No hay usuarios registrados.</td></tr>
                        ) : usuarios.map(u => (
                            <tr key={u.id} className="border-b border-gray-100 hover:bg-gray-50">
                                <td className="px-4 py-3 font-medium">{u.nombre}</td>
                                <td className="px-4 py-3 text-gray-500">{u.correo}</td>
                                <td className="px-4 py-3"><span className="badge-rol">{ROL_LABELS[u.rol]}</span></td>
                                <td className="px-4 py-3 text-gray-500 text-xs">{u.etiquetaCargo ?? '—'}</td>
                                <td className="px-4 py-3 text-xs text-gray-500">
                                    {u.facultadNombre ?? u.programaNombre ?? 'Global'}
                                </td>
                                <td className="px-4 py-3">{ESTADO_BADGE(u.activo)}</td>
                                <td className="px-4 py-3 text-right whitespace-nowrap space-x-2">
                                    <button
                                        className="text-xs text-cue-primary hover:underline"
                                        onClick={() => abrirEditar(u)}
                                    >
                                        Editar
                                    </button>
                                    <button
                                        className="text-xs text-amber-600 hover:underline"
                                        onClick={() => setConfirmReset(u)}
                                    >
                                        Resetear pwd
                                    </button>
                                    <button
                                        onClick={() => handleToggle(u)}
                                        className={`text-xs px-2 py-0.5 rounded border transition-colors ${
                                            u.activo
                                                ? 'border-red-300 text-red-600 hover:bg-red-50'
                                                : 'border-green-300 text-green-600 hover:bg-green-50'
                                        }`}
                                    >
                                        {u.activo ? 'Desactivar' : 'Activar'}
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* ── Modal: crear usuario ────────────────────────────────────────── */}
            {modalCrear && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-lg my-8">
                        <div className="p-6 border-b border-gray-200">
                            <h2 className="text-lg font-bold text-gray-800">Crear nuevo usuario</h2>
                        </div>
                        <form onSubmit={handleCrear} className="p-6 space-y-4">
                            {error && (
                                <div className="bg-red-50 border border-red-200 text-red-700 rounded-lg px-4 py-3 text-sm">{error}</div>
                            )}
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo *</label>
                                    <input className="input-field" required value={formCrear.nombre}
                                           onChange={e => setFormCrear({ ...formCrear, nombre: e.target.value })} />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Correo electrónico *</label>
                                    <input className="input-field" type="email" required value={formCrear.correo}
                                           onChange={e => setFormCrear({ ...formCrear, correo: e.target.value })} />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Rol *</label>
                                    <select className="input-field" required value={formCrear.rol}
                                            onChange={e => handleRolChange(e.target.value as Rol)}>
                                        <option value="">Seleccionar...</option>
                                        {ROL_OPTIONS.map(r => <option key={r} value={r}>{ROL_LABELS[r]}</option>)}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                                    <input className="input-field" value={formCrear.telefono}
                                           onChange={e => setFormCrear({ ...formCrear, telefono: e.target.value })} />
                                </div>

                                {/* Etiqueta de cargo — solo para Coordinación Académica */}
                                {formCrear.rol === 'COORDINACION_ACADEMICA' && (
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Etiqueta de cargo *</label>
                                        <select className="input-field" required value={formCrear.etiquetaCargo}
                                                onChange={e => setFormCrear({ ...formCrear, etiquetaCargo: e.target.value as EtiquetaCargo })}>
                                            <option value="">Seleccionar...</option>
                                            <option value="COORDINACION_ACADEMICA">Coordinación Académica</option>
                                            <option value="SECRETARIA">Secretaría</option>
                                        </select>
                                    </div>
                                )}

                                {/* Selector de facultad — CoordAcad */}
                                {formCrear.rol === 'COORDINACION_ACADEMICA' && (
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Facultad (scope)</label>
                                        <select className="input-field" value={formCrear.facultadId}
                                                onChange={e => setFormCrear({ ...formCrear, facultadId: e.target.value })}>
                                            <option value="">— Sin asignar —</option>
                                            {facultades.map(f => (
                                                <option key={f.id} value={f.id}>{f.nombre}</option>
                                            ))}
                                        </select>
                                    </div>
                                )}

                                {/* Selector de programa — CoordPracticas y Estudiante */}
                                {['COORDINADOR_PRACTICAS', 'ESTUDIANTE'].includes(formCrear.rol) && (
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-1">Programa (scope) *</label>
                                        <select className="input-field" required value={formCrear.programaId}
                                                onChange={e => setFormCrear({ ...formCrear, programaId: e.target.value })}>
                                            <option value="">— Selecciona un programa —</option>
                                            {programas.map(p => (
                                                <option key={p.id} value={p.id}>
                                                    {p.nombre} ({p.facultadNombre})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                )}
                            </div>

                            <div className="flex gap-3 pt-2">
                                <button type="button" className="btn-secondary flex-1"
                                        onClick={() => { setModalCrear(false); setError('') }}>
                                    Cancelar
                                </button>
                                <button type="submit" className="btn-primary flex-1">
                                    Crear y enviar contraseña
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* ── Modal: editar usuario ───────────────────────────────────────── */}
            {modalEditar && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6">
                        <h2 className="text-lg font-bold text-gray-800 mb-1">Editar Usuario</h2>
                        <p className="text-xs text-gray-500 mb-4">
                            {modalEditar.correo} · <span className="badge-rol">{ROL_LABELS[modalEditar.rol]}</span>
                        </p>
                        {error && (
                            <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>
                        )}
                        <form onSubmit={handleEditar} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo *</label>
                                <input className="input-field" required value={formEditar.nombre}
                                       onChange={e => setFormEditar({ ...formEditar, nombre: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                                <input className="input-field" value={formEditar.telefono}
                                       onChange={e => setFormEditar({ ...formEditar, telefono: e.target.value })} />
                            </div>

                            {modalEditar.rol === 'COORDINACION_ACADEMICA' && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Facultad (scope)</label>
                                    <select className="input-field" value={formEditar.facultadId}
                                            onChange={e => setFormEditar({ ...formEditar, facultadId: e.target.value })}>
                                        <option value="">— Sin asignar —</option>
                                        {facultades.map(f => (
                                            <option key={f.id} value={f.id}>{f.nombre}</option>
                                        ))}
                                    </select>
                                </div>
                            )}

                            {['COORDINADOR_PRACTICAS', 'ESTUDIANTE'].includes(modalEditar.rol) && (
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Programa (scope)</label>
                                    <select className="input-field" value={formEditar.programaId}
                                            onChange={e => setFormEditar({ ...formEditar, programaId: e.target.value })}>
                                        <option value="">— Sin asignar —</option>
                                        {programas.map(p => (
                                            <option key={p.id} value={p.id}>
                                                {p.nombre} ({p.facultadNombre})
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            )}

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

            {/* ── Modal: confirmar restablecer contraseña ─────────────────────── */}
            {confirmReset && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6">
                        <h2 className="text-lg font-bold text-gray-800 mb-2">Restablecer contraseña</h2>
                        <p className="text-sm text-gray-600 mb-4">
                            Se generará una nueva contraseña temporal para{' '}
                            <strong>{confirmReset.nombre}</strong> y se enviará a{' '}
                            <strong>{confirmReset.correo}</strong>.
                        </p>
                        <div className="flex gap-3">
                            <button className="btn-secondary flex-1" onClick={() => setConfirmReset(null)}>
                                Cancelar
                            </button>
                            <button className="btn-primary flex-1" onClick={handleRestablecerPassword}>
                                Confirmar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
