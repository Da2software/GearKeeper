#!/usr/bin/env python3
"""
Generate make/model seed JSON from NHTSA vPIC API.

This script is intended to keep catalogs updated from a public API source
instead of fully manual SQL maintenance.
"""

from __future__ import annotations

import json
import pathlib
import urllib.request
from typing import Any


BASE_URL: str = "https://vpic.nhtsa.dot.gov/api/vehicles"

# Curated focus list for Mexico market (cars).
TARGET_CAR_MAKES: set[str] = {
    "TOYOTA",
    "HONDA",
    "FORD",
    "HYUNDAI",
    "KIA",
    "VOLKSWAGEN",
    "NISSAN",
    "CHEVROLET",
    "TESLA",
    "MAZDA",
    "BMW",
    "MERCEDES-BENZ",
    "AUDI",
    "RENAULT",
    "PEUGEOT",
    "MG",
    "SUZUKI",
    "MITSUBISHI",
    "SUBARU",
    "FIAT",
    "JEEP",
    "RAM",
    "DODGE",
    "GMC",
}

TARGET_MOTO_MAKES: set[str] = {
    "HONDA",
    "YAMAHA",
    "SUZUKI",
    "KAWASAKI",
    "KTM",
    "ROYAL ENFIELD",
    "BMW",
    "TVS",
    "BAJAJ",
    "HERO",
    "HARLEY-DAVIDSON",
    "DUCATI",
}


def fetch_json(url: str) -> dict[str, Any]:
    with urllib.request.urlopen(url, timeout=30) as response:
        payload: bytes = response.read()
    data: dict[str, Any] = json.loads(payload.decode("utf-8"))
    return data


def get_makes_for_vehicle_type(vehicle_type: str) -> list[dict[str, Any]]:
    url: str = f"{BASE_URL}/GetMakesForVehicleType/{vehicle_type}?format=json"
    data: dict[str, Any] = fetch_json(url)
    results: list[dict[str, Any]] = data.get("Results", [])
    return results


def get_models_for_make_id(make_id: int) -> list[str]:
    url: str = f"{BASE_URL}/GetModelsForMakeId/{make_id}?format=json"
    data: dict[str, Any] = fetch_json(url)
    raw_results: list[dict[str, Any]] = data.get("Results", [])
    names: list[str] = []
    for item in raw_results:
        model_name: str = str(item.get("Model_Name", "")).strip()
        if model_name:
            names.append(model_name)
    deduped: list[str] = sorted(set(names))
    return deduped


def normalize_make_name(name: str) -> str:
    normalized: str = name.strip().upper()
    return normalized


def build_catalog() -> dict[str, Any]:
    car_makes_raw: list[dict[str, Any]] = get_makes_for_vehicle_type("car")
    moto_makes_raw: list[dict[str, Any]] = get_makes_for_vehicle_type("motorcycle")

    car_catalog: list[dict[str, Any]] = []
    for make in car_makes_raw:
        make_id: int = int(make.get("MakeId", 0))
        make_name: str = str(make.get("MakeName", "")).strip()
        if make_id <= 0 or not make_name:
            continue
        if normalize_make_name(make_name) not in TARGET_CAR_MAKES:
            continue
        models: list[str] = get_models_for_make_id(make_id)
        car_catalog.append(
            {
                "make_id": make_id,
                "name": make_name,
                "models": models,
            }
        )

    moto_catalog: list[dict[str, Any]] = []
    for make in moto_makes_raw:
        make_id = int(make.get("MakeId", 0))
        make_name = str(make.get("MakeName", "")).strip()
        if make_id <= 0 or not make_name:
            continue
        if normalize_make_name(make_name) not in TARGET_MOTO_MAKES:
            continue
        models = get_models_for_make_id(make_id)
        # Keep only makes that have at least one model.
        if not models:
            continue
        moto_catalog.append(
            {
                "make_id": make_id,
                "name": make_name,
                "models": models,
            }
        )

    output: dict[str, Any] = {
        "source": "NHTSA vPIC API",
        "car_count": len(car_catalog),
        "motorcycle_count": len(moto_catalog),
        "cars": sorted(car_catalog, key=lambda item: str(item["name"]).lower()),
        "motorcycles": sorted(moto_catalog, key=lambda item: str(item["name"]).lower()),
    }
    return output


def write_catalog(path: pathlib.Path, catalog: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(catalog, ensure_ascii=False, indent=2), encoding="utf-8")


def main() -> int:
    output_path: pathlib.Path = pathlib.Path("app/src/main/assets/db/nhtsa_catalog.json")
    catalog: dict[str, Any] = build_catalog()
    write_catalog(output_path, catalog)
    print(f"Wrote catalog to {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

