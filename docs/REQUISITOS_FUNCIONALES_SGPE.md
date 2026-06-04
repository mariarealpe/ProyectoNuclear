Análisis de requisitos para diagrama E-R

María José Realpe Vallejo
Santiago Acosta Calvo
Jeshua Gomez Cortés
Valeria Tobar Garzón
Sara Camacho Rodriguez

Mg. David Cano Baquero

Corporación Universitaria Empresarial Alexander von Humboldt
Facultad de Ingenierías y Ciencias Básicas
Análisis y diseño de sistemas de información
Armenia - Quindío
2026

REQUISITOS FUNCIONALES

Sistema de Gestión de Prácticas Empresariales – SGPE
Mayo 2026

Convenciones:   Entidad del modelo E-R         Atributo de entidad

ÉPICA 1 – Dashboard y Panel de Inicio

RF-01-01 – Panel de inicio personalizado por rol

Cada usuario visualiza al iniciar sesión un panel adaptado a su rol. El Administrador ve
resumen global del sistema; la Dirección ve indicadores gerenciales por facultad; la
Coordinación Académica ve estudiantes cargados, aptos vs no aptos y prácticas activas; el
Coordinador de Práctica y la Secretaria ven tareas pendientes; el Docente Asesor ve sus
estudiantes asignados y cortes pendientes; la Empresa ve sus vacantes y estudiantes
vinculados; el Tutor Empresarial ve sus estudiantes a cargo y evaluaciones pendientes; el
Estudiante ve el estado actual de su práctica, documentos pendientes y calificaciones.

RF-01-02 – Tarjetas de resumen con indicadores clave

El panel de inicio muestra tarjetas con contadores dinámicos según el rol. Para el
Coordinador: vacantes pendientes, asignaciones pendientes, estudiantes en práctica activa,
cierres pendientes. Para el Docente Asesor: estudiantes asignados, calificaciones pendientes.
Para el Tutor Empresarial: practicantes a cargo, evaluaciones por diligenciar. Para el
Estudiante: estado de su práctica y documentos pendientes.

RF-01-03 – Centro de notificaciones y alertas

Ícono de campana en la barra superior con contador de notificaciones no leídas. Panel lateral
cronológico con notificacioness agrupadas por tipo: asignaciones nuevas, vacantes por
aprobar, calificaciones pendientes, documentos por diligenciar, encuestas de satisfacción
pendientes, prácticas próximas a cerrar. Cada notificación incluye descripción, fecha, un tipo
de esta y botón de acción directa al módulo origen.

RF-01-04 – Filtros globales persistentes por facultad y programa

Para los roles con scope múltiple (Admin, Dirección, Coordinación Académica), la barra
superior incluye selectores de Facultad y Programa que aplican simultáneamente a todos los
indicadores, gráficos y tablas del panel. La selección se conserva durante la sesión al navegar
entre módulos.

ÉPICA 2 – Gestión de Usuarios y Acceso

RF-02-01 – Registro y gestión de usuarios por rol

El Administrador puede crear, editar, activar e inactivar usuarios. Al crear un usuario se
solicita: nombre completo, correo electrónico (credencial única), rol asignado y scope
(facultad o programa según el rol). El sistema genera contraseña temporal y la envía al correo
del nuevo usuario.

RF-02-02 – Autenticación y control de acceso por rol y scope

El sistema valida credenciales al iniciar sesión y redirige al panel personalizado según el rol.
El acceso a módulos, vistas y acciones está restringido según el rol y el scope del usuario.
Las rutas no autorizadas retornan error 403 y no se muestran en el menú.

RF-02-03 – Recuperación de contraseña

El usuario puede solicitar restablecimiento de contraseña desde la pantalla de login
ingresando su correo registrado. El sistema envía un enlace de restablecimiento válido por 24
horas al correo institucional.

RF-02-04 – Bitácora de auditoría de accesos y acciones

El sistema registra automáticamente todas las acciones críticas: inicio/cierre de sesión,
intentos fallidos, creación y modificación de registros, aprobaciones, rechazos, calificaciones,
cierres y envíos de correo. Cada entrada incluye: usuario, fecha/hora, módulo, tipo de acción,
registro afectado, valores anteriores y nuevos.

RF-02-05 – Gestión de facultades y programas (configuración estructural)

