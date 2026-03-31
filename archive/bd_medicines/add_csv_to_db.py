import csv
import re
from datetime import datetime
import psycopg2
from psycopg2.extras import execute_values

# --------------------------
# Helper function
# --------------------------
def extract_price(container_str):
    """
    Extracts the first valid number after the currency symbol '৳' in a string.
    Returns 0.0 if no valid number is found.
    """
    if not container_str:
        return 0.0
    match = re.search(r'৳\s*([\d]+(?:\.\d+)?)', container_str)
    if match:
        return float(match.group(1))
    return 0.0

# --------------------------
# Database connection
# --------------------------
conn = psycopg2.connect(
    dbname="pharmacy",
    user="postgres",
    password="root1234",
    host="localhost",
    port="5432"
)
cur = conn.cursor()

# --------------------------
# Read CSV and prepare rows
# --------------------------
with open('postgres_medicines.csv', 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    rows = []
    for row in reader:

        # Create a tuple for insertion
        rows.append((
            row['category'],
            row['created_at'] or datetime.now(),
            row['description'] or None,
            row['dosage_form'],
            row['generic_name'],
            row['manufacturer'],
            row['name'],
            row['strength'],
        ))

# --------------------------
# Insert into PostgreSQL
# --------------------------
sql = """
INSERT INTO bd_medicines (
    category, created_at, description, dosage_form, generic_name,
    manufacturer, name, strength
) VALUES %s
"""
execute_values(cur, sql, rows)

# Commit and close
conn.commit()
cur.close()
conn.close()

print("CSV imported successfully!")