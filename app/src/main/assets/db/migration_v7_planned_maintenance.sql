PRAGMA foreign_keys = ON;

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
