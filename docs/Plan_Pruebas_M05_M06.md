**MÓDULO 06 -- VACANTES Y POSTULACIONES**

**PT-06-POS-01 --- Asignación de estudiante apto a vacante activa**

+:-------------+:------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**     | PT-06-POS-01                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                            |                              +-----------------------------+---------------+
|                            |                              | **FECHA EJECUCIÓN**         |               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**           | Asignación de estudiante     | **MÓDULO DEL SISTEMA**      | Vacantes y    |
|                            | apto a vacante activa        |                             | Postulaciones |
+----------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de  | Validar que el Coordinador pueda asignar un estudiante en estado Apto y    |
| prueba:**                  | sin práctica activa a una vacante activa con cupos, decrementando el cupo  |
|                            | y notificando a los actores.                                               |
+----------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| \- El Coordinador tiene sesión iniciada y permisos sobre su programa.                                   |
|                                                                                                         |
| \- Existe al menos una vacante en estado Activa con cupos disponibles.                                  |
|                                                                                                         |
| \- Existen estudiantes en estado Apto sin práctica activa.                                              |
|                                                                                                         |
| \- El servicio de correo está habilitado.                                                               |
+---------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                  |
+---------------------------------------------------------------------------------------------------------+
| 1\. Iniciar sesión como Coordinador.                                                                    |
|                                                                                                         |
| 2\. Seleccionar una vacante activa con cupos disponibles.                                               |
|                                                                                                         |
| 3\. Consultar el listado de estudiantes Aptos sin práctica activa.                                      |
|                                                                                                         |
| 4\. Revisar el perfil y hoja de vida del candidato.                                                     |
|                                                                                                         |
| 5\. Confirmar la asignación (opcionalmente con nota de justificación).                                  |
|                                                                                                         |
| 6\. Verificar el decremento del cupo y el envío de notificaciones.                                      |
+-------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                      | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                           | ESPERADA DE   |                             | DEL SISTEMA** |
|                                           | LA            |                             |               |
|                                           | APLICACIÓN**  |                             |               |
+--------------+-------------+--------------+               +--------------+--------------+               |
| **CAMPO**    | **VALOR**   | **TIPO       |               | **SI**       | **NO**       |               |
|              |             | ESCENARIO**  |               |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Confirmar    | Estudiante  | Positivo     | Asignación    |              |              |               |
| asignación   | Apto sin    |              | registrada    |              |              |               |
|              | práctica    |              | con fecha,    |              |              |               |
|              | activa      |              | usuario       |              |              |               |
|              |             |              | responsable y |              |              |               |
|              |             |              | nota          |              |              |               |
|              |             |              | opcional;     |              |              |               |
|              |             |              | cupo          |              |              |               |
|              |             |              | decrementado. |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Notificación | Asignación  | Positivo     | Correo a      |              |              |               |
|              | confirmada  |              | estudiante,   |              |              |               |
|              |             |              | empresa y     |              |              |               |
|              |             |              | Tutor         |              |              |               |
|              |             |              | Empresarial   |              |              |               |
|              |             |              | enviado en    |              |              |               |
|              |             |              | menos de 5    |              |              |               |
|              |             |              | minutos.      |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Candidato no | Estudiante  | Negativo     | El estudiante |              |              |               |
| apto         | en estado   |              | no aparece en |              |              |               |
|              | distinto a  |              | el listado de |              |              |               |
|              | Apto        |              | candidatos.   |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Estudiante   | Estudiante  | Negativo     | El estudiante |              |              |               |
| con práctica | ya en       |              | no aparece    |              |              |               |
| activa       | práctica    |              | como          |              |              |               |
|              |             |              | candidato.    |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Doble        | Estudiante  | Negativo     | El sistema    |              |              |               |
| asignación   | ya asignado |              | impide la     |              |              |               |
|              | a otra      |              | asignación    |              |              |               |
|              | vacante     |              | simultánea.   |              |              |               |
|              | activa      |              |               |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Rechazo de   | Cambiar a   | Negativo     | El sistema    |              |              |               |
| postulado    | Rechazado   |              | exige motivo  |              |              |               |
|              | sin motivo  |              | obligatorio   |              |              |               |
|              |             |              | antes de      |              |              |               |
|              |             |              | guardar.      |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                    |
+---------------------------------------------------------------------------------------------------------+
| \- La asignación queda registrada y trazable, el cupo de la vacante se actualiza y los actores quedan   |
| notificados.                                                                                            |
+---------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                             |
+--------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                              | **Veredicto**                |
+--------------------------------------------------------------------------+------------------------------+
| N/A                                                                      | N/A                          |
+--------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                        | **Probador**                 |
+--------------------------------------------------------------------------+------------------------------+
|                                                                          | N/A                          |
+--------------------------------------------------------------------------+------------------------------+

**MÓDULO 06 -- VACANTES Y POSTULACIONES**

**PT-06-POS-02 --- Seguimiento y cancelación de asignaciones**

