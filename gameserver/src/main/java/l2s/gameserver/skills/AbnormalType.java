package l2s.gameserver.skills;

/**
 * @author Bonux
 **/
public enum AbnormalType
{
	NONE(-1),
	AB_HAWK_EYE(0),
	ALL_ATTACK_DOWN(1),
	ALL_ATTACK_UP(2),
	ALL_SPEED_DOWN(3),
	ALL_SPEED_UP(4),
	ANTARAS_DEBUFF(5),
	ARMOR_EARTH(6),
	ARMOR_FIRE(7),
	ARMOR_HOLY(8),
	ARMOR_UNHOLY(9),
	ARMOR_WATER(10),
	ARMOR_WIND(11),
	ATTACK_SPEED_UP_BOW(12),
	ATTACK_TIME_DOWN(13),
	ATTACK_TIME_UP(14),
	AVOID_DOWN(15),
	AVOID_UP(16),
	AVOID_UP_SPECIAL(17),
	BERSERKER(18),
	BIG_BODY(19),
	BIG_HEAD(20),
	BLEEDING(21),
	BOW_RANGE_UP(22),
	BUFF_QUEEN_OF_CAT(23), // SERVITOR_BLESSING(23),
	BUFF_UNICORN_SERAPHIM(24), // SERVITOR_GIFT(24),
	CANCEL_PROB_DOWN(25),
	CASTING_TIME_DOWN(26),
	CASTING_TIME_UP(27),
	CHEAP_MAGIC(28),
	CRITICAL_DMG_DOWN(29),
	CRITICAL_DMG_UP(30),
	CRITICAL_PROB_DOWN(31),
	CRITICAL_PROB_UP(32),
	DANCE_OF_AQUA_GUARD(33),
	DANCE_OF_CONCENTRATION(34),
	DANCE_OF_EARTH_GUARD(35),
	DANCE_OF_FIRE(36),
	DANCE_OF_FURY(37),
	DANCE_OF_INSPIRATION(38),
	DANCE_OF_LIGHT(39),
	DANCE_OF_MYSTIC(40),
	DANCE_OF_PROTECTION(41),
	DANCE_OF_SHADOW(42),
	DANCE_OF_SIREN(43),
	DANCE_OF_VAMPIRE(44),
	DANCE_OF_WARRIOR(45),
	DEBUFF_NIGHTSHADE(46),
	DEBUFF_SHIELD(47),
	DECREASE_WEIGHT_PENALTY(48),
	DERANGEMENT(49),
	DETECT_WEAKNESS(50),
	DMG_SHIELD(51),
	DOT_ATTR(52),
	DOT_MP(53),
	DRAGON_BREATH(54),
	DUELIST_SPIRIT(55),
	FATAL_POISON(56),
	FISHING_MASTERY_DOWN(57),
	FLY_AWAY(58),
	FOCUS_DAGGER(59),
	HEAL_EFFECT_DOWN(60),
	HEAL_EFFECT_UP(61),
	HERO_BUFF(62),
	HERO_DEBUFF(63),
	HIT_DOWN(64),
	HIT_UP(65),
	HOLY_ATTACK(66),
	HP_RECOVER(67),
	HP_REGEN_DOWN(68),
	HP_REGEN_UP(69),
	LIFE_FORCE_ORC(70),
	LIFE_FORCE_OTHERS(71),
	MAGIC_CRITICAL_UP(72),
	MAJESTY(73),
	MAX_BREATH_UP(74),
	MAX_HP_DOWN(75),
	MAX_HP_UP(76),
	MAX_MP_UP(77),
	MA_DOWN(78),
	MA_UP(79),
	MA_UP_HERB(80),
	MD_DOWN(81),
	MD_UP(82),
	MD_UP_ATTR(83),
	MIGHT_MORTAL(84),
	MP_COST_DOWN(85),
	MP_COST_UP(86),
	MP_RECOVER(87),
	MP_REGEN_UP(88),
	MULTI_BUFF(89),
	MULTI_DEBUFF(90),
	PARALYZE(91),
	PA_DOWN(92),
	PA_PD_UP(93),
	PA_UP(94),
	PA_UP_HERB(95),
	PA_UP_SPECIAL(96),
	PD_DOWN(97),
	PD_UP(98),
	PD_UP_BOW(99),
	PD_UP_SPECIAL(100),
	PINCH(101),
	POISON(102),
	POLEARM_ATTACK(103),
	POSSESSION(104),
	PRESERVE_ABNORMAL(105),
	PUBLIC_SLOT(106),
	RAGE_MIGHT(107),
	REDUCE_DROP_PENALTY(108),
	REFLECT_ABNORMAL(109),
	RESIST_BLEEDING(110),
	RESIST_DEBUFF_DISPEL(111),
	RESIST_DERANGEMENT(112),
	RESIST_HOLY_UNHOLY(113),
	RESIST_POISON(114),
	RESIST_SHOCK(115),
	RESIST_SPIRITLESS(116),
	REUSE_DELAY_DOWN(117),
	REUSE_DELAY_UP(118),
	ROOT_PHYSICALLY(119),
	ROOT_MAGICALLY(120),
	SHIELD_DEFENCE_UP(121),
	SHIELD_PROB_UP(122),
	SILENCE(123),
	SILENCE_ALL(124),
	SILENCE_PHYSICAL(125),
	SLEEP(126),
	SNIPE(127),
	SONG_OF_CHAMPION(128),
	SONG_OF_EARTH(129),
	SONG_OF_FLAME_GUARD(130),
	SONG_OF_HUNTER(131),
	SONG_OF_INVOCATION(132),
	SONG_OF_LIFE(133),
	SONG_OF_MEDITATION(134),
	SONG_OF_RENEWAL(135),
	SONG_OF_STORM_GUARD(136),
	SONG_OF_VENGEANCE(137),
	SONG_OF_VITALITY(138),
	SONG_OF_WARDING(139),
	SONG_OF_WATER(140),
	SONG_OF_WIND(141),
	SPA_DISEASE_A(142),
	SPA_DISEASE_B(143),
	SPA_DISEASE_C(144),
	SPA_DISEASE_D(145),
	SPEED_DOWN(146),
	SPEED_UP(147),
	SPEED_UP_SPECIAL(148), // skillid 10093
	SSQ_TOWN_BLESSING(149),
	SSQ_TOWN_CURSE(150),
	STEALTH(151),
	STUN(152),
	THRILL_FIGHT(153),
	TOUCH_OF_DEATH(154),
	TOUCH_OF_LIFE(155),
	TURN_FLEE(156),
	TURN_PASSIVE(157),
	TURN_STONE(158),
	ULTIMATE_BUFF(159),
	ULTIMATE_DEBUFF(160),
	VALAKAS_ITEM(161),
	VAMPIRIC_ATTACK(162),
	WATCHER_GAZE(163),
	RESURRECTION_SPECIAL(164),
	COUNTER_SKILL(165),
	AVOID_SKILL(166),
	CP_UP(167),
	CP_DOWN(168),
	CP_REGEN_UP(169),
	CP_REGEN_DOWN(170),
	INVINCIBILITY(171),
	ABNORMAL_INVINCIBILITY(172),
	PHYSICAL_STANCE(173),
	MAGICAL_STANCE(174),
	COMBINATION(175),
	ANESTHESIA(176),
	CRITICAL_POISON(177),
	SEIZURE_PENALTY(178),
	ABNORMAL_ITEM(179),
	SEIZURE_A(180),
	SEIZURE_B(181),
	SEIZURE_C(182),
	FORCE_MEDITATION(183),
	MIRAGE(184),
	POTION_OF_GENESIS(185),
	PVP_DMG_UP(186),
	PVP_DMG_DOWN(187),
	IRON_SHIELD(188),
	TRANSFER_DAMAGE(189),
	SONG_OF_ELEMENTAL(190),
	DANCE_OF_ALIGNMENT(191),
	ARCHER_SPECIAL(192),
	SPOIL_BOMB(193),
	FIRE_DOT(194),
	WATER_DOT(195),
	WIND_DOT(196),
	EARTH_DOT(197),
	HEAL_POWER_UP(198),
	RECHARGE_UP(199),
	NORMAL_ATTACK_BLOCK(200),
	DISARM(201),
	DEATH_MARK(202),
	KAMAEL_SPECIAL(203),
	TRANSFORM(204),
	DARK_SEED(205),
	REAL_TARGET(206),
	FREEZING(207),
	TIME_CHECK(208),
	MA_MD_UP(209),
	DEATH_CLACK(210),
	HOT_GROUND(211),
	EVIL_BLOOD(212),
	ALL_REGEN_UP(213),
	ALL_REGEN_DOWN(214),
	IRON_SHIELD_I(215),
	ARCHER_SPECIAL_I(216),
	T_CRT_RATE_UP(217),
	T_CRT_RATE_DOWN(218),
	T_CRT_DMG_UP(219),
	T_CRT_DMG_DOWN(220),
	INSTINCT(221),
	OBLIVION(222),
	WEAK_CONSTITUTION(223),
	THIN_SKIN(224),
	ENERVATION(225),
	SPITE(226),
	MENTAL_IMPOVERISH(227),
	ATTRIBUTE_POTION(228),
	TALISMAN(229),
	MULTI_DEBUFF_FIRE(230),
	MULTI_DEBUFF_WATER(231),
	MULTI_DEBUFF_WIND(232),
	MULTI_DEBUFF_EARTH(233),
	MULTI_DEBUFF_HOLY(234),
	MULTI_DEBUFF_UNHOLY(235),
	LIFE_FORCE_KAMAEL(236),
	MA_UP_SPECIAL(237),
	PK_PROTECT(238),
	MAXIMUM_ABILITY(239),
	TARGET_LOCK(240),
	PROTECTION(241),
	WILL(242),
	SEED_OF_KNIGHT(243),
	EXPOSE_WEAK_POINT(244),
	FORCE_OF_DESTRUCTION(245),
	ELEMENTAL_ARMOR(246),
	SUMMON_CONDITION(247),
	IMPROVE_PA_PD_UP(248),
	IMPROVE_MA_MD_UP(249),
	IMPROVE_HP_MP_UP(250),
	IMPROVE_CRT_RATE_DMG_UP(251),
	IMPROVE_SHIELD_RATE_DEFENCE_UP(252),
	IMPROVE_SPEED_AVOID_UP(253),
	LIMIT(254),
	MULTI_DEBUFF_SOUL(255),
	CURSE_LIFE_FLOW(256),
	BETRAYAL_MARK(257),
	TRANSFORM_HANGOVER(258),
	TRANSFORM_SCRIFICE(259),
	SONG_OF_WINDSTORM(260),
	DANCE_OF_BLADESTORM(261),
	IMPROVE_VAMPIRIC_HASTE(262),
	WEAPON_MASTERY(263),
	APELLA(264),
	TRANSFORM_SCRIFICE_P(265),
	SUB_TRIGGER_HASTE(266),
	SUB_TRIGGER_DEFENCE(267),
	SUB_TRIGGER_CRT_RATE_UP(268),
	SUB_TRIGGER_SPIRIT(269),
	MIRAGE_TRAP(270),
	DEATH_PENALTY(271),
	ENTRY_FOR_GAME(272),
	BLOOD_CONSTRACT(273),
	DWARF_ATTACK_BUFF(274),
	DWARF_DEFENCE_BUFF(275),
	EVASION_BUFF(276),
	BLESS_THE_BLOOD(277),
	PVP_WEAPON_BUFF(278),
	PVP_WEAPON_DEBUFF(279),
	SEED_OF_CRITICAL(280),
	VP_UP(281),
	BOT_PENALTY(282),
	HIDE(283),
	DD_RESIST(284),
	SONG_OF_PURIFICATION(285),
	DANCE_OF_BERSERKER(286),
	REFLECT_MAGIC_DD(287),
	FINAL_SECRET(288),
	STIGMA_OF_SILEN(289),
	SEED_DEBUFF_A(290),
	SEED_DEBUFF_B(291),
	SEED_DEBUFF_C(292),
	SEED_DEBUFF_D(293),
	SEED_DEBUFF_E(294),
	SEED_DEBUFF_F(295),
	SEED_DEBUFF_G(296),
	SEED_DEBUFF_H(297),
	SEED_DEBUFF_I(298),
	SEED_BUFF_A(299),
	AGATHION_BUFF(300),
	COUNTER_CRITICAL(301),
	COUNTER_CRITICAL_TRIGGER(302),
	ATTACK_TIME_DOWN_SPECIAL(303),
	BLOCK_SPEED_UP(304),
	BLOCK_SHIELD_UP(305),
	DEATHWORM(306),
	MULTI_DEBUFF_A(307),
	MULTI_DEBUFF_B(308),
	MULTI_DEBUFF_C(309),
	MULTI_DEBUFF_D(310),
	MULTI_DEBUFF_E(311),
	MULTI_DEBUFF_F(312),
	MULTI_DEBUFF_G(313),
	STIGMA_A(314),
	MULTI_BUFF_A(315),
	VAMPIRIC_ATTACK_SPECIAL(316),
	BLOCK_RESURRECTION(317),
	IMPROVE_HIT_DEFENCE_CRT_RATE_UP(318),
	IMPROVE_MAGIC_SPEED_CRT_RATE_UP(319),
	EVENT_GAWI(320),
	EVENT_BAWI(321),
	EVENT_BO(322),
	EVENT_WIN(323),
	EVENT_TERRITORY(324),
	EVENT_SANTA_REWARD(325),
	VP_KEEP(326),
	WP_CHANGE_EVENT(327),
	SIGNAL_A(328),
	SIGNAL_B(329),
	SIGNAL_C(330),
	SIGNAL_D(331),
	SIGNAL_E(332),
	CHANGE_ATTR_W(333),
	CHAGNE_ATTR_A(334),
	EVENT_BUF1(335),
	EVENT_BUF2(336),
	EVENT_BUF3(337),
	EVENT_BUF4(338),
	EVENT_BUF5(339),
	EVENT_BUF6(340),
	EVENT_BUF7(341),
	EVENT_BUF8(342),
	EVENT_BUF9(343),
	EVENT_BUF10(344),
	SOA_BUFF1(345),
	SOA_BUFF2(346),
	SOA_BUFF3(347),
	DAMAGE_AMPLIFY(348),
	VIBRATION(349),
	BLOCK_TRANSFORM(350),
	SKILL_IGNORE(351),
	SIGNAL_G(352),
	KNIGHT_AURA(353),
	PATIENCE(354),
	VOTE(355),
	MP_SHIELD(356),
	VP_CHANGE(357),
	ABILITY_CHANGE(358),
	WISPERING_OF_BATTLE(359),
	MOTION_OF_DEFENCE(360),
	ARMOR_ELEMENT_ALL(361),
	MORALE_UP(362),
	TIME_BOMB(363),
	KNIGHT_SHIELD(364),
	AIRBIND(365),
	CHANGEBODY(366),
	KNOCKDOWN(367),
	MAX_HP_UP_K(368),
	BR_EVENT_BUF1(369),
	BR_EVENT_BUF2(370),
	BR_EVENT_BUF3(371),
	BR_EVENT_BUF4(372),
	BR_EVENT_BUF5(373),
	BR_EVENT_BUF6(374),
	BR_EVENT_BUF7(375),
	BR_EVENT_BUF8(376),
	BR_EVENT_BUF9(377),
	BR_EVENT_BUF10(378),
	POSSESSION_SPECIAL(379),
	DEPORT(380),
	FORCE_HP_UP(381),
	SPECIAL_BERSERKER(382),
	BATTLE_CRY(383),
	AVOID_SKILL_SPECIAL(384),
	SUPER_MOVE(385),
	CONFUSION(386),
	SUPER_BUFF(387),
	SUPER_AVOID(388),
	MOVEMENT(389),
	CRITICAL_SPECIAL(392),
	WEAPON_MASTER_SPECIAL(393),
	CLASS_CHANGE(394),
	DC_MOD(395),
	ABSORB(396),
	ENCHANTER_MOD(397),
	SHIELD_ATTACK(400),
	AURA(402),
	LIFE_FORCE_HEALER(403),
	LIFE_FORCE_HEALER_SELF(404),
	TURN_CRYSTAL(405),
	MARK_OF_LUMI(406),
	ENERGY_OF_TOTEM_1(407),
	ENERGY_OF_TOTEM_2(408),
	ENERGY_OF_TOTEM_3(409),
	ENERGY_OF_TOTEM_4(410),
	DEATH_PENALTY_GD(411),
	BUFF_SPECIAL_ATTACK(415),
	BUFF_SPECIAL_DEFENCE(416),
	BUFF_SPECIAL_CONDITION(417),
	BUFF_SPECIAL_CRITICAL(418),
	BUFF_SPECIAL_HITAVOID(419),
	BUFF_SPECIAL_MOVE(420),
	BUFF_SPECIAL_CLASS(421),
	BUFF_SPECIAL_AURA(422),
	BUFF_SPECIAL_MULTI(423),
	EARTHWORM_DEBUFF(424),
	SPECIAL_RIDE(425),
	SPIRIT_SHARING(426),
	SUBSTITUTE_BUFF(427),
	ANTI_SUMMON(428),
	NPC_FURY(429),
	PET_FURY(430),
	CLAN_ADVENT(431),
	DEATH_PENALTY_BLOCK(432),
	SYNERGY_SIGEL(433),
	SYNERGY_TIR(434),
	SYNERGY_OTHEL(435),
	SYNERGY_YR(436),
	SYNERGY_FEOH(437),
	SYNERGY_IS(438),
	SYNERGY_WYNN(439),
	SYNERGY_EOLH(440),
	MARK_DEBUF_A(441, true),
	MARK_DEBUF_B(442, true),
	MARK_DEBUF_C(443, true),
	MARK_DEBUF_D(444, true),
	ACADEMY_UP(445),
	CLAN_FRIEND(446),
	CLAN_BOUNDARY(447),
	CLAN_PRISON(448),
	RACE_HUMAN1(449),
	RACE_ELF1(450),
	RACE_DARKELF1(451),
	RACE_ORC1(452),
	RACE_ORC2(453),
	RACE_DWAF1(454),
	RACE_KAMAEL1(456),
	NPC_ATTACK1(457),
	NPC_ATTACK2(458),
	NPC_ATTACK3(459),
	WEAKENED_DEATH_PENALTY(460),
	VP_MENTOR_RUNE(461),
	INVINCIBILITY_SPECIAL(462),
	BUFF_MENTEE1(463),
	BUFF_PCCAFE_EXP1(464),
	SYNERGY_PARTY_BUF(465),
	NOTICE_PORTAL(466),
	EXP_BOTTLE(467),
	RESIST_DEATH(468),
	RHAPSODY(469),
	NOCHAT(470),
	FLAG_BUF(471),
	FLAG_DEBUF(472),
	BUFF_TREE(473),
	INSTANT_EV_BUFF1(474),
	INSTANT_EV_BUFF2(475),
	INSTANT_EV_BUFF3(476),
	INSTANT_EV_BUFF4(477),
	INSTANT_EV_BUFF5(478),
	INSTANT_EV_BUFF6(479),
	SEED_TALISMAN1(480),
	SEED_TALISMAN2(481),
	SEED_TALISMAN3(482),
	SEED_TALISMAN4(483),
	SEED_TALISMAN5(484),
	SEED_TALISMAN6(485),
	LUMIERE_BUFF(486),
	BRIGHTNESS_BLESS(487),
	CURIOUS_HOUSE(488),
	SPECIAL_MOVE_UP(489),
	STAR_AGATHION_EXP_SP_BUFF1(490),
	NPC_MULTI_BUFF1(491),
	DUAL_ATTACK_UP(493),
	DUAL_SKILL_UP(494),
	DUAL_DEFENCE_UP(495),
	DUAL_DMG_SHIELD(496),
	KALIE_BUFF(497),
	SOUL_SHIELD(498), // skillid 17118, 27580
	BR_UTHANKA_BUFF(499),
	FIELD_RAID_BUFF1(500),
	PD_UP_DMAGIC(501),
	PREMIUM_BUFF(502),
	RUNWAY_ARMOR(503),
	RUNWAY_WEAPON(504),
	G_EV_BUFF1(505),
	BATTLE_TOLERANCE(506),
	SHOOTING_STANCE(508),
	ELUSIVE_MIRAGE(509), // TODO: name
	ASSASSINS_REFLEX(510), // TODO: name
	BODY_DESTRUCTION(511),
	MD_DOWN_AWAKEN(512), // TODO: name
	FREEZE_SLOW(514), // TODO: name
	YUL_EYE(515),
	MA_UP_SPECIAL_AWAKEN(516),
	CLASS_IS_BUFF(518), // TODO: name
	GREATER_SERVITOR_BUFF(519), // TODO: name
	MASS_HEAL_BUFF(520), // TODO: name
	VITALITY_TIME_UP(530), // TODO: name
	HORSE_POWER(531), // TODO: name
	ACADEMY_BENEFACTION(532), // TODO: name
	WIND_BLEND(536), // TODO: name
	ALONERS_TACT(537), // TODO: name
	SHADOW_HUNTER(538), // TODO: name
	CRITICAL_CHANCES(539), // TODO: name
	SHADOW_FLASH(540), // TODO: name
	CRIPPLING_DANCE(542), // TODO: name
	DESERT_THIRST(546), // TODO: name
	FACEOFF(548), // TODO: name
	FURY(549), // TODO: name
	POTION_OF_PROTECTION(552), // TODO: name, skillid 18160, 18156
	POWER_BLUFF(561), // TODO: name
	ARCANE_PROTECTION(572), // TODO: name
	HEAL_RESISTANCE(575), // TODO: name
	DITTY(578), // TODO: name
	STORM_SIGN(580), // TODO: name
	EYE_STORM(582), // TODO: name
	SQUALL(583), // TODO: name
	SAYHA_FURY(584), // TODO: name
	SAYHA_BLESSING(585), // TODO: name
	COMPELLING_WIND(587), // TODO: name
	DIVINE_STORM(588), // TODO: name
	SYNERGY_LENKER(589),
	SYNERGY_SEER(590),
	INSIDE_POSITION(593), // TODO: name
	BLOCK_ESCAPE(595), // TODO: name
	STEEL_MIND(596), // TODO: name
	SPALLATION(597), // TODO: name
	BLOCK_INVINCIBILITY(605), // TODO: name
	SONG_WEAPON(610), // TODO: name
	SONG_DEFENCE(612), // TODO: name
	DIVINITY_OF_EINHASAD(618), // TODO: name
	SHARING_EQUIPMENT(620), // TODO: name
	SHILLIEN_PROTECTION(621), // TODO: name
	BARRIER(622), // TODO: name
	ARMOR_BREAKER(623), // TODO: name
	EXOSKELETAL_SHIELD(626), // TODO: name
	BIG_BODY_COMBINATION(629), // TODO: name
	BIG_BODY_COMBINATION_TG(630), // TODO: name
	CURSE_EXPOSURE(633), // TODO: name
	FREEZING_INVOKE(634), // TODO: name
	CLAN_TEAMWORK(643), // TODO: name
	CHANT_BUFF(644), // TODO: name
	MAGE_BANE(-1), // TODO: name, id
	WARRIOR_BANE(-1), // TODO: name, id
	EVA_PROTECTION(-1), // TODO: name, id
	SHIELD_SACRIFICE(-1), // TODO: name, id
	TENACITY(-1), // TODO: name, id
    DESTROYER_ROAR(-1), // TODO: name, id
    DETECT_DARKNESS(-1), // TODO: name, id
	ABNORMAL_ITEM2(-1), // TODO: name, id
	FREEZE(-1), // TODO: name, id
	SONG_OF_ARCHERY(-1),
	DANCE_OF_SAGE(-1),
	SONG_OF_THIEF(-1),
	SUMMONER_LINK(-1),
	SACRIFICE(-1),
	MARK_OF_TRICK(-1),
	MARK_OF_PLAGUE(-1),
	MARK_OF_WEAKNESS(-1),
	EXP_SPECIAL(-1),
	TALISMAN_TRIGGER_SPECIAL(-1), // TODO: name, id
	COUNTER_FLIP(-1), // TODO: name, id
	EASTERLY_WIND_STRIKE(-1), // TODO: name, id
	MANA_BURST(-1), // TODO: name, id
	GRAN_KAINS_NECKLACE(-1),
	PAAGRIOS_EARRING(-1),
	SAYHAS_RING(-1),
	EINHASADS_NECKLACE(-1),
	EVAS_EARRING(-1),
	MAPHRS_RING(-1),
	SHILENS_NECKLACE(-1),
	SHILENS_EARRING(-1),
	SHILENS_RING(-1),
	TALISMAN_OF_INSOLENCE(-1), // TODO: name, id
	GARNET(-1), // TODO: name, id
	JADE(-1), // TODO: name, id
	RUBY(-1), // TODO: name, id
	SAPPHIRE(-1), // TODO: name, id
	DIAMOND(-1), // TODO: name, id
	PEARL(-1), // TODO: name, id
	VITAL_STONE(-1), // TODO: name, id
	GIANT_ITEM_EQUIP(-1), // TODO: name, id
	SHIELD_OF_LIGHT(-1), // TODO: name, id
	RESEARCH_FAIL(-1), // TODO: name, id
	RESEARCH_REWARD(-1), // TODO: name, id
	RESEARCH_SUCCESS(-1), // TODO: name, id
	LA_VIE_EN_ROSES_ENERGY(-1), // TODO: name, id
	SHIELD_OF_SACRIFICE(-1), // TODO: name, id
	AGATHION_SONG_DANCE(-1), // TODO: name, id
	APPEARANCE_TRANSFORM(-1), // TODO: name, id
	ELEMENTAL_EXP_RATE(-1), // TODO: name, id
	DISARMS(-1), // TODO: name, id
	HOT_SPRING_BUFF(0), // TODO: name
	YELLOW_TALISMAN(-1), // TODO: name, id
	GREY_TALISMAN(-1), // TODO: name, id
	GUARD_TALISMAN(-1), // TODO: name, id
	BENEFACTION_TALISMAN(-1),
	SERVER_RANK(-1),
	RACE_RANK(-1), // TODO: name, id
	RANK_BUFF_TRANSFORM(-1),
	KAMAEL_VEIL(-1), // TODO: name, id
	SHARP_AIM (-1); // TODO: name, id

	public static final AbnormalType[] VALUES = values();

	private final int _clientId;
	private final boolean _stackable;

	private AbnormalType(int clientId)
	{
		_clientId = clientId;
		_stackable = false;
	}

	private AbnormalType(int clientId, boolean stackable)
	{
		_clientId = clientId;
		_stackable = stackable;
	}

	public int getClientId()
	{
		return _clientId;
	}

	public boolean isStackable()
	{
		return _stackable;
	}
}