# --- !Ups
ALTER TABLE notification
    RENAME TO notification_preference;

ALTER TABLE notification_preference DROP constraint pk_notification,
ADD constraint pk_notification_preference primary key (id);

# --- !Downs
