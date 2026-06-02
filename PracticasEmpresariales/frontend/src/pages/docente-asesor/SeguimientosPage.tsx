/**
 * Vista del Docente Asesor — revisión de seguimientos semanales.
 * Sprint 3: requiere el endpoint /seguimientos en el backend.
 */
export default function SeguimientosPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Seguimientos Semanales</h1>
                <p className="text-sm text-gray-500">Revisa y comenta los avances registrados por tus estudiantes cada semana.</p>
            </div>

            <div className="card border-2 border-dashed border-orange-300 bg-orange-50">
                <div className="flex items-start gap-4">
                    <span className="text-3xl">📋</span>
                    <div>
                        <p className="font-bold text-orange-800">Módulo de Seguimientos — Sprint 3</p>
                        <p className="text-sm text-orange-700 mt-1">Disponible en Sprint 3. Incluirá:</p>
                        <ul className="text-sm text-orange-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Listado de seguimientos pendientes de revisión</li>
                            <li>Visualización del informe semanal del estudiante</li>
                            <li>Registro de retroalimentación del docente</li>
                            <li>Calificación parcial por corte</li>
                            <li>Historial completo de seguimientos por estudiante</li>
                        </ul>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {['Pendientes de revisión', 'Revisados este mes', 'Estudiantes con retraso'].map((t, i) => (
                    <div key={i} className="card border border-dashed border-gray-200">
                        <p className="text-xs font-semibold text-gray-400 uppercase">{t}</p>
                        <p className="text-4xl font-bold text-gray-300 mt-2">—</p>
                        <p className="text-xs text-gray-400 mt-1">Disponible en Sprint 3</p>
                    </div>
                ))}
            </div>
        </div>
    )
}
