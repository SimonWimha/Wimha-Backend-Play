# --- !Ups
ALTER TABLE flash ADD COLUMN favorite boolean;

# --- !Downs
ALTER TABLE flash DROP favorite;