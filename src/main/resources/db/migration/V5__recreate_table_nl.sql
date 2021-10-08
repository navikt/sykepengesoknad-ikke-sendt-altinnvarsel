DROP TABLE NARMESTE_LEDER;

CREATE TABLE NARMESTE_LEDER
(
    ID                           VARCHAR(36) DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    OPPDATERT                    TIMESTAMP WITH TIME ZONE NOT NULL,
    NARMESTE_LEDER_ID            UUID UNIQUE,
    ORGNUMMER                    VARCHAR                  NOT NULL,
    BRUKER_FNR                   VARCHAR                  NOT NULL,
    NARMESTE_LEDER_FNR           VARCHAR                  NOT NULL,
    NARMESTE_LEDER_TELEFONNUMMER VARCHAR                  NOT NULL,
    NARMESTE_LEDER_EPOST         VARCHAR                  NOT NULL,
    ARBEIDSGIVER_FORSKUTTERER    BOOLEAN,
    AKTIV_FOM                    DATE                     NOT NULL,
    AKTIV_TOM                    DATE                     NULL,
    TIMESTAMP                    TIMESTAMP WITH TIME ZONE NOT NULL
);


CREATE INDEX BRUKER_FNR_IDX ON NARMESTE_LEDER (BRUKER_FNR);
