-- Test Data for krs_schema.todo_goals
-- This script inserts 50 test records with various dates, priorities, and completion statuses.
-- Assumes User ID 1 (Admin) exists in krs_schema.users

INSERT INTO krs_schema.todo_goals 
(task_description, priority, target_date, is_done, done_note, is_active, created_at, created_by, updated_at, updated_by, done_date, done_by)
VALUES
-- Past Data (Yesterday / Last Week)
('Review foundation blueprints for Block B', 'HIGH', CURRENT_DATE - 3, true, 'Approved with minor remarks', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '2 days', 1),
('Schedule concrete mixer for next week', 'MEDIUM', CURRENT_DATE - 2, true, 'Scheduled for Tuesday morning', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', 1),
('Safety inspection at Detroj Site', 'HIGH', CURRENT_DATE - 1, true, 'All safety measures met', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', 1),
('Order additional steel bars', 'LOW', CURRENT_DATE - 5, true, 'Order placed via Vendor A', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '4 days', 1),
('Finalize monthly budget report', 'HIGH', CURRENT_DATE - 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Check inventory for cement bags', 'MEDIUM', CURRENT_DATE - 2, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Update client on project milestone', 'HIGH', CURRENT_DATE - 4, true, 'Client is happy with the progress', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '3 days', 1),
('Approve RA Bill #42', 'HIGH', CURRENT_DATE - 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Send site photos to stakeholders', 'LOW', CURRENT_DATE - 2, true, 'Sent via email', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', 1),
('Resolve hindrance at north gate', 'MEDIUM', CURRENT_DATE - 3, true, 'Gate cleared and accessible', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '2 days', 1),

-- Today's Data
('Morning stand-up meeting with engineers', 'HIGH', CURRENT_DATE, true, 'Discussed daily targets', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1),
('Review daily labor attendance', 'MEDIUM', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Inspect electrical wiring at Tower 1', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Approve material requisition for sand', 'MEDIUM', CURRENT_DATE, true, 'Approved 50 tons', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1),
('Follow up on pending vendor payments', 'LOW', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Prepare QA/QC report for slab casting', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Coordinate with local authorities for permit', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Review architect changes for lobby', 'MEDIUM', CURRENT_DATE, true, 'Changes accepted', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1),
('Verify cement quality from new batch', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Update project timeline in MS Project', 'LOW', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Check scaffolding safety tags', 'HIGH', CURRENT_DATE, true, 'All tags green', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1),
('Sign off on plumbing layout', 'MEDIUM', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),

-- Tomorrow's Data
('Client visit preparation', 'HIGH', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Cast 3rd floor slab', 'HIGH', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Conduct safety drill', 'MEDIUM', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Order interior paints', 'LOW', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Clear debris from south wing', 'MEDIUM', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Review RA Bill #43 drafts', 'HIGH', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Verify window frame measurements', 'MEDIUM', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Renew machinery insurance', 'HIGH', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),

-- Future Data (Next few days)
('Weekly progress report submission', 'HIGH', CURRENT_DATE + 2, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Crane maintenance check', 'HIGH', CURRENT_DATE + 2, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Approve HVAC layout', 'MEDIUM', CURRENT_DATE + 2, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Plan labor shifts for next month', 'LOW', CURRENT_DATE + 3, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Meeting with interior designers', 'MEDIUM', CURRENT_DATE + 3, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Audit site inventory', 'HIGH', CURRENT_DATE + 3, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Check soil testing report for Phase 2', 'HIGH', CURRENT_DATE + 4, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Renew contractor licenses', 'MEDIUM', CURRENT_DATE + 4, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Pay utility bills for site office', 'LOW', CURRENT_DATE + 5, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Finalize landscaping vendor', 'MEDIUM', CURRENT_DATE + 5, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),

-- Miscellaneous Active Tasks (No specific date, defaulting to today for visibility)
('Update security protocols at main gate', 'MEDIUM', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Procure safety helmets', 'LOW', CURRENT_DATE, true, 'Bought 100 helmets', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1),
('Fix water leak in site office', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Check concrete curing at Block C', 'HIGH', CURRENT_DATE - 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Approve elevator installation blueprints', 'MEDIUM', CURRENT_DATE + 2, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Schedule pest control', 'LOW', CURRENT_DATE + 6, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Review firewall logs for site network', 'LOW', CURRENT_DATE - 3, true, 'No anomalies found', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', 1),
('Negotiate cement prices for Q4', 'HIGH', CURRENT_DATE + 7, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Set up temporary lighting on floor 5', 'MEDIUM', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Conduct quarterly safety audit', 'HIGH', CURRENT_DATE + 14, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Check generator fuel levels', 'LOW', CURRENT_DATE - 1, true, 'Filled to max', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1),
('Submit environmental compliance form', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Inspect crane cables', 'HIGH', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Reconcile petty cash for site expenses', 'MEDIUM', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Draft newsletter for stakeholders', 'LOW', CURRENT_DATE + 4, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Evaluate bids for plumbing sub-contractor', 'HIGH', CURRENT_DATE + 5, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Organize fire safety training', 'MEDIUM', CURRENT_DATE + 7, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Update worker biometric registry', 'LOW', CURRENT_DATE - 2, true, 'Added 15 new workers', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP - INTERVAL '1 day', 1),
('Check curing compound stock', 'MEDIUM', CURRENT_DATE, true, 'Adequate stock available', true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1),
('Review delay claims by sub-contractor A', 'HIGH', CURRENT_DATE - 4, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Approve timesheets for week 42', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Send samples of tiles to architect', 'MEDIUM', CURRENT_DATE + 1, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Test backup power grid', 'HIGH', CURRENT_DATE + 2, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Clear site entrance for heavy trucks', 'HIGH', CURRENT_DATE, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL),
('Order custom glass panels for lobby', 'MEDIUM', CURRENT_DATE + 8, false, NULL, true, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, 1, NULL, NULL);
