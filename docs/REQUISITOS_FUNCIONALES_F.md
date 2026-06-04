Universidad Alexander Von Humboldt

Facultad de Ingenierías

Programa de Ingeniería de Software

Sistema de Gestión de Prácticas Empresariales CUE

Patrones de diseño a implementar

Santiago Acosta Calvo

Jeshua Gomez Cortes

María José Realpe Vallejo

Presentado a:

Ing, Diana María Valencia

Armenia, Quindío

2026

# 

# MÓDULO 01 -- Dashboard y Panel de Inicio

  ----------------------------------------------------------------------------------------------------------------
  **ID          **Nombre del     **Descripción del     **Criterio de            **Regla del        **Prioridad**
  Requisito**   Requisito**      Requisito**           Aceptación**             Negocio**          
  ------------- ---------------- --------------------- ------------------------ ------------------ ---------------
  RF-01-01      Panel de inicio  Cada usuario          Cada rol visualiza       El sistema         ● Alta
                personalizado    visualiza al iniciar  únicamente la            identifica el rol  
                por rol          sesión un panel       información y accesos    del usuario        
                                 adaptado a su rol. El directos que le          autenticado y      
                                 DTI (Administrador)   corresponden. El panel   renderiza el panel 
                                 ve resumen global y   carga en menos de 3      correspondiente.   
                                 gestión de usuarios;  segundos. Ningún rol     Los accesos        
                                 la Dirección ve       puede ver información de directos del panel 
                                 indicadores           otro rol desde el panel  redirigen al       
                                 gerenciales por       de inicio.               módulo con filtros 
                                 facultad; la                                   preaplicados según 
                                 Coordinación                                   el contexto del    
                                 Académica ve                                   usuario.           
                                 estudiantes cargados,                                             
                                 aptos vs no aptos y                                               
                                 prácticas activas; el                                             
                                 Coordinador de                                                    
                                 Práctica ve tareas                                                
                                 pendientes (vacantes                                              
                                 por aprobar,                                                      
                                 asignaciones por                                                  
                                 gestionar, cierres                                                
                                 pendientes); la                                                   
                                 Empresa ve sus                                                    
                                 vacantes y                                                        
                                 estudiantes                                                       
                                 vinculados; el Tutor                                              
                                 Empresarial ve sus                                                
                                 estudiantes a cargo y                                             
                                 evaluaciones                                                      
                                 pendientes; el                                                    
                                 Docente Asesor ve sus                                             
                                 estudiantes asignados                                             
                                 y cortes pendientes;                                              
                                 el Estudiante ve el                                               
                                 estado actual de su                                               
                                 práctica, documentos                                              
                                 pendientes y                                                      
                                 calificaciones.                                                   

  RF-01-02      Tarjetas de      El panel muestra      Las tarjetas muestran    Los contadores se  ● Alta
                resumen con      tarjetas con          valor numérico           recalculan al      
                indicadores      contadores dinámicos  actualizado en tiempo    recargar el panel. 
                clave            según el rol. Para el real. Al hacer clic en   Cada tarjeta es un 
                                 Coordinador: vacantes una tarjeta, el sistema  acceso directo al  
                                 pendientes,           navega al listado        módulo             
                                 asignaciones          correspondiente con el   correspondiente    
                                 pendientes,           filtro preaplicado.      con el filtro del  
                                 estudiantes en        Contadores urgentes se   estado activo.     
                                 práctica, cierres     resaltan                                    
                                 pendientes. Para el   (rojo/amarillo/verde).                      
                                 Docente Asesor:                                                   
                                 estudiantes                                                       
                                 asignados,                                                        
                                 calificaciones                                                    
                                 pendientes. Para el                                               
                                 Tutor Empresarial:                                                
                                 practicantes a cargo,                                             
                                 evaluaciones por                                                  
                                 diligenciar. Para el                                              
                                 Estudiante: estado de                                             
                                 su práctica activa y                                              
                                 documentos                                                        
                                 pendientes.                                                       

  RF-01-03      Centro de        Ícono de campana en   El contador refleja solo No se generan      ● Media
                notificaciones y la barra superior con alertas activas no       alertas duplicadas 
                alertas          contador de alertas   leídas del usuario. Al   para el mismo      
                                 no leídas. Panel      resolver la acción       evento en el mismo 
                                 lateral cronológico   origen, la alerta se     día. Una alerta    
                                 con alertas agrupadas cierra automáticamente.  resuelta queda     
                                 por tipo:             Las alertas críticas     archivada con      
                                 asignaciones nuevas,  aparecen primero.        fecha de           
                                 vacantes por aprobar,                          resolución. Las    
                                 calificaciones                                 alertas respetan   
                                 pendientes,                                    el scope del rol.  
                                 documentos por                                                    
                                 diligenciar,                                                      
                                 encuestas de                                                      
                                 satisfacción                                                      
                                 pendientes, prácticas                                             
                                 próximas a cerrar.                                                
                                 Cada alerta incluye                                               
                                 descripción, fecha y                                              
                                 botón de acción                                                   
                                 directa al módulo                                                 
                                 origen.                                                           

  RF-01-04      Filtros globales Para los roles con    Al filtrar por facultad, Los roles con      ● Media
                persistentes por scope múltiple (DTI,  el selector de programa  scope único no ven 
                facultad y       Dirección,            muestra solo los         este filtro; el    
                programa         Coordinación          programas de esa         sistema aplica su  
                                 Académica), la barra  facultad. Todos los      scope              
                                 superior incluye      componentes del panel se automáticamente.   
                                 selectores de         recalculan sin recargar  El filtro global   
                                 Facultad y Programa   la página al cambiar el  se transmite al    
                                 que aplican a todos   filtro.                  navegar desde      
                                 los indicadores,                               cualquier tarjeta. 
                                 gráficos y tablas del                                             
                                 panel. La selección                                               
                                 se conserva durante                                               
                                 la sesión al navegar                                              
                                 entre módulos.                                                    
  ----------------------------------------------------------------------------------------------------------------

# MÓDULO 02 -- Gestión de Usuarios, Acceso y Configuración Estructural

  ----------------------------------------------------------------------------------------------------------
  **ID          **Nombre del     **Descripción del     **Criterio de      **Regla del        **Prioridad**
  Requisito**   Requisito**      Requisito**           Aceptación**       Negocio**          
  ------------- ---------------- --------------------- ------------------ ------------------ ---------------
  RF-02-01      Registro y       El DTI                El correo          No se puede        ● Alta
                gestión de       (Administrador) puede electrónico es     inactivar al único 
                usuarios por rol crear, editar,        único en el        DTI activo del     
                (DTI)            activar e inactivar   sistema. El        sistema. Cada rol  
                                 usuarios del sistema. usuario debe       solo puede ser     
                                 Al crear un usuario   cambiar la         asignado a los     
                                 se solicita: nombre   contraseña         scopes que le      
                                 completo, correo      temporal en el     corresponden. El   
                                 electrónico           primer inicio de   DTI es el único    
                                 (credencial única),   sesión. Un usuario que puede crear y  
                                 rol asignado (DTI,    inactivo no puede  gestionar todos    
                                 Dirección,            acceder al         los usuarios del   
                                 Coordinación          sistema; sus       sistema.           
                                 Académica,            registros se                          
                                 Coordinador de        conservan.                            
                                 Práctica, Docente                                           
                                 Asesor, Empresa                                             
                                 Formadora, Tutor                                            
                                 Empresarial,                                                
                                 Estudiante,                                                 
                                 Coordinador                                                 
                                 Académico) y scope                                          
                                 según el rol. El                                            
                                 sistema genera                                              
                                 contraseña temporal y                                       
                                 la envía al correo                                          
                                 del nuevo usuario.                                          

  RF-02-02      Autenticación y  El sistema valida     Un usuario solo    El scope se valida ● Alta
                control de       credenciales al       puede ver y operar en backend en cada 
                acceso por rol y iniciar sesión y      sobre la           petición, no solo  
                scope            redirige al panel     información        en la interfaz. Un 
                                 personalizado según   correspondiente a  Coordinador del    
                                 el rol. El acceso a   su scope. Los      Programa A no      
                                 módulos, vistas y     menús muestran     puede ver datos    
                                 acciones está         únicamente los     del Programa B     
                                 restringido según el  módulos permitidos bajo ninguna       
                                 rol y el scope del    para el rol.       circunstancia.     
                                 usuario (global,      Intentos de acceso                    
                                 facultad, programa,   no autorizado                         
                                 lista de asignados).  quedan registrados                    
                                 Las rutas no          en bitácora.                          
                                 autorizadas retornan                                        
                                 error 403 y no se                                           
                                 muestran en el menú.                                        

  RF-02-03      Recuperación de  El usuario puede      El enlace de       Si el correo no    ● Media
                contraseña       solicitar             restablecimiento   existe en el       
                                 restablecimiento de   expira en 24       sistema, se        
                                 contraseña desde la   horas. Solo se     muestra un mensaje 
                                 pantalla de login     envía al correo    neutro para no     
                                 ingresando su correo  exactamente        revelar            
                                 registrado. El        registrado en el   información de     
                                 sistema envía un      sistema. El enlace usuarios           
                                 enlace de             puede usarse una   registrados.       
                                 restablecimiento      sola vez.                             
                                 válido por 24 horas                                         
                                 al correo                                                   
                                 institucional.                                              

  RF-02-04      Bitácora de      El sistema registra   La bitácora es de  Si el sistema no   ● Media
                auditoría de     automáticamente todas solo lectura para  puede registrar    
                accesos y        las acciones          todos los roles.   una acción en la   
                acciones         críticas: inicio y    No puede           bitácora, debe     
                                 cierre de sesión,     modificarse ni     abortar la acción  
                                 intentos fallidos,    eliminarse. El DTI y notificar el     
                                 creación y            puede filtrarla y  error. Todas las   
                                 modificación de       exportarla.        acciones que       
                                 registros,                               modifiquen estado  
                                 aprobaciones,                            de prácticas,      
                                 rechazos,                                calificaciones,    
                                 calificaciones,                          documentos o       
                                 cierres y envíos de                      envíos de correo   
                                 correo. Cada entrada                     deben quedar       
                                 incluye: usuario,                        registradas.       
                                 fecha/hora, módulo,                                         
                                 tipo de acción,                                             
                                 registro afectado,                                          
                                 valores anteriores y                                        
                                 nuevos.                                                     

  RF-02-05      Gestión de       El DTI puede crear,   No se puede        La estructura      ● Alta
                facultades y     editar y desactivar   desactivar una     facultad →         
                programas        facultades y          facultad o         programa →         
                (configuración   programas académicos. programa con       catálogo de        
                estructural)     Por cada programa se  estudiantes o      prácticas es la    
                                 configura: nombre,    prácticas activas. base de todo el    
                                 facultad a la que     Al agregar un      scope del sistema. 
                                 pertenece, número     nuevo programa,    Cualquier cambio   
                                 total de prácticas,   queda disponible   estructural queda  
                                 número de cortes por  inmediatamente     registrado en la   
                                 práctica y requisitos para asignar       bitácora de        
                                 por número de         coordinadores y    auditoría.         
                                 práctica (créditos    cargar                                
                                 mínimos, promedio     estudiantes. Los                      
                                 mínimo, práctica      requisitos por                        
                                 anterior aprobada,    práctica son                          
                                 documentos            configurables sin                     
                                 requeridos).          tocar código.                         

  RF-02-06      Gestión del      La Coordinación       El catálogo puede  La Coordinación    ● Alta
                catálogo de      Académica administra  crearse sin tener  Académica solo     
                prácticas por la el catálogo           estudiantes        gestiona el        
                Coordinación     institucional de      asignados. El      catálogo de su     
                Académica        prácticas de forma    nombre y la        scope. El número   
                                 independiente. Por    materia núcleo son de práctica debe   
                                 cada práctica del     obligatorios. No   ser único por      
                                 catálogo se registra: se puede           programa. Al       
                                 número de práctica,   desactivar una     marcar un          
                                 nombre de la          práctica del       estudiante como    
                                 práctica, materia     catálogo que tenga Apto, el sistema   
                                 núcleo obligatoria    estudiantes        usa el catálogo    
                                 (nombre y código),    activos. Los       para crear         
                                 programa académico,   cambios en el      automáticamente la 
                                 número de cortes de   catálogo aplican   instancia          
                                 seguimiento, duración solo a nuevas      correspondiente.   
                                 estándar en semanas y asignaciones.                         
                                 documentos                                                  
                                 requeridos. El                                              
                                 catálogo constituye                                         
                                 la plantilla base                                           
                                 sobre la cual el                                            
                                 sistema crea                                                
                                 automáticamente                                             
                                 instancias                                                  
                                 individuales para                                           
                                 cada estudiante al                                          
                                 marcar aptitud.                                             

  RF-02-07      Clasificación de El DTI puede          La clasificación   Solo el DTI        ● Media
                empresas por     clasificar las        es configurable    gestiona la        
                industria,       empresas formadoras   sin tocar código.  clasificación      
                facultad y       según industria,      Una empresa puede  global de          
                programa (DTI)   facultad y programa   estar vinculada a  empresas. El       
                                 académico al que      múltiples          Coordinador de     
                                 están vinculadas, con programas. Los     Práctica puede     
                                 el fin de facilitar   filtros de         filtrar empresas   
                                 la generación de      clasificación      por la             
                                 reportes segmentados  están disponibles  clasificación      
                                 y la asignación de    en todos los       vigente dentro de  
                                 vacantes a los        reportes y         su scope.          
                                 programas             listados de                           
                                 correspondientes.     empresas.                             

  RF-02-08      Creación y       El DTI puede          Una facultad o     La creación de     ● Alta
                gestión de       registrar nuevas      programa nuevo     programas y        
                programas y      facultades y          queda disponible   facultades es      
                facultades       programas académicos  inmediatamente     exclusiva del DTI. 
                nuevas (DTI)     en el sistema,        tras su creación.  Cualquier cambio   
                                 definir los           No se puede        estructural queda  
                                 parámetros de cada    eliminar una       registrado en la   
                                 programa (semestres,  facultad o         bitácora de        
                                 número de prácticas,  programa con       auditoría.         
                                 materias núcleo) y    usuarios o                            
                                 habilitarlos para que prácticas                             
                                 la Coordinación       asociadas; solo                       
                                 Académica y los       desactivar.                           
                                 Coordinadores de                                            
                                 Práctica empiecen a                                         
                                 operar sobre ellos.                                         
  ----------------------------------------------------------------------------------------------------------

