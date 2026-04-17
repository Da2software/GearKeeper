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
