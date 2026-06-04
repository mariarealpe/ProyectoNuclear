-- ================================================================
-- SISTEMA DE GESTIÓN DE PRÁCTICAS EMPRESARIALES — CUE
-- Script completo de inicialización MySQL
-- Universidad Alexander Von Humboldt — v1.2
-- Sprint 1 + Sprint 2
-- ================================================================
--
-- INSTRUCCIONES DE USO:
--
--   1. Abre phpMyAdmin o cualquier cliente MySQL
--   2. Ejecuta TODO este archivo (PARTE 1 + PARTE 2 + PARTE 3)
--   3. Inicia el backend Spring Boot UNA VEZ
--      → DataInitializer crea los usuarios con contraseñas BCrypt correctas
--   4. Ejecuta la PARTE 4 (datos que dependen de los usuarios)
--
--   Credenciales de prueba una vez que el backend arranque:
--     Admin DTI:        dti@cue.edu.co          / Admin2026!
--     Coordinación:     coordinacion@cue.edu.co  / Test2026!
--     Secretaria:       secretaria@cue.edu.co    / Test2026!
--     Coord. Prácticas: coordpracticas@cue.edu.co/ Test2026!
--     Docente Asesor:   docente@cue.edu.co       / Test2026!
--     Tutor Empresarial:tutor@cue.edu.co         / Test2026!
--     Estudiante:       estudiante@cue.edu.co    / Test2026!
--     Dirección:        direccion@cue.edu.co     / Test2026!
--
-- ================================================================


-- ================================================================
-- PARTE 1: BASE DE DATOS
-- ================================================================

CREATE DATABASE IF NOT EXISTS practicas_cue
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE practicas_cue;

-- Desactivar temporalmente las FK para poder crear tablas en cualquier orden
SET FOREIGN_KEY_CHECKS = 0;
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';


-- ================================================================
-- PARTE 2: ESQUEMA — TABLAS (en orden de dependencias)
-- ================================================================

