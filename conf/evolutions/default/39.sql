# --- !Ups
ALTER TABLE delayed_notification DROP constraint fk_flash_id,
ADD constraint fk_flash_id foreign key (flash_id)
   references position (id) on delete cascade;


# --- !Downs