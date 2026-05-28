import { useEffect, useState } from 'react'
import { EmpresaResponse, TutorEmpresarialResponse } from '../../types'
import { empresaService } from '../../services/empresaService'
import { tutorService, CrearTutorRequest } from '../../services/tutorService'

/**
 * GPE-151 — Gestión de tutores empresariales (Persona 2).
 *
 * Crea cuentas Usuario (rol TUTOR_EMPRESARIAL) vinculadas a una empresa.
 * El tutor recibe por correo su contraseña temporal (PATRON OBSERVER del backend).
 */
export default function TutoresPage() {
    const [tutores, setTutores] = useState<TutorEmpresarialResponse[]>([])
    const [empresas, setEmpresas] = useState<EmpresaResponse[]>([])
    const [empresaFiltro, setEmpresaFiltro] = useState<string>('')
    const [loading, setLoading] = useState(true)
    const [modal, setModal] = useState(false)
    const [error, setError] = useState('')

    const formInicial: CrearTutorRequest = {
        nombre: '', correo: '', telefono: '',
        empresaId: 0, cargo: '', area: '',
        telefonoCorporativo: '', esResponsablePrincipal: false,
    }
    const [form, setForm] = useState<CrearTutorRequest>(formInicial)

    const cargar = () => {
        setLoading(true)
        const empresaId = empresaFiltro ? Number(empresaFiltro) : undefined
        tutorService.listar(0, 50, empresaId)
            .then(pg => setTutores(pg.content ?? []))
            .finally(() => setLoading(false))
    }

    const cargarEmpresas = () => {
        empresaService.listar(0, 100)
            .then(pg => setEmpresas(pg.content ?? []))
    }

    useEffect(() => { cargar() /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, [empresaFiltro])
    useEffect(() => { cargarEmpresas() }, [])

    const abrirNuevo = () => {
        setForm(formInicial)
        setError('')
        setModal(true)
    }

    const crear = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')
        try {
            await tutorService.crear({
                ...form,
                empresaId: Number(form.empresaId),
            })
            setModal(false)
            cargar()
        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { mensaje?: string } } })?.response?.data?.mensaje
            setError(msg ?? 'No se pudo crear el tutor.')
        }
    }

    const toggleActivo = async (t: TutorEmpresarialResponse) => {
        if (t.activo) await tutorService.desactivar(t.id)
        else await tutorService.activar(t.id)
        cargar()
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between gap-4 flex-wrap">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Tutores Empresariales</h1>
                    <p className="text-sm text-gray-500">Asigna y gestiona los tutores que supervisarán a los practicantes en cada empresa.</p>
                </div>
                <div className="flex gap-2">
                    <select
                        className="input-field max-w-xs"
                        value={empresaFiltro}
                        onChange={e => setEmpresaFiltro(e.target.value)}
                    >
                        <option value="">— Todas las empresas —</option>
                        {empresas.map(e => <option key={e.id} value={e.id}>{e.razonSocial}</option>)}
                    </select>
                    <button className="btn-primary" onClick={abrirNuevo}>+ Nuevo Tutor</button>
                </div>
            </div>

            <div className="card overflow-x-auto p-0">
                <table className="w-full text-sm">
                    <thead className="bg-gray-50 border-b border-gray-200">
                    <tr>
                        {['Nombre', 'Correo', 'Empresa', 'Cargo', 'Área', 'Principal', 'Estado', ''].map(h => (
                            <th key={h} className="text-left px-4 py-3 text-gray-600 font-semibold">{h}</th>
                        ))}
                    </tr>
                    </thead>
                    <tbody>
                    {loading ? (
                        <tr><td colSpan={8} className="text-center py-8 text-gray-400">Cargando...</td></tr>
                    ) : tutores.length === 0 ? (
                        <tr><td colSpan={8} className="text-center py-8 text-gray-400">No hay tutores registrados.</td></tr>
                    ) : tutores.map(t => (
                        <tr key={t.id} className="border-b border-gray-100 hover:bg-gray-50">
                            <td className="px-4 py-3 font-medium">{t.nombre}</td>
                            <td className="px-4 py-3 text-gray-500 text-xs">{t.correo}</td>
                            <td className="px-4 py-3 text-gray-500">{t.empresaRazonSocial}</td>
                            <td className="px-4 py-3 text-gray-700">{t.cargo}</td>
                            <td className="px-4 py-3 text-gray-500">{t.area ?? '—'}</td>
                            <td className="px-4 py-3 text-center">
                                {t.esResponsablePrincipal ? <span title="Responsable principal">⭐</span> : '—'}
                            </td>
                            <td className="px-4 py-3">
                                <span className={t.activo ? 'badge-apto' : 'badge-no-apto'}>
                                    {t.activo ? 'Activo' : 'Inactivo'}
                                </span>
                            </td>
                            <td className="px-4 py-3 text-right whitespace-nowrap">
                                <button
                                    className={`text-xs hover:underline ${t.activo ? 'text-red-600' : 'text-green-700'}`}
                                    onClick={() => toggleActivo(t)}
                                >{t.activo ? 'Desactivar' : 'Activar'}</button>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>

            {modal && (
                <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
                    <div className="bg-white rounded-xl shadow-2xl w-full max-w-xl p-6 my-8">
                        <h2 className="text-lg font-bold text-gray-800 mb-2">Nuevo Tutor Empresarial</h2>
                        <p className="text-xs text-gray-500 mb-4">
                            Se creará la cuenta de usuario y recibirá por correo una contraseña temporal.
                        </p>
                        {error && <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 text-sm mb-4">{error}</div>}
                        <form onSubmit={crear} className="grid grid-cols-2 gap-4">
                            <div className="col-span-2">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Empresa *</label>
                                <select
                                    className="input-field"
                                    required
                                    value={form.empresaId || ''}
                                    onChange={e => setForm({ ...form, empresaId: Number(e.target.value) })}
                                >
                                    <option value="">— Selecciona empresa —</option>
                                    {empresas.filter(e => e.activo).map(e => (
                                        <option key={e.id} value={e.id}>{e.razonSocial}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo *</label>
                                <input className="input-field" required value={form.nombre}
                                       onChange={e => setForm({ ...form, nombre: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Correo *</label>
                                <input className="input-field" type="email" required value={form.correo}
                                       onChange={e => setForm({ ...form, correo: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                                <input className="input-field" value={form.telefono ?? ''}
                                       onChange={e => setForm({ ...form, telefono: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Tel. Corporativo</label>
                                <input className="input-field" value={form.telefonoCorporativo ?? ''}
                                       onChange={e => setForm({ ...form, telefonoCorporativo: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Cargo *</label>
                                <input className="input-field" required value={form.cargo}
                                       onChange={e => setForm({ ...form, cargo: e.target.value })} />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Área</label>
                                <input className="input-field" value={form.area ?? ''}
                                       onChange={e => setForm({ ...form, area: e.target.value })} />
                            </div>
                            <div className="col-span-2 flex items-center gap-2">
                                <input
                                    id="responsable"
                                    type="checkbox"
                                    checked={form.esResponsablePrincipal ?? false}
                                    onChange={e => setForm({ ...form, esResponsablePrincipal: e.target.checked })}
                                />
                                <label htmlFor="responsable" className="text-sm text-gray-700">
                                    Es el contacto principal de la empresa
                                </label>
                            </div>

                            <div className="col-span-2 flex gap-3 pt-2">
                                <button type="button" className="btn-secondary flex-1" onClick={() => setModal(false)}>Cancelar</button>
                                <button type="submit" className="btn-primary flex-1">Crear Tutor</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
}
