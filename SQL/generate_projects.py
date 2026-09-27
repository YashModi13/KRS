import random
from datetime import datetime, timedelta

villages = ["Modhera", "Detroj", "Saraswati", "Bavla", "Sanand", "Dholka", "Viramgam", "Mandal", "Dhandhuka"]
departments = ["Gujarat PWD", "Road & Building Dept", "Education Dept", "Health Dept"]
works = [
    "Construction of Primary School",
    "Upgradation of Rural Hospital",
    "Road Resurfacing",
    "Bridge Construction",
    "Community Hall Development",
    "Water Supply Scheme",
    "Public Library Construction",
    "Anganwadi Center",
    "Sports Complex"
]

sql = "INSERT INTO krs_schema.projects (work_order_number, village_name, department_name, package_no, tender_id, name_of_work, estimated_tender_cost, tendered_cost, work_order_date, defects_liability_period) VALUES\n"
values = []

for i in range(1, 41):
    wo_num = f"WO-{2026}-{1000+i}"
    village = random.choice(villages)
    dept = random.choice(departments)
    pkg = f"PKG-{2026}-{random.choice('ABCDE')}{random.randint(1,9)}"
    tender_id = f"TNDR-{10000+i}"
    work_name = f"{random.choice(works)} at {village}"
    
    est_cost = round(random.uniform(500000, 5000000), 2)
    tndr_cost = round(est_cost * random.uniform(0.9, 1.1), 2)
    
    # Date between 1 year ago and today
    days_ago = random.randint(10, 365)
    wo_date = (datetime.now() - timedelta(days=days_ago)).strftime('%Y-%m-%d')
    dlp = random.choice(["12 Months", "24 Months", "36 Months"])
    
    val = f"('{wo_num}', '{village}', '{dept}', '{pkg}', '{tender_id}', '{work_name}', {est_cost}, {tndr_cost}, '{wo_date}', '{dlp}')"
    values.append(val)

sql += ",\n".join(values) + ";\n"

with open(r"C:\Projects\KRS\SQL\projects_test_data.sql", "w") as f:
    f.write(sql)

print("Created projects_test_data.sql")
