CREATE TABLE IF NOT EXISTS `clanhall` (
  `id` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `name` varchar(40) NOT NULL DEFAULT '',
  `last_siege_date` INT UNSIGNED NOT NULL,
  `owner_id` INT NOT NULL DEFAULT '0',
  `own_date` INT UNSIGNED NOT NULL,
  `siege_date` INT UNSIGNED NOT NULL,
  `auction_min_bid` bigint(20) NOT NULL,
  `auction_length` int(11) NOT NULL,
  `auction_desc` text,
  `cycle` int(11) NOT NULL,
  `paid_cycle` int(11) NOT NULL,
  PRIMARY KEY (`id`,`name`)
);

-- ----------------------------
-- Records of clanhall
-- ----------------------------
INSERT INTO clanhall VALUES ('22', 'Moonstone Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('23', 'Onyx Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('24', 'Topaz Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('25', 'Ruby Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('26', 'Crystal Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('27', 'Onyx Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('28', 'Sapphire Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('29', 'Moonstone Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('30', 'Emerald Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('31', 'The Atramental Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('32', 'The Scarlet Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('33', 'The Viridian Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('36', 'The Golden Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('37', 'The Silver Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('38', 'The Mithril Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('39', 'Silver Manor', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('40', 'Gold Manor', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('41', 'The Bronze Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('42', 'The Golden Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('43', 'The Silver Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('44', 'The Mithril Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('45', 'The Bronze Chamber', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('46', 'Silver Manor', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('65', 'Emerald Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('66', 'Crystal Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('67', 'Sapphire Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('68', 'Aquamarine Hall', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('69', 'Blue Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('70', 'Brown Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('71', 'Yelow Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('72', 'White Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('73', 'Black Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');
INSERT INTO clanhall VALUES ('74', 'Green Barracks', '0', '0', '0', '0', '0', '0', null, '0', '0');

