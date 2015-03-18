# --- !Ups
create table notification (
  id                        varchar(40) not null,
  user_id                   varchar(40) not null,
  group_name                varchar(255) not null,
  constraint pk_notification primary key (id)
);

# --- !Downs
drop table if exists notification cascade;
