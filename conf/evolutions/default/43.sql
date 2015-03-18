# --- !Ups
ALTER TABLE wimha_user ADD COLUMN facebook_token text;
ALTER TABLE wimha_user ADD COLUMN facebook_token_expiration timestamp;

# --- !Downs
ALTER TABLE wimha_user DROP facebook_token;
ALTER TABLE wimha_user DROP facebook_token_expiration;