# MÓDULO 03 -- Gestión de Estudiantes

  -------------------------------------------------------------------------------------------------------
  **ID          **Nombre del   **Descripción del     **Criterio de       **Regla del      **Prioridad**
  Requisito**   Requisito**    Requisito**           Aceptación**        Negocio**        
  ------------- -------------- --------------------- ------------------- ---------------- ---------------
  RF-03-01      Registro       La Coordinación       El número de        La Coordinación  ● Alta
                individual de  Académica registra    identificación es   Académica solo   
                estudiantes    estudiantes con datos único en el         puede registrar  
                por            personales (nombre,   sistema. Todos los  estudiantes del  
                Coordinación   identificación,       campos obligatorios programa o       
                Académica      correo, teléfono,     deben completarse   facultad         
                               contacto de           antes de guardar.   asignada a su    
                               emergencia), datos    El estudiante queda scope. El        
                               académicos (programa, en estado Sin       Coordinador de   
                               facultad, semestre,   evaluar hasta que   Práctica no      
                               créditos aprobados,   la Coordinación     puede crear      
                               promedio acumulado) y Académica valide    estudiantes,     
                               documentos base (hoja sus requisitos.     solo verlos una  
                               de vida, paz y                            vez marcados     
                               salvo). Al crear el                       como Aptos.      
                               estudiante se genera                                       
                               automáticamente su                                         
                               expediente de                                              
                               prácticas vacío.                                           

  RF-03-02      Carga masiva   La Coordinación       La importación no   El archivo de    ● Media
                de estudiantes Académica puede       sobrescribe         importación no   
                desde Excel    importar estudiantes  identificaciones    puede superar 5  
                               desde un archivo      existentes; las     MB. Los          
                               .xlsx con plantilla   reporta como error. estudiantes      
                               descargable. El       Importaciones de    importados       
                               sistema valida fila   más de 50 registros quedan en estado 
                               por fila y muestra    se ejecutan en      Sin evaluar      
                               informe previo con    segundo plano con   hasta que se     
                               filas válidas (verde) notificación al     validen          
                               y errores (rojo con   terminar.           requisitos.      
                               descripción). El                                           
                               usuario puede                                              
                               importar solo las                                          
                               filas válidas o                                            
                               corregir y recargar.                                       

  RF-03-03      Validación de  La Coordinación       Un estudiante       Un estudiante no ● Alta
                requisitos,    Académica valida si   marcado Apto queda  puede ser        
                marcación de   un estudiante cumple  visible para el     marcado Apto     
                aptitud y      los requisitos        Coordinador de      para la Práctica 
                asignación     configurados para el  Práctica y la       N si la Práctica 
                automática de  número de práctica al instancia de        N-1 no está en   
                práctica       que aplica (créditos  práctica se crea    estado           
                               mínimos, promedio     automáticamente en  Completada. El   
                               mínimo, práctica      su expediente. Un   sistema debe     
                               anterior aprobada,    estudiante No apto  alertar si el    
                               documentos base       no puede iniciar    catálogo de la   
                               completos, paz y      proceso de          práctica no está 
                               salvo vigente) y le   práctica. La        configurado.     
                               asigna el estado Apto validación y la                      
                               o No apto. Al         creación quedan                      
                               confirmar la aptitud, registradas con                      
                               el sistema crea       fecha y usuario.                     
                               automáticamente la                                         
                               instancia de práctica                                      
                               correspondiente a                                          
                               partir del catálogo                                        
                               de prácticas,                                              
                               precargando nombre,                                        
                               materia núcleo,                                            
                               cortes, duración y                                         
                               documentos                                                 
                               requeridos.                                                

  RF-03-04      Expediente     Cada estudiante tiene El expediente es de El expediente    ● Alta
                histórico del  un expediente que     solo lectura para   nunca se         
                estudiante     agrupa: datos         las prácticas       elimina. Una     
                               personales y          cerradas. La        práctica cerrada 
                               académicos,           práctica activa es  no puede ser     
                               documentos base, y el editable por los    modificada salvo 
                               historial completo de roles               solicitud formal 
                               todas sus prácticas.  correspondientes.   con              
                               Cada práctica muestra Cualquier rol con   justificación    
                               su nombre, materia    acceso puede ver el registrada       
                               núcleo, estado,       historial completo  aprobada por el  
                               empresa asignada,     sin modificarlo.    Coordinador.     
                               docente asesor, tutor                                      
                               empresarial,                                               
                               documentos,                                                
                               calificaciones y                                           
                               línea de tiempo de                                         
                               hitos.                                                     

  RF-03-05      Listado de     Vista tabular con     Los filtros son     Cada rol ve      ● Alta
                estudiantes    todos los estudiantes acumulables. La     únicamente los   
                con filtros    según el scope del    tabla muestra el    estudiantes de   
                avanzados      usuario. Columnas:    total de resultados su scope.        
                               nombre,               encontrados. El                      
                               identificación,       ordenamiento por                     
                               programa, semestre,   columna opera sobre                  
                               número y nombre de    todos los registros                  
                               práctica actual,      filtrados, no solo                   
                               estado de aptitud,    la página visible.                   
                               estado de práctica                                         
                               activa. Filtros:                                           
                               programa, facultad,                                        
                               semestre, estado de                                        
                               aptitud, número de                                         
                               práctica, estado de                                        
                               práctica, empresa                                          
                               asignada, docente                                          
                               asesor.                                                    

  RF-03-06      Habilitación y Al ejecutarse el      La notificación a   Si el estudiante ● Alta
                asignación     cierre exitoso de la  la Coordinación     reprueba la      
                automática de  Práctica N, el        Académica se envía  Práctica N, no   
                la práctica    sistema actualiza     por correo y        se habilita ni   
                siguiente      automáticamente el    aparece en el panel crea la          
                               expediente a Práctica de inicio. El       siguiente hasta  
                               N Completada y        número de práctica  que la           
                               notifica a la         habilitado nunca    Coordinación     
                               Coordinación          puede superar el    Académica tome   
                               Académica para que    máximo configurado  una decisión     
                               evalúe los requisitos para el programa.   explícita.       
                               de la Práctica N+1.                                        
                               Cuando la                                                  
                               Coordinación                                               
                               Académica valida y                                         
                               marca al estudiante                                        
                               como Apto para la                                          
                               N+1, el sistema crea                                       
                               automáticamente la                                         
                               instancia desde el                                         
                               catálogo.                                                  

  RF-03-07      Formulario de  El estudiante         El formulario es de La hoja de vida  ● Alta
                hoja de vida   completa un           fácil               debe estar       
                del estudiante formulario de hoja de diligenciamiento.   Aprobada para    
                               vida en la plataforma El estado de la     que el           
                               con sus datos         hoja de vida es     estudiante sea   
                               académicos,           visible para el     elegible como    
                               experiencias,         Coordinador al      candidato en una 
                               habilidades y         momento de          asignación. El   
                               documentos adjuntos.  gestionar           Coordinador      
                               Dependiendo de la     asignaciones. El    puede rechazar   
                               completitud del       estudiante no puede la hoja de vida  
                               formulario, la hoja   postularse a        con              
                               de vida queda en      vacantes sin hoja   observaciones.   
                               estado Aprobada o     de vida en estado                    
                               Pendiente de revisión Aprobada.                            
                               por el Coordinador.                                        

  RF-03-08      Generación de  El sistema genera     El paz y salvo solo El paz y salvo   ● Alta
                paz y salvo    automáticamente el    se genera si ambas  es un requisito  
                (Tutor         paz y salvo del       encuestas están     para que el      
                Empresarial y  estudiante una vez    completas. La       estudiante pueda 
                Estudiante)    que tanto el Tutor    generación queda    ser marcado Apto 
                               Empresarial como el   registrada con      en la siguiente  
                               Estudiante hayan      fecha y usuario     práctica. El     
                               completado sus        responsable.        sistema notifica 
                               encuestas de                              al estudiante y  
                               satisfacción de                           al Coordinador   
                               práctica. La                              cuando el paz y  
                               generación del paz y                      salvo es         
                               salvo cambia el                           generado.        
                               estado del estudiante                                      
                               a Disponible para                                          
                               nueva práctica.                                            
  -------------------------------------------------------------------------------------------------------

# MÓDULO 04 -- Gestión de Empresas y Vacantes

  ------------------------------------------------------------------------------------------------------------------
  **ID          **Nombre del     **Descripción del Requisito**  **Criterio de      **Regla del       **Prioridad**
  Requisito**   Requisito**                                     Aceptación**       Negocio**         
  ------------- ---------------- ------------------------------ ------------------ ----------------- ---------------
  RF-04-01      Registro de      El Coordinador de Práctica     El NIT es único en Solo las empresas ● Alta
                empresas         puede registrar empresas       el sistema. No se  en estado         
                formadoras       formadoras con: NIT, razón     puede eliminar una Aprobada pueden   
                                 social, sector económico,      empresa con        publicar vacantes 
                                 dirección, municipio, teléfono vacantes activas o y recibir         
                                 y nombre del contacto          estudiantes        practicantes. El  
                                 principal. Una empresa puede   vinculados; solo   Coordinador puede 
                                 estar vinculada a múltiples    desactivar.        editar datos de   
                                 programas académicos. Las      Empresa inactiva   cualquier empresa 
                                 empresas deben ser aprobadas   no aparece en      de su programa.   
                                 por la Coordinación antes de   selectores de                        
                                 poder publicar vacantes.       nuevos registros.                    

  RF-04-02      Validación y     Cuando se registra una nueva   El Coordinador     Una empresa       ● Alta
                aprobación de    empresa formadora o cuando el  debe registrar un  rechazada puede   
                empresa          Tutor Empresarial carga sus    motivo al rechazar corregir su       
                formadora        datos y documentos, la         una empresa. La    información y     
                (Coordinación)   Coordinación de Prácticas      empresa recibe     solicitar nueva   
                                 revisa y aprueba o rechaza el  notificación       revisión. Una     
                                 registro. Solo las empresas    automática por     empresa Inactiva  
                                 aprobadas pueden participar en correo del         no puede          
                                 el proceso de prácticas y      resultado de la    vincularse a      
                                 tener vacantes activas.        revisión. El       nuevos            
                                                                historial de       estudiantes. El   
                                                                cambios de estado  registro de       
                                                                de la empresa      empresas y su     
                                                                queda registrado.  historial nunca   
                                                                                   se elimina.       

  RF-04-03      Registro y       Por cada empresa se pueden     Un Tutor           La gestión de     ● Alta
                gestión de       registrar uno o varios Tutores Empresarial solo   tutores puede     
                Tutores          Empresariales con: nombre      puede pertenecer a realizarla el     
                Empresariales    completo, cargo, correo        una empresa. Al    DTI, el           
                                 electrónico y teléfono. El     desactivar una     Coordinador de    
                                 Tutor Empresarial recibe       empresa, sus       Práctica o la     
                                 acceso al sistema con su       tutores quedan     misma Empresa     
                                 correo como credencial, con    inactivos          desde su portal.  
                                 scope limitado a los           automáticamente.                     
                                 estudiantes que le sean        El Tutor                             
                                 asignados.                     Empresarial no                       
                                                                puede ver                            
                                                                información de                       
                                                                otros tutores ni                     
                                                                de otras empresas.                   

  RF-04-04      Creación y       El Tutor Empresarial o el      La vacante en      Una empresa no    ● Alta
                publicación de   Coordinador crean vacantes     estado Pendiente   puede publicar    
                vacantes (Tutor  con: cargo, descripción del    no está disponible vacantes si está  
                Empresarial y    perfil, requisitos del         para procesos de   Inactiva o        
                Coordinación)    estudiante, número de cupos,   asignación. La     Rechazada. El     
                                 área de la empresa, modalidad  empresa puede      número de cupos   
                                 (presencial/remoto/híbrido),   editar o eliminar  debe ser entero   
                                 programa académico al que      una vacante        positivo mayor a  
                                 aplica y fechas de             mientras esté en   cero.             
                                 disponibilidad. Al guardar, la estado Pendiente o                   
                                 vacante queda en estado        Rechazada.                           
                                 Pendiente de aprobación.                                            

  RF-04-05      Aprobación o     El Coordinador de Práctica     El Coordinador     Solo el           ● Alta
                rechazo de       revisa las vacantes en estado  debe registrar un  Coordinador de    
                vacantes por el  Pendiente de aprobación. Puede motivo al rechazar Práctica tiene    
                Coordinador      aprobarlas, rechazarlas con    una vacante. La    poder de          
                                 motivo obligatorio o solicitar empresa recibe     aprobación final. 
                                 ajustes a la empresa. Al       notificación       Una vacante       
                                 aprobar, la vacante pasa a     automática por     aprobada puede    
                                 estado Activa y queda          correo del         pausarse o        
                                 disponible para la asignación  resultado de la    cerrarse          
                                 de estudiantes aptos.          revisión. El       posteriormente.   
                                                                historial de                         
                                                                cambios de estado                    
                                                                de la vacante                        
                                                                queda registrado.                    

  RF-04-06      Gestión del      Las vacantes tienen estados:   El Coordinador     Una vacante no    ● Alta
                ciclo de vida de Borrador → Pendiente de        solo accede a      puede volver a    
                la vacante       aprobación → Activa → Pausada  vacantes Activas   estado Activa     
                                 → Cerrada. El Coordinador      al gestionar       desde Cerrada. Al 
                                 puede pausar una vacante       asignaciones. Un   alcanzar el       
                                 activa o cerrarla. La empresa  cambio en una      número máximo de  
                                 puede editar una vacante       vacante activa     cupos ocupados,   
                                 activa, pero los cambios       genera nueva       la vacante pasa   
                                 vuelven a estado Pendiente de  revisión del       automáticamente a 
                                 aprobación.                    Coordinador. Al    Cupos completos.  
                                                                cerrarse una                         
                                                                vacante, las                         
                                                                asignaciones                         
                                                                pendientes se                        
                                                                notifican                            
                                                                automáticamente.                     

  RF-04-07      Listado de       Vista tabular con todas las    La Empresa ve solo Los indicadores   ● Alta
                vacantes con     vacantes según el scope del    sus propias        visuales de       
                filtros          usuario. Columnas: cargo,      vacantes. El       estado usan       
                                 empresa, área, modalidad,      Coordinador ve     colores           
                                 programa, cupos                todas las vacantes consistentes:     
                                 disponibles/totales, número de de su programa. Al Pendiente         
                                 asignaciones, estado. Filtros: gestionar          (amarillo),       
                                 empresa, programa, área,       asignaciones, el   Activa (verde),   
                                 modalidad, estado. Búsqueda    Coordinador        Pausada (gris),   
                                 por texto libre sobre cargo y  visualiza          Cerrada (rojo     
                                 empresa.                       únicamente las     oscuro).          
                                                                vacantes Activas                     
                                                                con cupos                            
                                                                disponibles.                         
  ------------------------------------------------------------------------------------------------------------------

