SET NAMES utf8mb4;

DROP TABLE IF EXISTS bitacora;
DROP TABLE IF EXISTS comprobante;
DROP TABLE IF EXISTS detalle_repuesto;
DROP TABLE IF EXISTS detalle_servicio;
DROP TABLE IF EXISTS orden_trabajo;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS servicio_taller;
DROP TABLE IF EXISTS repuesto;
DROP TABLE IF EXISTS vehiculo;
DROP TABLE IF EXISTS mecanico;
DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente (
    id_cliente       BIGINT       NOT NULL AUTO_INCREMENT,
    tipo_documento   ENUM ('NIT', 'DPI') NOT NULL,
    numero_documento VARCHAR(20)  NOT NULL,
    nombre           VARCHAR(120) NOT NULL,
    telefono         VARCHAR(20)  NULL,
    correo           VARCHAR(120) NULL,
    direccion        VARCHAR(200) NULL,
    fecha_registro   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_documento UNIQUE (tipo_documento, numero_documento)
) ENGINE = InnoDB;

CREATE TABLE mecanico (
    id_mecanico  BIGINT       NOT NULL AUTO_INCREMENT,
    codigo       VARCHAR(20)  NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    especialidad VARCHAR(80)  NULL,
    telefono     VARCHAR(20)  NULL,
    CONSTRAINT pk_mecanico PRIMARY KEY (id_mecanico),
    CONSTRAINT uq_mecanico_codigo UNIQUE (codigo)
) ENGINE = InnoDB;

CREATE TABLE usuario (
    id_usuario     BIGINT       NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50)  NOT NULL,
    nombre         VARCHAR(120) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            ENUM ('ADMIN', 'RECEPCION', 'MECANICO') NOT NULL,
    id_mecanico    BIGINT       NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    version_token  INT          NOT NULL DEFAULT 0,
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso  DATETIME     NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT uq_usuario_mecanico UNIQUE (id_mecanico),
    CONSTRAINT fk_usuario_mecanico FOREIGN KEY (id_mecanico) REFERENCES mecanico (id_mecanico),
    CONSTRAINT ck_usuario_rol_mecanico CHECK ((rol = 'MECANICO' AND id_mecanico IS NOT NULL)
        OR (rol <> 'MECANICO' AND id_mecanico IS NULL)),
    CONSTRAINT ck_usuario_version CHECK (version_token >= 0)
) ENGINE = InnoDB;

CREATE TABLE vehiculo (
    id_vehiculo BIGINT      NOT NULL AUTO_INCREMENT,
    placa       VARCHAR(10) NOT NULL,
    marca       VARCHAR(50) NOT NULL,
    modelo      VARCHAR(50) NOT NULL,
    anio        SMALLINT    NOT NULL,
    color       VARCHAR(30) NULL,
    id_cliente  BIGINT      NOT NULL,
    CONSTRAINT pk_vehiculo PRIMARY KEY (id_vehiculo),
    CONSTRAINT uq_vehiculo_placa UNIQUE (placa),
    CONSTRAINT fk_vehiculo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_vehiculo_anio CHECK (anio BETWEEN 1900 AND 2100)
) ENGINE = InnoDB;

CREATE TABLE repuesto (
    id_repuesto BIGINT         NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(30)    NOT NULL,
    nombre      VARCHAR(100)   NOT NULL,
    descripcion VARCHAR(255)   NULL,
    precio      DECIMAL(10, 2) NOT NULL,
    existencia  INT            NOT NULL DEFAULT 0,
    CONSTRAINT pk_repuesto PRIMARY KEY (id_repuesto),
    CONSTRAINT uq_repuesto_codigo UNIQUE (codigo),
    CONSTRAINT ck_repuesto_precio CHECK (precio >= 0),
    CONSTRAINT ck_repuesto_existencia CHECK (existencia >= 0)
) ENGINE = InnoDB;

CREATE TABLE servicio_taller (
    id_servicio     BIGINT         NOT NULL AUTO_INCREMENT,
    codigo          VARCHAR(20)    NOT NULL,
    descripcion     VARCHAR(150)   NOT NULL,
    costo_mano_obra DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_servicio_taller PRIMARY KEY (id_servicio),
    CONSTRAINT uq_servicio_taller_codigo UNIQUE (codigo),
    CONSTRAINT ck_servicio_taller_costo CHECK (costo_mano_obra >= 0)
) ENGINE = InnoDB;

