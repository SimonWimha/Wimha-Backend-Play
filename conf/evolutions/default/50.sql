# --- !Ups
ALTER TABLE delayed_notification
    RENAME TO delayed_mail_notification;

ALTER TABLE position
    RENAME TO flash;

ALTER TABLE pending_image_upload
    RENAME position_id TO flash_id;

ALTER TABLE pending_image_upload DROP constraint fk_position_id,
ADD constraint fk_flash_id foreign key (flash_id)
   references flash (id) on delete cascade;

# --- !Downs
