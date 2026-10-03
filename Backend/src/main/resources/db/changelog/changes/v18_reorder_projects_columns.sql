-- liquibase formatted sql

-- changeset Antigravity:28-reorder-projects-columns splitStatements:false runInTransaction:true
-- validCheckSum: ANY
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'krs_schema' AND table_name = 'projects'
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'krs_schema' AND table_name = 'projects_new'
-- comment: Re-arrange krs_schema.projects columns into a logical order (zero data loss).
--          Original column types are preserved; ALL foreign keys referencing projects are
--          captured dynamically and restored. Aborts (rolls back) if any column would be lost
--          or if a view depends on the table.

DO $$
DECLARE
    fk           RECORD;
    saved_fks    TEXT[] := ARRAY[]::TEXT[];
    missing_cols TEXT;
    old_count    BIGINT;
    new_count    BIGINT;
    stmt         TEXT;
BEGIN
    -- Safety: DROP ... CASCADE would silently drop dependent views
    IF EXISTS (
        SELECT 1 FROM information_schema.view_table_usage
        WHERE table_schema = 'krs_schema' AND table_name = 'projects'
    ) THEN
        RAISE EXCEPTION 'A view depends on krs_schema.projects; drop/recreate it manually before reordering.';
    END IF;

    -- 1. Capture every FK that references projects (so none are lost on CASCADE)
    FOR fk IN
        SELECT c.conrelid::regclass::text AS child_table,
               c.conname                  AS constraint_name,
               pg_get_constraintdef(c.oid) AS definition
        FROM pg_constraint c
        WHERE c.contype = 'f'
          AND c.confrelid = 'krs_schema.projects'::regclass
    LOOP
        saved_fks := saved_fks || format('ALTER TABLE %s ADD CONSTRAINT %I %s',
                                         fk.child_table, fk.constraint_name, fk.definition);
    END LOOP;

    -- 2. New table in logical order, using the ORIGINAL column types from v4/v5/v9/v13
    CREATE TABLE krs_schema.projects_new (
        -- Identifiers
        id                        BIGSERIAL PRIMARY KEY,
        sr_no                     BIGINT,
        notice_no                 VARCHAR(255),
        package_no                VARCHAR(255),
        tender_id                 VARCHAR(255),

        -- Work & Department
        department_name           VARCHAR(255),
        name_of_work              TEXT,
        village_name              VARCHAR(500),
        date_of_sub               DATE,

        -- Contact & Reference
        related_to                VARCHAR(255),
        ref_person                VARCHAR(255),

        -- Financials & Costs
        tender_fee                DECIMAL(15,2),
        tender_fee_no             VARCHAR(255),
        dd_no                     VARCHAR(255),
        emd_amt                   DECIMAL(15,2),
        emd_no                    VARCHAR(255),
        estimated_tender_cost     DECIMAL(15,2),
        tendered_cost             DECIMAL(15,2),
        above_below_percentage    DECIMAL(10,4),

        -- Work Order & Timeline
        work_awarded_status       VARCHAR(255),
        work_order_number         VARCHAR(255),
        work_order_date           DATE,
        time_limit                VARCHAR(255),
        time_limit_extension      DATE,
        completion_date_actual    DATE,
        completion_date_extended  DATE,

        -- Security Deposits & Deductions
        security_deposit_amount   DECIMAL(15,2),
        security_deposit_type     VARCHAR(100),
        security_deposit_date     DATE,
        sd_fdr_no                 VARCHAR(255),
        security_deposit_file     VARCHAR(255),
        retention_money_per_bill  DECIMAL(15,2),
        extra_excess_amount       DECIMAL(15,2),
        sd_rab_deduction          DECIMAL(15,2),
        sd_rab_return_amount      DECIMAL(15,2),
        additional_deduction      VARCHAR(255),

        -- Completion & Returns
        work_completed_amount     DECIMAL(15,2),
        pending_work_amount       DECIMAL(15,2),
        defects_liability_period  VARCHAR(255),
        dlp_ended_on              DATE,
        emd_return_status         VARCHAR(255),
        sd_return_status          VARCHAR(255),
        sd_rm_rab_return_status   VARCHAR(255),
        status                    VARCHAR(255),

        -- Documents & Files
        letter_by_krs_file        VARCHAR(255),
        letter_by_dept_file       VARCHAR(255),
        negotiation_letter_file   VARCHAR(255),

        -- Audit & Remarks
        remarks                   TEXT,
        is_active                 BOOLEAN DEFAULT TRUE,
        created_by                VARCHAR(255) DEFAULT NULL,
        created_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_by                VARCHAR(255) DEFAULT NULL,
        updated_at                TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    -- 3. Safety: every column of the old table must exist in the new one
    SELECT string_agg(o.column_name, ', ')
      INTO missing_cols
      FROM information_schema.columns o
     WHERE o.table_schema = 'krs_schema' AND o.table_name = 'projects'
       AND NOT EXISTS (
           SELECT 1 FROM information_schema.columns n
            WHERE n.table_schema = 'krs_schema' AND n.table_name = 'projects_new'
              AND n.column_name = o.column_name
       );
    IF missing_cols IS NOT NULL THEN
        RAISE EXCEPTION 'Reorder aborted - columns missing in new layout: %', missing_cols;
    END IF;

    -- 4. Copy data
    INSERT INTO krs_schema.projects_new (
        id, sr_no, notice_no, package_no, tender_id,
        department_name, name_of_work, village_name, date_of_sub,
        related_to, ref_person,
        tender_fee, tender_fee_no, dd_no, emd_amt, emd_no, estimated_tender_cost, tendered_cost, above_below_percentage,
        work_awarded_status, work_order_number, work_order_date, time_limit, time_limit_extension, completion_date_actual, completion_date_extended,
        security_deposit_amount, security_deposit_type, security_deposit_date, sd_fdr_no, security_deposit_file, retention_money_per_bill, extra_excess_amount, sd_rab_deduction, sd_rab_return_amount, additional_deduction,
        work_completed_amount, pending_work_amount, defects_liability_period, dlp_ended_on, emd_return_status, sd_return_status, sd_rm_rab_return_status, status,
        letter_by_krs_file, letter_by_dept_file, negotiation_letter_file,
        remarks, is_active, created_by, created_at, updated_by, updated_at
    )
    SELECT
        id, sr_no, notice_no, package_no, tender_id,
        department_name, name_of_work, village_name, date_of_sub,
        related_to, ref_person,
        tender_fee, tender_fee_no, dd_no, emd_amt, emd_no, estimated_tender_cost, tendered_cost, above_below_percentage,
        work_awarded_status, work_order_number, work_order_date, time_limit, time_limit_extension, completion_date_actual, completion_date_extended,
        security_deposit_amount, security_deposit_type, security_deposit_date, sd_fdr_no, security_deposit_file, retention_money_per_bill, extra_excess_amount, sd_rab_deduction, sd_rab_return_amount, additional_deduction,
        work_completed_amount, pending_work_amount, defects_liability_period, dlp_ended_on, emd_return_status, sd_return_status, sd_rm_rab_return_status, status,
        letter_by_krs_file, letter_by_dept_file, negotiation_letter_file,
        remarks, is_active, created_by, created_at, updated_by, updated_at
    FROM krs_schema.projects;

    -- 5. Safety: row counts must match
    SELECT COUNT(*) INTO old_count FROM krs_schema.projects;
    SELECT COUNT(*) INTO new_count FROM krs_schema.projects_new;
    IF old_count <> new_count THEN
        RAISE EXCEPTION 'Reorder aborted - row count mismatch (old %, new %)', old_count, new_count;
    END IF;

    -- 6. Swap tables (CASCADE removes the FKs captured in step 1)
    DROP TABLE krs_schema.projects CASCADE;
    ALTER TABLE krs_schema.projects_new RENAME TO projects;
    ALTER INDEX IF EXISTS krs_schema.projects_new_pkey RENAME TO projects_pkey;
    ALTER SEQUENCE IF EXISTS krs_schema.projects_new_id_seq RENAME TO projects_id_seq;

    -- 7. Sync id sequence to current max id
    PERFORM setval(
        pg_get_serial_sequence('krs_schema.projects', 'id'),
        COALESCE((SELECT MAX(id) FROM krs_schema.projects), 0) + 1,
        false
    );

    -- 8. Restore every captured foreign key exactly as it was
    FOREACH stmt IN ARRAY saved_fks LOOP
        EXECUTE stmt;
    END LOOP;

    RAISE NOTICE 'projects reordered: % rows, % foreign keys restored', new_count, COALESCE(array_length(saved_fks, 1), 0);
END $$;