# MÓDULO 05 -- Asignación de Estudiantes a Vacantes

+-------------+----------------+------------------+----------------+----------------------+---------------+
| **ID        | **Nombre del   | **Descripción    | **Criterio de  | **Regla del          | **Prioridad** |
| Requisito** | Requisito**    | del Requisito**  | Aceptación**   | Negocio**            |               |
+=============+================+==================+================+======================+===============+
| RF-05-01    | Asignación de  | El Coordinador   | Solo aparecen  | Solo el Coordinador  | ● Alta        |
|             | estudiantes a  | es el único      | como           | puede confirmar      |               |
|             | vacantes       | responsable de   | candidatos los | asignaciones. Un     |               |
|             | activas por el | asignar          | estudiantes en | estudiante no puede  |               |
|             | Coordinador    | estudiantes a    | estado Apto y  | ser asignado a más   |               |
|             |                | vacantes         | sin práctica   | de una vacante       |               |
|             |                | activas. El      | activa. La     | activa               |               |
|             |                | proceso es: (1)  | asignación     | simultáneamente. La  |               |
|             |                | el Coordinador   | queda          | asignación descuenta |               |
|             |                | selecciona una   | registrada con | automáticamente del  |               |
|             |                | vacante activa   | fecha, usuario | cupo disponible.     |               |
|             |                | con cupos        | responsable y  |                      |               |
|             |                | disponibles; (2) | nota opcional. | El Coordinador puede |               |
|             |                | consulta el      | La             | cambiar el estado de |               |
|             |                | listado de       | notificación   | cada                 |               |
|             |                | estudiantes      | por correo se  | postulado/asignación |               |
|             |                | Aptos sin        | envía en menos | a Aprobado o         |               |
|             |                | práctica activa; | de 5 minutos.  | Rechazado de forma   |               |
|             |                | (3) revisa el    |                | individual. Al       |               |
|             |                | perfil y hoja de |                | rechazar, debe       |               |
|             |                | vida del         |                | registrar un motivo  |               |
|             |                | candidato; (4)   |                | obligatorio. El      |               |
|             |                | confirma la      |                | cambio de estado     |               |
|             |                | asignación       |                | notifica             |               |
|             |                | opcionalmente    |                | automáticamente al   |               |
|             |                | con una nota de  |                | estudiante por       |               |
|             |                | justificación.   |                | correo y en su panel |               |
|             |                | El sistema       |                | con el resultado y   |               |
|             |                | registra la      |                | el motivo en caso de |               |
|             |                | asignación,      |                | rechazo.             |               |
|             |                | decrementa el    |                |                      |               |
|             |                | cupo disponible  |                |                      |               |
|             |                | y notifica       |                |                      |               |
|             |                | automáticamente  |                |                      |               |
|             |                | por correo y en  |                |                      |               |
|             |                | el panel al      |                |                      |               |
|             |                | estudiante       |                |                      |               |
|             |                | asignado, a la   |                |                      |               |
|             |                | empresa y al     |                |                      |               |
|             |                | Tutor            |                |                      |               |
|             |                | Empresarial si   |                |                      |               |
|             |                | ya está          |                |                      |               |
|             |                | designado.       |                |                      |               |
+-------------+----------------+------------------+----------------+----------------------+---------------+
| RF-05-02    | Gestión y      | El Coordinador   | El listado     | Una asignación no    | ● Alta        |
|             | seguimiento de | visualiza el     | permite        | puede cancelarse una |               |
|             | asignaciones   | listado completo | filtrar por    | vez iniciado         |               |
|             | activas        | de asignaciones  | estado,        | formalmente el       |               |
|             |                | activas de su    | empresa,       | proceso de           |               |
|             |                | programa con     | vacante,       | vinculación. Si la   |               |
|             |                | columnas:        | número de      | empresa rechaza al   |               |
|             |                | estudiante,      | práctica y     | candidato, el        |               |
|             |                | número y nombre  | fechas. El     | Coordinador puede    |               |
|             |                | de práctica,     | Coordinador    | cancelar con motivo  |               |
|             |                | vacante,         | puede acceder  | Rechazo empresa.     |               |
|             |                | empresa, fecha   | directamente   |                      |               |
|             |                | de asignación,   | al expediente  |                      |               |
|             |                | estado y último  | del estudiante |                      |               |
|             |                | cambio. Puede    | desde el       |                      |               |
|             |                | cancelar una     | listado. El    |                      |               |
|             |                | asignación antes | historial      |                      |               |
|             |                | de que se active | completo de    |                      |               |
|             |                | el proceso de    | cambios es     |                      |               |
|             |                | vinculación,     | visible para   |                      |               |
|             |                | registrando      | los roles      |                      |               |
|             |                | obligatoriamente | autorizados.   |                      |               |
|             |                | el motivo.       |                |                      |               |
+-------------+----------------+------------------+----------------+----------------------+---------------+
| RF-05-03    | Estados y      | Cada asignación  | El estudiante  | Una asignación en    | ● Alta        |
|             | trazabilidad   | tiene un flujo   | puede          | estado Vinculada no  |               |
|             | completa de    | de estados:      | consultar el   | puede volver a       |               |
|             | asignaciones   | Asignada → En    | estado de su   | estados anteriores.  |               |
|             |                | proceso de       | asignación en  | Si se cancela una    |               |
|             |                | vinculación →    | tiempo real    | asignación, el       |               |
|             |                | Vinculada        | desde su       | motivo es            |               |
|             |                | (convenio        | panel. Los     | obligatorio.         |               |
|             |                | firmado) /       | cambios de     |                      |               |
|             |                | Cancelada. El    | estado quedan  |                      |               |
|             |                | sistema registra | registrados en |                      |               |
|             |                | la fecha, el     | la bitácora de |                      |               |
|             |                | usuario          | auditoría. Las |                      |               |
|             |                | responsable y el | notificaciones |                      |               |
|             |                | motivo de cada   | incluyen el    |                      |               |
|             |                | cambio de        | estado actual, |                      |               |
|             |                | estado. Cada     | la descripción |                      |               |
|             |                | transición       | del cambio y   |                      |               |
|             |                | genera una       | el nombre del  |                      |               |
|             |                | notificación     | responsable.   |                      |               |
|             |                | automática por   |                |                      |               |
|             |                | correo a todos   |                |                      |               |
|             |                | los actores      |                |                      |               |
|             |                | involucrados.    |                |                      |               |
+-------------+----------------+------------------+----------------+----------------------+---------------+
| RF-05-04    | Notificaciones | El sistema envía | El correo se   | Las notificaciones   | ● Alta        |
|             | automáticas de | notificaciones   | envía en menos | respetan el scope    |               |
|             | asignación por | por correo ante: | de 5 minutos   | del rol receptor.    |               |
|             | correo         | (1) nueva        | tras el        | Las plantillas son   |               |
|             | electrónico    | asignación       | evento. En     | configurables por el |               |
|             |                | confirmada:      | caso de fallo, | DTI (RF-11-05).      |               |
|             |                | notifica al      | el sistema     | Todos los envíos     |               |
|             |                | estudiante, a la | reintenta      | quedan registrados   |               |
|             |                | empresa y al     | hasta 3 veces  | en la bitácora.      |               |
|             |                | Tutor            | con intervalos |                      |               |
|             |                | Empresarial; (2) | de 2 minutos.  |                      |               |
|             |                | cancelación:     | El Coordinador |                      |               |
|             |                | notifica al      | puede          |                      |               |
|             |                | estudiante y a   | consultar el   |                      |               |
|             |                | la empresa con   | historial de   |                      |               |
|             |                | el motivo; (3)   | envíos desde   |                      |               |
|             |                | cambio de        | el expediente. |                      |               |
|             |                | estado: notifica |                |                      |               |
|             |                | a todos los      |                |                      |               |
|             |                | actores. El      |                |                      |               |
|             |                | correo incluye:  |                |                      |               |
|             |                | nombre del       |                |                      |               |
|             |                | estudiante,      |                |                      |               |
|             |                | número y nombre  |                |                      |               |
|             |                | de la práctica,  |                |                      |               |
|             |                | cargo y empresa, |                |                      |               |
|             |                | estado actual y  |                |                      |               |
|             |                | enlace directo   |                |                      |               |
|             |                | al expediente.   |                |                      |               |
+-------------+----------------+------------------+----------------+----------------------+---------------+