+:------------+:-------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**     | PT-06-POS-02                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                            |                              +-----------------------------+---------------+
|                            |                              | **FECHA EJECUCIÓN**         |               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**           | Seguimiento y cancelación de | **MÓDULO DEL SISTEMA**      | Vacantes y    |
|                            | asignaciones                 |                             | Postulaciones |
+----------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de  | Validar que el Coordinador visualice el listado de asignaciones activas,   |
| prueba:**                  | lo filtre, acceda al expediente y cancele una asignación con motivo antes  |
|                            | de iniciar la vinculación.                                                 |
+----------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| \- El Coordinador tiene sesión iniciada.                                                                |
|                                                                                                         |
| \- Existen asignaciones activas en su programa en distintos estados.                                    |
+---------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                  |
+---------------------------------------------------------------------------------------------------------+
| 1\. Iniciar sesión como Coordinador.                                                                    |
|                                                                                                         |
| 2\. Abrir el listado de asignaciones activas.                                                           |
|                                                                                                         |
| 3\. Aplicar filtros por estado, empresa, vacante, número de práctica y fechas.                          |
|                                                                                                         |
| 4\. Abrir el expediente del estudiante desde una fila.                                                  |
|                                                                                                         |
| 5\. Cancelar una asignación que aún no inicia vinculación, registrando el motivo.                       |
+-------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                      | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                           | ESPERADA DE   |                             | DEL SISTEMA** |
|                                           | LA            |                             |               |
|                                           | APLICACIÓN**  |                             |               |
+-------------+--------------+--------------+               +--------------+--------------+               |
| **CAMPO**   | **VALOR**    | **TIPO       |               | **SI**       | **NO**       |               |
|             |              | ESCENARIO**  |               |              |              |               |
+-------------+--------------+--------------+---------------+--------------+--------------+---------------+
| Listado     | Asignaciones | Positivo     | Muestra       |              |              |               |
|             | del programa |              | columnas:     |              |              |               |
|             |              |              | estudiante,   |              |              |               |
|             |              |              | número/nombre |              |              |               |
|             |              |              | de práctica,  |              |              |               |
|             |              |              | vacante,      |              |              |               |
|             |              |              | empresa,      |              |              |               |
|             |              |              | fecha de      |              |              |               |
|             |              |              | asignación,   |              |              |               |
|             |              |              | estado y      |              |              |               |
|             |              |              | último        |              |              |               |
|             |              |              | cambio.       |              |              |               |
+-------------+--------------+--------------+---------------+--------------+--------------+---------------+
| Filtro      | Estado = En  | Positivo     | Lista solo    |              |              |               |
|             | proceso de   |              | las           |              |              |               |
|             | vinculación  |              | asignaciones  |              |              |               |
|             |              |              | que coinciden |              |              |               |
|             |              |              | con el        |              |              |               |
|             |              |              | filtro.       |              |              |               |
+-------------+--------------+--------------+---------------+--------------+--------------+---------------+
| Acceso a    | Botón en la  | Positivo     | Abre el       |              |              |               |
| expediente  | fila         |              | expediente    |              |              |               |
|             |              |              | del           |              |              |               |
|             |              |              | estudiante    |              |              |               |
|             |              |              | seleccionado. |              |              |               |
+-------------+--------------+--------------+---------------+--------------+--------------+---------------+
| Cancelación | Asignación   | Positivo     | La asignación |              |              |               |
| válida      | sin          |              | se cancela y  |              |              |               |
|             | vinculación  |              | queda         |              |              |               |
|             | iniciada +   |              | registrada    |              |              |               |
|             | motivo       |              | con el        |              |              |               |
|             |              |              | motivo.       |              |              |               |
+-------------+--------------+--------------+---------------+--------------+--------------+---------------+
| Cancelación | Cancelar     | Negativo     | El sistema    |              |              |               |
| sin motivo  | dejando el   |              | exige el      |              |              |               |
|             | motivo vacío |              | motivo        |              |              |               |
|             |              |              | obligatorio.  |              |              |               |
+-------------+--------------+--------------+---------------+--------------+--------------+---------------+
| Cancelación | Asignación   | Negativo     | No permite    |              |              |               |
| tardía      | con          |              | cancelar      |              |              |               |
|             | vinculación  |              | (salvo motivo |              |              |               |
|             | ya iniciada  |              | Rechazo       |              |              |               |
|             |              |              | empresa).     |              |              |               |
+-------------+--------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                    |
+---------------------------------------------------------------------------------------------------------+
| \- El listado refleja los cambios, el historial es visible para los roles autorizados y la cancelación  |
| queda trazada con su motivo.                                                                            |
+---------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                             |
+--------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                              | **Veredicto**                |
+--------------------------------------------------------------------------+------------------------------+
| N/A                                                                      | N/A                          |
+--------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                        | **Probador**                 |
+--------------------------------------------------------------------------+------------------------------+
|                                                                          | N/A                          |
+--------------------------------------------------------------------------+------------------------------+

**MÓDULO 06 -- VACANTES Y POSTULACIONES**

**PT-06-POS-03 --- Flujo de estados y trazabilidad de la asignación**

