import random

blocks = ["North Block", "South Block", "East Wing", "West Wing", "Main Campus", "Rural Area A", "Rural Area B"]
schools = ["Primary School 1", "High School Alpha", "Village School", "Govt Higher Sec School", "Model School"]

sql = "INSERT INTO krs_schema.project_locations (project_id, block, school_id, school_name, head, repairing, new_acr, new_mdm_sqm, new_cw_rmt, gtb, btb, cwsn_toilet, shed) VALUES\n"
values = []

# Generate 1 to 3 locations for projects 1 to 40
# Wait, what if the projects I generated have IDs starting from 4? 
# The user's screenshot showed IDs 1, 2, 3 as [NULL], and my inserts started from 4! 
# Let me just insert for project_id 4 to 43.

for project_id in range(4, 44):
    num_locations = random.randint(1, 3)
    for _ in range(num_locations):
        block = random.choice(blocks)
        school_id = f"SCH-{random.randint(100, 999)}"
        school_name = random.choice(schools)
        head = f"Mr. {random.choice(['Sharma', 'Patel', 'Desai', 'Mehta'])}"
        repairing = random.choice(['Yes', 'No', 'Partial'])
        new_acr = str(random.randint(0, 5))
        new_mdm_sqm = str(random.randint(10, 50))
        new_cw_rmt = str(random.randint(100, 500))
        gtb = str(random.randint(1, 4))
        btb = str(random.randint(1, 4))
        cwsn_toilet = random.choice(['1', '0'])
        shed = random.choice(['Yes', 'No'])

        val = f"({project_id}, '{block}', '{school_id}', '{school_name}', '{head}', '{repairing}', '{new_acr}', '{new_mdm_sqm}', '{new_cw_rmt}', '{gtb}', '{btb}', '{cwsn_toilet}', '{shed}')"
        values.append(val)

sql += ",\n".join(values) + ";\n"

with open(r"C:\Projects\KRS\SQL\project_locations_test_data.sql", "w") as f:
    f.write(sql)

print("Created project_locations_test_data.sql")
