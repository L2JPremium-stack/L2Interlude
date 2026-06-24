CREATE TABLE `player_mail` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `senderId` INT NOT NULL,
  `receiverId` INT NOT NULL,
  `itemObjectId` INT NOT NULL,
  `itemId` INT NOT NULL,
  `itemCount` BIGINT NOT NULL,
  `enchantLevel` INT DEFAULT 0,
  `expireTime` BIGINT NOT NULL,
  `claimed` BOOLEAN DEFAULT FALSE,
  `returned` BOOLEAN DEFAULT FALSE
);