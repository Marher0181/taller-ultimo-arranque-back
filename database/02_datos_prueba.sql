SET NAMES utf8mb4;

INSERT INTO cliente (id_cliente, tipo_documento, numero_documento, nombre, telefono, correo, direccion, fecha_registro) VALUES
    (1, 'NIT', '45890137', 'Transportes Quetzal, S.A.', '2334-5566', 'flota@transquetzal.com.gt', 'Calzada Roosevelt 22-43, Zona 11, Guatemala', '2026-08-03 09:15:00'),
    (2, 'DPI', '2543198740101', 'María Fernanda López García', '5512-3456', 'mafer.lopez@gmail.com', '12 Avenida 5-30, Zona 1, Guatemala', '2026-08-05 10:00:00'),
    (3, 'NIT', '7781245K', 'Carlos Enrique Méndez Ruiz', '4021-8899', 'cmendez@outlook.com', 'Boulevard Los Próceres 18-60, Zona 10, Guatemala', '2026-08-12 14:30:00'),
    (4, 'DPI', '3011765230301', 'Ana Lucía Morales Pérez', '3045-1122', 'ana.morales@yahoo.com', '5a. Calle Poniente 14, Antigua Guatemala', '2026-08-20 08:45:00'),
    (5, 'NIT', '3456781K', 'Distribuidora La Ceiba, S.A.', '2290-7744', 'compras@laceiba.com.gt', 'Km 15.5 Carretera a El Salvador, Santa Catarina Pinula', '2026-09-01 11:20:00'),
    (6, 'DPI', '1827364580901', 'José Roberto Hernández Castillo', '5788-6611', 'jrhernandez@gmail.com', '4a. Calle 12-25, Zona 3, Quetzaltenango', '2026-09-09 16:10:00');

INSERT INTO mecanico (id_mecanico, codigo, nombre, especialidad, telefono) VALUES
    (1, 'MEC-001', 'Luis Alberto Ramírez Soto', 'Motor y transmisión', '5501-2233'),
    (2, 'MEC-002', 'Pedro Pablo Ajú Xicará', 'Frenos y suspensión', '5502-3344'),
    (3, 'MEC-003', 'Sandra Yesenia Coc Caal', 'Electricidad automotriz', '5503-4455'),
    (4, 'MEC-004', 'Marvin Estuardo Gómez Paz', 'Mantenimiento preventivo', '5504-5566'),
    (5, 'MEC-005', 'Jorge Luis Tzul Batz', 'Alineación y balanceo', '5505-6677');

INSERT INTO usuario (id_usuario, username, nombre, password_hash, rol, id_mecanico, fecha_creacion) VALUES
    (1, 'admin', 'Administrador del Sistema', '$2a$10$V/OhEXxkRRr5i9Pz7.2hj.6XhL4x8.MAKVgc7hA8nhxweK3SkDlbG', 'ADMIN', NULL, '2026-08-01 08:00:00'),
    (2, 'recepcion', 'Karla Beatriz Recinos Solís', '$2a$10$gwrxwEW30Pi.SlA.b49ud.s8BaTvpKP9iYagTcKb.wLLmYA6ghQ6m', 'RECEPCION', NULL, '2026-08-01 08:05:00'),
    (3, 'lramirez', 'Luis Alberto Ramírez Soto', '$2a$10$EY6Ty8KvuP8Kd14k1nV6HO5GBMUCtMS/afsVeDAkY2cvaWStaYrOK', 'MECANICO', 1, '2026-08-01 08:10:00'),
    (4, 'paju', 'Pedro Pablo Ajú Xicará', '$2a$10$i4drAVFHW4qmWqJnlvKTSuzYA8R38QWhIEMhTV42gv0O1a9k5qChm', 'MECANICO', 2, '2026-08-01 08:15:00');

