# --- !Ups
ALTER TABLE thread ADD activity_id varchar(40);
ALTER TABLE thread ALTER COLUMN saction_id DROP NOT NULL;
ALTER TABLE thread ADD CONSTRAINT fk_thread_activity foreign key (activity_id) references activity (id) MATCH SIMPLE
ON UPDATE CASCADE ON DELETE CASCADE;

# --- !Downs
DELETE FROM thread WHERE saction_id IS NULL;
ALTER TABLE thread DROP activity_id;
