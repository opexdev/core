WITH duplicates AS (
    SELECT id
    FROM (
             SELECT id,
                    ROW_NUMBER() OVER (
                        PARTITION BY transaction_ref
                        ORDER BY id
                        ) rn
             FROM deposits
         ) t
    WHERE rn > 1
)
UPDATE deposits
SET transaction_ref = id::text || '_' || transaction_ref
WHERE id IN (SELECT id FROM duplicates);

alter table deposits
    add constraint transaction_ref_pk
        unique (transaction_ref);