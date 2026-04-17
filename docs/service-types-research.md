# Service types seed — research notes

The `service_type` catalog is **seeded on first install** and on DB upgrades when that migration re-runs `seed_popular_services.sql`. This mirrors how **brands/models** are documented in `docs/vehicle-data-research.md`, except there is **no public machine API** for “all maintenance job names” comparable to NHTSA vPIC for makes/models. Seeds are therefore **curated from recurring checklist themes** in independent and government-facing guidance.

## How this differs from vehicle brands/models

| Data | Primary approach |
|------|------------------|
| Brands / models | `scripts/generate_seed_from_nhtsa.py` + vPIC API + `seed_brands_models.sql` |
| Service types | Manual catalog aligned with published checklists + shop/owner-manual language; `INSERT OR IGNORE` for safe upgrades |

## References (reviewed for 2026 seed expansion)

- [Consumer Reports — Car maintenance checklist](https://www.consumerreports.org/cars/car-repair-maintenance/your-car-maintenance-checklist-a6808357214/) — fluids, filters, tires, brakes, battery, belts.
- [AAA Automotive — Time-stamped car maintenance checklist](https://www.aaa.com/autorepair/articles/time-stamped-car-maintenance-checklist) — monthly tires/fluids/lights; address brakes, leaks, check-engine promptly.
- [AAA — Road trip / seasonal prep (belts, hoses, fluids, battery, wipers)](https://www.aaa.com/autorepair/articles/the-2021-aaa-road-trip-starter-pack) — inspection clusters used in shop menus.
- [NHTSA — Equipment: tires, recalls, seasonal driving tips](https://www.nhtsa.gov/equipment) — tire pressure/tread, belts/hoses, A/C themes cited in agency consumer materials.

Diesel-specific lines (fuel filter, DEF, DPF) reflect **common diesel owner maintenance** called out in OEM and fleet summaries. Hybrid/EV lines reflect **high-voltage and 12V auxiliary** maintenance themes from manufacturer service schedules (wording kept generic).

## Seed coverage (current file)

`app/src/main/assets/db/seed_popular_services.sql` includes:

- **Fluids & filters:** oil, filters, coolant, brake/clutch/DEF, transmission/differential/transfer case, washer fluid.
- **Brakes & tires:** pads, rotors, fluid flush, rotation, alignment, replacement, pressure/tread check.
- **Propulsion & ignition:** spark plugs, coils, timing belt/chain, belts/hoses, turbo, EGR/PCV, O₂ sensor, injectors.
- **Steering & suspension:** alignment-related steering parts, shocks/struts, wheel bearings.
- **Electrical & HVAC:** battery (conventional and hybrid 12V), alternator, starter, lighting, A/C refrigerant, optional cabin/HVAC odor treatment.
- **Compliance & diagnostics:** safety inspection, emissions test, ABS/traction diagnostic.
- **Motorcycle:** chain, sprockets, valve clearance, fork oil, brake fluid flush.

The app still allows **user-defined** `service_type` rows for anything not in the seed (`is_seeded = 0`).

## Upgrading the seed

1. Edit `seed_popular_services.sql` — use new explicit `id` values and **`INSERT OR IGNORE`** so existing installs do not break.
2. Bump `GearKeeperDatabaseHelper.DATABASE_VERSION` and in `onUpgrade` call `runSqlAsset(..., "db/seed_popular_services.sql")` so older databases pick up new rows.