# MÓDULO 06 -- Vinculación y Documentos

  -----------------------------------------------------------------------------------------------------------
  **ID          **Nombre del     **Descripción del **Criterio de           **Regla del        **Prioridad**
  Requisito**   Requisito**      Requisito**       Aceptación**            Negocio**          
  ------------- ---------------- ----------------- ----------------------- ------------------ ---------------
  RF-06-01      Carga de carta   Al confirmarse la El sistema acepta       No se puede        ● Alta
                de presentación  asignación de un  archivos PDF, JPG o PNG confirmar el       
                                 estudiante a una  con tamaño máximo de 10 proceso de         
                                 vacante, el       MB. El repositorio      vinculación sin    
                                 Coordinador carga indica si la carta ha   carta de           
                                 la carta de       sido cargada o está     presentación       
                                 presentación del  pendiente. El documento cargada. El        
                                 estudiante        queda registrado con    documento queda    
                                 dirigida a la     nombre, fecha de carga  archivado con      
                                 empresa. El       y usuario responsable.  fecha y autor. Una 
                                 documento se                              práctica cerrada   
                                 adjunta al                                no permite         
                                 expediente en el                          reemplazar ni      
                                 repositorio de                            eliminar los       
                                 documentos de                             documentos de      
                                 vinculación y                             vinculación.       
                                 queda disponible                                             
                                 para                                                         
                                 visualización y                                              
                                 descarga por los                                             
                                 roles                                                        
                                 autorizados.                                                 

  RF-06-02      Carga y registro El Coordinador    El convenio no puede    Al confirmarse las ● Alta
                del convenio de  carga el convenio considerarse vigente    tres firmas y el   
                práctica (plan   de práctica. El   hasta que las tres      plan aprobado, el  
                de práctica)     Estudiante carga  firmas estén            sistema activa     
                                 su plan de        confirmadas. El Tutor   automáticamente el 
                                 práctica con      Empresarial puede       estado En práctica 
                                 objetivos y       confirmar su firma      para el estudiante 
                                 cronograma. El    desde su portal. El     y registra la      
                                 plan debe ser     plan de práctica no     fecha oficial de   
                                 aprobado primero  puede avanzar si está   inicio. El         
                                 por el Tutor      en estado Pendiente de  convenio queda     
                                 Empresarial y     aprobación.             archivado de forma 
                                 luego por el                              inmutable.         
                                 Docente Asesor                                               
                                 antes de iniciar                                             
                                 los seguimientos.                                            
                                 El sistema                                                   
                                 registra las                                                 
                                 firmas                                                       
                                 requeridas:                                                  
                                 Coordinador,                                                 
                                 Tutor Empresarial                                            
                                 y Estudiante.                                                

  RF-06-03      Confirmación de  Al completarse el La fecha de fin no      No se puede        ● Alta
                vinculación y    convenio firmado  puede ser anterior a la activar una        
                activación de    y el plan de      fecha de inicio. La     práctica sin       
                práctica         práctica          práctica activada queda convenio cargado,  
                                 aprobado, el      visible en el tablero   plan aprobado y    
                                 Coordinador       de seguimiento del      firmado por los    
                                 confirma la       Coordinador y del       tres actores. La   
                                 vinculación       Docente Asesor.         fecha de inicio es 
                                 ingresando las                            la referencia para 
                                 fechas oficiales                          calcular los       
                                 de inicio y fin.                          cortes de          
                                 El sistema activa                         evaluación.        
                                 el estado En                                                 
                                 práctica para el                                             
                                 estudiante,                                                  
                                 asigna el Docente                                            
                                 Asesor y notifica                                            
                                 a todos los                                                  
                                 actores por                                                  
                                 correo y en el                                               
                                 panel.                                                       

  RF-06-04      Repositorio de   Cada práctica     Los documentos pueden   Los documentos de  ● Alta
                documentos por   tiene un          ser visualizados y      una práctica       
                práctica         repositorio de    descargados por los     cerrada son        
                                 documentos        roles autorizados. El   inmutables. El     
                                 organizado por    repositorio muestra el  tamaño máximo por  
                                 categorías:       estado de cada          archivo es de 10   
                                 Documentos de     categoría               MB. Se aceptan     
                                 vinculación       (completo/pendiente).   formatos PDF, JPG  
                                 (cartas,          El Estudiante puede     y PNG. Cada actor  
                                 convenios, plan   descargar únicamente    solo puede ver,    
                                 de práctica),     los documentos que le   cargar o descargar 
                                 Documentos de     corresponden.           los documentos que 
                                 seguimiento                               le corresponden    
                                 (informes por                             según su rol y la  
                                 corte),                                   etapa actual de la 
                                 Evaluaciones y                            práctica; un actor 
                                 encuestas, Acta                           no puede acceder a 
                                 de cierre. Por                            documentos de      
                                 cada documento se                         etapas en las que  
                                 registra: nombre,                         no participa ni a  
                                 fecha de carga,                           documentos de      
                                 quién lo cargó y                          otros actores      
                                 estado.                                   fuera de su scope. 

  RF-06-05      Asignación de    Una vez creada la El Docente Asesor debe  Solo el            ● Alta
                Docente Asesor a práctica, el      estar disponible        Coordinador puede  
                la práctica      Coordinador       (activo en el sistema)  realizar la        
                (Coordinación)   asigna el Docente para ser asignado. La   asignación del     
                                 Asesor            asignación queda        Docente Asesor. Un 
                                 universitario y   registrada con fecha y  Docente Asesor     
                                 el Tutor          usuario responsable.    puede tener        
                                 Empresarial que                           múltiples          
                                 acompañarán al                            estudiantes        
                                 estudiante                                asignados          
                                 durante todo el                           simultáneamente    
                                 proceso. El                               sin límite         
                                 Docente Asesor                            superior           
                                 asignado puede                            configurado.       
                                 acceder al                                                   
                                 expediente del                                               
                                 estudiante y                                                 
                                 registrar                                                    
                                 seguimientos y                                               
                                 calificaciones.                                              

  RF-06-06      Asignación de    Una vez que la    El Tutor Empresarial    Solo el            ● Alta
                Tutor            empresa define    debe pertenecer a una   Coordinador puede  
                Empresarial a la quién será el     empresa en estado       confirmar          
                práctica         tutor empresarial Aprobada. La asignación formalmente la     
                (Coordinación)   responsable del   queda registrada con    asignación del     
                                 practicante, el   fecha y usuario         Tutor Empresarial. 
                                 Coordinador       responsable.            Un Tutor puede     
                                 registra la                               tener múltiples    
                                 asignación formal                         practicantes a     
                                 en el sistema. El                         cargo              
                                 Tutor Empresarial                         simultáneamente.   
                                 asignado puede                                               
                                 acceder al portal                                            
                                 con scope                                                    
                                 limitado a sus                                               
                                 estudiantes.                                                 
  -----------------------------------------------------------------------------------------------------------

# MÓDULO 07 -- Seguimiento a la Práctica

+-------------+---------------+---------------------+------------------+----------------+---------------+
| **ID        | **Nombre del  | **Descripción del   | **Criterio de    | **Regla del    | **Prioridad** |
| Requisito** | Requisito**   | Requisito**         | Aceptación**     | Negocio**      |               |
+=============+===============+=====================+==================+================+===============+
| RF-07-01    | Tablero de    | Vista tabular con   | El tablero       | Un estudiante  | ● Alta        |
|             | seguimiento   | todos los           | muestra en       | pasa a estado  |               |
|             | general       | estudiantes en      | tiempo real el   | En alerta si   |               |
|             | (Coordinador) | práctica activa del | estado de cada   | no tiene       |               |
|             |               | programa. Columnas: | estudiante. Los  | registro de    |               |
|             |               | nombre del          | indicadores      | actividad en   |               |
|             |               | estudiante,         | visuales de      | más de N días  |               |
|             |               | empresa, docente    | alerta (rojo,    | hábiles, donde |               |
|             |               | asesor asignado,    | amarillo, verde) | N es           |               |
|             |               | corte actual,       | permiten         | configurable   |               |
|             |               | estado del          | identificar      | por el         |               |
|             |               | seguimiento (Al día | situaciones      | Coordinador.   |               |
|             |               | / Pendiente / En    | urgentes. Desde  |                |               |
|             |               | alerta), último     | cada fila se     |                |               |
|             |               | registro de         | puede acceder    |                |               |
|             |               | actividad. Filtros: | directamente al  |                |               |
|             |               | empresa, docente    | expediente del   |                |               |
|             |               | asesor, corte       | estudiante.      |                |               |
|             |               | actual, estado de   |                  |                |               |
|             |               | seguimiento.        |                  |                |               |
+-------------+---------------+---------------------+------------------+----------------+---------------+
| RF-07-02    | Registro de   | Cada semana el      | El estudiante    | El plan de     | ● Alta        |
|             | seguimientos  | estudiante registra | puede registrar  | práctica debe  |               |
|             | semanales por | las actividades     | múltiples        | estar en       |               |
|             | el Estudiante | realizadas, logros, | evidencias por   | estado         |               |
|             |               | dificultades y      | seguimiento. No  | Aprobado por   |               |
|             |               | evidencias en el    | puede editar     | el Docente     |               |
|             |               | sistema. Este       | seguimientos de  | Asesor antes   |               |
|             |               | registro            | semanas          | de que el      |               |
|             |               | corresponde al      | anteriores ya    | estudiante     |               |
|             |               | seguimiento semanal | calificados. El  | pueda iniciar  |               |
|             |               | de la práctica.     | sistema indica   | los registros  |               |
|             |               | Solo se puede crear | la semana actual | semanales. El  |               |
|             |               | un seguimiento sin  | y el estado del  | número de      |               |
|             |               | plan de práctica    | seguimiento.     | semana es      |               |
|             |               | aprobado.           |                  | positivo y     |               |
|             |               |                     |                  | único dentro   |               |
|             |               |                     |                  | de la          |               |
|             |               |                     |                  | práctica.      |               |
+-------------+---------------+---------------------+------------------+----------------+---------------+
| RF-07-03    | Calificación  | El Docente Asesor   | La calificación  | El Docente     | ● Alta        |
|             | de            | revisa y califica   | determina        | Asesor solo    |               |
|             | seguimientos  | cada seguimiento    | automáticamente  | puede          |               |
|             | por el        | semanal del         | el estado del    | calificar      |               |
|             | Docente       | estudiante. La      | seguimiento. El  | seguimientos   |               |
|             | Asesor        | calificación es en  | Docente Asesor   | de sus propios |               |
|             |               | escala 0.0 a 5.0.   | puede registrar  | estudiantes    |               |
|             |               | El seguimiento pasa | observaciones    | asignados. Los |               |
|             |               | a estado APROBADO o | junto a la       | seguimientos   |               |
|             |               | RECHAZADO según la  | calificación. El | de semanas     |               |
|             |               | nota asignada. Si   | estudiante puede | anteriores ya  |               |
|             |               | es rechazado, el    | editar solo el   | calificados no |               |
|             |               | estudiante puede    | seguimiento      | pueden ser     |               |
|             |               | editar y reenviar   | rechazado más    | modificados.   |               |
|             |               | únicamente el       | reciente.        |                |               |
|             |               | seguimiento de la   |                  |                |               |
|             |               | semana más          |                  |                |               |
|             |               | reciente.           |                  |                |               |
|             |               |                     |                  |                |               |
|             |               | Una vez que el      |                  |                |               |
|             |               | estudiante edita el |                  |                |               |
|             |               | seguimiento         |                  |                |               |
|             |               | rechazado, este     |                  |                |               |
|             |               | vuelve a estado     |                  |                |               |
|             |               | Pendiente de        |                  |                |               |
|             |               | calificación y el   |                  |                |               |
|             |               | Docente Asesor      |                  |                |               |
|             |               | recibe una          |                  |                |               |
|             |               | notificación para   |                  |                |               |
|             |               | revisarlo           |                  |                |               |
|             |               | nuevamente. Este    |                  |                |               |
|             |               | ciclo de edición y  |                  |                |               |
|             |               | reenvío puede       |                  |                |               |
|             |               | repetirse hasta que |                  |                |               |
|             |               | el seguimiento      |                  |                |               |
|             |               | quede en estado     |                  |                |               |
|             |               | APROBADO.           |                  |                |               |
+-------------+---------------+---------------------+------------------+----------------+---------------+
| RF-07-04    | Registro de   | El Docente Asesor   | Las              | El Docente     | ● Alta        |
|             | observaciones | puede registrar     | observaciones    | Asesor solo    |               |
|             | y avances por | observaciones,      | quedan           | puede          |               |
|             | el Docente    | comentarios de      | registradas con  | registrar      |               |
|             | Asesor        | seguimiento y       | fecha y autor.   | observaciones  |               |
|             |               | novedades sobre     | El Coordinador   | sobre          |               |
|             |               | cada estudiante     | puede ver todas  | estudiantes    |               |
|             |               | asignado,           | las              | que le hayan   |               |
|             |               | organizadas por     | observaciones de | sido           |               |
|             |               | corte de            | sus estudiantes. | asignados. No  |               |
|             |               | evaluación. Puede   | El estudiante    | puede          |               |
|             |               | también cargar      | puede ver las    | modificar      |               |
|             |               | documentos de       | observaciones    | observaciones  |               |
|             |               | seguimiento         | que el Docente   | de cortes ya   |               |
|             |               | (informes de        | Asesor marque    | cerrados por   |               |
|             |               | visita, actas) al   | como visibles    | el             |               |
|             |               | expediente del      | para él.         | Coordinador.   |               |
|             |               | estudiante.         |                  |                |               |
+-------------+---------------+---------------------+------------------+----------------+---------------+
| RF-07-05    | Registro de   | El Tutor            | El Tutor         | El seguimiento | ● Alta        |
|             | avances y     | Empresarial         | Empresarial solo | del Tutor      |               |
|             | desempeño por | registra el avance  | puede registrar  | Empresarial es |               |
|             | el Tutor      | y desempeño del     | avances de sus   | independiente  |               |
|             | Empresarial   | practicante desde   | estudiantes      | al del Docente |               |
|             |               | su portal, por      | asignados. Los   | Asesor. Ambos  |               |
|             |               | corte de            | registros son    | aportan        |               |
|             |               | evaluación. Puede   | visibles para el | información    |               |
|             |               | indicar logros,     | Coordinador y el | complementaria |               |
|             |               | dificultades y      | Docente Asesor.  | sobre el mismo |               |
|             |               | observaciones       | El Tutor puede   | estudiante.    |               |
|             |               | generales.          | editar registros |                |               |
|             |               |                     | del corte activo |                |               |
|             |               |                     | hasta que el     |                |               |
|             |               |                     | Coordinador      |                |               |
|             |               |                     | cierre el corte. |                |               |
+-------------+---------------+---------------------+------------------+----------------+---------------+
| RF-07-06    | Alertas       | El sistema evalúa   | El umbral de     | Las alertas de | ● Media       |
|             | automáticas   | diariamente si cada | días sin         | inactividad    |               |
|             | de            | estudiante en       | actividad es     | son visibles   |               |
|             | inactividad   | práctica activa     | configurable por | únicamente     |               |
|             |               | tiene actividad     | programa. No se  | para el        |               |
|             |               | registrada en los   | generan alertas  | Coordinador y  |               |
|             |               | últimos N días      | duplicadas por   | el Docente     |               |
|             |               | hábiles. Al superar | el mismo         | Asesor del     |               |
|             |               | el umbral           | estudiante en el | estudiante     |               |
|             |               | configurado, genera | mismo día. Al    | afectado, no   |               |
|             |               | una alerta en el    | registrarse      | para el        |               |
|             |               | panel y envía un    | nueva actividad, | estudiante.    |               |
|             |               | correo electrónico  | la alerta se     |                |               |
|             |               | al Coordinador y al | cierra           |                |               |
|             |               | Docente Asesor con  | automáticamente. |                |               |
|             |               | el nombre del       |                  |                |               |
|             |               | estudiante, empresa |                  |                |               |
|             |               | y días sin          |                  |                |               |
|             |               | actividad.          |                  |                |               |
+-------------+---------------+---------------------+------------------+----------------+---------------+

