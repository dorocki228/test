package  l2s.Phantoms.enums;

public enum Condition
{
	/** цель проверки  - бот **/
	MIN_CP("От MIN_CP и ниже","MIN_CP:10"), 
	MIN_HP("От MIN_HP и ниже","MIN_HP:10"), 
	MIN_MP("От MIN_MP и ниже","MIN_MP:10"), 
	MAX_CP("От MAX_CP и выше","MAX_CP:10"),
	MAX_HP("От MAX_HP и выше","MAX_HP:10"), 	
	MAX_MP("От MAX_MP и выше","MAX_MP:10"), 		
	

	SUMMON_MAX_MP("от SUMMON_MAX_MP и выше ",""),
	SUMMON_MAX_HP("от SUMMON_MAX_HP и выше",""), 						
	SUMMON_MIN_HP("от SUMMON_MIN_HP и ниже",""),
	SUMMON_MIN_MP("от SUMMON_MIN_MP и ниже",""), 	
	
	MIN_PARTY_HP("от MIN_PARTY_HP и ниже","MIN_PARTY_HP:(60,3)  (минимум 3 сопартийца с хп ниже 60)"), 	
	USE_IN_PARTY("использовать в только в пати","USE_IN_PARTY"),
	NOT_USE_IN_PARTY("не использовать в пати","NOT_USE_IN_PARTY"),
	CANCEL_BUFF_BY_ID("отменить баф, использовать в ситуациях когда надо снять уд или любой другой баф","CANCEL_BUFF_BY_ID:10"),
	CHARGING("зарядка гладов тиров заряжает до максимально возможного значения в умении","CHARGING"), 																							
	SELF_SKILL_EFFECT("проверка эффектов на себе, в случае обнаружения - каст умения","SELF_SKILL_EFFECT:(5739,5744,1411)"),
	SELF_NOT_SKILL_EFFECT("проверка эффектов на себе, в случае обнаружения пропуск умения","SELF_NOT_SKILL_EFFECT:(5739,5744,1411)"), 
	CHANCE_CAST("шанс отменить использование умения за тик аи ","CHANCE_CAST:30"), 	
	USE_OLYMPIAD("использовать на олимпе","USE_OLYMPIAD"),
	NOT_USE_OLYMPIAD("не использовать на олимпе ","NOT_USE_OLYMPIAD"),
	NO_TARGET("каст если нет таргента","NO_TARGET"),
	CONSUMED_SOULS("души камаелей","CONSUMED_SOULS"),
	CHECK_WEAPON_ATTRIBUTE("проверка атрибута оружия","CHECK_WEAPON_ATTRIBUTE:(FIRE,WIND)"),	
	CUBIC("роверка на количество кубиков",""),
	SKILL_DISABLED("проверка существует или в откате умение","SKILL_DISABLED:90"),
	IS_IN_BATTLE("персонаж в бою",""),
	NOT_IN_BATTLE("персонаж вне боя ",""), 						
	LIVE_SUMMON("",""),
	
	NEXT_ACTION("",""),
	