INSERT INTO vehiculo (id_vehiculo, placa, marca, modelo, anio, color, id_cliente) VALUES
    (1, 'C512BCD', 'Isuzu', 'NPR', 2019, 'Blanco', 1),
    (2, 'C778KLM', 'Hino', 'Dutro', 2021, 'Blanco', 1),
    (3, 'P123ABC', 'Toyota', 'Corolla', 2018, 'Gris', 2),
    (4, 'P456DEF', 'Honda', 'CR-V', 2020, 'Negro', 3),
    (5, 'P789GHJ', 'Mazda', 'CX-5', 2022, 'Rojo', 4),
    (6, 'C334NPQ', 'Toyota', 'Hilux', 2017, 'Plateado', 5),
    (7, 'M902RST', 'Yamaha', 'FZ 2.0', 2023, 'Azul', 6),
    (8, 'P246XYZ', 'Kia', 'Rio', 2015, 'Blanco', 6);

INSERT INTO servicio_taller (id_servicio, codigo, descripcion, costo_mano_obra) VALUES
    (1, 'SRV-001', 'Cambio de aceite y filtro', 150.00),
    (2, 'SRV-002', 'Alineación computarizada', 175.00),
    (3, 'SRV-003', 'Balanceo de 4 llantas', 120.00),
    (4, 'SRV-004', 'Revisión del sistema de frenos', 200.00),
    (5, 'SRV-005', 'Cambio de pastillas y rectificado de discos', 250.00),
    (6, 'SRV-006', 'Diagnóstico electrónico con escáner', 300.00),
    (7, 'SRV-007', 'Afinamiento mayor', 650.00),
    (8, 'SRV-008', 'Cambio de batería y revisión de carga', 75.00);

INSERT INTO repuesto (id_repuesto, codigo, nombre, descripcion, precio, existencia) VALUES
    (1, 'REP-001', 'Aceite sintético 5W-30', 'Galón de aceite sintético para motor a gasolina', 245.00, 27),
    (2, 'REP-002', 'Filtro de aceite', 'Filtro de aceite universal roscado', 65.00, 23),
    (3, 'REP-003', 'Filtro de aire', 'Filtro de aire de motor', 110.00, 11),
    (4, 'REP-004', 'Pastillas de freno delanteras', 'Juego de pastillas cerámicas delanteras', 385.00, 6),
    (5, 'REP-005', 'Disco de freno', 'Disco de freno ventilado delantero', 520.00, 4),
    (6, 'REP-006', 'Bujía de iridio', 'Bujía de iridio de larga duración', 95.00, 10),
    (7, 'REP-007', 'Batería 12V 60Ah', 'Batería libre de mantenimiento', 1150.00, 0),
    (8, 'REP-008', 'Líquido de frenos DOT 4', 'Botella de 500 ml', 70.00, 4),
    (9, 'REP-009', 'Filtro de combustible', 'Filtro de combustible en línea', 135.00, 8),
    (10, 'REP-010', 'Banda de distribución', 'Kit de banda de distribución', 480.00, 2),
    (11, 'REP-011', 'Refrigerante', 'Galón de refrigerante concentrado', 125.00, 15),
    (12, 'REP-012', 'Amortiguador delantero', 'Amortiguador hidráulico delantero', 690.00, 4);

