DROP TABLE IF EXISTS `character_post_friends`;
CREATE TABLE `character_post_friends`(
  `object_id` INT(11) NOT NULL,
  `post_friend` INT(11) NOT NULL,
  PRIMARY KEY (`object_id`,`post_friend`)
);