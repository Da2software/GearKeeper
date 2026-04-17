PRAGMA foreign_keys = ON;

-- Master catalog for vehicle brands.
CREATE TABLE IF NOT EXISTS brand (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('car', 'motorcycle')),
    is_seeded INTEGER NOT NULL DEFAULT 1 CHECK (is_seeded IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, vehicle_type)
);

-- Brand-specific model catalog.
CREATE TABLE IF NOT EXISTS model (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    brand_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    is_seeded INTEGER NOT NULL DEFAULT 1 CHECK (is_seeded IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES brand(id) ON DELETE CASCADE,
    UNIQUE(brand_id, name)
);

-- User-owned vehicles. Required fields: brand_id, model_id, and name.
CREATE TABLE IF NOT EXISTS vehicle (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('car', 'motorcycle')),
    brand_id INTEGER NOT NULL,
    model_id INTEGER NOT NULL,
    name TEXT NOT NULL,

    -- Optional fields from product requirements + market research.
    year INTEGER CHECK (year BETWEEN 1886 AND 2100),
    license_plate TEXT,
    bin_number TEXT,
    vin TEXT UNIQUE CHECK (vin IS NULL OR LENGTH(vin) = 17),
    doors INTEGER CHECK (doors IS NULL OR doors > 0),
    engine_displacement_cc INTEGER CHECK (engine_displacement_cc IS NULL OR engine_displacement_cc > 0),
    fuel_type TEXT,
    transmission_type TEXT,
    transmission_subtype TEXT,
    purchase_date TEXT,
    current_odometer_km INTEGER CHECK (current_odometer_km IS NULL OR current_odometer_km >= 0),
    odometer_reminder_enabled INTEGER NOT NULL DEFAULT 0 CHECK (odometer_reminder_enabled IN (0, 1)),
    odometer_reminder_interval_unit TEXT NOT NULL DEFAULT 'DAYS' CHECK (odometer_reminder_interval_unit IN ('DAYS', 'MONTHS')),
    odometer_reminder_interval_value INTEGER NOT NULL DEFAULT 7 CHECK (odometer_reminder_interval_value > 0),
    odometer_reminder_skip_until TEXT,
    color TEXT,
    warranty_expiration_date TEXT,
    notes TEXT,

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (brand_id) REFERENCES brand(id) ON DELETE RESTRICT,
    FOREIGN KEY (model_id) REFERENCES model(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_brand_vehicle_type ON brand(vehicle_type);
CREATE INDEX IF NOT EXISTS idx_model_brand_id ON model(brand_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_brand_id ON vehicle(brand_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_model_id ON vehicle(model_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_type ON vehicle(vehicle_type);
CREATE INDEX IF NOT EXISTS idx_vehicle_odometer ON vehicle(current_odometer_km);

-- Service catalog (user can add custom rows; popular ones are seeded).
CREATE TABLE IF NOT EXISTS service_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    name_es TEXT,
    is_seeded INTEGER NOT NULL DEFAULT 0 CHECK (is_seeded IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name)
);

-- One maintenance visit/event per vehicle (can include multiple service types).
CREATE TABLE IF NOT EXISTS maintenance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    performed_date TEXT NOT NULL,
    odometer_km INTEGER CHECK (odometer_km IS NULL OR odometer_km >= 0),
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS maintenance_service (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    maintenance_id INTEGER NOT NULL,
    service_type_id INTEGER NOT NULL,
    FOREIGN KEY (maintenance_id) REFERENCES maintenance(id) ON DELETE CASCADE,
    FOREIGN KEY (service_type_id) REFERENCES service_type(id) ON DELETE RESTRICT,
    UNIQUE(maintenance_id, service_type_id)
);

CREATE INDEX IF NOT EXISTS idx_maintenance_vehicle ON maintenance(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_service_maintenance ON maintenance_service(maintenance_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_service_type ON maintenance_service(service_type_id);

-- Latest reading (by recorded_at, then id) is shown on vehicle list and vehicle header.
CREATE TABLE IF NOT EXISTS odometer_reading (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id INTEGER NOT NULL,
    odometer_km INTEGER NOT NULL CHECK (odometer_km >= 0),
    recorded_at TEXT NOT NULL,
    maintenance_id INTEGER,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE,
    FOREIGN KEY (maintenance_id) REFERENCES maintenance(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_odometer_reading_vehicle_date
    ON odometer_reading(vehicle_id, recorded_at DESC, id DESC);

-- Future / planned service (not yet performed); drives incoming list and reminders.
CREATE TABLE IF NOT EXISTS planned_maintenance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    schedule_mode TEXT NOT NULL CHECK (schedule_mode IN ('DATE', 'ODOMETER')),
    target_date TEXT,
    target_odometer_km INTEGER CHECK (target_odometer_km IS NULL OR target_odometer_km >= 0),
    lead_days INTEGER NOT NULL CHECK (lead_days >= 0),
    lead_odometer_km INTEGER NOT NULL CHECK (lead_odometer_km >= 0),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE,
    CHECK (
        (schedule_mode = 'DATE' AND target_date IS NOT NULL AND target_odometer_km IS NULL)
        OR
        (schedule_mode = 'ODOMETER' AND target_odometer_km IS NOT NULL AND target_date IS NULL)
    )
);

CREATE TABLE IF NOT EXISTS planned_maintenance_service (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    planned_maintenance_id INTEGER NOT NULL,
    service_type_id INTEGER NOT NULL,
    FOREIGN KEY (planned_maintenance_id) REFERENCES planned_maintenance(id) ON DELETE CASCADE,
    FOREIGN KEY (service_type_id) REFERENCES service_type(id) ON DELETE RESTRICT,
    UNIQUE(planned_maintenance_id, service_type_id)
);

CREATE INDEX IF NOT EXISTS idx_planned_maintenance_vehicle ON planned_maintenance(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_planned_maintenance_service_plan ON planned_maintenance_service(planned_maintenance_id);

CREATE TABLE IF NOT EXISTS maintenance_replaced_part (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    maintenance_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    part_or_serial TEXT NOT NULL DEFAULT '',
    brand TEXT,
    store TEXT,
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (maintenance_id) REFERENCES maintenance(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_replaced_part_maintenance ON maintenance_replaced_part(maintenance_id);
CREATE INDEX IF NOT EXISTS idx_replaced_part_title ON maintenance_replaced_part(title);
CREATE INDEX IF NOT EXISTS idx_replaced_part_created ON maintenance_replaced_part(created_at);