INSERT INTO orden_trabajo (id_orden, id_vehiculo, id_mecanico, descripcion_problema, estado, fecha_creacion, fecha_inicio, fecha_finalizacion, fecha_cancelacion, motivo_cancelacion, id_usuario_registro, id_usuario_inicio, id_usuario_cierre) VALUES
    (1, 3, 4, 'Mantenimiento de 60,000 km', 'FINALIZADA', '2026-09-02 08:30:00', '2026-09-02 09:15:00', '2026-09-02 15:40:00', NULL, NULL, 2, 1, 1),
    (2, 1, 2, 'Ruido metálico al frenar', 'FINALIZADA', '2026-09-08 07:50:00', '2026-09-08 08:30:00', '2026-09-09 11:10:00', NULL, NULL, 2, 4, 4),
    (3, 4, 3, 'Testigo de check engine encendido', 'FINALIZADA', '2026-09-15 10:05:00', '2026-09-15 10:40:00', '2026-09-15 17:20:00', NULL, NULL, 2, 1, 1),
    (4, 6, 1, 'Vibración en el volante a alta velocidad', 'FINALIZADA', '2026-09-28 09:00:00', '2026-09-28 09:30:00', '2026-09-28 14:00:00', NULL, NULL, 2, 3, 3),
    (5, 2, 4, 'Servicio preventivo de flota', 'FINALIZADA', '2026-10-05 08:00:00', '2026-10-05 08:20:00', '2026-10-05 13:45:00', NULL, NULL, 2, 1, 1),
    (6, 8, 3, 'El vehículo no enciende por las mañanas', 'FINALIZADA', '2026-10-14 07:40:00', '2026-10-14 08:00:00', '2026-10-14 10:30:00', NULL, NULL, 2, 1, 1),
    (7, 5, 2, 'Pedal de freno esponjoso', 'EN_PROCESO', '2026-10-22 09:10:00', '2026-10-22 10:00:00', NULL, NULL, NULL, 2, 4, NULL),
    (8, 1, 1, 'Pérdida de potencia y humo negro', 'EN_PROCESO', '2026-10-23 08:15:00', '2026-10-23 09:00:00', NULL, NULL, NULL, 2, 3, NULL),
    (9, 7, 5, 'Revisión general de motocicleta', 'EN_PROCESO', '2026-10-26 11:00:00', '2026-10-26 11:30:00', NULL, NULL, NULL, 2, 1, NULL),
    (10, 3, 5, 'Desgaste irregular en llantas delanteras', 'PENDIENTE', '2026-10-27 08:05:00', NULL, NULL, NULL, NULL, 2, NULL, NULL),
    (11, 6, 4, 'Golpeteo en la suspensión delantera', 'PENDIENTE', '2026-10-27 09:20:00', NULL, NULL, NULL, NULL, 2, NULL, NULL),
    (12, 4, 1, 'Cambio de banda de distribución', 'CANCELADA', '2026-10-19 08:00:00', '2026-10-19 08:40:00', NULL, '2026-10-19 12:00:00', 'El cliente decidió realizar la reparación en otro taller', 2, 3, 2);

INSERT INTO detalle_servicio (id_orden, id_servicio, costo_mano_obra, fecha_registro) VALUES
    (1, 1, 150.00, '2026-09-02 09:20:00'),
    (1, 7, 650.00, '2026-09-02 09:21:00'),
    (2, 4, 200.00, '2026-09-08 08:35:00'),
    (2, 5, 250.00, '2026-09-08 08:36:00'),
    (3, 6, 300.00, '2026-09-15 10:45:00'),
    (4, 2, 175.00, '2026-09-28 09:35:00'),
    (4, 3, 120.00, '2026-09-28 09:36:00'),
    (5, 1, 150.00, '2026-10-05 08:25:00'),
    (6, 8, 75.00, '2026-10-14 08:05:00'),
    (6, 6, 300.00, '2026-10-14 08:06:00'),
    (7, 4, 200.00, '2026-10-22 10:05:00'),
    (8, 6, 300.00, '2026-10-23 09:05:00'),
    (8, 7, 650.00, '2026-10-23 09:06:00'),
    (9, 1, 150.00, '2026-10-26 11:35:00'),
    (10, 2, 175.00, '2026-10-27 08:05:00'),
    (12, 7, 650.00, '2026-10-19 08:45:00');