El Administrador puede crear, editar y desactivar facultades y programas académicos. Por
cada programa se configura: nombre, facultad, número total de prácticas, número de cortes
por práctica y requisitos por número de práctica (promedio mínimo, práctica anterior
aprobada, documentos requeridos).

RF-02-06 – Gestión del catálogo de prácticas por la Coordinación Empresarial

La Coordinación Académica administra el catálogo institucional de prácticas de forma
independiente. Por cada práctica se registra: número, nombre, materia núcleo obligatoria
(nombre y código), programa académico, número de cortes, duración en semanas y
documentos requeridos. El catálogo es la plantilla base para crear instancias individuales al
marcar aptitud.

ÉPICA 3 – Gestión de Estudiantes

RF-03-01 – Registro individual de estudiantes por DTI

El DTI registra estudiantes con datos personales (nombre, identificación, correo, teléfono,
contacto de emergencia), datos académicos (programa, facultad, semestre, créditos
aprobados, promedio acumulado) y documentos base. Al crear el estudiante se genera
automáticamente su expediente de prácticas vacío.

RF-03-02 – Carga masiva de estudiantes desde Excel

La Coordinación Académica puede importar estudiantes desde un archivo .xlsx con plantilla
descargable.

RF-03-03 – Validación de requisitos, marcación de aptitud y asignación automática de
práctica

La Coordinación Académica valida si un estudiante cumple los requisitos para el número de
práctica al que aplica (créditos mínimos, promedio mínimo, práctica anterior aprobada,
documentos completos, paz y salvo) y le asigna estado "Apto" o "No apto". Al confirmar
aptitud, el sistema crea automáticamente la instancia de práctica desde el catálogo,
precargando nombre, materia núcleo, cortes, duración y documentos requeridos. La práctica
se crea en estado "Asignada – Pendiente de inicio".

RF-03-04 – Expediente histórico del estudiante

Cada estudiante tiene un expediente que agrupa: datos personales y académicos, documentos
base, y el historial completo de todas sus prácticas. Cada práctica muestra nombre, materia
núcleo, estado, empresa, docente asesor, tutor empresarial, documentos, calificaciones y línea
de tiempo de hitos.

RF-03-05 – Listado de estudiantes con filtros avanzados

Vista tabular con todos los estudiantes según el scope del usuario. Columnas: nombre,
identificación, programa, semestre, número y nombre de práctica actual, estado de aptitud,
estado de práctica activa. Filtros disponibles: programa, facultad, semestre, estado de aptitud,
número de práctica, estado de práctica, empresa asignada, docente asesor. Búsqueda por
texto libre.

RF-03-06 – Habilitación y asignación automática de la práctica siguiente

Al ejecutarse el cierre exitoso de la Práctica N, el sistema actualiza el expediente a "Práctica
N Completada" y notifica a la Coordinación Académica para que evalúe la Práctica N+1. Al
marcar Apto para N+1, el sistema crea automáticamente la instancia desde el catálogo en
estado "Asignada – Pendiente de inicio".

ÉPICA 4 – Gestión de Empresas y Vacantes

RF-04-01 – Registro de empresas vinculadas

El Administrador, el Coordinador de Práctica o la Secretaria pueden registrar empresas con:
NIT, razón social, sector económico, dirección, municipio, teléfono y nombre del contacto
principal. Una empresa puede estar vinculada a múltiples programas académicos.

RF-04-02 – Registro y gestión de Tutores Empresariales

Por cada empresa se pueden registrar uno o varios Tutores Empresariales con: nombre
completo, cargo, correo electrónico y teléfono. El Tutor Empresarial recibe acceso al sistema
con su correo como credencial, con scope limitado a los estudiantes que le sean asignados.

RF-04-03 – Creación de vacantes por la empresa o coordinación empresarial

La Empresa Vinculada o coordinación empresarial crea vacantes con: cargo, descripción del
perfil, requisitos del estudiante, número de cupos, área, modalidad
(presencial/remoto/híbrido), programa académico al que aplica y fechas de disponibilidad. Al
guardar, la vacante queda en estado "Pendiente de aprobación".

RF-04-04 – Aprobación o rechazo de vacantes por el Coordinador

El Coordinador de Práctica revisa las vacantes en estado "Pendiente de aprobación". Puede
aprobarlas, rechazarlas con motivo obligatorio o solicitar ajustes a la empresa. Al aprobar, la
vacante pasa a estado "Activa" y queda disponible para asignación de estudiantes aptos.

