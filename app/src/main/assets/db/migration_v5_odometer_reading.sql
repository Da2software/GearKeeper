-- Odometer register: discrete readings; latest (by recorded_at, id) drives display.

CREATE TABLE IF NOT EXISTS odometer_reading (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_id INTEGER NOT NULL,
    odometer_km INTEGER NOT NULL CHECK (odometer_km >= 0),
    recorded_at TEXT NOT NULL,
    maintenance_id INTEGER,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(id) ON DELETE CASCADE,
    FOREIGN KEY (maintenance_id) REFERENCES maintenance(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_odometer_reading_vehicle_date
    ON odometer_reading(vehicle_id, recorded_at DESC, id DESC);

-- Seed one reading per vehicle that already had current_odometer_km set.
INSERT INTO odometer_reading (vehicle_id, odometer_km, recorded_at, maintenance_id)
SELECT
    v.id,
    v.current_odometer_km,
    CASE
        WHEN LENGTH(v.created_at) >= 10 THEN SUBSTR(v.created_at, 1, 10)
        ELSE DATE('now')
    END,
    NULL
FROM vehicle v
WHERE v.current_odometer_km IS NOT NULL;
