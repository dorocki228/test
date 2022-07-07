package gve.zones.model;

import com.google.common.base.Enums;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.HardSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.bbs.OutpostCommunityBoardEntry;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.OutpostInstance;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.Optional;

public enum GveOutpost
{
	//start location fire
	of_fire_post_1("Outlaw Forest","Лес Разбойников","亡命之林", "69C37F", Fraction.FIRE, new Location(92936, -4872, -3400), new Location(93528, -4632, -3392), new Location(93496, -5145, -3440), new Location(93160, -5464, -3424)),
	sos_fire_post_1("Swamp of Screams","Болото Криков","尖叫沼泽","69C37F", Fraction.FIRE, new Location(76040, -60376, -2336), new Location(75463, -60921, -2320), new Location(75272, -60168, -2312), new Location(76040, -59144, -2320)),
	hs_fire_post_1("Hot Springs","Горячие Источники","温泉）","69C37F", Fraction.FIRE, new Location(143896, -108184, -3456), new Location(143272, -108280, -3472), new Location(143720, -107336, -3440), new Location(144360, -108440, -3448)),
	ti_fire_post_1("Talking Island","Говорящий Остров","会说话的岛","69C37F", Fraction.FIRE, new Location(-106216, 232008, -3664), new Location(-104904, 232264, -3688), new Location(-105672, 233816, -3640), new Location(-107096, 232728, -3600)),
	//mid location fire
	elven_fire_mid("Elven Village (Mid)","Эльфийская деревня (Средний уровень)","精灵村（中）","E1E338", Fraction.FIRE, new Location(36152, 69208, -3648), new Location(36456, 69992, -3648), new Location(37240, 69000, -3664), new Location(35993, 67786, -3696)),
	oren_fire_mid("Town of Oren (Mid)","Орен (Средний уровень)","奥伦镇（中）","E1E338", Fraction.FIRE, new Location(74616, 59624, -2744), new Location(75496, 60056, -2776), new Location(74840, 58360, -2664), new Location(73768, 59384, -2808)),
	kv_fire_post_1("Ketra Orc Outpost","Застава Орков Кетра","凯特拉兽人前哨站","E1E338", Fraction.FIRE, new Location(136104, -78824, -4032), new Location(136520, -79592, -4040), new Location(135544, -78056, -3784), new Location(136968, -77720, -4056)),
	garden_fire_post_1("Garden of Genesis","Сад Бытия","创世园","E1E338", Fraction.FIRE, new Location(208512, 120432, -1304), new Location(207892, 120153, -1304), new Location(208800, 120192, -1328), new Location(208812, 121069, -1304)),
	ac_fire_post_1("Abandoned Camp","Заброшенный Лагерь","废弃营地","E1E338", Fraction.FIRE, new Location(-59944, 137336, -2328), new Location(-59464, 137303, -2384), new Location(-60088, 138216, -2360), new Location(-61048, 136552, -2104)),
	vos_fire_post_1("Valley of Saints","Долина Святых","圣人谷","E1E338", Fraction.FIRE, new Location(84840, -81241, -3560), new Location(85768, -80920, -3664), new Location(85272, -82376, -3568), new Location(83800, -81640, -3576)),
	varka_mid_fire_post_1("Varka Silenos Stronghold","Лагерь Фавнов Варка","瓦尔卡·塞勒诺斯要塞","E1E338", Fraction.FIRE, new Location(120760, -57928, -2112), new Location(119656, -58088, -2072), new Location(121992, -58264, -2088), new Location(120552, -59384, -1992)),
	alab_fire_post_1("Archaic Laboratory","Древняя Лаборатория","古代实验室","E1E338", Fraction.FIRE, new Location(87463, -109976, -3328), new Location(87560, -110840, -3296), new Location(86472, -110744, -3112), new Location(86568, -109336, -3328)),
	bs_mid_fire_post_1("Blazing Swamp","Раскаленные Топи","炽热沼泽","E1E338", Fraction.FIRE, new Location(145656, -21560, -3120), new Location(146376, -22376, -3040), new Location(144648, -21640, -3120), new Location(145832, -20520, -3080)),
	//high location fire
	elven_fire_high("Elven Village (High)","Эльфийская деревня (Высокий уровень)","精灵村（高）","FA3E3E", Fraction.FIRE, new Location(41672, 84184, -3536), new Location(41847, 83064, -3536), new Location(42776, 84456, -3568), new Location(40888, 83272, -3520)),
	oren_fire_high("Town of Oren (High)","Орен (Высокий уровень)","奥伦镇（高）","FA3E3E", Fraction.FIRE, new Location(71912, 81240, -3672), new Location(71992, 80360, -3632), new Location(70680, 80776, -3680), new Location(71416, 82120, -3616)),
	aden_high_fire_post_1("Aden Castle","Замок Аден","亚丁城堡","FA3E3E", Fraction.FIRE, new Location(147464, 2392, -464), new Location(147880, 1512, -464), new Location(147063, 1511, -464), new Location(147448, 1304, -144)),
	forest_fire_post_1("Forest of The Dead","Лес Неупокоенных","死者森林","FA3E3E",Fraction.FIRE, new Location(52328, -56888, -3344), new Location(52904, -57272, -3432), new Location(51720, -57480, -3224), new Location(52584, -58008, -3144)),
	dino_fire_post_1("Dino Island","Первобытный Остров","恐龙岛","FA3E3E", Fraction.FIRE, new Location(3385, -7405, -3392), new Location(3576, -6525, -3176), new Location(2546, -8339, -3392), new Location(2792, -6776, -3264)),
	gc_fire_post_1("Giant Cave","Пещера Гигантов","巨洞","FA3E3E", Fraction.FIRE, new Location(179810, 59522, -3992), new Location(179336, 59016, -3992), new Location(179176, 60184, -3992), new Location(180488, 58744, -3992)),
	roa_high_fire_post_1("Ruins of Agony","Руины Страданий","痛苦的废墟","FA3E3E",Fraction.FIRE, new Location(-53880, 108328, -3736), new Location(-54520, 107944, -3744), new Location(-53880, 107544, -3728), new Location(-54744, 108695, -3720)),
	it_fire_post_1("Imperial Tomb","Гробница Императоров","皇陵","FA3E3E", Fraction.FIRE, new Location(177784, -75448, -2728), new Location(176968, -75336, -2728), new Location(177912, -76312, -2888), new Location(178904, -75304, -2728)),
	al_fire_post_1("Antharas Lair","Логово Антараса","安塔拉斯巢穴","FA3E3E", Fraction.FIRE, new Location(144072, 121112, -3904), new Location(145000, 121624, -3912), new Location(144360, 119576, -3912), new Location(143016, 121271, -3912)),
	sol_fire_post_1("Shrine of Loyalty","Усыпальница Верности","忠诚神殿","FA3E3E", Fraction.FIRE, new Location(185944, -62600, -2968), new Location(184920, -62632, -2960), new Location(185608, -63576, -2856), new Location(186408, -61320, -2960)),
	pagan_high_fire_post_1("Pagan Temple","Языческий Храм","异教寺庙","FA3E3E", Fraction.FIRE, new Location(-9880, -39816, -10944), new Location(-8776, -39816, -10912), new Location(-9592, -39096, -10912), new Location(-10312, -40536, -10912)),

