# --- !Ups

ALTER TABLE chat_member ADD last_see timestamp default CURRENT_TIMESTAMP;

# --- !Downs

ALTER TABLE chat_member DROP last_see;
