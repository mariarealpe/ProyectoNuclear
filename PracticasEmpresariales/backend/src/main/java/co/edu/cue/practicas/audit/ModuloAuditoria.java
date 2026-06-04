package co.edu.cue.practicas.audit;

/**
 * Constantes con los nombres de los módulos del sistema usados en la bitácora de auditoría.
 *
 * Centralizar estos valores evita escribir strings sueltos ("AUTH", "USUARIOS", etc.)
 * en cada servicio, lo que podría generar inconsistencias si alguien escribe el nombre
 * diferente (ej. "USUARIO" en lugar de "USUARIOS"). El compilador detecta el error
 * si se usa una constante que no existe.
 *
 * Se usa en todos los servicios al construir registros de BitacoraAuditoria:
 *   .modulo(ModuloAuditoria.AUTH)
 *   .modulo(ModuloAuditoria.USUARIOS)
 *   etc.
 */
public final class ModuloAuditoria {

    // Constructor privado para evitar instanciar esta clase; solo tiene constantes estáticas
    private ModuloAuditoria() {}

    /** Módulo de autenticación: login y cambio de contraseña */
    public static final String AUTH       = "AUTH";

    /** Módulo de gestión de usuarios: crear, editar, activar, desactivar */
    public static final String USUARIOS   = "USUARIOS";

    /** Módulo de gestión de facultades: crear, editar, desactivar */
    public static final String FACULTADES = "FACULTADES";

    /** Módulo de gestión de programas académicos: crear, desactivar */
    public static final String PROGRAMAS  = "PROGRAMAS";

    /** Módulo de gestión de empresas: crear, editar, activar, desactivar */
    public static final String EMPRESAS   = "EMPRESAS";

    /** Módulo de gestión de tutores empresariales: crear, editar, activar, desactivar */
    public static final String TUTORES_EMPRESARIALES = "TUTORES_EMPRESARIALES";

    /** Módulo de vacantes: crear, editar, aprobar, rechazar, cerrar */
    public static final String VACANTES   = "VACANTES";

    /** Módulo de evaluaciones del Docente Asesor — RF-08-01 */
    public static final String EVALUACIONES_DOCENTE = "EVALUACIONES_DOCENTE";

    /** Módulo de evaluaciones del Tutor Empresarial — RF-08-02 */
    public static final String EVALUACIONES_TUTOR = "EVALUACIONES_TUTOR";

    /** Módulo de la nota final registrada por el Coordinador — RF-08-04 */
    public static final String NOTA_FINAL = "NOTA_FINAL";

    /** Módulo de plantillas y notificaciones por correo — RF-11-05 */
    public static final String NOTIFICACIONES = "NOTIFICACIONES";

    /** Módulo de encuestas de cierre y autoevaluación — RF-08-05, RF-08-06 */
    public static final String ENCUESTAS = "ENCUESTAS";

    /** Módulo del cierre formal de práctica — RF-09-02, RF-09-03 */
    public static final String CIERRE_PRACTICA = "CIERRE_PRACTICA";
}
