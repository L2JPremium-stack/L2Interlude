CREATE TABLE IF NOT EXISTS solo_farm_spawnlist (
    id INT NOT NULL AUTO_INCREMENT,
    npc_id INT NOT NULL,
    loc_x INT NOT NULL,
    loc_y INT NOT NULL,
    loc_z INT NOT NULL,
    heading INT NOT NULL DEFAULT 0,
    respawn_delay INT NOT NULL DEFAULT 3,
    is_boss TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);


INSERT INTO solo_farm_spawnlist (npc_id, loc_x, loc_y, loc_z, heading, respawn_delay, is_boss) VALUES
(20432, 147520, 25550, -2000, 0, 3, 0),
(20433, 147610, 25640, -2000, 0, 3, 0),
(20434, 147430, 25700, -2000, 0, 4, 0),
(20436, 147700, 25610, -2000, 0, 5, 0);