	//start location fire
	of_water_post_1("Outlaw Forest","Лес Разбойников","亡命之林","69C37F", Fraction.WATER, new Location(81400, -4888, -3168), new Location(80888, -4616, -3152), new Location(80856, -5016, -3152), new Location(81144, -5496, -3088)),
	sos_water_post_1("Swamp of Screams","Болото Криков","尖叫沼泽","69C37F", Fraction.WATER, new Location(89912, -61272, -2304), new Location(90712, -60840, -2304), new Location(90232, -62264, -2304), new Location(89048, -62040, -2184)),
	hs_water_post_1("Hot Springs","Горячие Источники","温泉）","69C37F", Fraction.WATER, new Location(154440, -108072, -2696), new Location(155096, -107464, -2632), new Location(153992, -107496, -2736), new Location(154583, -108616, -2688)),
	ti_water_post_1("Talking Island","Говорящий Остров","会说话的岛","69C37F", Fraction.WATER, new Location(-105416, 224728, -3632), new Location(-104840, 223416, -3600), new Location(-106280, 223784, -3632), new Location(-104120, 224760, -3624)),
	//mid location fire
	elven_water_mid("Elven Village (Mid)","Эльфийская деревня (Средний уровень)","精灵村（中）","E1E338", Fraction.WATER, new Location(51448, 65352, -3536), new Location(51464, 63960, -3600), new Location(49992, 65320, -3616), new Location(51304, 66664, -3600)),
	oren_water_mid("Town of Oren (Mid)","Орен (Средний уровень)","奥伦镇（中）","E1E338", Fraction.WATER, new Location(88536, 62760, -3696), new Location(87160, 62504, -3664), new Location(88919, 61287, -3696), new Location(89992, 63176, -3584)),
	kv_water_post_1("Ketra Orc Outpost","Застава Орков Кетра","凯特拉兽人前哨站","E1E338", Fraction.WATER, new Location(145336, -70632, -4096), new Location(146168, -70825, -4080), new Location(145896, -69592, -3640), new Location(144648, -69896, -4024)),
	garden_water_post_1("Garden of Genesis","Сад Бытия","创世园","E1E338", Fraction.WATER, new Location(218986, 110005, -1304), new Location(218470, 109453, -1304), new Location(219515, 110406, -1304), new Location(218552, 110376, -1336)),
	ac_water_post_1("Abandoned Camp","Заброшенный Лагерь","废弃营地", "E1E338",Fraction.WATER, new Location(-54248, 146408, -2880), new Location(-54856, 146968, -2872), new Location(-54952, 146216, -2880), new Location(-53000, 147432, -2752)),
	vos_water_post_1("Valley of Saints","Долина Святых","圣人谷","E1E338", Fraction.WATER, new Location(77287, -74104, -3056), new Location(77256, -74824, -3016), new Location(76296, -74280, -3136), new Location(77912, -72760, -2992)),
	varka_mid_water_post_1("Varka Silenos Stronghold","Лагерь Фавнов Варка","瓦尔卡·塞勒诺斯要塞","E1E338", Fraction.WATER, new Location(120008, -46424, -2808), new Location(120664, -47288, -2808), new Location(120312, -45480, -2824), new Location(121400, -46136, -2888)),
	alab_water_post_1("Archaic Laboratory","Древняя Лаборатория","古代实验室","E1E338", Fraction.WATER, new Location(95976, -111192, -3344), new Location(95624, -111928, -3184), new Location(96712, -112009, -3288), new Location(97208, -110328, -3200)),
	bs_mid_water_post_1("Blazing Swamp","Раскаленные Топи","炽热沼泽","E1E338", Fraction.WATER, new Location(147128, -5080, -4560), new Location(148296, -5080, -4544), new Location(146168, -4088, -4504), new Location(146264, -6008, -4528)),
	//high location fire
	elven_water_high("Elven Village (High)","Эльфийская деревня (Высокий уровень)","精灵村（高）","FA3E3E", Fraction.WATER, new Location(51416, 82504, -3328), new Location(51464, 81528, -3312), new Location(50008, 82616, -3392), new Location(51256, 83752, -3360)),
	oren_water_high("Town of Oren (High)","Орен (Высокий уровень)","奥伦镇（高）","FA3E3E", Fraction.WATER, new Location(92344, 89176, -3568), new Location(91144, 89112, -3568), new Location(92808, 87944, -3568), new Location(93512, 89944, -3552)),
	aden_high_water_post_1("Aden Castle","Замок Аден","亚丁城堡","FA3E3E", Fraction.WATER, new Location(147646, 7560, -464), new Location(148216, 7912, -464), new Location(146680, 7912, -464), new Location(146696, 8344, -208)),
	forest_water_post_1("Forest of The Dead","Лес Неупокоенных","死者森林","FA3E3E", Fraction.WATER, new Location(57032, -48408, -3040), new Location(57400, -48888, -2960), new Location(58024, -47784, -2896), new Location(56792, -47592, -2968)),
	dino_water_post_1("Dino Island","Первобытный Остров","恐龙岛", "FA3E3E",Fraction.WATER, new Location(10584, -23656, -3664), new Location(11384, -24120, -3648), new Location(10488, -25048, -3680), new Location(9688, -23848, -3744)),
	gc_water_post_1("Giant Cave","Пещера Гигантов","巨洞","FA3E3E", Fraction.WATER, new Location(186824, 56408, -4576), new Location(186360, 55496, -4568), new Location(187736, 55352, -4568), new Location(187560, 58040, -4576)),
	roa_high_water_post_1("Ruins of Agony","Руины Страданий","痛苦的废墟","FA3E3E", Fraction.WATER, new Location(-43912, 116472, -3592), new Location(-43416, 117880, -3568), new Location(-43128, 116728, -3552), new Location(-44440, 116824, -3552)),
	it_water_post_1("Imperial Tomb","Гробница Императоров","皇陵","FA3E3E", Fraction.WATER, new Location(183704, -83736, -5960), new Location(183672, -83064, -5872), new Location(184472, -83848, -5864), new Location(182856, -83832, -6064)),
	al_water_post_1("Antharas Lair","Логово Антараса","安塔拉斯巢穴","FA3E3E", Fraction.WATER, new Location(140872, 108664, -3936), new Location(140680, 108072, -3944), new Location(140328, 108904, -3944), new Location(141512, 108792, -3944)),
	sol_water_post_1("Shrine of Loyalty","Усыпальница Верности","忠诚神殿","FA3E3E", Fraction.WATER, new Location(187656, -57080, -3136), new Location(187640, -55784, -2952), new Location(186280, -56808, -3120), new Location(186616, -58168, -2840)),
	pagan_high_water_post_1("Pagan Temple","Языческий Храм","异教寺庙","FA3E3E", Fraction.WATER, new Location(-22840, -41688, -10944), new Location(-23944, -41688, -10912), new Location(-23128, -42328, -10912), new Location(-22408, -41064, -10912));

