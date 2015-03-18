# --- !Ups
ALTER TABLE social_action ADD suggest_comment TEXT;
# --- !Downs
ALTER TABLE social_action DROP suggest_comment;
