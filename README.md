# GearKeeper

GearKeeper is a simple Android app to manage vehicle maintenance with the basic tools most drivers actually need.

## What it does today

- Manage your vehicles in one place.
- Keep a reusable service catalog (built-in and custom service types).
- Log maintenance records with date, notes, and related services.
- Plan upcoming maintenance tasks and track pending work.
- Register odometer updates for each vehicle.
- Get odometer reminder notifications with background checks.
- Review maintenance history and replaced parts.
- Export and import local database backups.
- Configure language (English/Spanish/system) and distance unit (km/mi).

## Why it is useful

- Keeps maintenance data organized without extra complexity.
- Helps avoid missing important service intervals.
- Gives a clear view of what was done, what is pending, and what is next.
- Works fully with local storage, so data stays on-device and easy to back up.

## Simple by design

GearKeeper is intentionally focused on core maintenance workflows, not on heavy or enterprise features.
It is built to be straightforward, fast to use, and reliable for day-to-day tracking.

## Current scope

Included now:
- Vehicle management
- Service catalog management
- Maintenance history logging
- Planned maintenance tracking
- Odometer reminders
- Replaced parts tracking
- Settings and local backup/restore

Not included yet:
- Cloud sync
- Multi-device account system
- Advanced analytics dashboards
- Receipt/image attachment management

## Tech snapshot

- Android (Kotlin + Jetpack Compose)
- Local database persistence
- WorkManager for scheduled reminder checks