# MÓDULO 08 -- Calificaciones y Evaluaciones

  --------------------------------------------------------------------------------------------------------
  **ID          **Nombre del     **Descripción del     **Criterio de    **Regla del        **Prioridad**
  Requisito**   Requisito**      Requisito**           Aceptación**     Negocio**          
  ------------- ---------------- --------------------- ---------------- ------------------ ---------------
  RF-08-01      Registro de nota El Docente Asesor     El Docente       El registro de     ● Alta
                del Docente      registra su nota de   Asesor solo      nota por el        
                Asesor           evaluación final del  puede registrar  Docente Asesor es  
                                 estudiante al         nota de sus      requisito previo   
                                 concluir la práctica. estudiantes      para que el        
                                 Ingresa nota numérica asignados. La    Coordinador pueda  
                                 (rango 0.0 a 5.0) y   nota debe estar  registrar la nota  
                                 observaciones sobre   dentro del rango final definitiva.  
                                 el desempeño general  0.0 a 5.0. La    Solo el Docente    
                                 del practicante. La   nota puede       Asesor de la       
                                 evaluación final se   modificarse      práctica puede     
                                 calcula               hasta que el     completar la       
                                 automáticamente como  Coordinador      evaluación.        
                                 promedio de los       cierre el                           
                                 seguimientos y la     proceso de                          
                                 evaluación del        evaluación.                         
                                 Docente Asesor, sin                                       
                                 permitir ingreso                                          
                                 manual del promedio.                                      

  RF-08-02      Registro de nota El Tutor Empresarial  El Tutor         El registro de     ● Alta
                del Tutor        registra su nota de   Empresarial solo nota por el Tutor  
                Empresarial      evaluación del        puede registrar  Empresarial es un  
                                 practicante desde su  nota de sus      insumo de          
                                 portal, valorando el  estudiantes      referencia. El     
                                 desempeño en la       asignados. El    Coordinador es el  
                                 empresa. Ingresa nota formulario debe  responsable de     
                                 numérica (rango 0.0 a ser simple e     registrar la nota  
                                 5.0) y observaciones  intuitivo para   final definitiva.  
                                 generales sobre el    usuarios         La evaluación debe 
                                 desempeño del         externos. El     registrarse antes  
                                 practicante.          Tutor puede      del cierre de la   
                                                       editar la nota   práctica.          
                                                       hasta que el                        
                                                       Coordinador                         
                                                       cierre el                           
                                                       proceso de                          
                                                       evaluación.                         

  RF-08-03      Revisión del     Tanto el Docente      Las dos          Ambos actores      ● Alta
                proyecto de      Asesor como el Tutor  evaluaciones     tienen acceso de   
                práctica         Empresarial revisan   (Docente Asesor  lectura al         
                (Docente Asesor  el proyecto de        y Tutor          proyecto cargado   
                y Tutor          práctica del          Empresarial)     por el estudiante. 
                Empresarial)     estudiante y definen  deben estar      La nota de cada    
                                 la nota final del     completas antes  uno es             
                                 estudiante en función de que el        independiente y no 
                                 del desempeño         Coordinador      editable por el    
                                 observado y los       registre la nota otro.              
                                 resultados del        final. Los                          
                                 proyecto presentado   resultados                          
                                 en la sustentación.   quedan                              
                                                       registrados con                     
                                                       fecha y usuario                     
                                                       responsable.                        

  RF-08-04      Registro de nota El Coordinador de     Solo el          El sistema no      ● Alta
                final por el     Práctica registra la  Coordinador      realiza cálculos   
                Coordinador      nota final del        puede registrar  automáticos de la  
                                 estudiante tomando    la nota final.   nota final; el     
                                 como referencia las   El sistema       Coordinador        
                                 notas del Docente     muestra las      ingresa            
                                 Asesor y del Tutor    notas de         manualmente el     
                                 Empresarial. La nota  referencia para  valor. Una vez     
                                 final determina el    facilitar la     registrada la nota 
                                 resultado de la       decisión. El     y ejecutado el     
                                 práctica (Aprobado /  sistema indica   cierre, el         
                                 Reprobado) según la   claramente si la registro queda     
                                 nota mínima           nota supera el   inmutable.         
                                 configurada por       mínimo                              
                                 programa.             (Aprobado) o no                     
                                                       (Reprobado).                        

  RF-08-05      Encuesta de      Al finalizar la       El estado de la  La estructura de   ● Alta
                satisfacción del práctica y entrar en  encuesta         la encuesta es     
                Tutor            fase de cierre, el    (Pendiente / En  configurable por   
                Empresarial ---  sistema envía         borrador /       el DTI. Los        
                notificaciones y automáticamente un    Completada) es   resultados         
                recordatorios    correo de invitación  visible en       individuales son   
                                 al Tutor Empresarial  tiempo real en   confidenciales;    
                                 con enlace directo a  el checklist de  solo se exponen    
                                 la encuesta de        cierre con       datos agregados en 
                                 satisfacción. Si la   indicador visual reportes. Los      
                                 encuesta permanece    de color. La     recordatorios      
                                 pendiente, el sistema encuesta debe    automáticos se     
                                 envía correos de      completarse      detienen al        
                                 recordatorio          antes del cierre completarse la     
                                 automático con la     formal. Las      encuesta o al      
                                 frecuencia            respuestas son   ejecutarse el      
                                 configurada en        visibles para el cierre.            
                                 RF-11-05 (por defecto Coordinador en                      
                                 cada 3 días hábiles)  reportes                            
                                 hasta que sea         agregados.                          
                                 completada o el                                           
                                 Coordinador ejecute                                       
                                 el cierre por                                             
                                 excepción.                                                

  RF-08-06      Autoevaluación y Al finalizar la       El estado de la  Las respuestas del ● Media
                encuesta del     práctica y entrar en  encuesta del     estudiante sobre   
                Estudiante ---   fase de cierre, el    Estudiante       la empresa son     
                notificaciones y sistema envía         (Pendiente /     confidenciales     
                recordatorios    automáticamente un    Completada) es   individualmente;   
                                 correo de invitación  visible en el    solo se presentan  
                                 al Estudiante con     checklist de     en reportes        
                                 enlace directo a su   cierre. La       agregados. Los     
                                 autoevaluación y      autoevaluación y recordatorios      
                                 encuesta de           encuesta son     automáticos se     
                                 satisfacción. Si la   requisito        detienen al        
                                 encuesta permanece    obligatorio para completarse la     
                                 pendiente, el sistema habilitar el     encuesta.          
                                 envía correos de      cierre formal.                      
                                 recordatorio          El estudiante no                    
                                 automático con la     puede editar                        
                                 frecuencia            respuestas una                      
                                 configurada en        vez enviadas.                       
                                 RF-11-05 hasta que                                        
                                 sea completada.                                           
  --------------------------------------------------------------------------------------------------------

# MÓDULO 09 -- Cierre de Práctica

  ------------------------------------------------------------------------------------------------------
  **ID          **Nombre del     **Descripción del     **Criterio de   **Regla del       **Prioridad**
  Requisito**   Requisito**      Requisito**           Aceptación**    Negocio**         
  ------------- ---------------- --------------------- --------------- ----------------- ---------------
  RF-09-01      Checklist de     Antes de ejecutar el  El botón        El checklist es   ● Alta
                requisitos de    cierre formal, el     Ejecutar cierre configurable por  
                cierre con       sistema presenta un   solo se         programa. No se   
                seguimiento de   checklist automático  habilita cuando envía más de un   
                encuestas y      verificando: (1) nota todos los ítems recordatorio      
                recordatorios    del Docente Asesor    obligatorios    manual por actor  
                                 registrada; (2) nota  del checklist   en el mismo día.  
                                 del Tutor Empresarial están en verde. Los recordatorios 
                                 registrada; (3) nota  Los ítems       manuales se suman 
                                 final registrada por  pendientes se   a los             
                                 el Coordinador; (4)   muestran con    automáticos.      
                                 encuesta del Tutor    indicador rojo                    
                                 con estado visual     y enlace                          
                                 dinámico y botón de   directo a la                      
                                 recordatorio; (5)     acción                            
                                 encuesta del          requerida.                        
                                 Estudiante con estado                                   
                                 y botón de                                              
                                 recordatorio; (6)                                       
                                 documentos requeridos                                   
                                 cargados; (7)                                           
                                 sustentación final                                      
                                 programada y                                            
                                 registrada.                                             

  RF-09-02      Programar y      El Coordinador de     La fecha de     Solo el           ● Alta
                registrar        Prácticas programa la sustentación    Coordinador       
                sustentación     fecha de              debe ser        programa y        
                final            sustentación, asigna  posterior al    registra la       
                (Coordinación)   jurados y registra el inicio de la    sustentación.     
                                 resultado final con   práctica. El    Toda práctica     
                                 el acta firmada. La   acta debe estar debe tener        
                                 sustentación debe     cargada antes   sustentación      
                                 tener al menos un     de ejecutar el  registrada antes  
                                 jurado asignado. El   cierre formal.  del cierre        
                                 acta es obligatoria   El resultado de formal. La        
                                 antes de registrar el la sustentación sustentación con  
                                 resultado final.      (Aprobado /     resultado         
                                                       Reprobado)      Aprobado es       
                                                       queda           requisito del     
                                                       registrado en   checklist de      
                                                       el expediente.  cierre.           

  RF-09-03      Sustentación del El estudiante hace    El estudiante   El proyecto de    ● Alta
                proyecto de      entrega y             puede cargar el práctica debe     
                práctica         sustentación de su    proyecto en     estar cargado     
                (Estudiante)     proyecto de práctica  formato PDF,    antes de que el   
                                 ante el Docente       con tamaño      Coordinador pueda 
                                 Asesor y el Tutor     máximo de 10    programar la      
                                 Empresarial. El       MB. El          sustentación. El  
                                 sistema permite al    documento queda estudiante no     
                                 estudiante cargar el  registrado con  puede modificar   
                                 proyecto final en el  fecha de carga. el proyecto una   
                                 repositorio de        El Docente      vez que ha sido   
                                 documentos de la      Asesor y el     revisado por el   
                                 práctica.             Tutor           Docente Asesor.   
                                                       Empresarial                       
                                                       tienen acceso                     
                                                       de lectura al                     
                                                       proyecto                          
                                                       cargado.                          

  RF-09-04      Ejecución del    Al completarse el     El cierre       La práctica queda ● Alta
                cierre formal de checklist, el         requiere        en estado         
                la práctica      Coordinador ejecuta   confirmación    inmutable desde   
                                 el cierre formal. El  explícita del   el momento del    
                                 sistema lee la nota   Coordinador. El cierre. El        
                                 final previamente     proceso de      Coordinador de    
                                 registrada, determina cierre no puede Práctica es el    
                                 el resultado          deshacerse. El  único rol que     
                                 (Aprobado /           Coordinador     puede ejecutar el 
                                 Reprobado) y          puede cargar    cierre formal.    
                                 actualiza el estado   opcionalmente                     
                                 del estudiante en el  el acta de                        
                                 expediente. El        cierre como                       
                                 expediente pasa a     documento en el                   
                                 estado inmutable y el repositorio.                      
                                 sistema notifica el                                     
                                 resultado a todos los                                   
                                 actores.                                                

  RF-09-05      Actualización    Al cerrarse la        El estado del   Un estudiante con ● Alta
                automática de    práctica, el sistema  expediente se   Práctica N        
                estado del       actualiza el estado   actualiza de    Reprobada no      
                estudiante       del estudiante en el  forma inmediata puede acceder     
                                 expediente: Práctica  al ejecutar el  automáticamente a 
                                 N Completada si       cierre. La      la Práctica N+1.  
                                 aprobó, o Práctica N  notificación a  La Coordinación   
                                 Reprobada si no       la Coordinación Académica debe    
                                 alcanzó la nota       Académica       tomar una         
                                 mínima. Notifica      incluye el      decisión          
                                 automáticamente por   nombre del      explícita.        
                                 correo y en el panel  estudiante, el                    
                                 a la Coordinación     resultado y la                    
                                 Académica para que    nota final.                       
                                 evalúe la                                               
                                 habilitación de la                                      
                                 siguiente práctica.                                     

  RF-09-06      Archivado        Al ejecutarse el      Cualquier       Si se requiere    ● Alta
                inmutable del    cierre formal, toda   intento de      corregir un error 
                expediente de la la documentación de   modificar un    en una práctica   
                práctica cerrada la práctica queda     documento de    cerrada, el       
                                 archivada en el       una práctica    proceso debe      
                                 expediente del        cerrada debe    realizarse        
                                 estudiante en estado  retornar error  mediante          
                                 de solo lectura.      403. El         solicitud formal  
                                 Ningún actor puede    expediente      aprobada por la   
                                 modificar, reemplazar cerrado es      Dirección, con    
                                 ni eliminar           consultable por registro en       
                                 documentos de una     los roles       bitácora de       
                                 práctica cerrada.     autorizados en  auditoría.        
                                                       modo solo                         
                                                       lectura en                        
                                                       cualquier                         
                                                       momento futuro.                   
  ------------------------------------------------------------------------------------------------------