	private final Fraction fraction;
	private final List<Location> locations;
	private String name_en;
	private String name_ru;
	private String name_zh;
	
	private String color;
	
	public static final int ALIVE = 0;
	public static final int ATTACKED = 1;
	public static final int DEAD = 2;

	GveOutpost(String name_en,String name_ru,String name_zh, String color, Fraction fraction, Location... locations)
	{
		this.name_en = name_en;
		this.name_ru = name_ru;
		this.name_zh = name_zh;
		this.color = color;
		this.fraction = fraction;
		this.locations = List.of(locations);

		new OutpostCommunityBoardEntry(this).register();
	}
	
	public String getColor()
	{
		return color;
	}
	
	public String getName(Language lang)
	{
		switch(lang)
		{
			case CHINESE:
				return getNameZh();
			case ENGLISH:
				return getNameEn();
			case RUSSIAN:
				return getNameRu();
			default:
				return getNameEn();
		}
	}
	public String getNameEn()
	{
		return name_en;
	}

	public String getNameRu()
	{
		return name_ru;
	}
	public String getNameZh()
	{
		return name_zh;
	}
	
	public Fraction getFraction()
	{
		return fraction;
	}

	public List<Location> getLocations()
	{
		return locations;
	}

	public Location getMain()
	{
		return locations.isEmpty() ? null : locations.get(0);
	}

