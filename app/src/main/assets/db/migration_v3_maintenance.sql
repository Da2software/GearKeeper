PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS service_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    is_seeded INTEGER NOT NULL DEFAULT 0 CHECK (is_seeded IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name)
);

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