+:-------------+:------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**     | PT-06-POS-03                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                            |                              +-----------------------------+---------------+
|                            |                              | **FECHA EJECUCIÓN**         |               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**           | Flujo de estados y           | **MÓDULO DEL SISTEMA**      | Vacantes y    |
|                            | trazabilidad de la           |                             | Postulaciones |
|                            | asignación                   |                             |               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de  | Validar el flujo de estados (Asignada → En proceso de vinculación →        |
| prueba:**                  | Vinculada / Cancelada), su registro en bitácora y la consulta en tiempo    |
|                            | real por parte del estudiante.                                             |
+----------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| \- Existe una asignación en estado Asignada.                                                            |
|                                                                                                         |
| \- El estudiante tiene acceso a su panel.                                                               |
|                                                                                                         |
| \- La bitácora de auditoría está habilitada.                                                            |
+---------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                  |
+---------------------------------------------------------------------------------------------------------+
| 1\. Avanzar la asignación de Asignada a En proceso de vinculación.                                      |
|                                                                                                         |
| 2\. Avanzar a Vinculada.                                                                                |
|                                                                                                         |
| 3\. Verificar el registro de fecha, usuario responsable y motivo en cada transición.                    |
|                                                                                                         |
| 4\. Consultar el estado desde el panel del estudiante.                                                  |
|                                                                                                         |
| 5\. Intentar retroceder un estado y cancelar sin motivo.                                                |
+-------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                      | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                           | ESPERADA DE   |                             | DEL SISTEMA** |
|                                           | LA            |                             |               |
|                                           | APLICACIÓN**  |                             |               |
+--------------+-------------+--------------+               +--------------+--------------+               |
| **CAMPO**    | **VALOR**   | **TIPO       |               | **SI**       | **NO**       |               |
|              |             | ESCENARIO**  |               |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Transición   | Asignada →  | Positivo     | El estado     |              |              |               |
| de estado    | En proceso  |              | cambia y      |              |              |               |
|              | de          |              | genera        |              |              |               |
|              | vinculación |              | notificación  |              |              |               |
|              |             |              | automática a  |              |              |               |
|              |             |              | los actores.  |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Trazabilidad | Cada cambio | Positivo     | Registra      |              |              |               |
|              | de estado   |              | fecha,        |              |              |               |
|              |             |              | usuario       |              |              |               |
|              |             |              | responsable y |              |              |               |
|              |             |              | motivo en la  |              |              |               |
|              |             |              | bitácora.     |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Consulta del | Panel del   | Positivo     | Muestra el    |              |              |               |
| estudiante   | estudiante  |              | estado actual |              |              |               |
|              |             |              | de su         |              |              |               |
|              |             |              | asignación en |              |              |               |
|              |             |              | tiempo real.  |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Retroceso de | Vinculada → | Negativo     | El sistema    |              |              |               |
| estado       | estado      |              | impide volver |              |              |               |
|              | anterior    |              | a estados     |              |              |               |
|              |             |              | anteriores.   |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Cancelación  | Cambiar a   | Negativo     | El sistema    |              |              |               |
| sin motivo   | Cancelada   |              | exige motivo  |              |              |               |
|              | sin motivo  |              | obligatorio.  |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                    |
+---------------------------------------------------------------------------------------------------------+
| \- El estado de la asignación es coherente, las transiciones quedan auditadas y las notificaciones      |
| incluyen estado actual, descripción del cambio y responsable.                                           |
+---------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                             |
+--------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                              | **Veredicto**                |
+--------------------------------------------------------------------------+------------------------------+
| N/A                                                                      | N/A                          |
+--------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                        | **Probador**                 |
+--------------------------------------------------------------------------+------------------------------+
|                                                                          | N/A                          |
+--------------------------------------------------------------------------+------------------------------+

**MÓDULO 06 -- VACANTES Y POSTULACIONES**

**PT-06-POS-04 --- Notificación automática de eventos de asignación**

+:------------+:------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                              |
+---------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**    | PT-06-POS-04                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                           |                              +-----------------------------+---------------+
|                           |                              | **FECHA EJECUCIÓN**         |               |
+---------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**          | Notificación automática de   | **MÓDULO DEL SISTEMA**      | Vacantes y    |
|                           | eventos de asignación        |                             | Postulaciones |
+---------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de | Validar el envío de notificaciones por correo ante nueva asignación,       |
| prueba:**                 | cancelación y cambio de estado, con reintentos ante fallo y registro en    |
|                           | bitácora.                                                                  |
+---------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                     |
+--------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                     |
+--------------------------------------------------------------------------------------------------------+
| \- El servicio de correo está configurado.                                                             |
|                                                                                                        |
| \- Las plantillas de notificación están definidas (RF-11-05).                                          |
|                                                                                                        |
| \- Existen asignaciones sobre las que generar eventos.                                                 |
+--------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                 |
+--------------------------------------------------------------------------------------------------------+
| 1\. Confirmar una nueva asignación.                                                                    |
|                                                                                                        |
| 2\. Cancelar una asignación con motivo.                                                                |
|                                                                                                        |
| 3\. Realizar un cambio de estado.                                                                      |
|                                                                                                        |
| 4\. Simular un fallo de envío de correo.                                                               |
|                                                                                                        |
| 5\. Consultar el historial de envíos desde el expediente.                                              |
+------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                     | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                          | ESPERADA DE   |                             | DEL SISTEMA** |
|                                          | LA            |                             |               |
|                                          | APLICACIÓN**  |                             |               |
+-------------+-------------+--------------+               +--------------+--------------+               |
| **CAMPO**   | **VALOR**   | **TIPO       |               | **SI**       | **NO**       |               |
|             |             | ESCENARIO**  |               |              |              |               |
+-------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Nueva       | Asignación  | Positivo     | Notifica a    |              |              |               |
| asignación  | confirmada  |              | estudiante,   |              |              |               |
|             |             |              | empresa y     |              |              |               |
|             |             |              | Tutor         |              |              |               |
|             |             |              | Empresarial   |              |              |               |
|             |             |              | en menos de 5 |              |              |               |
|             |             |              | minutos.      |              |              |               |
+-------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Cancelación | Asignación  | Positivo     | Notifica a    |              |              |               |
|             | cancelada   |              | estudiante y  |              |              |               |
|             |             |              | empresa       |              |              |               |
|             |             |              | incluyendo el |              |              |               |
|             |             |              | motivo.       |              |              |               |
+-------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Contenido   | Evento de   | Positivo     | Incluye       |              |              |               |
| del correo  | asignación  |              | nombre del    |              |              |               |
|             |             |              | estudiante,   |              |              |               |
|             |             |              | número/nombre |              |              |               |
|             |             |              | de práctica,  |              |              |               |
|             |             |              | cargo y       |              |              |               |
|             |             |              | empresa,      |              |              |               |
|             |             |              | estado actual |              |              |               |
|             |             |              | y enlace al   |              |              |               |
|             |             |              | expediente.   |              |              |               |
+-------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Fallo de    | Servicio de | Negativo     | Reintenta     |              |              |               |
| envío       | correo no   |              | hasta 3 veces |              |              |               |
|             | disponible  |              | con           |              |              |               |
|             |             |              | intervalos de |              |              |               |
|             |             |              | 2 minutos.    |              |              |               |
+-------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Registro    | Envíos      | Positivo     | Quedan        |              |              |               |
|             | realizados  |              | registrados   |              |              |               |
|             |             |              | en la         |              |              |               |
|             |             |              | bitácora y    |              |              |               |
|             |             |              | respetan el   |              |              |               |
|             |             |              | scope del rol |              |              |               |
|             |             |              | receptor.     |              |              |               |
+-------------+-------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                   |
+--------------------------------------------------------------------------------------------------------+
| \- Todos los actores correspondientes quedan notificados, los envíos quedan auditados y los fallos se  |
| gestionan con reintentos.                                                                              |
+--------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                            |
+-------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                             | **Veredicto**                |
+-------------------------------------------------------------------------+------------------------------+
| N/A                                                                     | N/A                          |
+-------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                       | **Probador**                 |
+-------------------------------------------------------------------------+------------------------------+
|                                                                         | N/A                          |
+-------------------------------------------------------------------------+------------------------------+