RF-04-05 – Gestión del ciclo de vida de la vacante

Las vacantes tienen estados: Borrador → Pendiente de aprobación → Activa → Pausada →
Cerrada. El Coordinador puede pausar una vacante activa o cerrarla. La empresa puede editar
una vacante activa, pero los cambios vuelven a estado "Pendiente de aprobación".

RF-04-06 – Listado de vacantes con filtros

Vista tabular con todas las vacantes según el scope del usuario. Columnas: cargo, empresa,
área, modalidad, programa, cupos disponibles/totales, número de asignaciones, estado con
indicador visual de color. Filtros: empresa, programa, área, modalidad, estado. Búsqueda por
texto libre sobre cargo y empresa.

ÉPICA 5 – Postulación de Estudiantes a Vacantes

RF-05-01 – Asignación de estudiantes a vacantes activas por el Coordinador

El Coordinador de Práctica es el único responsable de postular estudiantes a las vacantes
activas. Proceso: (1) selecciona una vacante activa con cupos disponibles; (2) consulta el
listado de estudiantes Aptos sin práctica activa; (3) revisa el perfil y hoja de vida; (4)
confirma la asignación con nota opcional. El sistema notifica por correo y en el panel al
estudiante, empresa y tutor empresarial. Los estudiantes NO realizan postulaciones
autónomas.

RF-05-02 – Gestión y seguimiento de postulaciones activas

El Coordinador visualiza el listado completo de postulaciones activas con columnas:
estudiante, número y nombre de práctica, vacante, empresa, fecha de asignación, estado y
último cambio. Puede cancelar una postulación antes del inicio de vinculación registrando el
motivo obligatoriamente. Al cancelar, el cupo se libera y se notifica al estudiante y empresa
con el motivo.

RF-05-03 – Estados y trazabilidad completa de postulaciones

Cada postulación tiene un flujo de estados con trazabilidad completa: Asignada → En
proceso de vinculación → Vinculada (convenio firmado) / Cancelada. El sistema registra la
fecha, el usuario responsable y el motivo de cada cambio. Cada transición genera
notificación automática por correo a todos los actores involucrados.

RF-05-04 – Notificaciones automáticas de asignación por correo electrónico

El sistema envía notificaciones por correo ante eventos: (1) nueva asignación confirmada:
notifica al estudiante, empresa y tutor; (2) cancelación de asignación: notifica al estudiante y
empresa con el motivo; (3) cambio de estado: notifica a todos los actores. El correo incluye:
nombre del estudiante, número y nombre de práctica, cargo y empresa, estado actual y enlace
directo al expediente.

ÉPICA 6 – Vinculación y Documentos

RF-06-01 – Carga de carta de presentación

Al confirmarse la postulación, el Coordinador o la Secretaria cargan la carta de presentación
del estudiante dirigida a la empresa. El documento se adjunta al expediente en el repositorio
de documentos de vinculación y queda disponible para visualización y descarga por los roles
autorizados.

RF-06-02 – Carga y registro del convenio de práctica

El Coordinador o la Secretaria cargan el convenio o contrato de práctica firmado en PDF. El
sistema registra las firmas requeridas: Coordinador de Práctica, Tutor Empresarial y
Estudiante, con indicación de quién confirmó la firma y cuándo. El documento físico firmado
se adjunta escaneado al expediente.

RF-06-03 – Confirmación de vinculación y activación de práctica

Al completarse el convenio firmado, el Coordinador confirma la vinculación ingresando las
fechas oficiales de inicio y fin de la práctica. El sistema activa el estado "En práctica" para el
estudiante, asigna el Docente Asesor correspondiente y notifica a todos los actores
involucrados por correo y en el panel.

RF-06-04 – Repositorio de documentos por práctica

Cada práctica tiene un repositorio de documentos organizado por categorías: Documentos de
vinculación (cartas, convenios), Documentos de seguimiento (informes por corte),
Evaluaciones y encuestas, Acta de cierre. Por cada documento se registra: nombre, fecha de
carga, quién lo cargó y estado.

ÉPICA 7 – Seguimiento a la Práctica

RF-07-01 – Tablero de seguimiento general (Coordinador y Secretaria)

