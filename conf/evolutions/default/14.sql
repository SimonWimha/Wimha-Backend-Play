# --- !Ups
create table social_action_wimha_user (
	social_action_wimha_user_pk serial,
  	wimha_user_id             	varchar(40) not null,
  	social_action_id          	bigint not null,

  	PRIMARY KEY(social_action_wimha_user_pk),
    UNIQUE(wimha_user_id, social_action_id),
    FOREIGN KEY(wimha_user_id) REFERENCES wimha_user(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY(social_action_id) REFERENCES social_action(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

# --- !Downs
drop table if exists social_action_wimha_user cascade;
DELETE FROM social_action WHERE dtype in ('MultiUrl', 'MultiSugg');