INSERT INTO detalle_repuesto (id_detalle_repuesto, id_orden, id_repuesto, cantidad, precio_unitario, fecha_asignacion) VALUES
    (1, 1, 1, 1, 245.00, '2026-09-02 09:35:00'),
    (2, 1, 2, 1, 65.00, '2026-09-02 09:38:00'),
    (3, 1, 6, 4, 95.00, '2026-09-02 09:41:00'),
    (4, 1, 3, 1, 110.00, '2026-09-02 09:44:00'),
    (5, 2, 4, 1, 385.00, '2026-09-08 08:50:00'),
    (6, 2, 5, 2, 520.00, '2026-09-08 08:53:00'),
    (7, 2, 8, 2, 70.00, '2026-09-08 08:56:00'),
    (8, 3, 6, 4, 95.00, '2026-09-15 11:00:00'),
    (9, 5, 1, 2, 245.00, '2026-10-05 08:40:00'),
    (10, 5, 2, 1, 65.00, '2026-10-05 08:43:00'),
    (11, 5, 9, 1, 135.00, '2026-10-05 08:46:00'),
    (12, 6, 7, 1, 1150.00, '2026-10-14 08:20:00'),
    (13, 7, 8, 3, 70.00, '2026-10-22 10:20:00'),
    (14, 7, 4, 1, 385.00, '2026-10-22 10:23:00'),
    (15, 8, 9, 1, 135.00, '2026-10-23 09:20:00'),
    (16, 8, 10, 1, 480.00, '2026-10-23 09:23:00'),
    (17, 8, 6, 2, 95.00, '2026-10-23 09:26:00'),
    (18, 12, 10, 1, 480.00, '2026-10-19 09:00:00'),
    (19, 12, 11, 2, 125.00, '2026-10-19 09:03:00');

INSERT INTO comprobante (id_comprobante, id_orden, numero, fecha_emision, total_mano_obra, total_repuestos, subtotal, tasa_iva, iva, total) VALUES
    (1, 1, 'CMP-000001', '2026-09-02 15:40:00', 800.00, 800.00, 1600.00, 0.1200, 192.00, 1792.00),
    (2, 2, 'CMP-000002', '2026-09-09 11:10:00', 450.00, 1565.00, 2015.00, 0.1200, 241.80, 2256.80),
    (3, 3, 'CMP-000003', '2026-09-15 17:20:00', 300.00, 380.00, 680.00, 0.1200, 81.60, 761.60),
    (4, 4, 'CMP-000004', '2026-09-28 14:00:00', 295.00, 0.00, 295.00, 0.1200, 35.40, 330.40),
    (5, 5, 'CMP-000005', '2026-10-05 13:45:00', 150.00, 690.00, 840.00, 0.1200, 100.80, 940.80),
    (6, 6, 'CMP-000006', '2026-10-14 10:30:00', 375.00, 1150.00, 1525.00, 0.1200, 183.00, 1708.00);

