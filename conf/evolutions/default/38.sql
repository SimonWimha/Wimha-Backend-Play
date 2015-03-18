# --- !Ups
ALTER TABLE totem ADD COLUMN daily_digest BOOLEAN;

# --- !Downs
ALTER TABLE totem DROP daily_digest;