# MÓDULO 10 -- Reportes e Indicadores

  ------------------------------------------------------------------------------------------------------
  **ID          **Nombre del   **Descripción del     **Criterio de      **Regla del      **Prioridad**
  Requisito**   Requisito**    Requisito**           Aceptación**       Negocio**        
  ------------- -------------- --------------------- ------------------ ---------------- ---------------
  RF-10-01      Reporte de     Reporte tabular que   Los datos del      El scope del     ● Alta
                estado del     muestra el número de  reporte reflejan   usuario aplica   
                proceso por    estudiantes en cada   el estado en       también en los   
                programa y     estado del proceso    tiempo real. Los   reportes. Los    
                periodo        filtrado por          totales por estado periodos         
                               facultad, programa,   se muestran al     académicos son   
                               periodo académico y   pie. El reporte    configurables.   
                               número de práctica.   puede exportarse a                  
                               El Coordinador ve su  Excel o PDF.                        
                               programa; la                                              
                               Coordinación                                              
                               Académica ve su                                           
                               facultad; la                                              
                               Dirección ve todos.                                       

  RF-10-02      Reporte de     Reporte con las notas El reporte incluye Las notas        ● Alta
                notas          registradas de todos  tanto prácticas    exportadas       
                registradas    los estudiantes por   activas (notas     conservan el     
                               programa y periodo.   parciales o sin    formato numérico 
                               Columnas: estudiante, nota) como         con dos          
                               número de práctica,   cerradas (nota     decimales. El    
                               empresa, nota del     final). Las        reporte de notas 
                               Docente Asesor, nota  prácticas sin nota es confidencial  
                               del Tutor             final registrada   para la Empresa. 
                               Empresarial, nota     se muestran como                    
                               final registrada por  Pendiente.                          
                               el Coordinador,       Exportable a Excel                  
                               resultado.            y PDF.                              

  RF-10-03      Reporte de     Reporte con todas las El reporte de      Los datos de la  ● Media
                empresas y     empresas vinculadas,  empresas es        empresa que se   
                vacantes       número de vacantes    accesible para el  exponen en el    
                               por estado, número de Coordinador, la    reporte no       
                               estudiantes asignados Coordinación       incluyen         
                               histórico y activo, y Académica y la     información      
                               tasa de finalización  Dirección. La tasa financiera ni    
                               exitosa. Filtros:     de finalización    datos personales 
                               sector, programa,     exitosa se calcula de los tutores.  
                               periodo.              automáticamente.                    
                                                     Exportable a                        
                                                     Excel.                              

  RF-10-04      Tablero        Panel visual          El tablero muestra El tablero       ● Media
                gerencial de   exclusivo para la     gráficos           gerencial no     
                indicadores    Dirección con         interactivos. Al   expone datos     
                (Dirección)    indicadores agregados hacer clic en un   individuales de  
                               de toda la            indicador, navega  estudiantes ni   
                               universidad: total de al reporte         empresas; solo   
                               practicantes activos  detallado          indicadores      
                               por facultad, tasa de correspondiente.   agregados.       
                               aprobación global y   Los datos se                        
                               por programa, número  actualizan en                       
                               de empresas activas,  tiempo real.                        
                               tiempo promedio de                                        
                               gestión, prácticas                                        
                               cerradas en el                                            
                               periodo.                                                  

  RF-10-05      Reporte        Reporte con           Los resultados son El reporte de    ● Media
                consolidado de resultados agregados  siempre agregados; encuestas es     
                encuestas de   de las encuestas de   no se exponen      accesible para   
                satisfacción   satisfacción de       respuestas         el Coordinador,  
                               Tutores Empresariales individuales de    la Coordinación  
                               y Estudiantes.        estudiantes.       Académica y la   
                               Muestra promedios por Mínimo de 5        Dirección.       
                               pregunta, por empresa respuestas para                     
                               y por programa.       mostrar datos de                    
                               Identifica las        una empresa.                        
                               empresas y los        Exportable a                        
                               procesos mejor y peor Excel.                              
                               evaluados.                                                

  RF-10-06      Exportación de Todos los reportes    La exportación     La exportación   ● Media
                reportes a     tabulares pueden      respeta los        no incluye datos 
                Excel y PDF    exportarse a Excel    filtros activos.   fuera del scope  
                               (.xlsx) con todas las Para más de 1000   del usuario que  
                               columnas, encabezados filas, la          la genera.       
                               descriptivos en       exportación se                      
                               español y filtros     ejecuta en segundo                  
                               activos aplicados.    plano con                           
                               Los reportes de notas notificación al                     
                               y actas pueden        terminar. Las                       
                               exportarse también a  exportaciones                       
                               PDF.                  quedan registradas                  
                                                     en la bitácora de                   
                                                     auditoría.                          
  ------------------------------------------------------------------------------------------------------

# MÓDULO 11 -- Configuración del Sistema

  --------------------------------------------------------------------------------------------------------
  **ID          **Nombre del      **Descripción del     **Criterio de    **Regla del       **Prioridad**
  Requisito**   Requisito**       Requisito**           Aceptación**     Negocio**         
  ------------- ----------------- --------------------- ---------------- ----------------- ---------------
  RF-11-01      Asignación de     El Coordinador de     Los periodos     Solo el           ● Alta
                periodos de       Prácticas define los  deben tener      Coordinador de    
                prácticas         tiempos en los que se fecha de inicio  Prácticas puede   
                (Coordinador de   va a desarrollar la   y fecha de fin.  definir los       
                Prácticas)        práctica de cada      El sistema       periodos de       
                                  estudiante,           valida que la    prácticas. Los    
                                  incluyendo fechas de  fecha de fin sea periodos son      
                                  inicio, entrega y     posterior a la   configurables por 
                                  sustentación. Esto    fecha de inicio. programa y        
                                  permite al sistema    Al guardar el    semestre          
                                  calcular              periodo, el      académico.        
                                  automáticamente los   sistema                            
                                  cortes de seguimiento precalcula las                     
                                  y generar alertas de  fechas de cada                     
                                  vencimiento.          corte de                           
                                                        seguimiento.                       

  RF-11-02      Configuración de  El DTI (o la          Los cambios en   La configuración  ● Alta
                parámetros por    Coordinación          parámetros       por programa      
                programa          Académica para su     aplican solo a   permite que       
                                  facultad) configura   nuevas prácticas diferentes        
                                  los parámetros        iniciadas        facultades y      
                                  operativos por        después del      programas operen  
                                  programa: número de   cambio; las      con reglas        
                                  prácticas del         prácticas        distintas sin     
                                  programa, número de   activas          afectarse entre   
                                  cortes por práctica,  conservan la     sí.               
                                  nota mínima de        configuración                      
                                  aprobación,           con la que                         
                                  requisitos por número fueron                             
                                  de práctica, máximo   iniciadas.                         
                                  de asignaciones                                          
                                  simultáneas por                                          
                                  estudiante y umbral                                      
                                  de inactividad para                                      
                                  alertas.                                                 

  RF-11-03      Gestión de        El DTI gestiona los   No se puede      Los catálogos     ● Baja
                catálogos         catálogos de datos    inactivar un     maestros son      
                maestros          que alimentan los     ítem             globales para     
                                  selectores del        referenciado en  toda la           
                                  sistema: sectores     un registro      institución. Un   
                                  económicos de         activo. Al       cambio en un      
                                  empresas, áreas de    agregar un ítem  catálogo afecta   
                                  práctica,             nuevo, queda     todos los         
                                  modalidades, tipos de disponible       programas y       
                                  documentos            inmediatamente   facultades que lo 
                                  requeridos, estados   en todos los     usen.             
                                  personalizados.       formularios del                    
                                                        sistema.                           

  RF-11-04      Generación de     El DTI puede generar  El respaldo no   El respaldo es    ● Baja
                respaldo de datos manualmente un        incluye          siempre un        
                                  respaldo completo de  contraseñas ni   proceso manual;   
                                  datos en formato      archivos         no hay respaldos  
                                  Excel con hojas:      adjuntos         automáticos en    
                                  Estudiantes,          binarios. Si     esta versión.     
                                  Expedientes de        tarda más de 15                    
                                  prácticas, Empresas,  segundos se                        
                                  Vacantes, Notas       ejecuta en                         
                                  registradas,          segundo plano                      
                                  Evaluaciones,         con notificación                   
                                  Usuarios y Bitácora   al terminar. La                    
                                  de auditoría (últimos generación queda                   
                                  2 años).              registrada en la                   
                                                        bitácora de                        
                                                        auditoría.                         

  RF-11-05      Configuración de  El DTI configura las  Las plantillas   Los recordatorios ● Alta
                notificaciones    reglas y plantillas   son editables    automáticos de    
                por correo        del sistema de        sin tocar        encuestas se      
                electrónico       notificaciones por    código; el DTI   detienen al       
                                  correo. Por cada tipo puede            completarse la    
                                  de evento (nueva      previsualizar el encuesta o al     
                                  asignación, cambio de correo antes de  ejecutarse el     
                                  estado, encuesta      guardar. Cada    cierre formal. No 
                                  disponible,           tipo de evento   se envía más de   
                                  recordatorio de       tiene una        un recordatorio   
                                  encuesta pendiente,   plantilla        automático por    
                                  alerta de             independiente.   actor por día.    
                                  inactividad,          Los cambios                        
                                  confirmación de       aplican a partir                   
                                  vinculación,          del siguiente                      
                                  resultado de cierre)  envío.                             
                                  se define: plantilla                                     
                                  HTML con variables                                       
                                  dinámicas, roles                                         
                                  receptores, si el                                        
                                  correo es obligatorio                                    
                                  o informativo, y                                         
                                  frecuencia de                                            
                                  recordatorios                                            
                                  automáticos en días                                      
                                  hábiles.                                                 

  RF-11-06      Registros de      El sistema muestra al El registro es   Solo los roles    ● Media
                actividad por     DTI, a la             de solo lectura. administrativos   
                roles             Coordinación          Los filtros      tienen acceso al  
                administrativos   Académica y al        disponibles son: registro de       
                                  Coordinador de        tipo de acción,  actividad         
                                  Prácticas un registro rol que la       administrativa.   
                                  de todas las          realizó, fecha y Los registros no  
                                  actividades           entidad          pueden            
                                  administrativas       afectada. El     modificarse ni    
                                  realizadas, como      registro puede   eliminarse.       
                                  cancelación de        exportarse a                       
                                  prácticas,            Excel.                             
                                  vinculación o rechazo                                    
                                  de estudiantes a                                         
                                  empresas, cambios de                                     
                                  estado de vacantes,                                      
                                  entre otras,                                             
                                  organizadas por fecha                                    
                                  y tipo de acción.                                        
  --------------------------------------------------------------------------------------------------------

# SECCIÓN 3 -- ROLES DEL SISTEMA

El sistema cuenta con 8 roles, cada uno con scope y permisos definidos:

  -------------------------------------------------------------
  **Rol**               **Descripción y Responsabilidades**
  --------------------- ---------------------------------------
  DTI (Administrador    Superusuario con acceso total al
  del Sistema)          sistema. Crea y gestiona todos los
                        usuarios, roles, facultades, programas
                        y catálogos maestros. Accede a la
                        bitácora de auditoría completa.
                        Restablece contraseñas. Configura
                        plantillas de correo y parámetros del
                        sistema. Gestiona la clasificación de
                        empresas por industria y programa. Debe
                        existir al menos un DTI activo.

  Coordinador de        Gestión central del proceso de
  Prácticas             prácticas de su programa. Aprueba y
                        rechaza empresas formadoras y vacantes.
                        Asigna estudiantes a vacantes, docentes
                        asesores y tutores empresariales.
                        Confirma la vinculación, inicia
                        prácticas, registra la nota final,
                        programa sustentaciones y ejecuta el
                        cierre formal. Supervisa el tablero de
                        seguimiento activo. Genera reportes y
                        define periodos de prácticas.

  Coordinador Académico Gestiona el catálogo de prácticas
                        institucional. Registra y valida
                        estudiantes, determina aptitud (Apto /
                        No apto) y habilita la siguiente
                        práctica al cierre. Supervisa el estado
                        de los estudiantes de su
                        facultad/programa. Genera la lista de
                        estudiantes aptos por semestre.

  Docente Asesor        Supervisión académica y calificación de
                        la práctica desde la universidad.
                        Aprueba el plan de práctica, califica
                        los seguimientos semanales, registra la
                        evaluación final del estudiante y
                        revisa el proyecto de práctica. Accede
                        únicamente a sus estudiantes asignados.

  Tutor Empresarial     Supervisión del practicante en la
                        empresa. Aprueba el plan de práctica,
                        registra avances y desempeño, emite la
                        nota de evaluación, confirma su firma
                        en el convenio y completa la encuesta
                        de satisfacción. Genera el paz y salvo
                        junto al estudiante. Pertenece a una
                        empresa en estado Aprobada.

  Empresa Formadora     Registro, aprobación y gestión de
                        vacantes de práctica. Crea y publica
                        vacantes, registra tutores
                        empresariales, confirma la vinculación
                        del estudiante y actualiza datos de
                        contacto. Solo puede operar si está en
                        estado Aprobada.

  Estudiante            Actor central del proceso de práctica.
  Practicante           Completa la hoja de vida, postula a
                        vacantes disponibles, carga el plan de
                        práctica, registra los seguimientos
                        semanales, carga el proyecto final y
                        sustenta ante el Docente Asesor y el
                        Tutor Empresarial. Completa la encuesta
                        de satisfacción para obtener el paz y
                        salvo.

  Dirección Académica   Acceso a indicadores gerenciales y
                        reportes agregados de toda la
                        universidad. Visualiza el tablero
                        gerencial por facultad, aprueba
                        decisiones sobre prácticas reprobadas o
                        excepciones al proceso. No opera sobre
                        prácticas individuales directamente.
  -------------------------------------------------------------

# SECCIÓN 4 -- RESTRICCIONES OCL DEL SISTEMA

Las siguientes invariantes OCL (Object Constraint Language) definen las
restricciones formales del modelo de dominio. Estas restricciones deben
ser respetadas en toda implementación y prueba del sistema.

