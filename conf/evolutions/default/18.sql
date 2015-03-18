# --- !Ups
ALTER TABLE social_action_wimha_user DROP CONSTRAINT social_action_wimha_user_social_action_id_fkey;
ALTER TABLE social_action_wimha_user DROP CONSTRAINT social_action_wimha_user_wimha_user_id_fkey;


ALTER TABLE social_action_wimha_user ADD CONSTRAINT fk_wimha_user_join_user_saction FOREIGN KEY(wimha_user_id) REFERENCES wimha_user(id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE social_action_wimha_user ADD CONSTRAINT fk_social_action_join_user_saction FOREIGN KEY(social_action_id) REFERENCES social_action(id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE CASCADE;
