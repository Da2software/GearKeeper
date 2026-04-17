-- Add vehicle-level odometer reminder settings and odometer_reading.updated_at.

ALTER TABLE vehicle ADD COLUMN odometer_reminder_enabled INTEGER NOT NULL DEFAULT 0 CHECK (odometer_reminder_enabled IN (0, 1));
ALTER TABLE vehicle ADD COLUMN odometer_reminder_interval_unit TEXT NOT NULL DEFAULT 'DAYS' CHECK (odometer_reminder_interval_unit IN ('DAYS', 'MONTHS'));
ALTER TABLE vehicle ADD COLUMN odometer_reminder_interval_value INTEGER NOT NULL DEFAULT 7 CHECK (odometer_reminder_interval_value > 0);
ALTER TABLE vehicle ADD COLUMN odometer_reminder_skip_until TEXT;

ALTER TABLE odometer_reading ADD COLUMN updated_at TEXT NOT NULL DEFAULT '1970-01-01 00:00:00';
UPDATE odometer_reading
SET updated_at = COALESCE(created_at, '1970-01-01 00:00:00')
WHERE updated_at = '1970-01-01 00:00:00' OR updated_at IS NULL;
