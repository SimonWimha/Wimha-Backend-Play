# --- !Ups
ALTER TABLE social_action ADD CONSTRAINT fk_origin FOREIGN KEY (origin_socialaction_id) REFERENCES social_action (id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE;

# --- !Downs