Vista tabular con todos los estudiantes en práctica activa del programa. Columnas: nombre
del estudiante, empresa, docente asesor asignado, corte actual, estado del seguimiento (Al día
/ Pendiente / En alerta), último registro de actividad. Filtros: empresa, docente asesor, corte
actual, estado de seguimiento.

RF-07-02 – Registro de observaciones y seguimiento por el Docente Asesor

El Docente Asesor puede registrar observaciones, comentarios de seguimiento y novedades
sobre cada estudiante asignado. Puede también cargar documentos de seguimiento (informes
de visita, actas) al expediente del estudiante.

RF-07-03 – Registro de avances y desempeño por el Tutor Empresarial

El Tutor Empresarial registra el avance y desempeño del practicante desde su portal, por
corte de evaluación. Puede indicar logros, dificultades y observaciones generales.

RF-07-04 – Bitácora de avances del estudiante

El Estudiante puede registrar sus propios avances, actividades realizadas y reflexiones sobre
su práctica en un espacio de bitácora personal. Cada entrada tiene fecha y es visible para el
Docente Asesor y el Coordinador.

RF-07-05 – Alertas automáticas de inactividad

El sistema evalúa diariamente si cada estudiante en práctica activa tiene actividad registrada
(seguimiento del docente, avances del tutor o bitácora del estudiante) en los últimos N días
hábiles. Al superar el umbral, genera una alerta en el panel y envía correo al Coordinador y al
Docente Asesor con el nombre del estudiante, empresa y días sin actividad.

ÉPICA 8 – Calificaciones y Evaluaciones

RF-08-01 – Registro de nota del Docente Asesor

El Docente Asesor registra su nota de evaluación al estudiante al concluir la práctica.
Ingresa: nota numérica (dentro del rango configurado) y observaciones sobre el desempeño
general del practicante.

RF-08-02 – Registro de nota del Tutor Empresarial

El Tutor Empresarial registra su nota de evaluación del practicante desde su portal, valorando
el desempeño en la empresa. Ingresa: nota numérica (dentro del rango configurado) y
observaciones generales sobre el desempeño.

RF-08-04 – Registro de nota final por el Coordinador

El Coordinador de Práctica registra la nota final del estudiante tomando como referencia las
notas del Docente Asesor y el Tutor Empresarial. La nota final determina el resultado de la
práctica (Aprobado / Reprobado) según la nota mínima configurada por programa.

RF-08-05 – Encuesta de satisfacción del Tutor Empresarial — notificaciones y
recordatorios

Al finalizar la práctica y entrar en fase de cierre, el sistema envía automáticamente un correo
de invitación al Tutor Empresarial con enlace directo a la encuesta de satisfacción. Si
permanece pendiente, el sistema envía recordatorios automáticos con la frecuencia
configurada en RF-11-05 (por defecto cada 3 días hábiles).

RF-08-06 – Autoevaluación y encuesta del Estudiante — notificaciones y recordatorios

Al finalizar la práctica, el sistema envía automáticamente un correo al Estudiante con enlace
a su autoevaluación y a la encuesta de satisfacción sobre la empresa, el Tutor Empresarial y
el proceso. Si permanece pendiente, envía recordatorios automáticos con la frecuencia
configurada.

ÉPICA 9 – Cierre de Práctica

RF-09-01 – Checklist de requisitos de cierre con seguimiento de encuestas y recordatorios
Antes del cierre formal, el sistema presenta un checklist automático verificando: (1) nota del
Docente Asesor; (2) nota del Tutor Empresarial; (3) nota final del Coordinador; (4) Encuesta
del Tutor (estado visual + botón recordatorio); (5) Encuesta del Estudiante (estado visual +
botón recordatorio); (6) documentos requeridos cargados; (7) informe final del estudiante
cargado. Al hacer clic en "Enviar recordatorio", el sistema envía inmediatamente el correo y
registra fecha/hora.

RF-09-02 – Ejecución del cierre formal de la práctica

Al completarse el checklist, el Coordinador ejecuta el cierre formal. El sistema lee la nota
final registrada, determina el resultado (Aprobado / Reprobado) y actualiza el estado del
estudiante en el expediente. Al ejecutar el cierre, el expediente pasa a estado inmutable y el
sistema notifica por correo y en el panel al Estudiante, Docente Asesor, Tutor Empresarial y
Empresa.

RF-09-03 – Actualización automática de estado del estudiante

