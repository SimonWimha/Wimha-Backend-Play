# --- !Ups
create table member (
  id                        VARCHAR(40),
  totem_id                  VARCHAR(40),
  user_id                   VARCHAR(40),
  blocked                   boolean,
  constraint pk_member primary key (id),
  constraint fk_user_id foreign key (user_id)
  references wimha_user (id),
  constraint fk_totem_id foreign key (totem_id)
  references totem (id))
;


ALTER TABLE url RENAME member_id TO totem_id;
ALTER TABLE url DROP constraint fk_member_id,
ADD constraint fk_totem_id foreign key (totem_id)
   references totem (id) on delete cascade;

ALTER TABLE url ADD COLUMN member_id VARCHAR(40);
ALTER TABLE url ADD constraint fk_member_id foreign key (member_id)
   references member (id) on delete cascade;

# --- !Downs
ALTER TABLE url DROP member_id;
drop table if exists member cascade;
ALTER TABLE url RENAME totem_id TO member_id;
