# --- !Ups
create table premium (
  id                        varchar(40) not null,
  user_id                   varchar(40) not null,
  offer_id                  bigint not null,
  date_from                 timestamp not null,
  date_to                   timestamp,
  nb_interests              bigint not null,

  constraint pk_premium primary key (id),
  constraint fk_user_id foreign key (user_id) 
  references wimha_user (id),
  UNIQUE(user_id, offer_id)
);

# --- !Downs
drop table if exists premium cascade;
