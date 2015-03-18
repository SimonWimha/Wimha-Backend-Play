# --- !Ups
ALTER TABLE totem ADD COLUMN background_picture_id VARCHAR(40);
ALTER TABLE totem ADD constraint fk_background_id foreign key (background_picture_id)
   references picture (id) on delete cascade;

# --- !Downs
ALTER TABLE totem DROP background_picture_id;