**MÓDULO 07 -- VINCULACIÓN Y DOCUMENTACIÓN**

**PT-07-VIN-01 --- Carga de carta de presentación al expediente**

+:------------+:--------------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                                      |
+-----------------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**            | PT-07-VIN-01                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                                   |                              +-----------------------------+---------------+
|                                   |                              | **FECHA EJECUCIÓN**         |               |
+-----------------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**                  | Carga de carta de            | **MÓDULO DEL SISTEMA**      | Vinculación y |
|                                   | presentación al expediente   |                             | Documentación |
+-----------------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de         | Validar que el Coordinador cargue la carta de presentación (PDF/JPG/PNG ≤  |
| prueba:**                         | 10 MB) al expediente, con registro de nombre, fecha y usuario, y que sea   |
|                                   | requisito para confirmar la vinculación.                                   |
+-----------------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                             |
+----------------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                             |
+----------------------------------------------------------------------------------------------------------------+
| \- Existe una asignación confirmada de estudiante a vacante.                                                   |
|                                                                                                                |
| \- El Coordinador tiene sesión iniciada y acceso al repositorio.                                               |
+----------------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                         |
+----------------------------------------------------------------------------------------------------------------+
| 1\. Abrir el expediente de la práctica.                                                                        |
|                                                                                                                |
| 2\. Cargar la carta de presentación en formato válido y tamaño ≤ 10 MB.                                        |
|                                                                                                                |
| 3\. Intentar cargar un archivo \> 10 MB y uno de formato no permitido.                                         |
|                                                                                                                |
| 4\. Intentar confirmar la vinculación sin carta cargada.                                                       |
|                                                                                                                |
| 5\. Verificar el registro del documento (nombre, fecha, usuario).                                              |
+--------------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                             | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                                  | ESPERADA DE   |                             | DEL SISTEMA** |
|                                                  | LA            |                             |               |
|                                                  | APLICACIÓN**  |                             |               |
+-------------+---------------------+--------------+               +--------------+--------------+               |
| **CAMPO**   | **VALOR**           | **TIPO       |               | **SI**       | **NO**       |               |
|             |                     | ESCENARIO**  |               |              |              |               |
+-------------+---------------------+--------------+---------------+--------------+--------------+---------------+
| Carga       | PDF/JPG/PNG ≤ 10 MB | Positivo     | Documento     |              |              |               |
| válida      |                     |              | cargado y     |              |              |               |
|             |                     |              | registrado    |              |              |               |
|             |                     |              | con nombre,   |              |              |               |
|             |                     |              | fecha de      |              |              |               |
|             |                     |              | carga y       |              |              |               |
|             |                     |              | usuario       |              |              |               |
|             |                     |              | responsable.  |              |              |               |
+-------------+---------------------+--------------+---------------+--------------+--------------+---------------+
| Indicador   | Carta cargada       | Positivo     | El            |              |              |               |
| de estado   |                     |              | repositorio   |              |              |               |
|             |                     |              | indica la     |              |              |               |
|             |                     |              | carta como    |              |              |               |
|             |                     |              | cargada (no   |              |              |               |
|             |                     |              | pendiente).   |              |              |               |
+-------------+---------------------+--------------+---------------+--------------+--------------+---------------+
| Tamaño      | Archivo \> 10 MB    | Negativo     | El sistema    |              |              |               |
| excedido    |                     |              | rechaza la    |              |              |               |
|             |                     |              | carga por     |              |              |               |
|             |                     |              | superar el    |              |              |               |
|             |                     |              | tamaño        |              |              |               |
|             |                     |              | máximo.       |              |              |               |
+-------------+---------------------+--------------+---------------+--------------+--------------+---------------+
| Formato     | Archivo .docx /     | Negativo     | El sistema    |              |              |               |
| inválido    | .exe                |              | rechaza el    |              |              |               |
|             |                     |              | formato no    |              |              |               |
|             |                     |              | permitido.    |              |              |               |
+-------------+---------------------+--------------+---------------+--------------+--------------+---------------+
| Vinculación | Confirmar           | Negativo     | El sistema    |              |              |               |
| sin carta   | vinculación sin     |              | impide        |              |              |               |
|             | carta               |              | confirmar la  |              |              |               |
|             |                     |              | vinculación.  |              |              |               |
+-------------+---------------------+--------------+---------------+--------------+--------------+---------------+
| Práctica    | Reemplazar/eliminar | Negativo     | El sistema no |              |              |               |
| cerrada     | carta en práctica   |              | permite       |              |              |               |
|             | cerrada             |              | modificar     |              |              |               |
|             |                     |              | documentos de |              |              |               |
|             |                     |              | vinculación.  |              |              |               |
+-------------+---------------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                           |
+----------------------------------------------------------------------------------------------------------------+
| \- La carta queda archivada de forma trazable y disponible para los roles autorizados; sin ella no se puede    |
| confirmar la vinculación.                                                                                      |
+----------------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                                    |
+---------------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                                     | **Veredicto**                |
+---------------------------------------------------------------------------------+------------------------------+
| N/A                                                                             | N/A                          |
+---------------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                               | **Probador**                 |
+---------------------------------------------------------------------------------+------------------------------+
|                                                                                 | N/A                          |
+---------------------------------------------------------------------------------+------------------------------+