INSERT INTO bitacora (fecha, id_usuario, usuario, accion, entidad, id_entidad, detalle, direccion_ip) VALUES
    ('2026-09-02 08:30:00', 2, 'recepcion', 'CREAR', 'ORDEN', 1, 'P123ABC · mecánico MEC-004 · servicios 2', '127.0.0.1'),
    ('2026-09-02 09:15:00', 1, 'admin', 'INICIAR_ORDEN', 'ORDEN', 1, 'Reparación iniciada', '127.0.0.1'),
    ('2026-09-02 09:35:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 1, 'REP-001 · Aceite sintético 5W-30 · 1 unidades', '127.0.0.1'),
    ('2026-09-02 09:38:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 1, 'REP-002 · Filtro de aceite · 1 unidades', '127.0.0.1'),
    ('2026-09-02 09:41:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 1, 'REP-006 · Bujía de iridio · 4 unidades', '127.0.0.1'),
    ('2026-09-02 09:44:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 1, 'REP-003 · Filtro de aire · 1 unidades', '127.0.0.1'),
    ('2026-09-02 15:40:00', 1, 'admin', 'FINALIZAR_ORDEN', 'ORDEN', 1, 'Comprobante CMP-000001 · total Q 1792.00', '127.0.0.1'),
    ('2026-09-08 07:50:00', 2, 'recepcion', 'CREAR', 'ORDEN', 2, 'C512BCD · mecánico MEC-002 · servicios 2', '127.0.0.1'),
    ('2026-09-08 08:30:00', 4, 'paju', 'INICIAR_ORDEN', 'ORDEN', 2, 'Reparación iniciada', '127.0.0.1'),
    ('2026-09-08 08:50:00', 4, 'paju', 'ASIGNAR_REPUESTO', 'ORDEN', 2, 'REP-004 · Pastillas de freno delanteras · 1 unidades', '127.0.0.1'),
    ('2026-09-08 08:53:00', 4, 'paju', 'ASIGNAR_REPUESTO', 'ORDEN', 2, 'REP-005 · Disco de freno · 2 unidades', '127.0.0.1'),
    ('2026-09-08 08:56:00', 4, 'paju', 'ASIGNAR_REPUESTO', 'ORDEN', 2, 'REP-008 · Líquido de frenos DOT 4 · 2 unidades', '127.0.0.1'),
    ('2026-09-09 11:10:00', 4, 'paju', 'FINALIZAR_ORDEN', 'ORDEN', 2, 'Comprobante CMP-000002 · total Q 2256.80', '127.0.0.1'),
    ('2026-09-15 10:05:00', 2, 'recepcion', 'CREAR', 'ORDEN', 3, 'P456DEF · mecánico MEC-003 · servicios 1', '127.0.0.1'),
    ('2026-09-15 10:40:00', 1, 'admin', 'INICIAR_ORDEN', 'ORDEN', 3, 'Reparación iniciada', '127.0.0.1'),
    ('2026-09-15 11:00:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 3, 'REP-006 · Bujía de iridio · 4 unidades', '127.0.0.1'),
    ('2026-09-15 17:20:00', 1, 'admin', 'FINALIZAR_ORDEN', 'ORDEN', 3, 'Comprobante CMP-000003 · total Q 761.60', '127.0.0.1'),
    ('2026-09-28 09:00:00', 2, 'recepcion', 'CREAR', 'ORDEN', 4, 'C334NPQ · mecánico MEC-001 · servicios 2', '127.0.0.1'),
    ('2026-09-28 09:30:00', 3, 'lramirez', 'INICIAR_ORDEN', 'ORDEN', 4, 'Reparación iniciada', '127.0.0.1'),
    ('2026-09-28 14:00:00', 3, 'lramirez', 'FINALIZAR_ORDEN', 'ORDEN', 4, 'Comprobante CMP-000004 · total Q 330.40', '127.0.0.1'),
    ('2026-10-05 08:00:00', 2, 'recepcion', 'CREAR', 'ORDEN', 5, 'C778KLM · mecánico MEC-004 · servicios 1', '127.0.0.1'),
    ('2026-10-05 08:20:00', 1, 'admin', 'INICIAR_ORDEN', 'ORDEN', 5, 'Reparación iniciada', '127.0.0.1'),
    ('2026-10-05 08:40:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 5, 'REP-001 · Aceite sintético 5W-30 · 2 unidades', '127.0.0.1'),
    ('2026-10-05 08:43:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 5, 'REP-002 · Filtro de aceite · 1 unidades', '127.0.0.1'),
    ('2026-10-05 08:46:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 5, 'REP-009 · Filtro de combustible · 1 unidades', '127.0.0.1'),
    ('2026-10-05 13:45:00', 1, 'admin', 'FINALIZAR_ORDEN', 'ORDEN', 5, 'Comprobante CMP-000005 · total Q 940.80', '127.0.0.1'),
    ('2026-10-14 07:40:00', 2, 'recepcion', 'CREAR', 'ORDEN', 6, 'P246XYZ · mecánico MEC-003 · servicios 2', '127.0.0.1'),
    ('2026-10-14 08:00:00', 1, 'admin', 'INICIAR_ORDEN', 'ORDEN', 6, 'Reparación iniciada', '127.0.0.1'),
    ('2026-10-14 08:20:00', 1, 'admin', 'ASIGNAR_REPUESTO', 'ORDEN', 6, 'REP-007 · Batería 12V 60Ah · 1 unidades', '127.0.0.1'),
    ('2026-10-14 10:30:00', 1, 'admin', 'FINALIZAR_ORDEN', 'ORDEN', 6, 'Comprobante CMP-000006 · total Q 1708.00', '127.0.0.1'),
    ('2026-10-19 08:00:00', 2, 'recepcion', 'CREAR', 'ORDEN', 12, 'P456DEF · mecánico MEC-001 · servicios 1', '127.0.0.1'),
    ('2026-10-19 08:40:00', 3, 'lramirez', 'INICIAR_ORDEN', 'ORDEN', 12, 'Reparación iniciada', '127.0.0.1'),
    ('2026-10-19 09:00:00', 3, 'lramirez', 'ASIGNAR_REPUESTO', 'ORDEN', 12, 'REP-010 · Banda de distribución · 1 unidades', '127.0.0.1'),
    ('2026-10-19 09:03:00', 3, 'lramirez', 'ASIGNAR_REPUESTO', 'ORDEN', 12, 'REP-011 · Refrigerante · 2 unidades', '127.0.0.1'),
    ('2026-10-19 12:00:00', 2, 'recepcion', 'CANCELAR_ORDEN', 'ORDEN', 12, 'Repuestos reintegrados: 2 · El cliente decidió realizar la reparación en otro taller', '127.0.0.1'),
    ('2026-10-22 09:10:00', 2, 'recepcion', 'CREAR', 'ORDEN', 7, 'P789GHJ · mecánico MEC-002 · servicios 1', '127.0.0.1'),
    ('2026-10-22 10:00:00', 4, 'paju', 'INICIAR_ORDEN', 'ORDEN', 7, 'Reparación iniciada', '127.0.0.1'),
    ('2026-10-22 10:20:00', 4, 'paju', 'ASIGNAR_REPUESTO', 'ORDEN', 7, 'REP-008 · Líquido de frenos DOT 4 · 3 unidades', '127.0.0.1'),
    ('2026-10-22 10:23:00', 4, 'paju', 'ASIGNAR_REPUESTO', 'ORDEN', 7, 'REP-004 · Pastillas de freno delanteras · 1 unidades', '127.0.0.1'),
    ('2026-10-23 08:15:00', 2, 'recepcion', 'CREAR', 'ORDEN', 8, 'C512BCD · mecánico MEC-001 · servicios 2', '127.0.0.1'),
    ('2026-10-23 09:00:00', 3, 'lramirez', 'INICIAR_ORDEN', 'ORDEN', 8, 'Reparación iniciada', '127.0.0.1'),
    ('2026-10-23 09:20:00', 3, 'lramirez', 'ASIGNAR_REPUESTO', 'ORDEN', 8, 'REP-009 · Filtro de combustible · 1 unidades', '127.0.0.1'),
    ('2026-10-23 09:23:00', 3, 'lramirez', 'ASIGNAR_REPUESTO', 'ORDEN', 8, 'REP-010 · Banda de distribución · 1 unidades', '127.0.0.1'),
    ('2026-10-23 09:26:00', 3, 'lramirez', 'ASIGNAR_REPUESTO', 'ORDEN', 8, 'REP-006 · Bujía de iridio · 2 unidades', '127.0.0.1'),
    ('2026-10-26 11:00:00', 2, 'recepcion', 'CREAR', 'ORDEN', 9, 'M902RST · mecánico MEC-005 · servicios 1', '127.0.0.1'),
    ('2026-10-26 11:30:00', 1, 'admin', 'INICIAR_ORDEN', 'ORDEN', 9, 'Reparación iniciada', '127.0.0.1'),
    ('2026-10-27 08:05:00', 2, 'recepcion', 'CREAR', 'ORDEN', 10, 'P123ABC · mecánico MEC-005 · servicios 1', '127.0.0.1'),
    ('2026-10-27 09:20:00', 2, 'recepcion', 'CREAR', 'ORDEN', 11, 'C334NPQ · mecánico MEC-004 · servicios 0', '127.0.0.1');
