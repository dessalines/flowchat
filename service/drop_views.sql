BEGIN TRANSACTION;
    DO $$DECLARE r record;
         DECLARE s TEXT;
        BEGIN
            FOR r IN select table_schema,table_name
                     from information_schema.views
                     where table_schema = 'public'
            LOOP
                s := 'DROP VIEW IF EXISTS ' || quote_ident(r.table_name) || ' CASCADE;';
                EXECUTE s;

                RAISE NOTICE 's = % ',s;

            END LOOP;
        END$$;
    COMMIT TRANSACTION;
