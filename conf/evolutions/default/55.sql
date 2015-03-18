# --- !Ups
ALTER TABLE delayed_mail_notification ADD COLUMN data text;

# --- !Downs
ALTER TABLE delayed_mail_notification DROP data;