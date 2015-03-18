# --- !Ups
ALTER TABLE wimha_user ADD token varchar(40);
ALTER TABLE wimha_user ADD date timestamp;

# --- !Downs
ALTER TABLE wimha_user DROP token;
ALTER TABLE wimha_user DROP date;