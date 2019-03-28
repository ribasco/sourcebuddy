CREATE TABLE IF NOT EXISTS "SB_SERVER_DETAILS"
(
    server_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT,
    ip_address   TEXT    NOT NULL,
    port         INTEGER NOT NULL,
    app_id       INTEGER,
    status       INTEGER,
    tags         TEXT,
    os           TEXT,
    game_id      INTEGER,
    description  TEXT,
    directory    TEXT,
    bookmarked   INTEGER,
    steam_id     INTEGER,
    secure       INTEGER,
    version      INTEGER,
    dedicated    INTEGER,
    country_code TEXT,
    create_date  DATETIME,
    create_user  TEXT,
    update_date  DATETIME,
    update_user  TEXT,
    CONSTRAINT fk_steam_app
        FOREIGN KEY (app_id
            ) REFERENCES SB_STEAM_APP (app_id),
    CONSTRAINT fk_country_code
        FOREIGN KEY (country_code
            ) REFERENCES SB_COUNTRY (country_code)
);

CREATE UNIQUE INDEX IF NOT EXISTS "IDX_UNQ_IPPORT" ON "SB_SERVER_DETAILS" ("ip_address", "port");

CREATE TABLE IF NOT EXISTS "SB_MANAGED_GAMES"
(
    managed_game_id INTEGER PRIMARY KEY AUTOINCREMENT,
    app_id          INTEGER,
    create_date     DATETIME,
    create_user     TEXT,
    update_date     DATETIME,
    update_user     TEXT,
    CONSTRAINT fk_steam_app
        FOREIGN KEY (app_id
            ) REFERENCES SB_STEAM_APP (app_id)
);

CREATE TABLE IF NOT EXISTS SB_STEAM_APP
(
    app_id      INTEGER PRIMARY KEY NOT NULL,
    name        INTEGER,
    create_date DATETIME,
    create_user TEXT,
    update_date DATETIME,
    update_user TEXT
);

CREATE TABLE IF NOT EXISTS "SB_STEAM_APP_DETAILS"
(
    details_id           INTEGER PRIMARY KEY AUTOINCREMENT,
    app_id               INTEGER NOT NULL,
    name                 TEXT,
    short_description    TEXT,
    detailed_description TEXT,
    header_image_url     TEXT,
    "header_image"       BLOB,
    "type"               TEXT,
    create_date          DATETIME,
    create_user          TEXT,
    update_date          DATETIME,
    update_user          TEXT,
    CONSTRAINT fk_steamapp
        FOREIGN KEY (app_id
            ) REFERENCES SB_STEAM_APP (app_id)
);

CREATE TABLE IF NOT EXISTS SB_MANAGED_SERVERS
(
    server_id     INTEGER,
    rcon_password TEXT,
    create_date   DATETIME,
    create_user   TEXT,
    update_date   DATETIME,
    update_user   TEXT,
    CONSTRAINT fk_source_server
        FOREIGN KEY (server_id
            ) REFERENCES SB_SERVER_DETAILS (server_id)
);


CREATE TABLE IF NOT EXISTS "SB_COUNTRY"
(
    "country_code" TEXT NOT NULL UNIQUE,
    "name"         TEXT,
    create_date    DATETIME,
    create_user    TEXT,
    update_date    DATETIME,
    update_user    TEXT,
    PRIMARY KEY ("country_code")
);

CREATE TABLE IF NOT EXISTS "SB_SERVER_NAME_HISTORY"
(
    "server_id"   INTEGER,
    "name"        INTEGER,
    "create_date" DATETIME,
    "create_user" TEXT,
    "update_date" DATETIME,
    "update_user" TEXT,
    CONSTRAINT fk_source_server
        FOREIGN KEY (server_id
            ) REFERENCES SB_SERVER_DETAILS (server_id)
);

CREATE TABLE IF NOT EXISTS "SB_SERVER_PLAYER_HISTORY"
(
    "server_id"   INTEGER NOT NULL UNIQUE,
    "name"        TEXT,
    "create_date" DATETIME,
    "create_user" INTEGER,
    "update_date" DATETIME,
    "update_user" INTEGER,
    CONSTRAINT fk_source_server
        FOREIGN KEY (server_id
            ) REFERENCES SB_SERVER_DETAILS (server_id)
);