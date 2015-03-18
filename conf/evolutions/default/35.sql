# --- !Ups
TRUNCATE TABLE notification;
DROP TABLE activity CASCADE;
DROP TABLE activity_update CASCADE;
DROP TABLE chat CASCADE;
DROP TABLE chat_member CASCADE;
DROP TABLE chat_message CASCADE;
DROP TABLE friendship CASCADE;
DROP TABLE interest CASCADE;
DROP TABLE link CASCADE;
DROP TABLE localized_string CASCADE;
DROP TABLE received_friend_requests CASCADE;
DROP TABLE sent_friend_requests CASCADE;
DROP TABLE scale CASCADE;
DROP TABLE social_interest CASCADE;
DROP TABLE social_action_wimha_user CASCADE;
DROP TABLE suggestion CASCADE;
ALTER TABLE social_action DROP social_interest_id;
ALTER TABLE social_action DROP activity_id;
ALTER TABLE social_action DROP url;
ALTER TABLE social_action DROP new_friend_id;
ALTER TABLE social_action DROP suggest_comment;
ALTER TABLE social_action DROP origin_socialaction_id;
ALTER TABLE wimha_user DROP scale_id;
ALTER TABLE totem DROP activity_id;
ALTER TABLE totem DROP interest_id;
ALTER TABLE thread DROP saction_id;
ALTER TABLE thread DROP activity_id;

# --- !Downs

