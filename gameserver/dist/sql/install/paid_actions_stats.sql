CREATE TABLE IF NOT EXISTS `paid_actions_stats` (
    `type` VARCHAR(50) NOT NULL,
    `value` BIGINT(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`type`)
) ENGINE=InnoDB;