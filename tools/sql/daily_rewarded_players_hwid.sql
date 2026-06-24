CREATE TABLE IF NOT EXISTS daily_rewarded_players_hwid (
    day INT NOT NULL,
    hwid VARCHAR(255) NOT NULL,
    
    PRIMARY KEY (day, hwid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;