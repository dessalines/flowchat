--liquibase formatted sql
--changeset tyler:12

create table censored_word (
    id bigserial primary key,
    regex varchar(50) not null unique,
    created timestamp default current_timestamp
);

--rollback drop table censored_word

insert into censored_word (regex) values ('\bnig(\b|g?(a|er)?s?)\b'),('kikes?'),('retard'),('retarded'),('retards'),('\btard(s|ed)?\b'),('lib(er)?tard(s|ed)?'),('fucktard(s|ed)?'),('cunts?'),('fagg?(s|ots?|y)?\b'),( 'mongoloids?'),('towel\s*heads?'),('\bspi(c|k)s?\b'),('\bchinks?'),('feminazis?'),('niglets?'),('beaners?'),('\bcoons?\b'),('jungle\s*bunn(y|ies?)'),('jigg?aboo?s?'),('\bpakis?\b'),('rag\s*heads?'),('gooks?'),('g(y|i)ps(y|ies?)'),('cuck'),('fag'),('fagtard');

--rollback delete from censored_word;