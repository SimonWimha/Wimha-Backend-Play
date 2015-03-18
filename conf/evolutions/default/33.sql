# --- !Ups
ALTER TABLE comment ALTER COLUMN message_no_html TYPE TEXT;
ALTER TABLE social_action ADD COLUMN flash_id VARCHAR(40);
ALTER TABLE social_action ADD COLUMN totem_id VARCHAR(40);
ALTER TABLE social_action ADD COLUMN comment_id VARCHAR(40);

# --- !Downs
ALTER TABLE social_action DROP flash_id;
ALTER TABLE social_action DROP totem_id;
ALTER TABLE social_action DROP comment_id;
