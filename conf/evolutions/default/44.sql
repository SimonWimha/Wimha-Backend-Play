# --- !Ups
create table pending_image_upload (
  signature                     text,
  picture_id                    VARCHAR(40),
  position_id                   VARCHAR(40),
  ok                            boolean,
  logged                        boolean,
  email                         text,
  twitter_consumer_key          text,
  twitter_consumer_secret       text,
  twitter_access_token          text,
  twitter_access_token_secret   text,
  tweet                         text,
  image_tweet                   text,
  message_facebook_post         text,
  fb_poster_id                  VARCHAR(40),
  constraint pk_pending_image_upload primary key (signature),
  constraint fk_position_id foreign key (position_id)
  references position (id),
  constraint fk_poster_id foreign key (fb_poster_id)
  references wimha_user (id),
  constraint fk_picture_id foreign key (picture_id)
  references picture (id))
;

# --- !Downs
drop table if exists pending_image_upload;