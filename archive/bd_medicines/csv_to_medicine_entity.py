import csv
import re

# Input CSV file (original)
input_file = 'medicine.csv'

# Output CSV file (PostgreSQL-ready)
output_file = 'postgres_medicines.csv'

# Example default pharmacy_id
default_pharmacy_id = 1

# Function to extract numeric price from "package container" column
def extract_price(container_str):
    if not container_str:
        return 0.0
    # Match number after '৳' symbol
    match = re.search(r'৳\s*([\d,.]+)', container_str)
    if match:
        price_str = match.group(1).replace(',', '')
        return float(price_str)
    return 0.0

with open(input_file, newline='', encoding='utf-8') as csv_in, \
     open(output_file, 'w', newline='', encoding='utf-8') as csv_out:

    reader = csv.DictReader(csv_in)
    
    # PostgreSQL table columns
    fieldnames = [
        'id','category','created_at','description','dosage_form','generic_name',
        'manufacturer','name','strength'
    ]
    writer = csv.DictWriter(csv_out, fieldnames=fieldnames)
    writer.writeheader()
    
    # Auto-increment id starting from 1
    auto_id = 1
    for row in reader:
        writer.writerow({
            'id': auto_id,
            'category': row.get('type'),
            'created_at': '',  # leave empty for default NOW()
            'description': '',  # empty for now
            'dosage_form': row.get('dosage form'),
            'generic_name': row.get('generic'),
            'manufacturer': row.get('manufacturer'),
            'name': row.get('brand name'),
            'strength': row.get('strength'),
            'pharmacy_id': default_pharmacy_id
        })
        auto_id += 1  # increment id

print(f"PostgreSQL-ready CSV generated with auto-increment ID: {output_file}")