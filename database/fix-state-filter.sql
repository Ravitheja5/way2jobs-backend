-- One-time PostgreSQL repair for the state selector and imported job mappings.
-- Take a backup first. Run this script as one transaction against way2jobsDB.
BEGIN;

-- Keep existing IDs intact; this is idempotent and cannot create duplicate names.
INSERT INTO states (name) VALUES
    ('All India'), ('Andhra Pradesh'), ('Arunachal Pradesh'), ('Assam'),
    ('Bihar'), ('Chhattisgarh'), ('Goa'), ('Gujarat'), ('Haryana'),
    ('Himachal Pradesh'), ('Jharkhand'), ('Karnataka'), ('Kerala'),
    ('Madhya Pradesh'), ('Maharashtra'), ('Manipur'), ('Meghalaya'),
    ('Mizoram'), ('Nagaland'), ('Odisha'), ('Punjab'), ('Rajasthan'),
    ('Sikkim'), ('Tamil Nadu'), ('Telangana'), ('Tripura'),
    ('Uttar Pradesh'), ('Uttarakhand'), ('West Bengal'),
    ('Andaman and Nicobar Islands'), ('Chandigarh'),
    ('Dadra and Nagar Haveli and Daman and Diu'), ('Delhi'),
    ('Jammu and Kashmir'), ('Ladakh'), ('Lakshadweep'), ('Puducherry')
ON CONFLICT (name) DO NOTHING;

-- Repair exact state-name imports without assuming or changing numeric IDs.
UPDATE jobs j
SET state_id = s.id
FROM states s
WHERE BTRIM(j.location) = s.name
  AND j.state_id IS DISTINCT FROM s.id;

COMMIT;
