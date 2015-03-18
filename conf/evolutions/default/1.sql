
# --- !Ups

create table activity (
  dtype                     varchar(10) not null,
  id                        varchar(40) not null,
  home_interest_id          bigint,
  home_scale_id             bigint,
  title                     varchar(255),
  type_of_activity          varchar(255),
  description               TEXT,
  tags_as_json              TEXT,
  picture_id                varchar(40),
  language                  varchar(255),
  location                  varchar(255),
  urls_as_json              TEXT,
  date_from                 timestamp,
  date_to                   timestamp,
  price                     varchar(255),
  constraint pk_activity primary key (id))
;

create table activity_update (
  id                        bigint not null,
  activity_id               varchar(40),
  author_id                 varchar(40),
  date                      timestamp,
  constraint pk_activity_update primary key (id))
;

create table interest (
  id                        bigint not null,
  icon_id                   varchar(40),
  constraint pk_interest primary key (id))
;

create table localized_string (
  id                        bigint not null,
  interest_id               bigint not null,
  language                  varchar(255),
  text                      varchar(255),
  constraint pk_localized_string primary key (id))
;

create table picture (
  id                        varchar(40) not null,
  bytes                     bytea,
  content_type              varchar(255),
  constraint pk_picture primary key (id))
;

create table scale (
  id                        bigint not null,
  name                      varchar(255),
  country_code              varchar(255),
  level                     integer,
  map_id                    varchar(40),
  parent_id                 bigint,
  constraint ck_scale_level check (level in (0,1,2)),
  constraint pk_scale primary key (id))
;

create table social_action (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  social_interest_id        varchar(40),
  date                      timestamp,
  activity_id               varchar(40),
  friend_id                 varchar(40),
  url                       varchar(255),
  new_friend_id             varchar(40),
  constraint pk_social_action primary key (id))
;

create table social_interest (
  id                        varchar(40) not null,
  user_id                   varchar(40),
  interest_id               bigint,
  status                    varchar(255),
  constraint pk_social_interest primary key (id))
;

create table suggestion (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  activity_id               varchar(40),
  date                      timestamp,
  scale_id                  bigint,
  interest_id               bigint,
  url                       varchar(255),
  constraint pk_suggestion primary key (id))
;

create table wimha_user (
  id                        varchar(40) not null,
  firstname                 varchar(255),
  lastname                  varchar(255),
  email                     varchar(255),
  encrypted_password        varchar(255),
  scale_id                  bigint,
  picto_id                  varchar(40),
  authentication_method     integer,
  constraint ck_wimha_user_authentication_method check (authentication_method in (0,1,2,3)),
  constraint pk_wimha_user primary key (id))
;


create table received_friend_requests (
  social_interest_id             varchar(40) not null,
  wimha_user_id                  varchar(40) not null,
  constraint pk_received_friend_requests primary key (social_interest_id, wimha_user_id))
;

create table sent_friend_requests (
  social_interest_id             varchar(40) not null,
  wimha_user_id                  varchar(40) not null,
  constraint pk_sent_friend_requests primary key (social_interest_id, wimha_user_id))
;

create table friendship (
  social_interest_id             varchar(40) not null,
  wimha_user_id                  varchar(40) not null,
  constraint pk_friendship primary key (social_interest_id, wimha_user_id))
;
create sequence activity_update_seq;

create sequence interest_seq;

create sequence localized_string_seq;

create sequence scale_seq;

create sequence social_action_seq;

create sequence suggestion_seq;

