# --- !Ups
ALTER TABLE position ADD COLUMN question text;

UPDATE position
SET question = totem.question
FROM totem
WHERE totem.id = position.totem_id;

# --- !Downs
ALTER TABLE position DROP question;