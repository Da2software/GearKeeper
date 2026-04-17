# GearKeeper

GearKeeper is an Android app for tracking vehicle maintenance for cars and motorcycles.

The goal is to help users:
- Keep a history of maintenance records.
- Plan upcoming services.
- Receive reminders based on time and odometer usage.

---

## Product Vision

Provide a simple and reliable way to manage vehicle maintenance so users can avoid missing important services and keep a complete service history.

---

## Problem Statement

Users usually track vehicle maintenance with notes, memory, or scattered documents. This causes missed services, poor historical records, and weak planning for future maintenance.

GearKeeper solves this by combining:
- Vehicle data.
- Service catalogs (preloaded + custom).
- Maintenance records with multiple services per event.
- Intelligent reminder logic using date and odometer updates.

---

## Core Requirements

## 1) Vehicle Management

The app must support both cars and motorcycles.

Required vehicle fields:
- Brand (required).
- Model (required).
- Name (required; user-friendly label).

Optional vehicle fields (initial placeholders, to refine after research):
- Year.
- License plate.
- VIN/chassis number.
- Engine displacement/capacity.
- Fuel type.
- Transmission type.
- Purchase date.
- Current odometer.
- Notes.

Brand/model behavior:
- The database must include popular brands and models for cars and motorcycles as prefilled data.
- Users must be able to add custom brands and models if not available.

## 2) Service Catalog

The app must provide:
- Popular/common services preloaded in the database (for example: oil change, air filter, engine filter, brake fluid, etc.).
- User-defined custom services.

Services should be reusable entities that can be attached to maintenance records.

## 3) Maintenance Records

A maintenance record represents one maintenance event for one vehicle.

Rules:
- A maintenance record can include one or many services.
- Users can add multiple services completed in the same visit/event (for example: oil change + engine filter change).
- Pending or incoming planned services must be listed.
- Users must be able to mark a pending service as completed.
- When completing a pending service, users must be able to add additional services completed at the same time.

## 4) Reminders and Scheduling

The app must support reminder scheduling for future services.

Reminder inputs can include:
- Target date.
- Odometer threshold.
- Service interval strategy (time-based, distance-based, or both).

Reminder behavior:
- Show notifications/alerts for upcoming and overdue services.
- Use latest odometer updates to improve reminder accuracy.
- Support a clear calculation strategy for "when to notify".

Note: exact reminder algorithm will be specified in a dedicated design document.

## 5) Odometer Tracking

Users must be able to register odometer updates over time.

Odometer updates are used to:
- Track vehicle usage.
- Power distance-based reminder logic.
- Improve upcoming service predictions.

## 6) Receipts and Attachments (Deferred)

Future requirement:
- Allow attaching receipt images/files to maintenance records.

Current MVP behavior:
- Keep text-based receipt/reference information only (no file storage yet).

---

## Initial Data Requirements

Database seed data should include:
- Popular car brands and models.
- Popular motorcycle brands and models.
- Common maintenance service types.

Seed data must be extensible and versioned so future updates can add more brands/models/services without breaking existing user data.

---

## MVP Scope

In scope now:
- Vehicle creation and management (cars + motorcycles).
- Prefilled brands/models + custom additions.
- Prefilled services + custom services.
- Maintenance records with multiple services per record.
- Pending/incoming service tracking and completion flow.
- Reminder setup and basic notification rules.
- Odometer updates.
- Text-only receipt/reference metadata.

Out of scope for now:
- Image/file uploads for receipts.
- Advanced analytics dashboards.
- Cloud sync and multi-device account sync.

---

## Domain Model (Conceptual)

- Vehicle
  - type: car | motorcycle
  - brand
  - model
  - name
  - optional metadata

- ServiceType
  - name
  - category (optional)
  - source: prefilled | custom

- MaintenanceRecord
  - vehicleId
  - date
  - odometerAtService (optional but recommended)
  - notes
  - contains many MaintenanceRecordService items

- MaintenanceRecordService
  - serviceTypeId
  - status: completed | pending | upcoming
  - reminder policy

- OdometerEntry
  - vehicleId
  - date
  - odometerValue

- Reminder
  - target service
  - due date and/or due odometer
  - notification state

---

## Reminder Calculation Guidelines (Draft)

Base calculation inputs:
- Last completed date for service.
- Last completed odometer for service.
- Current date.
- Latest odometer reading.
- Configured interval (days/months and/or km/mi).

Suggested behavior:
- Trigger "upcoming" when service is within a configurable threshold (for example: X days or Y km before due).
- Trigger "overdue" when due date or due odometer is exceeded.
- If both date and odometer are configured, remind on whichever condition is reached first.

---

## Research Tasks (Required)

Before finalizing schema and UX, perform an external research review for:
- Common vehicle profile fields used by leading maintenance apps.
- Best reminder UX patterns (date + mileage combined scheduling).
- Data validation patterns for odometer history and anomaly detection.
- Practical defaults for popular service intervals by vehicle type.

Output of this research should update:
- Optional vehicle fields.
- Reminder algorithm defaults.
- Service interval templates.

---

## Quality and Architecture Notes

- Keep UI rendering and business logic separated (Compose UI vs ViewModel/domain logic).
- Keep reminder calculation logic testable and isolated from UI/framework code.
- Treat seed data as managed domain data, not hardcoded UI constants.
- Prefer clear domain contracts and explicit return types in all code.

---

## Short-Term Roadmap

1. Define domain entities and database schema.
2. Implement seed data loading for brands/models/services.
3. Implement vehicle CRUD with required vs optional fields.
4. Implement service catalog (prefilled + custom).
5. Implement maintenance record flow with multi-service support.
6. Implement odometer entry flow.
7. Implement reminder engine (basic rules first).
8. Add tests for reminder calculations and record status transitions.
9. Plan receipt file/image support as a separate milestone.

