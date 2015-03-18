# --- !Ups
ALTER TABLE pending_image_upload DROP constraint fk_position_id,
ADD constraint fk_position_id foreign key (position_id)
   references position (id) on delete cascade;

# --- !Downs