CREATE TABLE orden_trabajo (
    id_orden             BIGINT       NOT NULL AUTO_INCREMENT,
    id_vehiculo          BIGINT       NOT NULL,
    id_mecanico          BIGINT       NOT NULL,
    descripcion_problema VARCHAR(500) NOT NULL,
    estado               ENUM ('PENDIENTE', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio         DATETIME     NULL,
    fecha_finalizacion   DATETIME     NULL,
    fecha_cancelacion    DATETIME     NULL,
    motivo_cancelacion   VARCHAR(255) NULL,
    id_usuario_registro  BIGINT       NOT NULL,
    id_usuario_inicio    BIGINT       NULL,
    id_usuario_cierre    BIGINT       NULL,
    CONSTRAINT pk_orden_trabajo PRIMARY KEY (id_orden),
    CONSTRAINT fk_orden_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculo (id_vehiculo)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_orden_mecanico FOREIGN KEY (id_mecanico) REFERENCES mecanico (id_mecanico)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_orden_usuario_registro FOREIGN KEY (id_usuario_registro) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_orden_usuario_inicio FOREIGN KEY (id_usuario_inicio) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_orden_usuario_cierre FOREIGN KEY (id_usuario_cierre) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX ix_orden_estado (estado),
    INDEX ix_orden_fecha_creacion (fecha_creacion)
) ENGINE = InnoDB;

CREATE TABLE detalle_servicio (
    id_orden        BIGINT         NOT NULL,
    id_servicio     BIGINT         NOT NULL,
    costo_mano_obra DECIMAL(10, 2) NOT NULL,
    fecha_registro  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_detalle_servicio PRIMARY KEY (id_orden, id_servicio),
    CONSTRAINT fk_detalle_servicio_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo (id_orden)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_detalle_servicio_servicio FOREIGN KEY (id_servicio) REFERENCES servicio_taller (id_servicio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_detalle_servicio_costo CHECK (costo_mano_obra >= 0)
) ENGINE = InnoDB;

CREATE TABLE detalle_repuesto (
    id_detalle_repuesto BIGINT         NOT NULL AUTO_INCREMENT,
    id_orden            BIGINT         NOT NULL,
    id_repuesto         BIGINT         NOT NULL,
    cantidad            INT            NOT NULL,
    precio_unitario     DECIMAL(10, 2) NOT NULL,
    fecha_asignacion    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_detalle_repuesto PRIMARY KEY (id_detalle_repuesto),
    CONSTRAINT fk_detalle_repuesto_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo (id_orden)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_detalle_repuesto_repuesto FOREIGN KEY (id_repuesto) REFERENCES repuesto (id_repuesto)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_detalle_repuesto_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_repuesto_precio CHECK (precio_unitario >= 0)
) ENGINE = InnoDB;

CREATE TABLE comprobante (
    id_comprobante  BIGINT         NOT NULL AUTO_INCREMENT,
    id_orden        BIGINT         NOT NULL,
    numero          VARCHAR(20)    NOT NULL,
    fecha_emision   DATETIME       NOT NULL,
    total_mano_obra DECIMAL(12, 2) NOT NULL,
    total_repuestos DECIMAL(12, 2) NOT NULL,
    subtotal        DECIMAL(12, 2) NOT NULL,
    tasa_iva        DECIMAL(5, 4)  NOT NULL,
    iva             DECIMAL(12, 2) NOT NULL,
    total           DECIMAL(12, 2) NOT NULL,
    CONSTRAINT pk_comprobante PRIMARY KEY (id_comprobante),
    CONSTRAINT uq_comprobante_orden UNIQUE (id_orden),
    CONSTRAINT uq_comprobante_numero UNIQUE (numero),
    CONSTRAINT fk_comprobante_orden FOREIGN KEY (id_orden) REFERENCES orden_trabajo (id_orden)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_comprobante_montos CHECK (total_mano_obra >= 0 AND total_repuestos >= 0 AND iva >= 0 AND total >= 0),
    INDEX ix_comprobante_fecha_emision (fecha_emision)
) ENGINE = InnoDB;

CREATE TABLE bitacora (
    id_bitacora  BIGINT       NOT NULL AUTO_INCREMENT,
    fecha        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario   BIGINT       NULL,
    usuario      VARCHAR(50)  NOT NULL,
    accion       VARCHAR(40)  NOT NULL,
    entidad      VARCHAR(20)  NOT NULL,
    id_entidad   BIGINT       NULL,
    detalle      VARCHAR(500) NULL,
    direccion_ip VARCHAR(45)  NULL,
    CONSTRAINT pk_bitacora PRIMARY KEY (id_bitacora),
    CONSTRAINT fk_bitacora_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX ix_bitacora_fecha (fecha),
    INDEX ix_bitacora_entidad (entidad, id_entidad)
) ENGINE = InnoDB;