**MÓDULO 07 -- VINCULACIÓN Y DOCUMENTACIÓN**

**PT-07-VIN-02 --- Carga de convenio y plan de práctica con firmas**

+:------------+:--------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                                |
+-----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**      | PT-07-VIN-02                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                             |                              +-----------------------------+---------------+
|                             |                              | **FECHA EJECUCIÓN**         |               |
+-----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**            | Carga de convenio y plan de  | **MÓDULO DEL SISTEMA**      | Vinculación y |
|                             | práctica con firmas          |                             | Documentación |
+-----------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de   | Validar la carga del convenio por el Coordinador y del plan de práctica    |
| prueba:**                   | por el estudiante, el flujo de firmas (Coordinador, Tutor Empresarial,     |
|                             | Estudiante) y la aprobación previa del plan.                               |
+-----------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                       |
+----------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                       |
+----------------------------------------------------------------------------------------------------------+
| \- Existe una asignación en proceso de vinculación.                                                      |
|                                                                                                          |
| \- El Tutor Empresarial y el Docente Asesor están designados.                                            |
+----------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                   |
+----------------------------------------------------------------------------------------------------------+
| 1\. El Coordinador carga el convenio de práctica.                                                        |
|                                                                                                          |
| 2\. El Estudiante carga su plan de práctica con objetivos y cronograma.                                  |
|                                                                                                          |
| 3\. El Tutor Empresarial confirma su firma desde su portal.                                              |
|                                                                                                          |
| 4\. Registrar las firmas de Coordinador, Tutor Empresarial y Estudiante.                                 |
|                                                                                                          |
| 5\. Verificar el comportamiento con firmas incompletas y plan pendiente.                                 |
+--------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                       | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                            | ESPERADA DE   |                             | DEL SISTEMA** |
|                                            | LA            |                             |               |
|                                            | APLICACIÓN**  |                             |               |
+-------------+---------------+--------------+               +--------------+--------------+               |
| **CAMPO**   | **VALOR**     | **TIPO       |               | **SI**       | **NO**       |               |
|             |               | ESCENARIO**  |               |              |              |               |
+-------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Carga de    | Convenio      | Positivo     | El convenio   |              |              |               |
| convenio    | cargado por   |              | queda         |              |              |               |
|             | Coordinador   |              | registrado en |              |              |               |
|             |               |              | el            |              |              |               |
|             |               |              | expediente.   |              |              |               |
+-------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Carga de    | Plan con      | Positivo     | El plan queda |              |              |               |
| plan        | objetivos y   |              | registrado y  |              |              |               |
|             | cronograma    |              | disponible    |              |              |               |
|             |               |              | para          |              |              |               |
|             |               |              | aprobación.   |              |              |               |
+-------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Firma del   | Confirmación  | Positivo     | La firma del  |              |              |               |
| Tutor       | desde el      |              | Tutor         |              |              |               |
|             | portal        |              | Empresarial   |              |              |               |
|             |               |              | queda         |              |              |               |
|             |               |              | registrada.   |              |              |               |
+-------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Firmas      | Coordinador + | Positivo     | Activa estado |              |              |               |
| completas   | Tutor +       |              | En práctica y |              |              |               |
|             | Estudiante +  |              | registra      |              |              |               |
|             | plan aprobado |              | fecha oficial |              |              |               |
|             |               |              | de inicio; el |              |              |               |
|             |               |              | convenio      |              |              |               |
|             |               |              | queda         |              |              |               |
|             |               |              | inmutable.    |              |              |               |
+-------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Firmas      | Convenio sin  | Negativo     | El convenio   |              |              |               |
| incompletas | las tres      |              | no se         |              |              |               |
|             | firmas        |              | considera     |              |              |               |
|             |               |              | vigente.      |              |              |               |
+-------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Plan        | Plan en       | Negativo     | El plan de    |              |              |               |
| pendiente   | estado        |              | práctica no   |              |              |               |
|             | Pendiente de  |              | puede         |              |              |               |
|             | aprobación    |              | avanzar.      |              |              |               |
+-------------+---------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                     |
+----------------------------------------------------------------------------------------------------------+
| \- Con las tres firmas y el plan aprobado, la práctica pasa a En práctica con fecha de inicio registrada |
| y el convenio archivado de forma inmutable.                                                              |
+----------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                              |
+---------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                               | **Veredicto**                |
+---------------------------------------------------------------------------+------------------------------+
| N/A                                                                       | N/A                          |
+---------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                         | **Probador**                 |
+---------------------------------------------------------------------------+------------------------------+
|                                                                           | N/A                          |
+---------------------------------------------------------------------------+------------------------------+

**MÓDULO 07 -- VINCULACIÓN Y DOCUMENTACIÓN**

**PT-07-VIN-03 --- Activación de la práctica con fechas oficiales**

