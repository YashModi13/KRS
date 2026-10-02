import openpyxl
from datetime import datetime, date

def clean_val(val):
    if val is None or val == 'NA' or val == 'N/A' or val == '-':
        return "NULL"
    if isinstance(val, (datetime, date)):
        return f"'{val.strftime('%Y-%m-%d')}'"
    if isinstance(val, (int, float)):
        return str(val)
    # String escaping
    s = str(val).strip().replace("'", "''")
    return f"'{s}'"

def clean_num(val):
    if val is None or val == 'NA' or val == 'N/A' or val == '-':
        return "NULL"
    try:
        return str(float(val))
    except:
        return "NULL"

def clean_str(val):
    if val is None or val == 'NA' or val == 'N/A' or val == '-':
        return "NULL"
    s = str(val).strip().replace("'", "''")
    return f"'{s}'"

def clean_date(val):
    if val is None or val == 'NA' or val == 'N/A' or val == '-':
        return "NULL"
    if isinstance(val, (datetime, date)):
        return f"'{val.strftime('%Y-%m-%d')}'"
    s = str(val).strip()
    try:
        dt = datetime.strptime(s, "%Y-%m-%d")
        return f"'{dt.strftime('%Y-%m-%d')}'"
    except:
        return "NULL"

wb = openpyxl.load_workbook(r"c:\Projects\KRS\Client Data\TENDER DETAILS.xlsx", data_only=True)
ws = wb['TENDER FROM 01-01-2024']
rows = list(ws.iter_rows(values_only=True))

headers = rows[0]
data_rows = rows[1:]

cols = [
    "sr_no", "date_of_sub", "department_name", "tender_id", "notice_no",
    "name_of_work", "related_to", "tender_fee", "tender_fee_no", "emd_amt",
    "emd_no", "estimated_tender_cost", "tendered_cost", "above_below_percentage",
    "ref_person", "work_awarded_status", "work_order_number", "work_order_date",
    "time_limit", "security_deposit_amount", "sd_fdr_no", "remarks",
    "sd_rab_deduction", "sd_rab_return_amount", "additional_deduction",
    "work_completed_amount", "pending_work_amount", "completion_date_actual",
    "defects_liability_period", "dlp_ended_on", "emd_return_status",
    "sd_return_status", "sd_rm_rab_return_status", "status"
]

sql_statements = []
sql_statements.append("-- Seed data from Client Data/TENDER DETAILS.xlsx")
sql_statements.append("TRUNCATE TABLE krs_schema.projects RESTART IDENTITY CASCADE;\n")

batch_size = 50
values_batch = []

for idx, r in enumerate(data_rows):
    if not any(r):
        continue
    
    sr_no = clean_num(r[0])
    date_of_sub = clean_date(r[1])
    department_name = clean_str(r[2])
    tender_id = clean_str(r[3])
    notice_no = clean_str(r[4])
    name_of_work = clean_str(r[5])
    related_to = clean_str(r[6])
    tender_fee = clean_num(r[7])
    tender_fee_no = clean_str(r[8])
    emd_amt = clean_num(r[9])
    emd_no = clean_str(r[10])
    estimated_tender_cost = clean_num(r[11])
    tendered_cost = clean_num(r[12])
    above_below_percentage = clean_num(r[13])
    ref_person = clean_str(r[14])
    work_awarded_status = clean_str(r[15])
    work_order_number = clean_str(r[16])
    work_order_date = clean_date(r[17])
    time_limit = clean_str(r[18])
    security_deposit_amount = clean_num(r[19])
    sd_fdr_no = clean_str(r[20])
    remarks = clean_str(r[21])
    sd_rab_deduction = clean_num(r[22])
    sd_rab_return_amount = clean_num(r[23])
    additional_deduction = clean_str(r[24])
    work_completed_amount = clean_num(r[25])
    pending_work_amount = clean_num(r[26])
    completion_date_actual = clean_date(r[27])
    defects_liability_period = clean_str(r[28])
    dlp_ended_on = clean_date(r[29])
    emd_return_status = clean_str(r[30])
    sd_return_status = clean_str(r[31])
    sd_rm_rab_return_status = clean_str(r[32])
    status = clean_str(r[33])

    val_tuple = f"({sr_no}, {date_of_sub}, {department_name}, {tender_id}, {notice_no}, {name_of_work}, {related_to}, {tender_fee}, {tender_fee_no}, {emd_amt}, {emd_no}, {estimated_tender_cost}, {tendered_cost}, {above_below_percentage}, {ref_person}, {work_awarded_status}, {work_order_number}, {work_order_date}, {time_limit}, {security_deposit_amount}, {sd_fdr_no}, {remarks}, {sd_rab_deduction}, {sd_rab_return_amount}, {additional_deduction}, {work_completed_amount}, {pending_work_amount}, {completion_date_actual}, {defects_liability_period}, {dlp_ended_on}, {emd_return_status}, {sd_return_status}, {sd_rm_rab_return_status}, {status})"
    values_batch.append(val_tuple)

    if len(values_batch) == batch_size or idx == len(data_rows) - 1:
        sql = f"INSERT INTO krs_schema.projects ({', '.join(cols)}) VALUES\n" + ",\n".join(values_batch) + ";\n"
        sql_statements.append(sql)
        values_batch = []

output_path = r"c:\Projects\KRS\SQL\tender_details_data.sql"
with open(output_path, "w", encoding="utf-8") as f:
    f.write("\n".join(sql_statements))

print(f"Successfully generated {output_path} with {len(data_rows)} rows!")
