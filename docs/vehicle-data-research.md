# Vehicle Data Research (Initial)

This document captures the initial research used to define:
- Seeded popular vehicle brands/models.
- Optional fields for the `vehicle` table.

## Sources Reviewed

- [Visual Capitalist - Ranked: The World's Best-Selling Cars From 2024](https://www.visualcapitalist.com/ranked-the-worlds-best-selling-cars-from-2024/)
- [BestSellingCarsBlog - World Full Year 2024](https://bestsellingcarsblog.com/2025/07/world-full-year-2024-discover-the-top-500-best-selling-models/)
- [BestSellingCarsBlog - Mexico Full Year 2024](https://bestsellingcarsblog.com/2025/01/mexico-full-year-2024-nissan-versa-repeats-at-1-above-nissan-np30-and-chevy-aveo/)
- [MotorcyclesData - Best Selling Motorcycle Brands](https://www.motorcyclesdata.com/2024/03/14/best-selling-motorcycles/)
- [Fleetio - Vehicle Overview](https://help.fleetio.com/en_US/vehicle-overview)
- [Fleetio - VIN Decoding / data enrichment](https://www.fleetio.com/features/vin-decoding)
- [NHTSA vPIC API](https://vpic.nhtsa.dot.gov/api/)

## Optional Vehicle Fields Selected

Based on maintenance use cases and common fleet profiles, the following optional fields were selected:

- `year`
- `license_plate`
- `vin` (17-char check)
- `engine_displacement_cc`
- `fuel_type`
- `transmission_type`
- `purchase_date`
- `current_odometer_km`
- `color`
- `warranty_expiration_date`
- `notes`

Rationale:
- These fields support maintenance planning, parts matching, reminders, and user-level identification.
- They are common across vehicle/fleet systems and avoid overfitting the schema too early.

## Seed Data Scope

The first seed includes popular global brands/models for both:
- Cars
- Motorcycles

The current iteration expands coverage for Mexico market relevance, including
examples requested in review such as:
- Honda BR-V
- Volkswagen Virtus

Data file:
- `app/src/main/assets/db/seed_brands_models.sql`

Schema file:
- `app/src/main/assets/db/schema.sql`

## Next Iteration

- Regionalize seed packs (LATAM, North America, Europe, APAC).
- Add import versioning metadata (`seed_version` table).
- Add VIN decode enrichment as a non-blocking enhancement.
- Use API-assisted regeneration with:
  - `scripts/generate_seed_from_nhtsa.py`
  - Output catalog: `app/src/main/assets/db/nhtsa_catalog.json`

