import { useAuth } from '../../context/AuthContext'

export default function MiSeguimientoPage() {
    const { user } = useAuth()

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Seguimiento Semanal</h1>
                <p className="text-sm text-gray-500">
                    Hola, <span className="font-medium">{user?.nombre}</span>. Registra y consulta tus avances semanales de práctica.
                </p>
            </div>

            <div className="card border-2 border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-4">
                    <span className="text-3xl">📝</span>
                    <div>
                        <p className="font-bold text-orange-800">Módulo de Seguimiento Semanal — Sprint 3</p>
                        <p className="text-sm text-orange-700 mt-1">
                            El registro de seguimientos estará disponible en Sprint 3. Incluirá:
                        </p>
                        <ul className="text-sm text-orange-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Formulario de reporte semanal de avances</li>
                            <li>Historial completo de seguimientos enviados</li>
                            <li>Retroalimentación de tu docente asesor</li>
                            <li>Calificaciones parciales por corte de evaluación</li>
                        </ul>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {[
                    { label: 'Seguimientos enviados', valor: '—', icono: '✅' },
                    { label: 'Pendiente de enviar', valor: '—', icono: '⏳' },
                    { label: 'Calificación actual', valor: '—', icono: '🎯' },
                ].map(k => (
                    <div key={k.label} className="card text-center">
                        <span className="text-2xl">{k.icono}</span>
                        <p className="text-xs font-bold text-gray-500 uppercase mt-2">{k.label}</p>
                        <p className="text-4xl font-bold text-gray-300 mt-2">{k.valor}</p>
                        <p className="text-xs text-gray-400 mt-1">Disponible en Sprint 3</p>
                    </div>
                ))}
            </div>

            <div className="card">
                <p className="text-xs font-bold text-gray-500 uppercase mb-4">Historial de seguimientos</p>
                <div className="flex flex-col items-center justify-center py-10 text-center text-gray-400">
                    <span className="text-4xl mb-3">📋</span>
                    <p className="font-medium text-gray-500">Aún no tienes seguimientos registrados</p>
                    <p className="text-xs mt-1">El módulo de seguimientos se habilitará en Sprint 3.</p>
                </div>
            </div>
        </div>
    )
}