	/** цель проверки - таргент (моб\игрок)**/
	TARGET_MIN_CP("от TARGET_MIN_CP и ниже","TARGET_MIN_CP:10"), 			
	TARGET_MIN_HP("от TARGET_MIN_HP и ниже","TARGET_MIN_HP:10"), 					
	TARGET_MIN_MP("от TARGET_MIN_MP и ниже","TARGET_MIN_MP:10"), 	
	TARGET_MAX_CP("от TARGET_MAX_CP и выше","TARGET_MAX_CP:10"),
	TARGET_MAX_HP("от TARGET_MAX_HP и выше","TARGET_MAX_HP:10"), 	
	TARGET_MAX_MP("от TARGET_MAX_MP и выше ","TARGET_MAX_MP:10"),
	TARGET_MAX_HP_COUNT("от TARGET_MIN_HP_COUNT и ниже","от TARGET_MIN_HP_COUNT и ниже"),
	TARGET_MIN_HP_COUNT("от TARGET_MIN_HP_COUNT и ниже","от TARGET_MIN_HP_COUNT и ниже"), 	
	MASS("",""), 																		// массовый (используется когда вокруг таргента >= param целей (моб\игрок)), радиус поиска = радиусу умения 	- пример TARGET_MASS:3
	MASS_MONSTER("",""),											// массовый (используется когда вокруг таргента >= param мобов), радиус поиска = радиусу умения 		- пример MASS_MONSTER:3  (param == -1  - обратная проверка нет ли больше мобов вокруг)
	MASS_PLAYERS("",""),											// массовый (используется когда вокруг таргента >= param игроков), радиус поиска = радиусу умения 						- пример MASS_PLAYERS:3
	NPC_RACE("",""),															//	каст умения с проверкой рассы нпс (бафы гладов и тд) - пример NPC_RACE:BUG  (см. NpcRace)
	ONLY_MONSTER("",""), 										// атака только мобов\рб 																																											- пример ONLY_MONSTER:true
	ONLY_PLAYER("",""),		 									// атака только игрока 																																												- пример ONLY_PLAYER:true
	MIN_DISTANCE("",""), 										// от param и дальше 																																													- пример MIN_DISTANCE:10
	MAX_DISTANCE("",""), 										// от param и ближе 																																													- пример MAX_DISTANCE:10
	TARGET_NOT_RUNNING("",""),					// цель не бежит 
	TARGET_RUNNING("",""),									// цель бежит 
	TARGET_RUNS_AWAY("",""),							// цель убегает от нас
	TARGET_IS_COMING("",""),							// цель приближаеться 
	TARGET_SKILL_EFFECT("",""), 			// проверка эффектов на цели, в случае обнаружения - каст умения 																							- пример TARGET_SKILL_EFFECT:(5739,5744,1411)
	TARGET_NOT_SKILL_EFFECT			("проверка эффектов на цели, в случае обнаружения - пропуск умения","TARGET_NOT_SKILL_EFFECT:(5739,5744,1411)"), 						
	TARGET_MAGE("",""), 											// таргент маг 																																																- пример TARGET_MAGE:true
	TARGET_FIGHTER("",""), 								// таргент физ 																																																- пример TARGET_FIGHTER:true
	TARGET_SUMMON("",""), 									// таргент суммоны 																																														- пример TARGET_SUMMON:true
	TARGET_CLASS_ID("",""), 							// использовать только на профы 																																							- пример TARGET_CLASS_ID:(90,91,92)
	TARGET_NOT_USE_CLASS_ID("",""),// не использовать на определенные профы  																																		- пример TARGET_NOT_USE_CLASS_ID:(90,91,92)
	TARGET_SKILL_DISABLED("",""),		// проверка в откате ли умения в противника																																	 	- пример TARGET_SKILL_DISABLED:(90,91,92)
	TARGET_WEAPON_TYPE("",""),					// проверка на тип оружия противника																			- пример TARGET_WEAPON_TYPE:(Bow, Crossbow)
	NOT_SPOILED("",""),
	SPOILED("",""),
	MASS_MONSTER_SWEEPER("",""),
	MASS_MONSTER_SPOILED("",""),
	TARGET_ARMOR_TYPE("проверка на тип доспехов противника","TARGET_ARMOR_TYPE:(Bow, Crossbow)"),
	SUMMON_NPC_ID("для актионов сумонов",""),
	DEBUFF_CHANCE("проверка шанса прохождения дебафа","DEBUFF_CHANCE:20"),
	TARGET_CHECK_WEAPON_ATTRIBUTE("проверка атрибута оружия","CHECK_WEAPON_ATTRIBUTE:(FIRE,WIND)"),	
	TARGET_DIRECTION("",""),
	CANCELING_CAST("","");

	Condition(String coment, String example)
	{
		// TODO Auto-generated constructor stub
	}
}