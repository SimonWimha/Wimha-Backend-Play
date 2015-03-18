# --- !Ups
ALTER TABLE picture ADD version varchar(255);
# --- !Downs
ALTER TABLE picture DROP version;

