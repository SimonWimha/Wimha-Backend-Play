# --- !Ups
create table totem (
  id                        varchar(40) not null,
  token                     VARCHAR(40),
  owner_id             VARCHAR(40),
  name                      varchar(255),
  owner_mail                varchar(255),
  activity_id               varchar(40),
  picture_id                varchar(40),
  question                  text,
  description               text,
  birthdate                 timestamp,
  owner_name                text,
  interest_id               bigint,
  constraint uq_totem_name unique (name),
  constraint pk_totem primary key (id),
  constraint fk_wimha_user_id foreign key (owner_id) 
  references wimha_user (id),
  constraint fk_activity_id foreign key (activity_id) 
  references activity (id),
  constraint fk_picture_id foreign key (picture_id) 
  references picture (id))
;

create table position (
  id                        varchar(40) not null,
  token                     VARCHAR(40),
  totem_id                  varchar(40),
  flasher_id                  varchar(40),
  name                      varchar(255),
  mail                      varchar(255),
  message                   text,
  date_post                 varchar(255),
  lat                       varchar(255),
  lon                       varchar(255),
  address                   text,
  picture_id                varchar(40),
  city                      text,
  country                   text,
  country_code              VARCHAR(5),
  constraint pk_position primary key (id),
  constraint fk_totem_id foreign key (totem_id) 
  references totem (id) on delete cascade,
  constraint fk_wimha_user_id foreign key (flasher_id) 
  references wimha_user (id),
  constraint fk_picture_id foreign key (picture_id) 
  references picture (id))
;

# --- !Downs
drop table if exists position cascade;
drop table if exists totem cascade;