+:-------------+:------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**     | PT-07-VIN-03                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                            |                              +-----------------------------+---------------+
|                            |                              | **FECHA EJECUCIÓN**         |               |
+----------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**           | Activación de la práctica    | **MÓDULO DEL SISTEMA**      | Vinculación y |
|                            | con fechas oficiales         |                             | Documentación |
+----------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de  | Validar que el Coordinador confirme la vinculación ingresando fechas       |
| prueba:**                  | oficiales de inicio y fin, activando el estado En práctica, asignando      |
|                            | Docente Asesor y notificando a los actores.                                |
+----------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                      |
+---------------------------------------------------------------------------------------------------------+
| \- El convenio está firmado por los tres actores y el plan de práctica aprobado.                        |
|                                                                                                         |
| \- La carta de presentación está cargada.                                                               |
+---------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                  |
+---------------------------------------------------------------------------------------------------------+
| 1\. Abrir la práctica lista para vinculación.                                                           |
|                                                                                                         |
| 2\. Ingresar fechas oficiales de inicio y fin.                                                          |
|                                                                                                         |
| 3\. Confirmar la vinculación.                                                                           |
|                                                                                                         |
| 4\. Probar fecha de fin anterior a la de inicio.                                                        |
|                                                                                                         |
| 5\. Intentar activar sin convenio/plan/firmas completas.                                                |
+-------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                      | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                           | ESPERADA DE   |                             | DEL SISTEMA** |
|                                           | LA            |                             |               |
|                                           | APLICACIÓN**  |                             |               |
+--------------+-------------+--------------+               +--------------+--------------+               |
| **CAMPO**    | **VALOR**   | **TIPO       |               | **SI**       | **NO**       |               |
|              |             | ESCENARIO**  |               |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Confirmación | Fechas      | Positivo     | Activa estado |              |              |               |
| válida       | correctas + |              | En práctica,  |              |              |               |
|              | requisitos  |              | asigna        |              |              |               |
|              | completos   |              | Docente       |              |              |               |
|              |             |              | Asesor y      |              |              |               |
|              |             |              | notifica a    |              |              |               |
|              |             |              | todos los     |              |              |               |
|              |             |              | actores por   |              |              |               |
|              |             |              | correo y      |              |              |               |
|              |             |              | panel.        |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Visibilidad  | Práctica    | Positivo     | Queda visible |              |              |               |
|              | activada    |              | en el tablero |              |              |               |
|              |             |              | de            |              |              |               |
|              |             |              | seguimiento   |              |              |               |
|              |             |              | del           |              |              |               |
|              |             |              | Coordinador y |              |              |               |
|              |             |              | del Docente   |              |              |               |
|              |             |              | Asesor.       |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Fechas       | Fecha de    | Negativo     | El sistema    |              |              |               |
| inválidas    | fin         |              | rechaza la    |              |              |               |
|              | anterior a  |              | confirmación. |              |              |               |
|              | la de       |              |               |              |              |               |
|              | inicio      |              |               |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| Requisitos   | Sin         | Negativo     | El sistema    |              |              |               |
| incompletos  | convenio /  |              | impide        |              |              |               |
|              | plan /      |              | activar la    |              |              |               |
|              | firmas      |              | práctica.     |              |              |               |
+--------------+-------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                    |
+---------------------------------------------------------------------------------------------------------+
| \- La práctica queda activa con fecha de inicio como referencia para los cortes de evaluación y todos   |
| los actores quedan notificados.                                                                         |
+---------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                             |
+--------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                              | **Veredicto**                |
+--------------------------------------------------------------------------+------------------------------+
| N/A                                                                      | N/A                          |
+--------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                        | **Probador**                 |
+--------------------------------------------------------------------------+------------------------------+
|                                                                          | N/A                          |
+--------------------------------------------------------------------------+------------------------------+

**MÓDULO 07 -- VINCULACIÓN Y DOCUMENTACIÓN**

**PT-07-VIN-04 --- Gestión del repositorio documental de la práctica**

+:-------------+:--------------+:-------------+:--------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                                 |
+------------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**       | PT-07-VIN-04                 | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                              |                              +-----------------------------+---------------+
|                              |                              | **FECHA EJECUCIÓN**         |               |
+------------------------------+------------------------------+-----------------------------+---------------+
| **CASO DE USO:**             | Gestión del repositorio      | **MÓDULO DEL SISTEMA**      | Vinculación y |
|                              | documental de la práctica    |                             | Documentación |
+------------------------------+------------------------------+-----------------------------+---------------+
| **Descripción del caso de    | Validar el repositorio de documentos por práctica organizado por           |
| prueba:**                    | categorías, con control de acceso por rol/etapa, estado por categoría e    |
|                              | inmutabilidad en prácticas cerradas.                                       |
+------------------------------+----------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                        |
+-----------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                        |
+-----------------------------------------------------------------------------------------------------------+
| \- Existe una práctica con documentos en distintas categorías.                                            |
|                                                                                                           |
| \- Hay usuarios con distintos roles y scopes.                                                             |
+-----------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                    |
+-----------------------------------------------------------------------------------------------------------+
| 1\. Abrir el repositorio de una práctica.                                                                 |
|                                                                                                           |
| 2\. Verificar las categorías y el estado (completo/pendiente) de cada una.                                |
|                                                                                                           |
| 3\. Visualizar y descargar documentos como rol autorizado.                                                |
|                                                                                                           |
| 4\. Intentar acceder a documentos fuera del scope/etapa del rol.                                          |
|                                                                                                           |
| 5\. Cargar archivo no válido y probar modificación en práctica cerrada.                                   |
+---------------------------------------------+---------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                        | **RESPUESTA   | **COINCIDE**                | **RESPUESTA   |
|                                             | ESPERADA DE   |                             | DEL SISTEMA** |
|                                             | LA            |                             |               |
|                                             | APLICACIÓN**  |                             |               |
+--------------+---------------+--------------+               +--------------+--------------+               |
| **CAMPO**    | **VALOR**     | **TIPO       |               | **SI**       | **NO**       |               |
|              |               | ESCENARIO**  |               |              |              |               |
+--------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Organización | Categorías:   | Positivo     | Muestra el    |              |              |               |
|              | vinculación,  |              | repositorio   |              |              |               |
|              | seguimiento,  |              | organizado    |              |              |               |
|              | evaluaciones, |              | con el estado |              |              |               |
|              | acta de       |              | de cada       |              |              |               |
|              | cierre        |              | categoría.    |              |              |               |
+--------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Descarga     | Rol con       | Positivo     | Permite       |              |              |               |
| autorizada   | permiso sobre |              | visualizar y  |              |              |               |
|              | el documento  |              | descargar el  |              |              |               |
|              |               |              | documento.    |              |              |               |
+--------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Registro     | Cada          | Positivo     | Registra      |              |              |               |
|              | documento     |              | nombre, fecha |              |              |               |
|              |               |              | de carga,     |              |              |               |
|              |               |              | quién lo      |              |              |               |
|              |               |              | cargó y       |              |              |               |
|              |               |              | estado.       |              |              |               |
+--------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Acceso fuera | Actor         | Negativo     | El sistema    |              |              |               |
| de scope     | consulta      |              | deniega el    |              |              |               |
|              | documento de  |              | acceso.       |              |              |               |
|              | otra          |              |               |              |              |               |
|              | etapa/actor   |              |               |              |              |               |
+--------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Archivo      | \> 10 MB o    | Negativo     | El sistema    |              |              |               |
| inválido     | formato       |              | rechaza la    |              |              |               |
|              | distinto a    |              | carga.        |              |              |               |
|              | PDF/JPG/PNG   |              |               |              |              |               |
+--------------+---------------+--------------+---------------+--------------+--------------+---------------+
| Práctica     | Modificar     | Negativo     | Los           |              |              |               |
| cerrada      | documento de  |              | documentos    |              |              |               |
|              | práctica      |              | son           |              |              |               |
|              | cerrada       |              | inmutables;   |              |              |               |
|              |               |              | no permite la |              |              |               |
|              |               |              | acción.       |              |              |               |
+--------------+---------------+--------------+---------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                      |
+-----------------------------------------------------------------------------------------------------------+
| \- El repositorio refleja el estado real de cada categoría, cada actor accede solo a lo que le            |
| corresponde y los documentos de prácticas cerradas permanecen inmutables.                                 |
+-----------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                               |
+----------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                                | **Veredicto**                |
+----------------------------------------------------------------------------+------------------------------+
| N/A                                                                        | N/A                          |
+----------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                          | **Probador**                 |
+----------------------------------------------------------------------------+------------------------------+
|                                                                            | N/A                          |
+----------------------------------------------------------------------------+------------------------------+

