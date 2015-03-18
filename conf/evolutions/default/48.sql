# --- !Ups
ALTER TABLE totem
    RENAME TO tribu;

ALTER TABLE delayed_notification
    RENAME totem_id TO tribu_id;

ALTER TABLE delayed_notification DROP constraint fk_totem_id,
ADD constraint fk_tribu_id foreign key (tribu_id)
   references tribu (id) on delete cascade;

ALTER TABLE position
    RENAME totem_id TO tribu_id;

ALTER TABLE position DROP constraint fk_totem_id,
ADD constraint fk_tribu_id foreign key (tribu_id)
   references tribu (id) on delete cascade;

ALTER TABLE social_action
    RENAME totem_id TO tribu_id;

ALTER TABLE thread
    RENAME totem_id TO tribu_id;

ALTER TABLE thread DROP constraint fk_totem_id,
ADD constraint fk_tribu_id foreign key (tribu_id)
   references tribu (id) on delete cascade;


ALTER TABLE url RENAME totem_id TO tribu_id;

ALTER TABLE url DROP constraint fk_totem_id,
ADD constraint fk_tribu_id foreign key (tribu_id)
   references tribu (id) on delete cascade;

ALTER TABLE member RENAME totem_id TO tribu_id;

ALTER TABLE member DROP constraint fk_totem_id,
ADD constraint fk_tribu_id foreign key (tribu_id)
   references tribu (id) on delete cascade;


ALTER TABLE position ADD COLUMN member_id VARCHAR(40);
ALTER TABLE position ADD constraint fk_member_id foreign key (member_id)
   references member (id) on delete cascade;

# --- !Downs
