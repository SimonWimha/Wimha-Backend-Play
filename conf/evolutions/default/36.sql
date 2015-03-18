# --- !Ups
ALTER TABLE totem ADD COLUMN owner_vcard_email VARCHAR(200);
ALTER TABLE totem ADD COLUMN owner_vcard_facebook VARCHAR(200);
ALTER TABLE totem ADD COLUMN owner_vcard_twitter VARCHAR(200);

# --- !Downs
ALTER TABLE totem DROP owner_vcard_email;
ALTER TABLE totem DROP owner_vcard_facebook;
ALTER TABLE totem DROP owner_vcard_twitter;