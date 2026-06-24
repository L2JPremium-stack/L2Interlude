CREATE TABLE IF NOT EXISTS solo_farm_player_data (
    object_id INT NOT NULL,
    monster_balance INT NOT NULL DEFAULT 0,
    total_bought INT NOT NULL DEFAULT 0,
    total_killed INT NOT NULL DEFAULT 0,
    last_enter_time BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (object_id)
);
