CREATE TABLE IF NOT EXISTS hwid_account_bans (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    account_name VARCHAR(45) NOT NULL,
    device_id INT NOT NULL,
    reason VARCHAR(255) NOT NULL DEFAULT '',
    admin_name VARCHAR(64) NOT NULL DEFAULT '',
    active TINYINT(1) NOT NULL DEFAULT 1,
    banned_at DATETIME NOT NULL,
    unbanned_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_hwid_account_device (account_name, device_id),
    KEY idx_hwid_account_active (account_name, active),
    KEY idx_hwid_account_device_active (device_id, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
