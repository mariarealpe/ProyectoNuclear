/**
 * Vista del Tutor Empresarial — encuestas de satisfacción.
 * Sprint 3/4: requiere el endpoint /encuestas en el backend.
 */
export default function EncuestasPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-900">Encuestas de Satisfacción</h1>
                <p className="text-sm text-gray-500">Evalúa el desempeño de tus practicantes y el proceso de práctica institucional.</p>
            </div>

            <div className="card border-2 border-dashed border-purple-300 bg-purple-50">
                <div className="flex items-start gap-4">
                    <span className="text-3xl">📊</span>
                    <div>
                        <p className="font-bold text-purple-800">Encuestas de Satisfacción — Sprint 4</p>
                        <p className="text-sm text-purple-700 mt-1">Disponible en Sprint 4. Incluirá:</p>
                        <ul className="text-sm text-purple-700 mt-1 list-disc list-inside space-y-0.5">
                            <li>Encuesta de evaluación del desempeño del practicante</li>
                            <li>Encuesta de satisfacción con el proceso institucional</li>
                            <li>Historial de encuestas respondidas</li>
                            <li>Visualización de resultados consolidados</li>
                        </ul>
                    </div>
                </div>
            </div>

            <div className="card text-center py-10 text-gray-400">
                <span className="text-4xl block mb-3">📝</span>
                No hay encuestas pendientes de respuesta.
            </div>
        </div>
    )
}
