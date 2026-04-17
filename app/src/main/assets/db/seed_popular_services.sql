-- Popular maintenance / service catalog (cars, light trucks, diesel, motorcycles).
-- Curated from recurring items in public checklists and OEM-style routine care themes.
-- [name] = English (canonical / unique); [name_es] = Spanish UI label for seeded rows.
--
-- Note: There is no public “maintenance API” like NHTSA vPIC for makes; this seed is
-- hand-maintained and can be extended in-place. INSERT OR IGNORE keeps upgrades safe.

PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

INSERT OR IGNORE INTO service_type (id, name, name_es, is_seeded) VALUES
    (1, 'Engine oil change', 'Cambio de aceite del motor', 1),
    (2, 'Engine oil filter replacement', 'Cambio de filtro de aceite del motor', 1),
    (3, 'Engine air filter replacement', 'Cambio de filtro de aire del motor', 1),
    (4, 'Cabin / HVAC filter replacement', 'Cambio de filtro de habitáculo / climatización', 1),
    (5, 'Fuel filter replacement', 'Cambio de filtro de combustible', 1),
    (6, 'Spark plug replacement', 'Cambio de bujías', 1),
    (7, 'Coolant flush / replacement', 'Limpieza / sustitución del refrigerante', 1),
    (8, 'Brake fluid flush / replacement', 'Purga / sustitución del líquido de frenos', 1),
    (9, 'Brake pad replacement', 'Cambio de pastillas de freno', 1),
    (10, 'Brake rotor resurfacing / replacement', 'Rectificado / cambio de discos de freno', 1),
    (11, 'Transmission fluid service', 'Servicio de aceite de transmisión', 1),
    (12, 'Differential / final drive fluid service', 'Servicio de aceite de diferencial / transmisión final', 1),
    (13, 'Power steering fluid service', 'Servicio de líquido de dirección asistida', 1),
    (14, 'Tire rotation', 'Rotación de neumáticos', 1),
    (15, 'Wheel alignment', 'Alineación de dirección', 1),
    (16, 'Tire replacement / mounting & balancing', 'Sustitución de neumáticos / montaje y balanceo', 1),
    (17, 'Battery test / replacement', 'Prueba / sustitución de batería', 1),
    (18, 'Drive belt / serpentine belt replacement', 'Cambio de correa de accesorios / poli-V', 1),
    (19, 'Timing belt / chain service', 'Servicio de correa de distribución / cadena', 1),
    (20, 'Suspension inspection / shock & strut service', 'Inspección de suspensión / amortiguadores', 1),
    (21, 'Wiper blade replacement', 'Cambio de escobillas del limpiaparabrisas', 1),
    (22, 'General multi-point inspection', 'Inspección multipunto general', 1),
    (23, 'Chain clean & lubrication (motorcycle)', 'Limpieza y lubricación de cadena (motocicleta)', 1),
    (24, 'Chain / sprocket replacement (motorcycle)', 'Cambio de cadena / piñones (motocicleta)', 1),
    (25, 'Valve clearance check (motorcycle)', 'Revisión de holguras de válvulas (motocicleta)', 1),
    (26, 'Tire pressure & tread inspection', 'Inspección de presión y dibujo de neumáticos', 1),
    (27, 'Windshield washer fluid service', 'Servicio de líquido limpiaparabrisas', 1),
    (28, 'Engine hoses & accessory belt inspection', 'Inspección de mangueras y correa de accesorios', 1),
    (29, 'Manual transmission fluid change', 'Cambio de aceite de caja manual', 1),
    (30, 'Clutch hydraulic fluid service', 'Servicio de líquido hidráulico del embrague', 1),
    (31, 'Clutch assembly service', 'Servicio del conjunto de embrague', 1),
    (32, 'Transfer case fluid service (4WD / AWD)', 'Servicio de aceite de la caja de transferencia (4x4/AWD)', 1),
    (33, 'Wheel bearing service', 'Servicio de rodamientos de rueda', 1),
    (34, 'Ball joint & tie rod service', 'Servicio de rótulas y terminales de dirección', 1),
    (35, 'Steering rack / pump service', 'Servicio de cremallera / bomba de dirección', 1),
    (36, 'Radiator & cooling system inspection', 'Inspección del radiador y sistema de refrigeración', 1),
    (37, 'Water pump replacement', 'Cambio de bomba de agua', 1),
    (38, 'Thermostat replacement', 'Cambio de termostato', 1),
    (39, 'Ignition coil / high-tension lead service', 'Servicio de bobinas / cables de encendido', 1),
    (40, 'Fuel injector / induction service', 'Servicio de inyectores / admisión', 1),
    (41, 'Oxygen sensor replacement', 'Cambio de sonda lambda', 1),
    (42, 'A/C refrigerant service & leak check', 'Servicio de refrigerante A/C y detección de fugas', 1),
    (43, 'Exterior lighting inspection / bulb replacement', 'Inspección de iluminación exterior / bombillas', 1),
    (44, 'State safety inspection', 'Inspección de seguridad vehicular (estatal)', 1),
    (45, 'Emissions test / inspection', 'Prueba / inspección de emisiones', 1),
    (46, 'Diesel fuel filter service', 'Servicio de filtro de combustible diésel', 1),
    (47, 'Diesel exhaust fluid (DEF) refill', 'Relleno de AdBlue (DEF)', 1),
    (48, 'Diesel particulate filter (DPF) service', 'Servicio de filtro de partículas diésel (DPF)', 1),
    (49, 'Turbocharger / intercooler service', 'Servicio de turbocompresor / intercooler', 1),
    (50, 'PCV system service', 'Servicio del sistema PCV', 1),
    (51, 'EGR system service', 'Servicio del sistema EGR', 1),
    (52, 'Hybrid / EV high-voltage battery inspection', 'Inspección de batería de alta tensión (híbrido/EV)', 1),
    (53, 'Hybrid / EV 12V auxiliary battery service', 'Servicio de batería auxiliar 12 V (híbrido/EV)', 1),
    (54, 'Motorcycle fork oil change', 'Cambio de aceite de horquillas (motocicleta)', 1),
    (55, 'Motorcycle brake system fluid flush', 'Purga del sistema de frenos (motocicleta)', 1),
    (56, 'ABS / traction control diagnostic', 'Diagnóstico ABS / control de tracción', 1),
    (57, 'Alternator replacement', 'Sustitución del alternador', 1),
    (58, 'Starter motor replacement', 'Sustitución del motor de arranque', 1),
    (59, 'Cabin disinfection / HVAC odor treatment (optional)', 'Desinfección de habitáculo / tratamiento de olores HVAC (opcional)', 1),
    (60, 'Windshield chip repair', 'Reparación de impactos en parabrisas', 1);

COMMIT;
