package  l2s.Phantoms.Utils;


import  l2s.commons.util.Rnd;

public class ClassesDictionary
{
	private static final String[] allClasses =
	{"Duelist", // 0
			"Dreadnought","Phoenix Knight","Hell Knight","Sagittarius","Adventurer","Archmage","Soultaker","Arcana Lord","Cardinal","Hierophant", // 10
			"Eva Templar","Sword Muse","Wind Rider","Moonlight Sentinel","Mystic Muse","Elemental Master","Eva Saint","Shillien Templar","Spectral Dancer","Ghost Hunter", // 20
			"Ghost Sentinel","Storm Screamer","Spectral Master","Shillien Saint","Titan","Grand Khauatari","Dominator","Doomcryer","Fortune Seeker","Maestro", // 30
			"Doombringer","FemaleSoulHound","MaleSoulHound","Judicator","Trickster"
	
	};
	
	public static String getNameById(int id)
	{
		if (!containClass(id))
			return "unnamed";
		
		if (id <= 118)
			return allClasses[id-88];
		
		if (id == 132 || id == 133)
			return allClasses[32];
		
		if (id >= 148)
			return allClasses[id-113];
		
		return allClasses[id-100];
	}
	
	public static int Duelist = 88;
	public static int Dreadnought = 89;
	public static int PhoenixKnight = 90;
	public static int HellKnight = 91;
	public static int Sagittarius = 92;
	public static int Adventurer = 93;
	public static int Archmage = 94;
	public static int Soultaker = 95;
	public static int ArcanaLord = 96;
	public static int Cardinal = 97;
	public static int Hierophant = 98;
	public static int EvaTemplar = 99;
	public static int SwordMuse = 100;
	public static int WindRider = 101;
	public static int MoonlightSentinel = 102;
	public static int MysticMuse = 103;
	public static int ElementalMaster = 104;
	public static int EvaSaint = 105;
	public static int ShillienTemplar = 106;
	public static int SpectralDancer = 107;
	public static int GhostHunter = 108;
	public static int GhostSentinel = 109;
	public static int StormScreamer = 110;
	public static int SpectralMaster = 111;
	public static int ShillienSaint = 112;
	public static int Titan = 113;
	public static int GrandKhauatari = 114;
	public static int Dominator = 115;
	public static int Doomcryer = 116;
	public static int FortuneSeeker = 117;
	public static int Maestro = 118;
	public static int Trickster = 134;
	public static int Doombringer = 131;
	public static int Judicator = 136;
	public static int FemaleSoulHound = 133;
	public static int MaleSoulHound = 132;
	
	public static int[] classList =
	{88, // Duelist
			89, // Dreadnought
			90, // Phoenix Knight
			91, // Hell Knight
			92, // Sagittarius
			93, // Adventurer
			94, // Archmage
			95, // Soultaker
			96, // Arcana Lord
			97, // Cardinal
			98, // Hierophant
			99, // Eva Templar
			100, // Sword Muse
			101, // Wind Rider
			102, // Moonlight Sentinel
			103, // Mystic Muse
			104, // Elemental Master
			105, // Eva Saint
			106, // Shillien Templar
			107, // Spectral Dancer
			108, // Ghost Hunter
			109, // Ghost Sentinel
			110, // Storm Screamer
			111, // Spectral Master
			112, // Shillien Saint
			113, // Titan
			114, // Grand Khauatari
			115, // Dominator
			116, // Doomcryer
			117, // Fortune Seeker
			118, // Maestro
			131, // Doombringer
			132, // Soulhound
			133, // FSoulhound
			134, // Trickster
			136 // Judicator
	};
	
	public static int[] classArcherList =
	{92, // Sagittarius
			102, // Moonlight Sentinel
			109 // Ghost Sentinel
	};
	
	public static int[] classBattleMageList =
	{94, // Archmage
			95, // Soultaker
			103, // Mystic Muse
			110 // Storm Screamer
	};
	
	public static int[] classSummonerMageList =
	{96, // Arcana Lord
			104, // Elemental Master
			111 // Spectral Master
	};
	
	public static int[] classHealerMageList =
	{97 // Cardinal
	};
	
	public static int[] classSupportList =
	{98, // Hierophant
			105, // Eva Saint
			112, // Shillien Saint
			115, // Dominator
			116 // Doomcryer
	};
	
	private static boolean containClass(int id)
	{
		for(int i : classList)
		{
			if (i == id)
			{
				return true;
			}
		}
		return false;
	}
	
	public static int getRandomClass()
	{
		return classList[Rnd.get(classList.length)];
	}
	
	public static boolean isSummoner(int id)
	{
		return id == ArcanaLord || id == SpectralMaster || id == ElementalMaster;
	}
	
	public static boolean isMageSupport(int id)
	{
		return id == Hierophant || id == Dominator || id == Doomcryer || id == EvaSaint || id == ShillienSaint;
	}
	
	public static boolean isMeleeSupport(int id)
	{
		return id == SwordMuse || id == SpectralDancer;
	}
	
	public static boolean isHealer(int id)
	{
		return id == Cardinal;
	}
	
	public static boolean isMage(int id)
	{
		return id == Archmage || id == Soultaker || id == MysticMuse || id == StormScreamer;
	}
	
	public static boolean isTank(int id)
	{
		return id == PhoenixKnight || id == HellKnight || id == EvaTemplar || id == ShillienTemplar;
	}
	
	public static boolean isDagger(int id)
	{
		return id == Adventurer || id == WindRider || id == GhostHunter;
	}
	
	public static boolean isArcher(int id)
	{
		return id == Sagittarius || id == MoonlightSentinel || id == GhostSentinel || id == Trickster;
	}
	
	public static boolean isWarrior(int id)
	{
		return id == Duelist || id == Dreadnought || id == Titan || id == GrandKhauatari || id == FortuneSeeker || id == Maestro || id == Doombringer || id == FemaleSoulHound || id == MaleSoulHound;
		
	}
}
