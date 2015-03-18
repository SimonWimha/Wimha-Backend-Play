# --- !Ups
create table authenticator (
  id                        text not null,
  userId                    varchar(40) not null,
  provider                  varchar(255) not null,
  creationDate              timestamp not null,
  lastUsed                  timestamp not null,
  expirationDate            timestamp not null,
  constraint pk_authenticator primary key (id)
);

# --- !Downs
drop table if exists authenticator;