-- ----------------------------------------------------------------
-- TABLA: facultades
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS facultades (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(150) NOT NULL,
    descripcion     VARCHAR(500)          DEFAULT NULL,
    activa          TINYINT(1)   NOT NULL DEFAULT 1,
    creada_en       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizada_en  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_facultad_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: programas
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS programas (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    nombre                  VARCHAR(150) NOT NULL,
    descripcion             VARCHAR(500)          DEFAULT NULL,
    facultad_id             BIGINT       NOT NULL,
    numero_total_practicas  INT          NOT NULL DEFAULT 1,
    promedio_minimo_general DOUBLE                DEFAULT 3.0,
    activo                  TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_programa_facultad FOREIGN KEY (facultad_id)
        REFERENCES facultades (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: requisitos_practica
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS requisitos_practica (
    id                               BIGINT      NOT NULL AUTO_INCREMENT,
    programa_id                      BIGINT      NOT NULL,
    numero_practica                  INT         NOT NULL,
    creditos_minimos                 INT         NOT NULL DEFAULT 0,
    promedio_minimo                  DOUBLE      NOT NULL DEFAULT 0.0,
    requiere_practica_anterior_aprobada TINYINT(1) NOT NULL DEFAULT 0,
    documentos_requeridos            VARCHAR(1000)        DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_req_programa_practica (programa_id, numero_practica),
    CONSTRAINT fk_requisito_programa FOREIGN KEY (programa_id)
        REFERENCES programas (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: usuarios
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(200) NOT NULL,
    correo              VARCHAR(150) NOT NULL,
    password_hash       TEXT         NOT NULL,
    telefono            VARCHAR(20)           DEFAULT NULL,
    foto_perfil         VARCHAR(500)          DEFAULT NULL,
    rol                 VARCHAR(30)  NOT NULL,
    etiqueta_cargo      VARCHAR(30)           DEFAULT NULL,
    activo              TINYINT(1)   NOT NULL DEFAULT 1,
    primer_ingreso      TINYINT(1)   NOT NULL DEFAULT 1,
    ultimo_acceso       DATETIME              DEFAULT NULL,
    -- Scope por rol
    facultad_id         BIGINT                DEFAULT NULL,  -- COORDINACION_ACADEMICA
    programa_id         BIGINT                DEFAULT NULL,  -- COORDINADOR_PRACTICAS, ESTUDIANTE
    -- Campos exclusivos del ESTUDIANTE
    estado_estudiante   VARCHAR(15)           DEFAULT 'NO_APTO',
    motivo_no_apto      VARCHAR(500)          DEFAULT NULL,
    creditos_aprobados  INT                   DEFAULT 0,
    promedio_acumulado  DOUBLE                DEFAULT 0.0,
    creado_en           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_usuario_correo (correo),
    KEY idx_usuario_rol (rol),
    CONSTRAINT fk_usuario_facultad FOREIGN KEY (facultad_id)
        REFERENCES facultades (id),
    CONSTRAINT fk_usuario_programa FOREIGN KEY (programa_id)
        REFERENCES programas (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: empresas
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS empresas (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    nit                 VARCHAR(30)  NOT NULL,
    razon_social        VARCHAR(200) NOT NULL,
    nombre_comercial    VARCHAR(200)          DEFAULT NULL,
    sector              VARCHAR(100)          DEFAULT NULL,
    direccion           VARCHAR(300)          DEFAULT NULL,
    ciudad              VARCHAR(100)          DEFAULT NULL,
    correo_contacto     VARCHAR(150) NOT NULL,
    telefono            VARCHAR(20)           DEFAULT NULL,
    sitio_web           VARCHAR(300)          DEFAULT NULL,
    representante_legal VARCHAR(200)          DEFAULT NULL,
    descripcion         VARCHAR(1000)         DEFAULT NULL,
    activo              TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY idx_empresa_nit (nit),
    KEY idx_empresa_razon_social (razon_social)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: tutores_empresariales
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tutores_empresariales (
    id                     BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id             BIGINT      NOT NULL,
    empresa_id             BIGINT      NOT NULL,
    cargo                  VARCHAR(150) NOT NULL,
    area                   VARCHAR(150)         DEFAULT NULL,
    telefono_corporativo   VARCHAR(20)          DEFAULT NULL,
    es_responsable_principal TINYINT(1) NOT NULL DEFAULT 0,
    activo                 TINYINT(1)  NOT NULL DEFAULT 1,
    creado_en              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY idx_tutor_usuario (usuario_id),
    KEY idx_tutor_empresa (empresa_id),
    CONSTRAINT fk_tutor_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id),
    CONSTRAINT fk_tutor_empresa  FOREIGN KEY (empresa_id)  REFERENCES empresas (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: expedientes
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS expedientes (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    estudiante_id         BIGINT       NOT NULL,
    documentos_entregados VARCHAR(2000)         DEFAULT NULL,
    creado_en             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY idx_expediente_estudiante (estudiante_id),
    CONSTRAINT fk_expediente_estudiante FOREIGN KEY (estudiante_id)
        REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: hojas_vida
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hojas_vida (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    estudiante_id  BIGINT        NOT NULL,
    url_documento  VARCHAR(1000) NOT NULL,
    creado_en      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY idx_hoja_vida_estudiante (estudiante_id),
    CONSTRAINT fk_hv_estudiante FOREIGN KEY (estudiante_id)
        REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: practicas_catalogo  (Patrón Prototype — plantilla base)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS practicas_catalogo (
    id                               BIGINT       NOT NULL AUTO_INCREMENT,
    programa_id                      BIGINT       NOT NULL,
    numero_practica                  INT          NOT NULL,
    nombre                           VARCHAR(200) NOT NULL,
    descripcion                      VARCHAR(1000)         DEFAULT NULL,
    creditos_minimos                 INT                   DEFAULT 0,
    promedio_minimo                  DOUBLE                DEFAULT 0.0,
    requiere_practica_anterior_aprobada TINYINT(1)         DEFAULT 0,
    documentos_requeridos            VARCHAR(1000)         DEFAULT NULL,
    activo                           TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en                        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en                   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_catalogo_programa_numero (programa_id, numero_practica),
    KEY idx_catalogo_programa (programa_id),
    KEY idx_catalogo_numero (numero_practica),
    CONSTRAINT fk_catalogo_programa FOREIGN KEY (programa_id)
        REFERENCES programas (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: practicas  (instancias creadas por Prototype al marcar APTO)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS practicas (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    estudiante_id         BIGINT       NOT NULL,
    programa_id           BIGINT       NOT NULL,
    expediente_id         BIGINT                DEFAULT NULL,
    catalogo_id           BIGINT                DEFAULT NULL,
    docente_asesor_id     BIGINT                DEFAULT NULL,
    tutor_empresarial_id  BIGINT                DEFAULT NULL,
    numero_practica       INT          NOT NULL,
    nombre                VARCHAR(200) NOT NULL,
    descripcion           VARCHAR(1000)         DEFAULT NULL,
    documentos_requeridos VARCHAR(1000)         DEFAULT NULL,
    estado                VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE_ASIGNACION',
    notas_cerradas        TINYINT(1)   NOT NULL DEFAULT 0,
    creado_en             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_practica_estudiante (estudiante_id),
    KEY idx_practica_estado     (estado),
    KEY idx_practica_docente    (docente_asesor_id),
    KEY idx_practica_tutor      (tutor_empresarial_id),
    CONSTRAINT fk_practica_estudiante  FOREIGN KEY (estudiante_id)        REFERENCES usuarios (id),
    CONSTRAINT fk_practica_programa    FOREIGN KEY (programa_id)          REFERENCES programas (id),
    CONSTRAINT fk_practica_expediente  FOREIGN KEY (expediente_id)        REFERENCES expedientes (id),
    CONSTRAINT fk_practica_catalogo    FOREIGN KEY (catalogo_id)          REFERENCES practicas_catalogo (id),
    CONSTRAINT fk_practica_docente     FOREIGN KEY (docente_asesor_id)    REFERENCES usuarios (id),
    CONSTRAINT fk_practica_tutor       FOREIGN KEY (tutor_empresarial_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: vacantes
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vacantes (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    titulo              VARCHAR(200)  NOT NULL,
    descripcion         VARCHAR(2000) NOT NULL,
    empresa_id          BIGINT        NOT NULL,
    tutor_id            BIGINT                 DEFAULT NULL,
    programa_id         BIGINT        NOT NULL,
    modalidad           VARCHAR(20)   NOT NULL DEFAULT 'PRESENCIAL',
    jornada             VARCHAR(30)   NOT NULL DEFAULT 'TIEMPO_COMPLETO',
    ciudad              VARCHAR(100)           DEFAULT NULL,
    cupos               INT           NOT NULL DEFAULT 1,
    cupos_ocupados      INT           NOT NULL DEFAULT 0,
    duracion_meses      INT                    DEFAULT 6,
    remunerada          TINYINT(1)    NOT NULL DEFAULT 0,
    valor_remuneracion  DECIMAL(10,2)          DEFAULT NULL,
    fecha_inicio        DATE                   DEFAULT NULL,
    fecha_fin           DATE                   DEFAULT NULL,
    fecha_publicacion   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre        DATETIME               DEFAULT NULL,
    requisitos          VARCHAR(2000)          DEFAULT NULL,
    responsabilidades   VARCHAR(2000)          DEFAULT NULL,
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo      VARCHAR(1000)          DEFAULT NULL,
    aprobador_id        BIGINT                 DEFAULT NULL,
    fecha_decision      DATETIME               DEFAULT NULL,
    actualizado_en      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_vacante_empresa     (empresa_id),
    KEY idx_vacante_programa    (programa_id),
    KEY idx_vacante_estado      (estado),
    KEY idx_vacante_publicacion (fecha_publicacion),
    CONSTRAINT fk_vacante_empresa   FOREIGN KEY (empresa_id)   REFERENCES empresas (id),
    CONSTRAINT fk_vacante_tutor     FOREIGN KEY (tutor_id)     REFERENCES tutores_empresariales (id),
    CONSTRAINT fk_vacante_programa  FOREIGN KEY (programa_id)  REFERENCES programas (id),
    CONSTRAINT fk_vacante_aprobador FOREIGN KEY (aprobador_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: bitacora_auditoria
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bitacora_auditoria (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id            BIGINT                DEFAULT NULL,
    nombre_usuario        VARCHAR(200)          DEFAULT NULL,
    rol_usuario           VARCHAR(30)           DEFAULT NULL,
    etiqueta_cargo_usuario VARCHAR(30)          DEFAULT NULL,
    fecha_hora            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modulo                VARCHAR(100) NOT NULL,
    tipo_accion           VARCHAR(30)  NOT NULL,
    registro_afectado_id  BIGINT                DEFAULT NULL,
    registro_afectado_tipo VARCHAR(100)         DEFAULT NULL,
    valores_anteriores    TEXT                  DEFAULT NULL,
    valores_nuevos        TEXT                  DEFAULT NULL,
    ip_origen             VARCHAR(50)           DEFAULT NULL,
    exitoso               TINYINT(1)   NOT NULL DEFAULT 1,
    motivo_fallo          VARCHAR(500)          DEFAULT NULL,

    PRIMARY KEY (id),
    KEY idx_bitacora_fecha   (fecha_hora),
    KEY idx_bitacora_usuario (usuario_id),
    KEY idx_bitacora_tipo    (tipo_accion),
    CONSTRAINT fk_bitacora_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: bitacora_vacante
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bitacora_vacante (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    vacante_id      BIGINT        NOT NULL,
    usuario_id      BIGINT                 DEFAULT NULL,
    estado_anterior VARCHAR(20)            DEFAULT NULL,
    estado_nuevo    VARCHAR(20)   NOT NULL,
    motivo          VARCHAR(1000)          DEFAULT NULL,
    fecha_hora      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_bvacante_vacante (vacante_id),
    KEY idx_bvacante_fecha   (fecha_hora),
    CONSTRAINT fk_bvacante_vacante  FOREIGN KEY (vacante_id)  REFERENCES vacantes (id),
    CONSTRAINT fk_bvacante_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: bitacora_validacion
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bitacora_validacion (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    expediente_id   BIGINT      NOT NULL,
    estudiante_id   BIGINT      NOT NULL,
    validador_id    BIGINT               DEFAULT NULL,
    estado_anterior VARCHAR(15)          DEFAULT NULL,
    estado_nuevo    VARCHAR(15)          DEFAULT NULL,
    motivo          VARCHAR(500)         DEFAULT NULL,
    fecha_hora      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_validacion_estudiante (estudiante_id),
    KEY idx_validacion_fecha      (fecha_hora),
    CONSTRAINT fk_validacion_expediente FOREIGN KEY (expediente_id) REFERENCES expedientes (id),
    CONSTRAINT fk_validacion_estudiante FOREIGN KEY (estudiante_id) REFERENCES usuarios (id),
    CONSTRAINT fk_validacion_validador  FOREIGN KEY (validador_id)  REFERENCES usuarios (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Reactivar comprobación de claves foráneas
SET FOREIGN_KEY_CHECKS = 1;


-- ================================================================
-- PARTE 3: DATOS MAESTROS DE REFERENCIA
-- (No dependen de usuarios — ejecutar antes de iniciar el backend)
-- ================================================================

-- ----------------------------------------------------------------
-- Facultades
-- ----------------------------------------------------------------
INSERT INTO facultades (nombre, descripcion, activa) VALUES
    ('Facultad de Ingeniería',
     'Programas de ingeniería de sistemas, electrónica y afines',
     1),
    ('Facultad de Ciencias Empresariales',
     'Programas de administración, contaduría y negocios internacionales',
     1)
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);


-- ----------------------------------------------------------------
-- Programas académicos
-- ----------------------------------------------------------------
INSERT INTO programas (nombre, descripcion, facultad_id, numero_total_practicas, promedio_minimo_general, activo) VALUES
    ('Ingeniería de Sistemas',
     'Formación en desarrollo de software, redes y bases de datos',
     1, 2, 3.2, 1),
    ('Administración de Empresas',
     'Formación en gestión organizacional y estrategia empresarial',
     2, 1, 3.0, 1),
    ('Ingeniería Electrónica',
     'Formación en sistemas embebidos, automatización y telecomunicaciones',
     1, 1, 3.0, 1)
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);


-- ----------------------------------------------------------------
-- Requisitos por número de práctica (Builder — configurables)
-- ----------------------------------------------------------------
INSERT INTO requisitos_practica
    (programa_id, numero_practica, creditos_minimos, promedio_minimo, requiere_practica_anterior_aprobada, documentos_requeridos)
VALUES
    -- Ingeniería de Sistemas — Práctica 1
    (1, 1, 80, 3.0, 0,
     'Hoja de vida actualizada,Carta de presentación,Fotocopia del documento de identidad,Certificado de notas'),
    -- Ingeniería de Sistemas — Práctica 2
    (1, 2, 120, 3.2, 1,
     'Hoja de vida actualizada,Carta de presentación,Informe práctica anterior aprobada,Paz y salvo academia'),
    -- Administración de Empresas — Práctica 1
    (2, 1, 60, 3.0, 0,
     'Hoja de vida actualizada,Carta de presentación,Fotocopia del documento de identidad'),
    -- Ingeniería Electrónica — Práctica 1
    (3, 1, 70, 3.0, 0,
     'Hoja de vida actualizada,Carta de presentación,Fotocopia del documento de identidad')
ON DUPLICATE KEY UPDATE
    creditos_minimos = VALUES(creditos_minimos),
    promedio_minimo  = VALUES(promedio_minimo),
    documentos_requeridos = VALUES(documentos_requeridos);


-- ----------------------------------------------------------------
-- Catálogo de prácticas (Prototype — plantillas institucionales)
-- ----------------------------------------------------------------
INSERT INTO practicas_catalogo
    (programa_id, numero_practica, nombre, descripcion,
     creditos_minimos, promedio_minimo, requiere_practica_anterior_aprobada,
     documentos_requeridos, activo)
VALUES
    -- Sistemas: Práctica 1
    (1, 1, 'Práctica Empresarial I — Sistemas',
     'Primera práctica de inmersión en entorno laboral de desarrollo de software',
     80, 3.0, 0,
     'Hoja de vida,Plan de práctica,Carta aval de la empresa,Póliza de responsabilidad civil',
     1),
    -- Sistemas: Práctica 2
    (1, 2, 'Práctica Empresarial II — Sistemas',
     'Segunda práctica con roles de mayor responsabilidad en proyectos tecnológicos',
     120, 3.2, 1,
     'Hoja de vida,Plan de práctica,Informe semestral,Carta aval de la empresa,Acta de inicio',
     1),
    -- Administración: Práctica 1
    (2, 1, 'Práctica Empresarial — Administración',
     'Práctica de gestión organizacional en empresas del sector productivo o servicios',
     60, 3.0, 0,
     'Hoja de vida,Plan de práctica,Carta aval de la empresa',
     1),
    -- Electrónica: Práctica 1
    (3, 1, 'Práctica Empresarial — Electrónica',
     'Práctica en automatización, mantenimiento o telecomunicaciones',
     70, 3.0, 0,
     'Hoja de vida,Plan de práctica,Carta aval de la empresa,Certificado de aptitud técnica',
     1)
ON DUPLICATE KEY UPDATE
    nombre       = VALUES(nombre),
    descripcion  = VALUES(descripcion),
    activo       = VALUES(activo);


-- ----------------------------------------------------------------
-- Empresas vinculadas
-- ----------------------------------------------------------------
INSERT INTO empresas
    (nit, razon_social, nombre_comercial, sector, direccion, ciudad,
     correo_contacto, telefono, sitio_web, representante_legal, descripcion, activo)
VALUES
    ('900123456-7', 'TechCo S.A.', 'TechCo',
     'Desarrollo de Software', 'Calle 10 # 5-20', 'Armenia',
     'contacto@techco.com.co', '3001234567', 'https://techco.com.co',
     'Carlos Restrepo', 'Empresa líder en desarrollo de aplicaciones empresariales', 1),

    ('800456789-3', 'DataSystems Ltda.', 'DataSys',
     'Tecnología e Innovación', 'Cra 15 # 22-40', 'Pereira',
     'info@datasystems.com.co', '3101112233', 'https://datasystems.com.co',
     'Marcela Torres', 'Especialistas en inteligencia de negocios y analítica de datos', 1),

    ('700987654-1', 'Grupo Empresarial Andino S.A.S.', 'GEA',
     'Consultoría Empresarial', 'Av. Bolívar # 3-15', 'Manizales',
     'practicas@gea.com.co', '3209876543', NULL,
     'Andrés Ospina', 'Firma de consultoría en estrategia y transformación organizacional', 1),

    ('600112233-5', 'Automatización Industrial del Eje S.A.', 'AIEJE',
     'Automatización y Control', 'Zona Industrial Norte, Bodega 7', 'Armenia',
     'rrhh@aieje.com.co', '3152233445', NULL,
     'Luis Hernández', 'Integración de sistemas de control y automatización industrial', 1)
ON DUPLICATE KEY UPDATE
    razon_social = VALUES(razon_social),
    activo       = VALUES(activo);


-- ================================================================
-- FIN PARTE 3
--
-- Ahora inicia el backend Spring Boot UNA VEZ.
-- DataInitializer creará los 8 usuarios de prueba con
-- contraseñas BCrypt correctas.
--
-- Una vez arrancado y con los usuarios creados,
-- ejecuta la PARTE 4 de abajo.
-- ================================================================


-- ================================================================
-- PARTE 4: DATOS DE INTEGRACIÓN
-- (Ejecutar DESPUÉS del primer arranque del backend)
-- ================================================================

-- ----------------------------------------------------------------
-- 4.1  Asignar scopes (facultad / programa) a los usuarios de prueba
--      que crea DataInitializer
-- ----------------------------------------------------------------
UPDATE usuarios
    SET facultad_id = 1
    WHERE correo IN ('coordinacion@cue.edu.co', 'secretaria@cue.edu.co')
      AND facultad_id IS NULL;

UPDATE usuarios
    SET programa_id = 1
    WHERE correo IN ('coordpracticas@cue.edu.co', 'estudiante@cue.edu.co')
      AND programa_id IS NULL;


-- ----------------------------------------------------------------
-- 4.2  Vincular el tutor de prueba con su empresa
-- ----------------------------------------------------------------
INSERT IGNORE INTO tutores_empresariales
    (usuario_id, empresa_id, cargo, area, es_responsable_principal, activo, creado_en, actualizado_en)
SELECT
    u.id,
    1,                        -- TechCo S.A.
    'Líder de Desarrollo',
    'Ingeniería de Software',
    1,
    1,
    NOW(),
    NOW()
FROM usuarios u
WHERE u.correo = 'tutor@cue.edu.co'
  AND NOT EXISTS (
      SELECT 1 FROM tutores_empresariales te WHERE te.usuario_id = u.id
  );


-- ----------------------------------------------------------------
-- 4.3  Expediente vacío para el estudiante de prueba
--      (se crea automáticamente al marcar APTO en producción,
--       aquí lo generamos manualmente para tests)
-- ----------------------------------------------------------------
INSERT IGNORE INTO expedientes (estudiante_id, creado_en, actualizado_en)
SELECT id, NOW(), NOW()
FROM usuarios
WHERE correo = 'estudiante@cue.edu.co'
  AND NOT EXISTS (
      SELECT 1 FROM expedientes e WHERE e.estudiante_id = usuarios.id
  );


-- ----------------------------------------------------------------
-- 4.4  Vacantes de prueba
-- ----------------------------------------------------------------
INSERT INTO vacantes
    (titulo, descripcion, empresa_id, tutor_id, programa_id,
     modalidad, jornada, ciudad, cupos, duracion_meses,
     remunerada, valor_remuneracion, fecha_inicio, fecha_fin,
     requisitos, responsabilidades, estado)
VALUES
    -- Vacante DISPONIBLE (ya aprobada) — para probar asignación
    ('Desarrollador Backend Java',
     'Práctica en el equipo de backend desarrollando microservicios con Spring Boot y MySQL. '
     'Participarás en el ciclo completo: diseño, desarrollo y despliegue.',
     1,                        -- TechCo S.A.
     (SELECT id FROM tutores_empresariales LIMIT 1),
     1,                        -- Ing. de Sistemas
     'PRESENCIAL', 'TIEMPO_COMPLETO', 'Armenia',
     2, 6,
     1, 1300000.00,
     '2026-07-01', '2026-12-31',
     'Conocimientos en Java 17+, Spring Boot, SQL. Inglés básico.',
     'Desarrollar APIs REST, escribir pruebas unitarias, participar en code reviews.',
     'DISPONIBLE'),

    -- Vacante PENDIENTE — para probar flujo de aprobación
    ('Analista de Datos Jr.',
     'Práctica en análisis de datos empresariales con Python, Power BI y SQL Server. '
     'Apoyo en la construcción de dashboards y reportes gerenciales.',
     2,                        -- DataSystems Ltda.
     NULL,
     1,                        -- Ing. de Sistemas
     'HIBRIDO', 'TIEMPO_COMPLETO', 'Pereira',
     1, 6,
     1, 1200000.00,
     '2026-07-15', '2026-12-15',
     'Conocimientos en SQL, Excel avanzado. Python básico deseable.',
     'Construir reportes en Power BI, limpiar y transformar datos, apoyar al área de BI.',
     'PENDIENTE'),

    -- Vacante para Administración
    ('Practicante Gestión Humana',
     'Apoyo al área de Talento Humano en procesos de selección, inducción y bienestar laboral.',
     3,                        -- Grupo Empresarial Andino
     NULL,
     2,                        -- Administración de Empresas
     'PRESENCIAL', 'MEDIO_TIEMPO', 'Manizales',
     1, 6,
     0, NULL,
     '2026-07-01', '2026-12-31',
     'Estudiante de Administración con interés en RRHH. Excel intermedio.',
     'Apoyar procesos de selección, actualizar bases de datos de candidatos, coordinar inducciones.',
     'PENDIENTE'),

    -- Vacante para Electrónica
    ('Practicante Automatización Industrial',
     'Práctica en mantenimiento y programación de PLCs en planta de producción.',
     4,                        -- AIEJE
     NULL,
     3,                        -- Ing. Electrónica
     'PRESENCIAL', 'TIEMPO_COMPLETO', 'Armenia',
     1, 6,
     1, 1100000.00,
     '2026-08-01', '2027-01-31',
     'Conocimientos en PLCs (Siemens/Allen Bradley), instrumentación básica.',
     'Programar y mantener PLCs, documentar procedimientos, apoyar diagnóstico de fallas.',
     'PENDIENTE');


-- ----------------------------------------------------------------
-- 4.5  Práctica asignada al estudiante de prueba (Prototype ejecutado)
-- ----------------------------------------------------------------
INSERT IGNORE INTO practicas
    (estudiante_id, programa_id, expediente_id, catalogo_id,
     numero_practica, nombre, descripcion, documentos_requeridos, estado)
SELECT
    u.id,
    1,                         -- Ing. de Sistemas
    e.id,
    c.id,
    1,
    c.nombre,
    c.descripcion,
    c.documentos_requeridos,
    'PENDIENTE_ASIGNACION'
FROM usuarios u
JOIN expedientes e ON e.estudiante_id = u.id
JOIN practicas_catalogo c ON c.programa_id = 1 AND c.numero_practica = 1
WHERE u.correo = 'estudiante@cue.edu.co'
  AND NOT EXISTS (
      SELECT 1 FROM practicas p
      WHERE p.estudiante_id = u.id AND p.numero_practica = 1
  );


-- ----------------------------------------------------------------
-- 4.6  Entradas de bitácora de ejemplo
-- ----------------------------------------------------------------
INSERT INTO bitacora_auditoria
    (usuario_id, nombre_usuario, rol_usuario, etiqueta_cargo_usuario,
     fecha_hora, modulo, tipo_accion,
     registro_afectado_tipo, exitoso)
SELECT
    u.id, u.nombre, u.rol, NULL,
    NOW(), 'AUTH', 'LOGIN_EXITOSO',
    'Usuario', 1
FROM usuarios u
WHERE u.correo = 'dti@cue.edu.co';

INSERT INTO bitacora_auditoria
    (usuario_id, nombre_usuario, rol_usuario, etiqueta_cargo_usuario,
     fecha_hora, modulo, tipo_accion,
     registro_afectado_tipo, exitoso)
SELECT
    u.id, u.nombre, u.rol, u.etiqueta_cargo,
    NOW(), 'USUARIOS', 'CREAR',
    'Usuario', 1
FROM usuarios u
WHERE u.correo = 'coordinacion@cue.edu.co';


-- ================================================================
-- VERIFICACIÓN FINAL
-- ================================================================
SELECT 'BASE DE DATOS' AS seccion, 'practicas_cue' AS valor
UNION ALL
SELECT 'Tablas creadas', CAST(COUNT(*) AS CHAR)
    FROM information_schema.tables
    WHERE table_schema = 'practicas_cue' AND table_type = 'BASE TABLE'
UNION ALL
SELECT 'Facultades',        CAST(COUNT(*) AS CHAR) FROM facultades
UNION ALL
SELECT 'Programas',         CAST(COUNT(*) AS CHAR) FROM programas
UNION ALL
SELECT 'Req. por práctica', CAST(COUNT(*) AS CHAR) FROM requisitos_practica
UNION ALL
SELECT 'Catálogo',          CAST(COUNT(*) AS CHAR) FROM practicas_catalogo
UNION ALL
SELECT 'Empresas',          CAST(COUNT(*) AS CHAR) FROM empresas
UNION ALL
SELECT 'Usuarios',          CAST(COUNT(*) AS CHAR) FROM usuarios
UNION ALL
SELECT 'Vacantes',          CAST(COUNT(*) AS CHAR) FROM vacantes;


-- ----------------------------------------------------------------
-- TABLA: evaluaciones_docente  (RF-08-01 — una evaluación por práctica)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evaluaciones_docente (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    practica_id     BIGINT      NOT NULL,
    docente_id      BIGINT      NOT NULL,
    nota            DOUBLE      NOT NULL,
    resultado       VARCHAR(20) NOT NULL,
    observaciones   LONGTEXT    NOT NULL,
    creado_en       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_evaluacion_practica (practica_id),
    KEY idx_evaluacion_docente (docente_id),
    CONSTRAINT fk_eval_practica FOREIGN KEY (practica_id) REFERENCES practicas (id),
    CONSTRAINT fk_eval_docente  FOREIGN KEY (docente_id)  REFERENCES usuarios  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ================================================================
-- SPRINT 4 — ALTERACIONES INCREMENTALES
-- Ejecutar SOLO si la BD ya existe (instalaciones anteriores al Sprint 4).
-- En instalaciones nuevas el CREATE TABLE de practicas ya incluye estos campos.
-- ================================================================

-- RF-08-01 base: relación formal Practica ↔ Docente Asesor
ALTER TABLE practicas
    ADD COLUMN IF NOT EXISTS docente_asesor_id BIGINT DEFAULT NULL,
    ADD KEY IF NOT EXISTS idx_practica_docente (docente_asesor_id);

-- FK separada para evitar error si la columna ya existía con otra FK
ALTER TABLE practicas
    ADD CONSTRAINT IF NOT EXISTS fk_practica_docente
        FOREIGN KEY (docente_asesor_id) REFERENCES usuarios (id);


-- ----------------------------------------------------------------
-- RF-08-02 base: relación formal Practica ↔ Tutor Empresarial
-- ----------------------------------------------------------------
ALTER TABLE practicas
    ADD COLUMN IF NOT EXISTS tutor_empresarial_id BIGINT DEFAULT NULL,
    ADD KEY IF NOT EXISTS idx_practica_tutor (tutor_empresarial_id);

ALTER TABLE practicas
    ADD CONSTRAINT IF NOT EXISTS fk_practica_tutor
        FOREIGN KEY (tutor_empresarial_id) REFERENCES usuarios (id);


-- ----------------------------------------------------------------
-- RF-08-04 base: flag de cierre de notas (inmutabilidad — Proxy)
-- ----------------------------------------------------------------
ALTER TABLE practicas
    ADD COLUMN IF NOT EXISTS notas_cerradas TINYINT(1) NOT NULL DEFAULT 0;


-- ----------------------------------------------------------------
-- TABLA: evaluaciones_tutor  (RF-08-02 — una evaluación por práctica)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evaluaciones_tutor (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    practica_id     BIGINT      NOT NULL,
    tutor_id        BIGINT      NOT NULL,
    nota            DOUBLE      NOT NULL,
    resultado       VARCHAR(20) NOT NULL,
    observaciones   LONGTEXT    NOT NULL,
    creado_en       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_eval_tutor_practica (practica_id),
    KEY idx_eval_tutor (tutor_id),
    CONSTRAINT fk_eval_tutor_practica FOREIGN KEY (practica_id) REFERENCES practicas (id),
    CONSTRAINT fk_eval_tutor_usuario  FOREIGN KEY (tutor_id)    REFERENCES usuarios  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: notas_finales  (RF-08-04 — nota final del Coordinador)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notas_finales (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    practica_id     BIGINT      NOT NULL,
    coordinador_id  BIGINT      NOT NULL,
    nota            DOUBLE      NOT NULL,
    resultado       VARCHAR(20) NOT NULL,
    observaciones   LONGTEXT             DEFAULT NULL,
    cerrada         TINYINT(1)  NOT NULL DEFAULT 0,
    cerrada_en      DATETIME             DEFAULT NULL,
    creado_en       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_nota_final_practica (practica_id),
    KEY idx_nota_final_coord (coordinador_id),
    CONSTRAINT fk_nota_final_practica FOREIGN KEY (practica_id)    REFERENCES practicas (id),
    CONSTRAINT fk_nota_final_coord    FOREIGN KEY (coordinador_id) REFERENCES usuarios  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------
-- TABLA: plantillas_notificacion  (RF-11-05 — plantillas configurables por evento)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS plantillas_notificacion (
    id                             BIGINT       NOT NULL AUTO_INCREMENT,
    evento                         VARCHAR(60)  NOT NULL,
    asunto                         VARCHAR(250) NOT NULL,
    cuerpo_html                    LONGTEXT     NOT NULL,
    rol_receptor                   VARCHAR(30)           DEFAULT NULL,
    obligatorio                    TINYINT(1)   NOT NULL DEFAULT 0,
    frecuencia_recordatorio_dias   INT          NOT NULL DEFAULT 3,
    activa                         TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en                      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_plantilla_evento (evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
