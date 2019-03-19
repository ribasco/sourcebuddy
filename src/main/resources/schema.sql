CREATE TABLE IF NOT EXISTS "SOURCE_SERVER_DETAILS"
(
  "server_id"   INTEGER PRIMARY KEY AUTOINCREMENT,
  "name"        TEXT,
  "ip_address"  TEXT    NOT NULL,
  "port"        INTEGER NOT NULL,
  "app_id"      INTEGER,
  "status"      INTEGER,
  "tags"        TEXT,
  "os"          TEXT,
  "last_update" DATETIME,
  "game_id"     INTEGER,
  "description" TEXT,
  "directory"   TEXT,
  "version"     INTEGER
);