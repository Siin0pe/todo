-- Donnees de depart minimales

INSERT INTO app_user (username, email, password)
VALUES ('demo', 'demo@example.com', '$2a$10$hZf6EdQrSxW5H0S8G0M8f.wfAI6mYj9xQJ3f6nB2kW8q7xG3q2J9W')
ON CONFLICT (username) DO NOTHING;

INSERT INTO category (label)
VALUES ('Immobilier')
ON CONFLICT (label) DO NOTHING;

INSERT INTO annonce (title, description, adress, mail, date, status, author_id, category_id, version)
SELECT
    'Annonce de demo',
    'Annonce creee pour valider rapidement l API',
    '10 rue des Fleurs',
    'demo@example.com',
    CURRENT_TIMESTAMP,
    'PUBLISHED',
    u.id,
    c.id,
    0
FROM app_user u
CROSS JOIN category c
WHERE u.username = 'demo'
  AND c.label = 'Immobilier'
  AND NOT EXISTS (
      SELECT 1
      FROM annonce a
      WHERE a.title = 'Annonce de demo'
  );
