DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT UPDATE ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
            GRANT DELETE ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
        END IF;
    END
$$;
