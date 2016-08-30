--liquibase formatted sql
--changeset tyler:9 splitStatements:false

-- create a schema named "audit"
CREATE schema audit;
REVOKE CREATE ON schema audit FROM public;

CREATE TABLE audit.logged_actions (
    id bigint not null,
    schema_name text NOT NULL,
    TABLE_NAME text NOT NULL,
    p_user_name text,
    action_tstamp TIMESTAMP WITH TIME zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action TEXT NOT NULL CHECK (action IN ('I','D','U')),
    original_data text,
    new_data text,
    query text
) WITH (fillfactor=100);

REVOKE ALL ON audit.logged_actions FROM public;

-- You may wish to use different permissions; this lets anybody
-- see the full audit data. In Pg 9.0 and above you can use column
-- permissions for fine-grained control.
GRANT SELECT ON audit.logged_actions TO public;

CREATE INDEX logged_actions_schema_table_idx
ON audit.logged_actions(((schema_name||'.'||TABLE_NAME)::TEXT));

CREATE INDEX logged_actions_action_tstamp_idx
ON audit.logged_actions(action_tstamp);

CREATE INDEX logged_actions_action_idx
ON audit.logged_actions(action);

--
-- Now, define the actual trigger function:
--
CREATE OR REPLACE FUNCTION audit.if_modified_func()
RETURNS TRIGGER AS $$
DECLARE
    v_old_data TEXT;
    v_new_data TEXT;
BEGIN
    IF (TG_OP = 'UPDATE') THEN
        v_old_data := ROW(OLD.*);
        v_new_data := ROW(NEW.*);
        INSERT INTO audit.logged_actions (id, schema_name,table_name,p_user_name,action,original_data,new_data,query)
        VALUES (NEW.id, TG_TABLE_SCHEMA::TEXT,TG_TABLE_NAME::TEXT,session_user::TEXT,substring(TG_OP,1,1),v_old_data,v_new_data, current_query());
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        v_old_data := ROW(OLD.*);
        INSERT INTO audit.logged_actions (id, schema_name,table_name,p_user_name,action,original_data,query)
        VALUES (NEW.id, TG_TABLE_SCHEMA::TEXT,TG_TABLE_NAME::TEXT,session_user::TEXT,substring(TG_OP,1,1),v_old_data, current_query());
        RETURN OLD;
    ELSIF (TG_OP = 'INSERT') THEN
        v_new_data := ROW(NEW.*);
        INSERT INTO audit.logged_actions (id, schema_name,table_name,p_user_name,action,new_data,query)
        VALUES (NEW.id, TG_TABLE_SCHEMA::TEXT,TG_TABLE_NAME::TEXT,session_user::TEXT,substring(TG_OP,1,1),v_new_data, current_query());
        RETURN NEW;
    ELSE
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - Other action occurred: %, at %',TG_OP,now();
        RETURN NULL;
    END IF;

EXCEPTION
    WHEN data_exception THEN
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
        RETURN NULL;
    WHEN unique_violation THEN
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
        RETURN NULL;
    WHEN OTHERS THEN
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
        RETURN NULL;
END;
$$ LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = pg_catalog, audit;

--rollback DROP SCHEMA IF EXISTS audit CASCADE

--
-- To add this trigger to a table, use:
-- CREATE TRIGGER tablename_audit
-- AFTER INSERT OR UPDATE OR DELETE ON tablename
-- FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();


CREATE TRIGGER user_audit
AFTER DELETE ON user_
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER full_user_audit
AFTER DELETE ON full_user
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER discussion_audit
AFTER UPDATE OR DELETE ON discussion
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER discussion_tag_audit
AFTER UPDATE OR DELETE ON discussion_tag
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER favorite_discussion_user_audit
AFTER INSERT OR UPDATE OR DELETE ON favorite_discussion_user
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER comment_audit
AFTER UPDATE OR DELETE ON comment
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER community_audit
AFTER UPDATE OR DELETE ON community
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER community_tag_audit
AFTER UPDATE OR DELETE ON community_tag
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER community_user_audit
AFTER INSERT OR UPDATE OR DELETE ON community_user
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER discussion_user_audit
AFTER INSERT OR UPDATE OR DELETE ON discussion_user
FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

create view community_audit_view as
select audit.logged_actions.*,
discussion.id as discussion_id,
discussion.community_id,
comment.user_id,
user_.name as user_name
from audit.logged_actions
inner join comment on comment.id = audit.logged_actions.id
inner join discussion on comment.discussion_id = discussion.id
inner join user_ on comment.user_id = user_.id
where audit.logged_actions.table_name = 'comment'
union
select audit.logged_actions.*,
discussion.id as discussion_id,
discussion.community_id,
null as user_id,
null as user_name
from audit.logged_actions
inner join discussion on discussion.id = audit.logged_actions.id
where audit.logged_actions.table_name = 'discussion'
union
select audit.logged_actions.*,
null as discussion_id,
community_user.community_id,
community_user.user_id,
user_.name as user_name
from audit.logged_actions
inner join community_user on community_user.id = audit.logged_actions.id
inner join user_ on community_user.user_id = user_.id
where audit.logged_actions.table_name = 'community_user'
order by action_tstamp desc;








