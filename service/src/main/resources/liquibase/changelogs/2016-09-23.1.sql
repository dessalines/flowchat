--liquibase formatted sql
--changeset tyler:11

create table view_type (
    id bigserial primary key,
    radio_value varchar(50) not null unique,
    created timestamp default current_timestamp
);

--rollback drop table view_type cascade;

insert into view_type(radio_value) values ('card'),('list');

create table sort_type (
    id bigserial primary key,
    radio_value varchar(50) not null unique,
    created timestamp default current_timestamp
);

--rollback drop table sort_type cascade;

insert into sort_type(radio_value) values ('modified__desc'),('time-3600'),('time-86400'),
    ('time-604800'),('time-2628000'), ('time-31540000'), ('avg_rank__desc');


create table user_setting (
    id bigserial primary key,
    user_id bigint not null,
    default_view_type_id bigint not null default 2,
    default_sort_type_id bigint not null default 3,
    read_onboard_alert boolean not null default false,
    created timestamp default current_timestamp,
    constraint fk1_user_setting_user foreign key (user_id)
        references user_ (id)
        on update cascade on delete cascade,
    constraint fk2_user_setting_view_type foreign key (default_view_type_id)
        references view_type (id)
        on update cascade on delete cascade,
    constraint fk3_user_setting_sort_type foreign key (default_sort_type_id)
        references sort_type (id)
        on update cascade on delete cascade
);

--rollback drop table user_setting;

insert into user_setting (user_id)
select id from user_;

drop view user_view;

create view user_view as
select u.id,
    u.name,
    fu.id as full_user_id,
    fu.email,
    us.default_view_type_id,
    vt.radio_value as default_view_type_radio_value,
    st.radio_value as default_sort_type_radio_value,
    us.read_onboard_alert,
    l.id as login_id,
    l.auth,
    l.expire_time,
    u.created
from user_ as u
left join full_user as fu on u.id = fu.user_id
left join user_setting as us on u.id = us.user_id
left join view_type as vt on us.default_view_type_id = vt.id
left join sort_type as st on us.default_sort_type_id = st.id
left join login as l on l.user_id = u.id;


--rollback create view user_view as select u.id,     u.name,     fu.id as full_user_id,     fu.email from user_ as u join full_user as fu on u.id = fu.user_id;



