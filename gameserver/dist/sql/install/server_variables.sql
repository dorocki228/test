CREATE TABLE IF NOT EXISTS `server_variables` (
	`name` VARCHAR(86) NOT NULL DEFAULT '',
	`value` MEDIUMTEXT CHARACTER SET UTF8 NOT NULL,
	PRIMARY KEY (`name`)
) ENGINE=MyISAM;

INSERT INTO `server_variables` VALUES
    ('gve_zone_status_outlaw_forest','ACTIVATED'),
    ('gve_zone_status_swamp_of_scream','ACTIVATED'),
    ('gve_zone_status_hot_springs','ACTIVATED'),
    ('gve_zone_status_talking_island','ACTIVATED'),
    ('gve_zone_status_ketra','ENABLED'),
    ('gve_zone_status_bs_mid','ENABLED'),
    ('gve_zone_status_valey_skins','ENABLED'),
    ('gve_zone_status_varka_mid','ACTIVATED'),
    ('gve_zone_status_abadon_camp','ACTIVATED'),
    ('gve_zone_status_anrchaic','ACTIVATED'),
    ('gve_zone_status_giant_cave','ENABLED'),
    ('gve_zone_status_antaras_lair','ACTIVATED'),
    ('gve_zone_status_imperial_tomb','ENABLED'),
    ('gve_zone_status_shrin_of_loyality','ENABLED'),
    ('gve_zone_status_aden_high','ENABLED'),
    ('gve_zone_status_roa_high','ACTIVATED'),
    ('gve_zone_status_dino_island','DISABLED'),
    ('gve_zone_status_garden-genesis','ENABLED'),
    ('gve_zone_status_forest-dead','DISABLED'),
    ('gve_zone_status_pagan_high','ACTIVATED'),
    ('gve_zone_status_school','ACTIVATED'),
    ('gve_zone_status_cemetry','ENABLED'),
    ('gve_zone_status_talking','DISABLED'),
    ('gve_zone_active_count_gve_low','4'),
    ('gve_zone_active_count_gve_mid','3'),
    ('gve_zone_active_count_gve_high','3'),
    ('gve_zone_active_count_gve_pvp','1');