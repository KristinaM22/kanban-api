INSERT INTO task (title, description, status, priority, version)
SELECT
    'Task ' || i,
    'Sample description for task ' || i,
    CASE (i % 3)
        WHEN 0 THEN 'TO_DO'
        WHEN 1 THEN 'IN_PROGRESS'
        ELSE 'DONE'
    END,
    CASE (i % 3)
        WHEN 0 THEN 'LOW'
        WHEN 1 THEN 'MED'
        ELSE 'HIGH'
    END,
    0
FROM generate_series(1, 50) AS s(i);