Al cerrarse la práctica, el sistema actualiza el estado del estudiante: "Práctica N Completada"
si aprobó, o "Práctica N Reprobada" si no alcanzó la nota mínima. Notifica automáticamente
a la Coordinación Académica para que evalúe la habilitación de la siguiente práctica.

RF-09-04 – Archivado inmutable del expediente de la práctica cerrada

Al ejecutarse el cierre formal, toda la documentación de la práctica queda archivada en el
expediente del estudiante en estado de solo lectura. Ningún actor puede modificar,
reemplazar ni eliminar documentos de una práctica cerrada.

ÉPICA 10 – Reportes e Indicadores

RF-10-01 – Reporte de estado del proceso por programa y periodo

Reporte tabular que muestra el número de estudiantes en cada estado del proceso (aptos sin
iniciar, en asignación, en práctica, completados, reprobados) filtrado por facultad, programa,
periodo académico y número de práctica.

RF-10-02 – Reporte de notas registradas

Reporte con las notas registradas de todos los estudiantes por programa y periodo. Columnas:
estudiante, número de práctica, empresa, nota del Docente Asesor, nota del Tutor

Empresarial, nota final registrada por el Coordinador, resultado. Filtros: programa, periodo,
docente asesor, empresa, resultado.

RF-10-03 – Reporte de empresas y vacantes

Reporte con todas las empresas vinculadas, número de vacantes por estado (pendiente,
activa, cerrada), número de estudiantes asignados histórico y activo, y tasa de finalización
exitosa (prácticas completadas / total iniciadas en la empresa). Filtros: sector, programa,
periodo.

RF-10-04 – Tablero gerencial de indicadores (Dirección)

Panel visual exclusivo para la Dirección con indicadores agregados: total de practicantes
activos por facultad, tasa de aprobación global y por programa, número de empresas activas,
tiempo promedio de gestión (días desde Apto hasta inicio de práctica), prácticas cerradas en
el periodo. Los indicadores responden a filtros de periodo.

RF-10-05 – Reporte consolidado de encuestas de satisfacción

Reporte con resultados agregados de las encuestas de satisfacción de Tutores Empresariales y
Estudiantes. Muestra promedios por pregunta, por empresa y por programa. Identifica las
empresas y procesos mejor y peor evaluados. Incluye indicador de tasa de respuesta por
programa y periodo.

RF-10-06 – Exportación de reportes a Excel y PDF

Todos los reportes tabulares pueden exportarse a Excel (.xlsx) con todas las columnas,
encabezados en español y filtros activos aplicados. Los reportes de notas y actas pueden
exportarse también a PDF. El nombre del archivo sigue el patrón:
[TipoReporte]_[Programa]_[Periodo]_[FechaExportacion].

ÉPICA 11 – Configuración del Sistema

RF-11-02 – Configuración de parámetros por programa

El Administrador (o la Coordinación Académica para su facultad) configura los parámetros
operativos por programa: número de prácticas, número de cortes por práctica, nota mínima
de aprobación, requisitos por número de práctica, máximo de asignaciones simultáneas por
estudiante y umbral de inactividad para alertas.

RF-11-03 – Gestión de catálogos maestros

El Administrador gestiona los catálogos de datos que alimentan los selectores del sistema:
sectores económicos de empresas, áreas de práctica, modalidades, tipos de documentos
requeridos, estados personalizados. Los ítems activos aparecen en todos los selectores del
sistema.

RF-11-04 – Generación de respaldo de datos

El Administrador puede generar manualmente un respaldo completo de datos en formato
Excel con hojas: Estudiantes, Expedientes de prácticas, Empresas, Vacantes, Notas

registradas, Evaluaciones, Usuarios y Bitácora de auditoría (últimos 2 años). Nombre del
archivo: "Respaldo_GestionPracticas_AAAA-MM-DD_HHMMSS.xlsx".

RF-11-05 – Configuración de notificaciones por correo electrónico

El Administrador configura las reglas y plantillas del sistema de notificaciones por correo.
Por cada tipo de evento se define: plantilla HTML con variables dinámicas
({{nombre_estudiante}}, {{empresa}}, {{nombre_practica}}, {{enlace_encuesta}}), roles
receptores, si el correo es obligatorio o informativo, y frecuencia de recordatorios
automáticos en días hábiles (por defecto 3 días para encuestas pendientes). El sistema envía
todos los correos a través del servidor SMTP institucional.


