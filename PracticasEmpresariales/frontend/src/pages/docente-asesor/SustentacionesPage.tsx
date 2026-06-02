/**
 * Vista del Docente Asesor — sustentaciones como jurado.
 * Sprint 4: requiere el endpoint /sustentaciones en el backend.
 */
export default function SustentacionesPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Sustentaciones</h1>
                <p className="text-sm text-gray-500">Calendario y actas de sustentaciones finales como jurado.</p>
            </div>

            <div className="card border-2 border-dashed border-purple-300 bg-purple-50">
                <div className="flex items-start gap-4">
                    <span className="text-3xl">🎓</span>
                    <div>
                        <p className="font-bold text-purple-800">Módulo de Sustentaciones — Sprint 4</p>
                        <p className="text-sm text-purple-700 mt-1">Disponible en Sprint 4. Incluirá:</p>
                        <ul className="text-sm text-purple-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Calendario de sustentaciones programadas</li>
                            <li>Acceso al informe final del estudiante</li>
                            <li>Registro de calificación final como jurado</li>
                            <li>Generación del acta de sustentación</li>
                            <li>Cierre formal de la práctica</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    )
}
