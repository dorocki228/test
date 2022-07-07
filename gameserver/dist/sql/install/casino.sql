DROP TABLE IF EXISTS `casino_rooms`;
CREATE TABLE `casino_rooms`
(
    `id`            INT NOT NULL AUTO_INCREMENT,
    `creator_id`	INT NOT NULL,
    `name`          VARCHAR(35),
    `bet`			INT NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=MyISAM;

DROP TABLE IF EXISTS `casino_history`;
CREATE TABLE `casino_history`
(
    `obj_id_1`      INT NOT NULL,
    `obj_id_2`      INT NOT NULL,
    `bet`           INT NOT NULL,
    `date`          INT UNSIGNED NOT NULL,
    `winner_id`     INT NOT NULL
) ENGINE=MyISAM;
