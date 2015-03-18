# --- !Ups
ALTER TABLE picture DROP bytes;
# --- !Downs
ALTER TABLE picture ADD bytes bytea;