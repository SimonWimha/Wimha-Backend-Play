# --- !Ups

ALTER TABLE wimha_user ADD lang VARCHAR(40);

# --- !Downs

ALTER TABLE wimha_user DROP lang;