	public int getStatus()
	{
		List<Spawner> spawns = SpawnManager.getInstance().getSpawners(name());

		for(Spawner s : spawns)
		{
			HardSpawner hs = (HardSpawner) s;

			List<NpcInstance> allReSpawned = hs.getAllReSpawned();
			if(allReSpawned.stream().anyMatch(npc -> npc.getNpcId() == OutpostInstance.FIRE_FLAG || npc.getNpcId() == OutpostInstance.WATER_FLAG))
			{ return DEAD; }
			if(s.getAllSpawned().stream().filter(npc -> npc.getNpcId() == OutpostInstance.FIRE_FLAG || npc.getNpcId() == OutpostInstance.WATER_FLAG).anyMatch(Creature::isInCombat))
			{ return ATTACKED; }
		}
		return ALIVE;
	}

	public int getRespawnTime()
	{
		List<Spawner> spawns = SpawnManager.getInstance().getSpawners(name());

		for(Spawner s : spawns)
		{
			HardSpawner hs = (HardSpawner) s;

			List<NpcInstance> allReSpawned = hs.getAllReSpawned();
			if(allReSpawned.stream().anyMatch(npc -> npc.getNpcId() == OutpostInstance.FIRE_FLAG || npc.getNpcId() == OutpostInstance.WATER_FLAG))
			{ return s.getRespawnTime(); }
		}
		return 0;
	}

	public static Optional<GveOutpost> find(String id)
	{
		return Enums.getIfPresent(GveOutpost.class, id).toJavaUtil();
	}
}
