# --- !Ups

ALTER TABLE wimha_user ADD id_fb varchar(255) DEFAULT NULL;

# --- !Downs

ALTER TABLE wimha_user DROP id_fb;