**MÓDULO 07 -- VINCULACIÓN Y DOCUMENTACIÓN**

**PT-07-VIN-05 --- Asignación de Docente Asesor a la práctica**

+:------------+:------------+:-------------+:----------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                                |
+---------------------------+--------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**    | PT-07-VIN-05                   | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                           |                                +-----------------------------+---------------+
|                           |                                | **FECHA EJECUCIÓN**         |               |
+---------------------------+--------------------------------+-----------------------------+---------------+
| **CASO DE USO:**          | Asignación de Docente Asesor a | **MÓDULO DEL SISTEMA**      | Vinculación y |
|                           | la práctica                    |                             | Documentación |
+---------------------------+--------------------------------+-----------------------------+---------------+
| **Descripción del caso de | Validar que solo el Coordinador asigne un Docente Asesor activo a la         |
| prueba:**                 | práctica, con registro de fecha y usuario, y que el asesor acceda al         |
|                           | expediente.                                                                  |
+---------------------------+------------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                       |
+----------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                       |
+----------------------------------------------------------------------------------------------------------+
| \- Existe una práctica creada.                                                                           |
|                                                                                                          |
| \- Hay Docentes Asesores activos e inactivos en el sistema.                                              |
+----------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                   |
+----------------------------------------------------------------------------------------------------------+
| 1\. Iniciar sesión como Coordinador y abrir la práctica.                                                 |
|                                                                                                          |
| 2\. Asignar un Docente Asesor activo.                                                                    |
|                                                                                                          |
| 3\. Verificar el registro de fecha y usuario responsable.                                                |
|                                                                                                          |
| 4\. Confirmar que el Docente Asesor accede al expediente.                                                |
|                                                                                                          |
| 5\. Probar asignar un asesor inactivo y asignar con otro rol.                                            |
+------------------------------------------+-----------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                     | **RESPUESTA     | **COINCIDE**                | **RESPUESTA   |
|                                          | ESPERADA DE LA  |                             | DEL SISTEMA** |
|                                          | APLICACIÓN**    |                             |               |
+-------------+-------------+--------------+                 +--------------+--------------+               |
| **CAMPO**   | **VALOR**   | **TIPO       |                 | **SI**       | **NO**       |               |
|             |             | ESCENARIO**  |                 |              |              |               |
+-------------+-------------+--------------+-----------------+--------------+--------------+---------------+
| Asignación  | Docente     | Positivo     | Asignación      |              |              |               |
| válida      | Asesor      |              | registrada con  |              |              |               |
|             | activo      |              | fecha y usuario |              |              |               |
|             |             |              | responsable.    |              |              |               |
+-------------+-------------+--------------+-----------------+--------------+--------------+---------------+
| Acceso del  | Docente     | Positivo     | Accede al       |              |              |               |
| asesor      | Asesor      |              | expediente y    |              |              |               |
|             | asignado    |              | puede registrar |              |              |               |
|             |             |              | seguimientos y  |              |              |               |
|             |             |              | calificaciones. |              |              |               |
+-------------+-------------+--------------+-----------------+--------------+--------------+---------------+
| Carga       | Asesor con  | Positivo     | Permite         |              |              |               |
| múltiple    | varios      |              | múltiples       |              |              |               |
|             | estudiantes |              | estudiantes     |              |              |               |
|             |             |              | asignados sin   |              |              |               |
|             |             |              | límite superior |              |              |               |
|             |             |              | configurado.    |              |              |               |
+-------------+-------------+--------------+-----------------+--------------+--------------+---------------+
| Asesor      | Docente     | Negativo     | El sistema      |              |              |               |
| inactivo    | Asesor      |              | impide la       |              |              |               |
|             | inactivo    |              | asignación.     |              |              |               |
+-------------+-------------+--------------+-----------------+--------------+--------------+---------------+
| Rol no      | Otro rol    | Negativo     | Solo el         |              |              |               |
| autorizado  | intenta     |              | Coordinador     |              |              |               |
|             | asignar     |              | puede realizar  |              |              |               |
|             |             |              | la asignación.  |              |              |               |
+-------------+-------------+--------------+-----------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                     |
+----------------------------------------------------------------------------------------------------------+
| \- El Docente Asesor queda asignado y trazado, con acceso al expediente del estudiante.                  |
+----------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                              |
+---------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                               | **Veredicto**                |
+---------------------------------------------------------------------------+------------------------------+
| N/A                                                                       | N/A                          |
+---------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                         | **Probador**                 |
+---------------------------------------------------------------------------+------------------------------+
|                                                                           | N/A                          |
+---------------------------------------------------------------------------+------------------------------+

