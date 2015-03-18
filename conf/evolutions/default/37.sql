# --- !Ups
ALTER TABLE totem ADD COLUMN post_fb BOOLEAN;

# --- !Downs
ALTER TABLE totem DROP post_fb;