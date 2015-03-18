# --- !Ups

ALTER TABLE social_interest ADD visibility int NOT NULL DEFAULT (0);

# --- !Downs

ALTER TABLE social_interest DROP visibility;
