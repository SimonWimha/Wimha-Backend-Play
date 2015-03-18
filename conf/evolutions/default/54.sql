# --- !Ups
ALTER TABLE authenticator
    RENAME userId TO user_id;

ALTER TABLE authenticator
    RENAME creationDate TO creation_date;

ALTER TABLE authenticator
    RENAME lastUsed TO last_used;

ALTER TABLE authenticator
    RENAME expirationDate TO expiration_date;

ALTER TABLE authenticator
    RENAME TO cookie;

# --- !Downs
ALTER TABLE cookie
    RENAME TO authenticator;

ALTER TABLE authenticator
    RENAME user_id TO userId;

ALTER TABLE authenticator
    RENAME creation_date TO creationDate;

ALTER TABLE authenticator
    RENAME last_used TO lastUsed;

ALTER TABLE authenticator
    RENAME expiration_date TO expirationDate;
