# --- !Ups

delete from social_action where id in (select id from social_action so WHERE NOT EXISTS (select user_id from social_interest
                        where interest_id in (select interest_id
                                    from social_interest
                                    where user_id = so.friend_id)
                        and
                        id = so.social_interest_id
                        )
                        AND "dtype" in ('Suggestion', 'UrlSuggest')
union
select id from social_action so WHERE NOT EXISTS (select user_id from social_interest
                        where interest_id in (select interest_id
                                    from social_interest
                                    where user_id = so.new_friend_id)
                        and
                        id = so.social_interest_id
                        )
                        AND "dtype" = 'AcceptedFr');

# --- !Downs