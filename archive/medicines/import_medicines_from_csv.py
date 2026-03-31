import csv
import re
from datetime import datetime

import psycopg2
from psycopg2.extras import execute_values


def extract_price(container_str: str) -> float:
    """
    Extract the first valid number after the currency symbol '৳' in a string.
    Returns 0.0 if no valid number is found.
    """
    if not container_str:
        return 0.0
    m = re.search(r"৳\s*([\d,.]+)", container_str)
    if m:
        price_str = m.group(1).replace(",", "")
        try:
            return float(price_str)
        except ValueError:
            return 0.0
    return 0.0


conn = psycopg2.connect(
    dbname="pharmacy",
    user="postgres",
    password="root1234",
    host="localhost",
    port="5432",
)
cur = conn.cursor()

DEFAULT_MIN_STOCK = 20
DEFAULT_PHARMACY_ID = 1  # change if you want another pharmacy

rows = []

with open("medicine.csv", newline="", encoding="utf-8") as f:
    reader = csv.DictReader(f)

    for row in reader:
        brand_name = row.get("brand name")
        manufacturer = row.get("manufacturer")
        package_container = row.get("package container", "")

        if not brand_name or not manufacturer:
            continue

        cur.execute(
            """
            SELECT id
            FROM bd_medicines
            WHERE name = %s AND manufacturer = %s
            LIMIT 1
            """,
            (brand_name, manufacturer),
        )
        res = cur.fetchone()
        if not res:
            continue

        bd_medicine_id = res[0]
        price = extract_price(package_container)

        rows.append(
            (
                bd_medicine_id,
                DEFAULT_MIN_STOCK,
                price if price is not None else 0.0,
                datetime.now(),
                DEFAULT_PHARMACY_ID,
            )
        )


if rows:
    sql = """
    INSERT INTO medicines (
        bd_medicine_id,
        min_stock,
        price,
        created_at,
        pharmacy_id
    ) VALUES %s
    """
    execute_values(cur, sql, rows)
    conn.commit()

cur.close()
conn.close()

print(f"Inserted {len(rows)} medicines.")