## 4.1 Estudiante Practicante

El Estudiante es el actor central del proceso. Su estado determina en
qué fase del proceso se encuentra y qué acciones puede realizar.

### Multiplicidades

- Un Estudiante puede tener máximo 1 práctica activa (EN_CURSO) por
  semestre.

- Un Estudiante puede tener 0..\* prácticas en su historial (FINALIZADA
  o CANCELADA).

- Un Estudiante tiene exactamente 0..1 HojaDeVida activa en el sistema.

### Operaciones CRUD

- Crear: El DTI crea el usuario del estudiante.

- Leer: El propio estudiante lee su información; el Coordinador lee
  todas.

- Actualizar: Si estado = EN_PRACTICA, los campos carrera y semestre son
  de solo lectura. El perfil básico (nombre, foto, teléfono) es editable
  siempre.

- Eliminar: PROHIBIDO. Los datos académicos son registro permanente.
  Solo se permite desactivar la cuenta (cuenta.activa = false).

### Invariantes OCL

> context Estudiante
>
> \-- Un estudiante solo puede tener 1 practica activa simultanea
>
> inv maxUnaPracticaActiva:
>
> self.practicas-\>select(p \| p.estado =
> EstadoPractica::EN_CURSO)-\>size() \<= 1
>
> \-- Solo puede ser postulado si su estado es APTO
>
> inv soloAptoPostulable:
>
> self.postulaciones-\>notEmpty() implies self.estadoAptitud =
> EstadoAptitud::APTO
>
> \-- Requiere asignatura nucleo aprobada para estar EN_PRACTICA
>
> inv requiereNucleoAprobado:
>
> self.practicaActiva-\>notEmpty() implies
>
> self.asignaturasAprobadas-\>includes(self.practicaActiva.materiaNucleo)
>
> \-- Semestre minimo segun programa academico
>
> inv semestresMinimos:
>
> self.practicaActiva-\>notEmpty() implies
>
> self.semestre \>= self.programa.semestresMinimosRequeridos
>
> \-- La hoja de vida debe estar Valida para poder ser postulado
>
> inv hvValidaParaPostular:
>
> self.postulaciones-\>notEmpty() implies
>
> self.hojaDeVida.estado = EstadoHV::VALIDA
>
> \-- Transiciones de estado validas (solo avance, nunca retroceso)
>
> inv transicionEstadoValida:
>
> let orden = Sequence{
>
> EstadoEstudiante::SIN_EVALUAR,
>
> EstadoEstudiante::APTO,
>
> EstadoEstudiante::NO_APTO,
>
> EstadoEstudiante::EN_PRACTICA,
>
> EstadoEstudiante::PRACTICA_FINALIZADA
>
> } in
>
> orden-\>indexOf(self.estadoAnterior) \<= orden-\>indexOf(self.estado)

## 4.2 Hoja de Vida

La Hoja de Vida se trata como un subobjeto versionado del Estudiante.
Cada actualización genera una nueva versión; la versión anterior se
conserva en el historial.

### Multiplicidades

- Una HojaDeVida pertenece a exactamente 1 Estudiante.

- La versión siempre es positiva (\>= 1).

- Si el Estudiante está EN_PRACTICA, la HV no puede ser reemplazada.

### Operaciones CRUD

- Crear: Al subir primera HV.

- Leer: Estudiante y Coordinador.

- Actualizar: Genera nueva versión; versión anterior persiste en
  historial.

- Eliminar: PROHIBIDO.

### Invariantes OCL

> context HojaDeVida
>
> \-- Una HV pertenece a exactamente un Estudiante
>
> inv perteneceAEstudiante:
>
> self.estudiante-\>size() = 1
>
> \-- La version siempre es positiva
>
> inv versionPositiva:
>
> self.version \>= 1
>
> \-- Si el estudiante esta EN_PRACTICA, la HV no puede ser reemplazada
>
> inv hvInmutableEnPractica:
>
> self.estudiante.estado = EstadoEstudiante::EN_PRACTICA implies
>
> not self.esReemplazable
>
> \-- El estado solo puede ser uno de los valores definidos
>
> inv estadoValido:
>
> Set{EstadoHV::VALIDA, EstadoHV::INCOMPLETA}-\>includes(self.estado)

## 4.3 Práctica Empresarial

La Práctica Empresarial es la entidad central del flujo de negocio.
Agrupa todos los actores, documentos, seguimientos y evaluaciones del
proceso.

### Multiplicidades

- Una Práctica tiene exactamente 1 Estudiante asignado.

- Una Práctica tiene exactamente 1 DocenteAsesor y 1 TutorEmpresarial.

- Una Práctica almacena 0..\* Seguimientos, 0..\* Documentos, 0..\*
  Encuestas.

- Una Práctica almacena exactamente 0..1 PlanPractica.

- Una Práctica almacena exactamente 0..1 Sustentacion.

### Operaciones CRUD

- Crear: Solo el Coordinador. Requiere postulación activa y empresa
  Aprobada.

- Leer: Todos los actores asignados a la práctica.

- Actualizar: El Coordinador asigna tutor, docente y programa
  sustentación. El estado solo avanza; nunca retrocede.

- Eliminar: ABSOLUTAMENTE PROHIBIDO. Una práctica cancelada cambia a
  estado CANCELADA pero todos sus registros se conservan para auditoría.

### Invariantes OCL

> context Practica
>
> \-- Un estudiante tiene maximo 1 practica activa por semestre
>
> inv maxUnaPorSemestre:
>
> Practica.allInstances()
>
> -\>select(p \| p.estudiante = self.estudiante
>
> and p.semestre = self.semestre
>
> and p.estado = EstadoPractica::EN_CURSO)
>
> -\>size() \<= 1
>
> \-- El cierre requiere checklist completo
>
> inv cierreRequiereChecklist:
>
> self.estado = EstadoPractica::CERRADA implies
>
> self.checklist.notaDocenteRegistrada
>
> and self.checklist.notaTutorRegistrada
>
> and self.checklist.notaFinalRegistrada
>
> and self.checklist.encuestaTutorCompletada
>
> and self.checklist.encuestaEstudianteCompletada
>
> and self.checklist.sustentacionRegistrada
>
> \-- La cancelacion no puede ser por voluntad del estudiante
>
> inv cancelacionNoVoluntaria:
>
> self.estado = EstadoPractica::CANCELADA implies
>
> self.motivoCancelacion \<\> \'Solicitud_estudiante\'
>
> \-- El seguimiento no puede iniciarse sin planeador aprobado por tutor
> y docente
>
> inv seguimientoRequierePlaneador:
>
> self.seguimientos-\>notEmpty() implies
>
> self.planPractica.estado = EstadoPlan::APROBADO_DOCENTE
>
> \-- Fecha de cierre siempre posterior a fecha de inicio
>
> inv fechasCoherentes:
>
> self.fechaCierre-\>notEmpty() implies
>
> self.fechaCierre \> self.fechaInicio
>
> \-- Transicion de estado: solo avanza, nunca retrocede
>
> inv estadoAvanza:
>
> let orden = Sequence{
>
> EstadoPractica::ASIGNADA,
>
> EstadoPractica::EN_PROCESO_VINCULACION,
>
> EstadoPractica::EN_CURSO,
>
> EstadoPractica::EN_CIERRE,
>
> EstadoPractica::CERRADA
>
> } in
>
> orden-\>indexOf(self.estadoAnterior) \<= orden-\>indexOf(self.estado)

## 4.4 Docente Asesor

### Multiplicidades

- Un DocenteAsesor puede tener 0..\* estudiantes asignados (sin límite
  superior).

- Un DocenteAsesor está en estado disponible (true/false).

### Operaciones CRUD

- Crear: El DTI se encarga de la creación del usuario del docente.

- Leer: El docente lee a sus propios estudiantes y prácticas asignadas.

- Actualizar: Puede editar perfil (foto, teléfono). El Coordinador puede
  cambiar disponible = true/false.

- Eliminar: PROHIBIDO. Se desactiva (disponible = false).

### Invariantes OCL

> context DocenteAsesor
>
> \-- Un docente tiene lista ilimitada de estudiantes (0..\*)
>
> inv estudiantesIlimitados:
>
> self.practicasAsignadas-\>size() \>= 0
>
> \-- Solo puede calificar seguimientos de sus propios estudiantes
>
> inv soloCalificaSusEstudiantes:
>
> self.seguimientosCalificados-\>forAll(s \|
>
> self.practicasAsignadas-\>includes(s.practica))
>
> \-- Disponibilidad: puede marcarse activo o inactivo por el
> Coordinador
>
> inv disponibilidadCoherente:
>
> not self.disponible implies self.practicasAsignadas-\>isEmpty()

## 4.5 Tutor Empresarial

### Multiplicidades

- Un TutorEmpresarial puede tener 0..\* estudiantes a cargo (sin límite
  superior).

- Un TutorEmpresarial pertenece a exactamente 1 EmpresaFormadora.

- El TutorEmpresarial solo puede operar si su empresa está en estado
  Aprobada.

### Operaciones CRUD

- Crear: El DTI se encarga de crear al usuario Tutor Empresarial.

- Leer: Accede a sus practicantes y encuestas asignadas.

- Actualizar: Edita su perfil (foto, teléfono, cargo).

- Eliminar: PROHIBIDO. Se desactiva si la empresa sale del convenio.

### Invariantes OCL

> context TutorEmpresarial
>
> \-- Lista ilimitada de estudiantes practicantes a cargo (0..\*)
>
> inv estudiantesIlimitados:
>
> self.practicasAsignadas-\>size() \>= 0
>
> \-- Solo puede aprobar planeadores de sus propios estudiantes
> practicantes
>
> inv soloApruebaSusEstudiantes:
>
> self.planesAprobados-\>forAll(p \|
>
> self.practicasAsignadas-\>includes(p.practica))
>
> \-- Debe pertenecer a una empresa en estado Aprobada
>
> inv empresaActiva:
>
> self.empresa.estado = EstadoEmpresa::APROBADA

## 4.6 Empresa Formadora

### Multiplicidades

- Una EmpresaFormadora puede tener 0..\* TutoresEmpresariales asignados.

- Una EmpresaFormadora puede tener 0..\* Vacantes activas (solo si está
  Aprobada).

- Una EmpresaFormadora puede recibir 0..\* practicantes simultáneos
  (según cupos de vacantes).

### Operaciones CRUD

- Crear: La empresa la crea la Coordinación de Prácticas.

- Actualizar: El Coordinador actualiza estado (Activa/Inactiva). La
  empresa puede actualizar datos de contacto bajo el rol del tutor.

- Eliminar: PROHIBIDO. Pasa a estado Inactiva. El historial de convenios
  y prácticas asociadas se conserva.

### Invariantes OCL

> context EmpresaFormadora
>
> \-- Solo puede tener vacantes activas si esta Aprobada
>
> inv vacantesRequierenAprobacion:
>
> self.vacantes-\>select(v \| v.estado =
> EstadoVacante::ACTIVA)-\>notEmpty()
>
> implies self.estado = EstadoEmpresa::APROBADA
>
> \-- El NIT es unico en todo el sistema
>
> inv nitUnico:
>
> EmpresaFormadora.allInstances()
>
> -\>select(e \| e.nit = self.nit)-\>size() = 1
>
> \-- No puede vincularse a nuevos estudiantes si esta Inactiva o
> Rechazada
>
> inv vinculacionRestringida:
>
> (self.estado = EstadoEmpresa::INACTIVA or
>
> self.estado = EstadoEmpresa::RECHAZADA)
>
> implies self.nuevasVinculaciones = 0
>
> \-- Estado valido dentro del conjunto definido
>
> inv estadoValido:
>
> Set{EstadoEmpresa::PENDIENTE, EstadoEmpresa::APROBADA,
>
> EstadoEmpresa::RECHAZADA, EstadoEmpresa::INACTIVA}
>
> -\>includes(self.estado)

## 4.7 Vacante

### Multiplicidades

- Una Vacante pertenece a exactamente 1 EmpresaFormadora.

- Una Vacante puede tener 0..cuposTotales practicantes asignados.

### Operaciones CRUD

- Crear: El Tutor Empresarial o el Coordinador crea la vacante; el
  Coordinador la aprueba.

- Leer: Empresa, Coordinador, roles autorizados.

- Actualizar: La empresa puede editar vacantes Pendientes o Rechazadas.
  El Coordinador gestiona el ciclo de vida.

- Eliminar: PROHIBIDO. Las vacantes cerradas se conservan en el
  historial.

### Invariantes OCL

> context Vacante
>
> \-- Los cupos ocupados no pueden superar los cupos totales
>
> inv cuposValidos:
>
> self.cuposOcupados \<= self.cuposTotales
>
> \-- Los cupos son positivos
>
> inv cuposPositivos:
>
> self.cuposTotales \> 0
>
> \-- Solo la coordinacion crea y publica vacantes
>
> inv soloCoordinacionCrea:
>
> self.creadoPor.rol = Rol::COORDINADOR_PRACTICAS
>
> or self.creadoPor.rol = Rol::TUTOR_EMPRESARIAL
>
> \-- Una vacante cerrada no acepta nuevos postulantes
>
> inv cerradaNoAcepta:
>
> self.estado = EstadoVacante::CERRADA implies
>
> self.cuposOcupados = self.cuposOcupados@pre

## 4.8 Seguimiento Semanal

### Multiplicidades

- Una Práctica tiene 0..\* Seguimientos.

