import { useEffect, useState } from 'react'
import { EmpresaResponse } from '../../types'
import { empresaService, CrearEmpresaRequest } from '../../services/empresaService'

/**
 * GPE-150 — Gestión de empresas (Persona 2).
 *
 * Permite al Admin DTI y al Coordinador de Prácticas registrar y administrar
 * el directorio de empresas que ofrecen vacantes de práctica empresarial.
 */
export default function EmpresasPage() {
    const [empresas, setEmpresas] = useState<EmpresaResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [filtro, setFiltro] = useState('')
    const [modal, setModal] = useState(false)
    const [editandoId, setEditandoId] = useState<number | null>(null)
    const [error, setError] = useState('')

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

    useEffect(() => { cargar() /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, [])

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

    const toggleActivo = async (e: EmpresaResponse) => {
        if (e.activo) await empresaService.desactivar(e.id)
        else await empresaService.activar(e.id)
        cargar()
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Empresas</h1>
                    <p className="text-sm text-gray-500">Directorio de empresas vinculadas al programa de prácticas.</p>
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

            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                        {['NIT', 'Razón Social', 'Sector', 'Ciudad', 'Contacto', 'Estado', ''].map(h => (
                            <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {loading ? (
                        <tr><td colSpan={7} className="text-center py-8 text-gray-400">Cargando...</td></tr>
                    ) : empresas.length === 0 ? (
                        <tr><td colSpan={7} className="text-center py-8 text-gray-400">No hay empresas registradas.</td></tr>
                    ) : empresas.map(e => (
                        <tr key={e.id} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="px-4 py-3 font-mono text-xs">{e.nit}</td>
                            <td className="px-4 py-3">
                                <div className="font-medium text-gray-900">{e.razonSocial}</div>
                                {e.nombreComercial && <div className="text-xs text-gray-500">{e.nombreComercial}</div>}
                            </td>
                            <td className="px-4 py-3 text-gray-500">{e.sector ?? '—'}</td>
                            <td className="px-4 py-3 text-gray-500">{e.ciudad ?? '—'}</td>
                            <td className="px-4 py-3 text-gray-500 text-xs">
                                <div>{e.correoContacto}</div>
                                {e.telefono && <div>{e.telefono}</div>}
                            </td>
                            <td className="px-4 py-3">
                                <span className={e.activo ? 'badge-apto' : 'badge-no-apto'}>
                                    {e.activo ? 'Activa' : 'Inactiva'}
                                </span>
                            </td>
                            <td className="px-4 py-3 text-right whitespace-nowrap">
                                <button
                                    className="text-cue-accent hover:underline text-xs mr-3"
                                    onClick={() => abrirEditar(e)}
                                >Editar</button>
                                <button
                                    className={`text-xs hover:underline ${e.activo ? 'text-red-600' : 'text-green-700'}`}
                                    onClick={() => toggleActivo(e)}
                                >{e.activo ? 'Desactivar' : 'Activar'}</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

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

                            <div className="col-span-2 flex gap-3 pt-2">
                                <button type="button" className="btn-secondary flex-1" onClick={() => setModal(false)}>Cancelar</button>
                                <button type="submit" className="btn-primary flex-1">
                                    {editandoId ? 'Guardar cambios' : 'Crear empresa'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