alter table activity add constraint fk_activity_homeInterest_1 foreign key (home_interest_id) references interest (id);
create index ix_activity_homeInterest_1 on activity (home_interest_id);
alter table activity add constraint fk_activity_homeScale_2 foreign key (home_scale_id) references scale (id);
create index ix_activity_homeScale_2 on activity (home_scale_id);
alter table activity add constraint fk_activity_picture_3 foreign key (picture_id) references picture (id);
create index ix_activity_picture_3 on activity (picture_id);
alter table activity_update add constraint fk_activity_update_activity_4 foreign key (activity_id) references activity (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_activity_update_activity_4 on activity_update (activity_id);
alter table activity_update add constraint fk_activity_update_author_5 foreign key (author_id) references wimha_user (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_activity_update_author_5 on activity_update (author_id);
alter table interest add constraint fk_interest_icon_6 foreign key (icon_id) references picture (id);
create index ix_interest_icon_6 on interest (icon_id);
alter table localized_string add constraint fk_localized_string_interest_7 foreign key (interest_id) references interest (id);
create index ix_localized_string_interest_7 on localized_string (interest_id);
alter table scale add constraint fk_scale_map_8 foreign key (map_id) references picture (id);
create index ix_scale_map_8 on scale (map_id);
CREATE INDEX ix_scale_name_10
   ON scale USING btree ("name" ASC NULLS LAST);
alter table scale add constraint fk_scale_parent_9 foreign key (parent_id) references scale (id);
create index ix_scale_parent_9 on scale (parent_id);
alter table social_action add constraint fk_social_action_socialIntere_10 foreign key (social_interest_id) references social_interest (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_social_action_socialIntere_10 on social_action (social_interest_id);
alter table social_action add constraint fk_social_action_activity_11 foreign key (activity_id) references activity (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_social_action_activity_11 on social_action (activity_id);
alter table social_action add constraint fk_social_action_friend_12 foreign key (friend_id) references wimha_user (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_social_action_friend_12 on social_action (friend_id);
alter table social_action add constraint fk_social_action_newFriend_13 foreign key (new_friend_id) references wimha_user (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_social_action_newFriend_13 on social_action (new_friend_id);
alter table social_interest add constraint fk_social_interest_user_14 foreign key (user_id) references wimha_user (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_social_interest_user_14 on social_interest (user_id);
alter table social_interest add constraint fk_social_interest_interest_15 foreign key (interest_id) references interest (id);
create index ix_social_interest_interest_15 on social_interest (interest_id);
alter table suggestion add constraint fk_suggestion_activity_16 foreign key (activity_id) references activity (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;
create index ix_suggestion_activity_16 on suggestion (activity_id);
alter table suggestion add constraint fk_suggestion_scale_17 foreign key (scale_id) references scale (id);
create index ix_suggestion_scale_17 on suggestion (scale_id);
alter table suggestion add constraint fk_suggestion_interest_18 foreign key (interest_id) references interest (id);
create index ix_suggestion_interest_18 on suggestion (interest_id);
alter table wimha_user add constraint fk_wimha_user_scale_19 foreign key (scale_id) references scale (id);
create index ix_wimha_user_scale_19 on wimha_user (scale_id);
alter table wimha_user add constraint fk_wimha_user_picto_20 foreign key (picto_id) references picture (id);
create index ix_wimha_user_picto_20 on wimha_user (picto_id);



alter table received_friend_requests add constraint fk_received_friend_requests_s_01 foreign key (social_interest_id) references social_interest (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;

alter table received_friend_requests add constraint fk_received_friend_requests_w_02 foreign key (wimha_user_id) references wimha_user (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;

alter table sent_friend_requests add constraint fk_sent_friend_requests_socia_01 foreign key (social_interest_id) references social_interest (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;

alter table sent_friend_requests add constraint fk_sent_friend_requests_wimha_02 foreign key (wimha_user_id) references wimha_user (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;

alter table friendship add constraint fk_friendship_social_interest_01 foreign key (social_interest_id) references social_interest (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;

alter table friendship add constraint fk_friendship_wimha_user_02 foreign key (wimha_user_id) references wimha_user (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE;


# --- !Downs

drop table if exists activity cascade;

drop table if exists activity_update cascade;

drop table if exists interest cascade;

drop table if exists localized_string cascade;

drop table if exists picture cascade;

drop table if exists scale cascade;

drop table if exists social_action cascade;

drop table if exists social_interest cascade;

drop table if exists received_friend_requests cascade;

drop table if exists sent_friend_requests cascade;

drop table if exists friendship cascade;

drop table if exists suggestion cascade;

drop table if exists wimha_user cascade;

drop sequence if exists activity_update_seq;

drop sequence if exists interest_seq;

drop sequence if exists localized_string_seq;

drop sequence if exists scale_seq;

drop sequence if exists social_action_seq;

drop sequence if exists suggestion_seq;

