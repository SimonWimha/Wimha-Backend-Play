# --- !Ups
ALTER TABLE chat_member DROP constraint fk_wimha_user_id,
ADD constraint fk_wimha_user_id foreign key (wimha_user_id)
   references wimha_user (id) on delete cascade;

ALTER TABLE chat_message DROP constraint fk_user_id,
ADD constraint fk_user_id foreign key (user_id)
   references wimha_user (id) on delete cascade;

# --- !Downs