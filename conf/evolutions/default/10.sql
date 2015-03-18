# --- !Ups
ALTER TABLE social_action ADD origin_socialaction_id bigint;
# --- !Downs
ALTER TABLE social_action DROP origin_socialaction_id;