**MÓDULO 07 -- VINCULACIÓN Y DOCUMENTACIÓN**

**PT-07-VIN-06 --- Asignación de Tutor Empresarial a la práctica**

+:------------+:-------------+:-------------+:-----------------+:-------------+:-------------+:--------------+
| **INFORMACIÓN GLOBAL DEL CASO DE PRUEBA**                                                                  |
+----------------------------+---------------------------------+-----------------------------+---------------+
| **CASO DE PRUEBA No.**     | PT-07-VIN-06                    | **VERSIÓN DE EJECUCIÓN**    | 1.0           |
|                            |                                 +-----------------------------+---------------+
|                            |                                 | **FECHA EJECUCIÓN**         |               |
+----------------------------+---------------------------------+-----------------------------+---------------+
| **CASO DE USO:**           | Asignación de Tutor Empresarial | **MÓDULO DEL SISTEMA**      | Vinculación y |
|                            | a la práctica                   |                             | Documentación |
+----------------------------+---------------------------------+-----------------------------+---------------+
| **Descripción del caso de  | Validar que solo el Coordinador registre formalmente al Tutor Empresarial (de |
| prueba:**                  | una empresa Aprobada) responsable del practicante, con registro de fecha y    |
|                            | usuario y acceso con scope limitado.                                          |
+----------------------------+-------------------------------------------------------------------------------+
| **CASO DE PRUEBA**                                                                                         |
+------------------------------------------------------------------------------------------------------------+
| **Precondiciones**                                                                                         |
+------------------------------------------------------------------------------------------------------------+
| \- Existe una práctica creada y una empresa en estado Aprobada con tutores registrados.                    |
+------------------------------------------------------------------------------------------------------------+
| **Pasos de la prueba**                                                                                     |
+------------------------------------------------------------------------------------------------------------+
| 1\. Iniciar sesión como Coordinador y abrir la práctica.                                                   |
|                                                                                                            |
| 2\. Registrar al Tutor Empresarial de una empresa Aprobada.                                                |
|                                                                                                            |
| 3\. Verificar el registro de fecha y usuario responsable.                                                  |
|                                                                                                            |
| 4\. Confirmar que el tutor accede con scope limitado a sus estudiantes.                                    |
|                                                                                                            |
| 5\. Probar tutor de empresa no Aprobada y asignación con otro rol.                                         |
+-------------------------------------------+------------------+-----------------------------+---------------+
| **DATOS DE ENTRADA**                      | **RESPUESTA      | **COINCIDE**                | **RESPUESTA   |
|                                           | ESPERADA DE LA   |                             | DEL SISTEMA** |
|                                           | APLICACIÓN**     |                             |               |
+-------------+--------------+--------------+                  +--------------+--------------+               |
| **CAMPO**   | **VALOR**    | **TIPO       |                  | **SI**       | **NO**       |               |
|             |              | ESCENARIO**  |                  |              |              |               |
+-------------+--------------+--------------+------------------+--------------+--------------+---------------+
| Asignación  | Tutor de     | Positivo     | Asignación       |              |              |               |
| válida      | empresa      |              | registrada con   |              |              |               |
|             | Aprobada     |              | fecha y usuario  |              |              |               |
|             |              |              | responsable.     |              |              |               |
+-------------+--------------+--------------+------------------+--------------+--------------+---------------+
| Acceso del  | Tutor        | Positivo     | Accede al portal |              |              |               |
| tutor       | asignado     |              | con scope        |              |              |               |
|             |              |              | limitado a sus   |              |              |               |
|             |              |              | estudiantes.     |              |              |               |
+-------------+--------------+--------------+------------------+--------------+--------------+---------------+
| Carga       | Tutor con    | Positivo     | Permite          |              |              |               |
| múltiple    | varios       |              | múltiples        |              |              |               |
|             | practicantes |              | practicantes a   |              |              |               |
|             |              |              | cargo            |              |              |               |
|             |              |              | simultáneamente. |              |              |               |
+-------------+--------------+--------------+------------------+--------------+--------------+---------------+
| Empresa no  | Tutor de     | Negativo     | El sistema       |              |              |               |
| aprobada    | empresa no   |              | impide la        |              |              |               |
|             | Aprobada     |              | asignación.      |              |              |               |
+-------------+--------------+--------------+------------------+--------------+--------------+---------------+
| Rol no      | Otro rol     | Negativo     | Solo el          |              |              |               |
| autorizado  | intenta      |              | Coordinador      |              |              |               |
|             | confirmar    |              | puede confirmar  |              |              |               |
|             |              |              | formalmente la   |              |              |               |
|             |              |              | asignación.      |              |              |               |
+-------------+--------------+--------------+------------------+--------------+--------------+---------------+
| **Post condiciones**                                                                                       |
+------------------------------------------------------------------------------------------------------------+
| \- El Tutor Empresarial queda asignado y trazado, con acceso restringido a sus estudiantes.                |
+------------------------------------------------------------------------------------------------------------+
| **RESULTADOS DE LA PRUEBA**                                                                                |
+-----------------------------------------------------------------------------+------------------------------+
| **Defectos y desviaciones**                                                 | **Veredicto**                |
+-----------------------------------------------------------------------------+------------------------------+
| N/A                                                                         | N/A                          |
+-----------------------------------------------------------------------------+------------------------------+
| **Observaciones**                                                           | **Probador**                 |
+-----------------------------------------------------------------------------+------------------------------+
|                                                                             | N/A                          |
+-----------------------------------------------------------------------------+------------------------------+