- Cada Seguimiento es creado por exactamente 1 Estudiante y calificado
  por exactamente 1 DocenteAsesor.

### Operaciones CRUD

- Crear: Solo el Estudiante.

- Leer: Estudiante, DocenteAsesor, Coordinador.

- Actualizar: El Estudiante puede editar el contenido del seguimiento de
  la semana más reciente si su estado es RECHAZADO. El Docente Asesor
  puede actualizar calificación y observaciones en cualquier seguimiento
  PENDIENTE.

- Eliminar: PROHIBIDO. Si fue rechazado, el estudiante edita y reenvía.
  El registro original se versiona.

### Invariantes OCL

> context Seguimiento
>
> \-- La calificacion va de 0.0 a 5.0
>
> inv rangoCalificacion:
>
> self.calificacion-\>notEmpty() implies
>
> (self.calificacion \>= 0.0 and self.calificacion \<= 5.0)
>
> \-- El estado se calcula automaticamente segun calificacion
>
> inv estadoCalculado:
>
> self.calificacion-\>notEmpty() implies (
>
> (self.calificacion \>= self.practica.programa.notaMinima implies
>
> self.estado = EstadoSeguimiento::APROBADO)
>
> and
>
> (self.calificacion \< self.practica.programa.notaMinima implies
>
> self.estado = EstadoSeguimiento::RECHAZADO)
>
> )
>
> \-- Solo el seguimiento de la semana mas reciente es editable
>
> inv soloUltimoEditable:
>
> self.estado = EstadoSeguimiento::RECHAZADO implies
>
> self.numeroSemana = self.practica.seguimientos
>
> -\>collect(s \| s.numeroSemana)-\>max()
>
> \-- No se puede crear seguimiento sin plan aprobado
>
> inv requierePlanAprobado:
>
> self.practica.planPractica.estado = EstadoPlan::APROBADO_DOCENTE
>
> \-- El numero de semana es positivo y unico dentro de la practica
>
> inv semanaUnica:
>
> self.numeroSemana \>= 1 and
>
> Seguimiento.allInstances()
>
> -\>select(s \| s.practica = self.practica
>
> and s.numeroSemana = self.numeroSemana)-\>size() = 1

## 4.9 Coordinador de Prácticas

### Multiplicidades

- Un Coordinador supervisa 0..\* prácticas activas (sin cota superior).

- Un Coordinador tiene exactamente 1 scope (programa).

### Operaciones CRUD

- Crear: El DTI crea el usuario del Coordinador de Prácticas.

- Leer: Accede a todas las prácticas, estudiantes, empresas y vacantes
  de su scope.

- Actualizar: Gestiona todo el flujo del proceso de prácticas dentro de
  su programa.

- Eliminar: PROHIBIDO. Se desactiva (activo = false).

### Invariantes OCL

> context Coordinador
>
> \-- Cada coordinador supervisa 0..\* practicas activas (sin cota
> superior)
>
> inv supervisionIlimitada:
>
> self.practicasSupervisadas-\>size() \>= 0
>
> \-- Solo el Coordinador puede crear vacantes y confirmar asignaciones
>
> inv accionesExclusivas:
>
> Vacante.allInstances()-\>select(v \| v.estado = EstadoVacante::ACTIVA)
>
> -\>forAll(v \| v.aprobadoPor.rol = Rol::COORDINADOR_PRACTICAS)
>
> \-- Un Coordinador tiene exactamente un rol en el sistema
>
> inv rolUnico:
>
> self.roles-\>size() = 1

## 4.10 Administrador DTI (Super Usuario)

### Multiplicidades

- Debe existir al menos 1 AdministradorDTI activo en el sistema.

- El DTI tiene acceso irrestricto a todos los objetos del sistema.

### Operaciones CRUD

- Crear: Solo otro AdministradorDTI activo puede crear nuevos DTI y
  todos los usuarios del sistema.

- Leer: Acceso completo a todos los objetos y logs del sistema.

- Actualizar: Puede editar cualquier usuario, rol, contraseña y
  configuración.

- Eliminar: PROHIBIDO. La cuenta DTI se desactiva (activo = false),
  nunca se borra del sistema.

### Invariantes OCL

> context AdministradorDTI
>
> \-- Acceso irrestricto a todos los objetos del sistema
>
> inv superUsuarioAccesoTotal:
>
> self.permisos-\>includesAll(Permiso.allInstances())
>
> \-- Puede crear, editar y desactivar cualquier usuario
>
> inv puedeGestionarUsuarios:
>
> Usuario.allInstances()-\>forAll(u \| self.puedeEditar(u))
>
> \-- Puede asignar o revocar cualquier rol del sistema
>
> inv puedeGestionarRoles:
>
> Rol.allInstances()-\>forAll(r \| self.puedeAsignar(r))
>
> \-- Puede restablecer contrasenias de cualquier usuario
>
> inv puedeRestablecerContrasena:
>
> Usuario.allInstances()-\>forAll(u \| self.puedeResetearContrasena(u))
>
> \-- Acceso completo al log de auditoria del sistema
>
> inv accesoAuditoria:
>
> self.puedeVerBitacora = true
>
> \-- Debe existir al menos 1 administrador DTI activo
>
> inv minimoUnDTIActivo:
>
> AdministradorDTI.allInstances()
>
> -\>select(d \| d.activo = true)-\>size() \>= 1

## 4.11 Plan de Práctica

El Plan de Práctica es cargado por el Estudiante y debe pasar por un
flujo de aprobación: primero el Tutor Empresarial y luego el Docente
Asesor. Solo cuando está en estado AprobadoDocente se pueden iniciar los
seguimientos semanales.

### Multiplicidades

- Un PlanPractica pertenece a exactamente 1 Practica.

- El PlanPractica tiene al menos 1 objetivo definido.

- Los estados válidos son: PENDIENTE → APROBADO_TUTOR → APROBADO_DOCENTE
  / RECHAZADO.

### Operaciones CRUD

- Crear: El Estudiante carga el plan de práctica.

- Leer: Estudiante, Tutor Empresarial, Docente Asesor, Coordinador.

- Actualizar: El Tutor Empresarial y el Docente Asesor aprueban o
  rechazan el plan. El Estudiante puede editar y reenviar si fue
  rechazado.

- Eliminar: PROHIBIDO.

### Invariantes OCL

> context PlanPractica
>
> \-- El seguimiento no puede iniciarse sin plan en estado
> AprobadoDocente
>
> inv seguimientoRequierePlanAprobado:
>
> self.practica.seguimientos-\>notEmpty() implies
>
> self.estado = EstadoPlan::APROBADO_DOCENTE
>
> \-- El plan pertenece a exactamente una practica
>
> inv perteneceAPractica:
>
> self.practica-\>size() = 1
>
> \-- Solo el estudiante puede cargar el plan (cargadoPor)
>
> inv soloCargaEstudiante:
>
> self.cargadoPor.rol = Rol::ESTUDIANTE
>
> \-- Transicion de estado valida (no puede saltarse AprobadoTutor)
>
> inv transicionValida:
>
> let orden = Sequence{
>
> EstadoPlan::PENDIENTE,
>
> EstadoPlan::APROBADO_TUTOR,
>
> EstadoPlan::APROBADO_DOCENTE,
>
> EstadoPlan::RECHAZADO
>
> } in
>
> orden-\>indexOf(self.estadoAnterior) \<= orden-\>indexOf(self.estado)
>
> or self.estado = EstadoPlan::RECHAZADO
>
> \-- Estado valido dentro del conjunto definido
>
> inv estadoValido:
>
> Set{EstadoPlan::PENDIENTE, EstadoPlan::APROBADO_TUTOR,
>
> EstadoPlan::APROBADO_DOCENTE, EstadoPlan::RECHAZADO}
>
> -\>includes(self.estado)
>
> \-- Debe tener al menos un objetivo definido
>
> inv tieneObjetivos:
>
> self.objetivos-\>size() \>= 1

## 4.12 Evaluación Final

La Evaluación Final es registrada por el Docente Asesor al concluir la
práctica. El promedio se calcula automáticamente a partir de los
seguimientos semanales y no puede ser ingresado manualmente.

### Multiplicidades

- Una Practica tiene exactamente 1 Evaluacion.

- La Evaluacion tiene un promedioFinal en rango 0.0 a 5.0.

### Operaciones CRUD

- Crear: El Docente Asesor registra la evaluación final.

- Leer: Docente Asesor, Coordinador, Dirección.

- Actualizar: Solo antes del cierre de la práctica.

- Eliminar: PROHIBIDO.

### Invariantes OCL

> context Evaluacion
>
> \-- El promedio final esta en rango 0.0 a 5.0
>
> inv rangoPromedio:
>
> self.promedioFinal \>= 0.0 and self.promedioFinal \<= 5.0
>
> \-- El promedio se calcula automaticamente; no se ingresa manualmente
>
> inv promedioCalculado:
>
> self.promedioFinal =
>
> self.practica.seguimientos-\>collect(s \| s.calificacion)-\>average()
>
> \-- La evaluacion debe registrarse antes del cierre de la practica
>
> inv antesDelCierre:
>
> self.practica.estado \<\> EstadoPractica::CERRADA
>
> or self.fechaRegistro \<= self.practica.fechaCierre
>
> \-- Estado valido
>
> inv estadoValido:
>
> Set{EstadoEvaluacion::PENDIENTE, EstadoEvaluacion::COMPLETADA}
>
> -\>includes(self.estado)
>
> \-- Solo el docente asesor de la practica puede completarla
>
> inv soloDocenteEvalua:
>
> self.completadaPor = self.practica.docenteAsesor

## 4.13 Sustentación Final

La Sustentación Final es programada por la Coordinación de Prácticas. Es
obligatoria antes del cierre formal de la práctica y debe tener al menos
un jurado asignado y el acta cargada.

### Multiplicidades

- Una Practica tiene exactamente 0..1 Sustentacion.

- Una Sustentacion tiene al menos 1 jurado asignado.

- El acta es obligatoria antes de registrar el resultado.

### Operaciones CRUD

- Crear: El Coordinador programa la sustentación.

- Leer: Todos los actores asignados a la práctica.

- Actualizar: Solo antes del cierre de la práctica.

- Eliminar: PROHIBIDO.

### Invariantes OCL

> context Sustentacion
>
> \-- Debe tener al menos 1 jurado asignado
>
> inv minimoUnJurado:
>
> self.jurados-\>size() \>= 1
>
> \-- El acta es obligatoria antes de registrar el resultado
>
> inv actaObligatoria:
>
> self.resultado-\>notEmpty() implies
>
> self.actaCargada = true
>
> \-- Toda practica debe tener sustentacion antes del cierre
>
> inv obligatoriaAntesCierre:
>
> self.practica.estado = EstadoPractica::CERRADA implies
>
> self.practica.sustentacion-\>notEmpty()
>
> \-- Resultado valido
>
> inv resultadoValido:
>
> Set{ResultadoSustentacion::APROBADO, ResultadoSustentacion::REPROBADO}
>
> -\>includes(self.resultado)
>
> \-- La fecha de sustentacion debe ser posterior al inicio de la
> practica
>
> inv fechaPosteriorAlInicio:
>
> self.fecha \> self.practica.fechaInicio
>
> \-- Solo la coordinacion programa la sustentacion
>
> inv soloCoordinacionPrograma:
>
> self.programadaPor.rol = Rol::COORDINADOR_PRACTICAS

## 4.14 Encuesta de Satisfacción

El sistema gestiona dos tipos de encuestas de satisfacción: una para el
Tutor Empresarial y otra para el Estudiante. Ambas son obligatorias
antes del cierre formal de la práctica.

### Multiplicidades

- Una Practica tiene 0..2 Encuestas (una por tipo: PARA_TUTOR y
  PARA_ESTUDIANTE).

- Cada Encuesta tiene al menos 1 pregunta.

- Ambas encuestas deben estar en estado COMPLETADA antes del cierre
  formal.

### Operaciones CRUD

- Crear: La Coordinación crea la encuesta y la envía al actor
  correspondiente.

- Leer: El actor receptor responde; el Coordinador y la Dirección ven
  resultados agregados.

- Actualizar: El actor puede guardar en borrador y completarla después.

- Eliminar: PROHIBIDO.

### Invariantes OCL

> context Encuesta
>
> \-- El tipo debe ser uno de los valores definidos
>
> inv tipoValido:
>
> Set{TipoEncuesta::PARA_TUTOR, TipoEncuesta::PARA_ESTUDIANTE}
>
> -\>includes(self.tipo)
>
> \-- Debe tener al menos una pregunta
>
> inv tienePreguntas:
>
> self.preguntas-\>size() \>= 1
>
> \-- No puede estar completada sin haber sido enviada primero
>
> inv completadaImplicaEnviada:
>
> self.estado = EstadoEncuesta::COMPLETADA implies
>
> self.fechaEnvio-\>notEmpty()
>
> \-- Obligatoria antes del cierre de la practica
>
> inv obligatoriaAntesCierre:
>
> self.practica.estado = EstadoPractica::CERRADA implies
>
> self.estado = EstadoEncuesta::COMPLETADA
>
> \-- Solo la coordinacion envia la encuesta
>
> inv soloCoordinacionEnvia:
>
> self.enviadaPor.rol = Rol::COORDINADOR_PRACTICAS
>
> \-- Si tipo = ParaTutor, solo el tutor asignado a esa practica la
> responde
>
> inv tutorCorrectoResponde:
>
> self.tipo = TipoEncuesta::PARA_TUTOR implies
>
> self.respondidaPor = self.practica.tutorEmpresarial
