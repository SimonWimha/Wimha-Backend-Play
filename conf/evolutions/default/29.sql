# --- !Ups
ALTER TABLE wimha_user ADD cover_picture_id VARCHAR(40);
ALTER TABLE comment ADD message_no_html VARCHAR(500);

# --- !Downs

ALTER TABLE wimha_user DROP cover_picture_id;
ALTER TABLE comment DROP message_no_html;