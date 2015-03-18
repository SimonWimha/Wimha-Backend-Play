# --- !Ups
ALTER TABLE pending_image_upload ADD COLUMN first_flash boolean;

# --- !Downs
ALTER TABLE pending_image_upload DROP first_flash;
