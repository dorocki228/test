package  l2s.Phantoms.objects;

import java.util.List;
import java.util.stream.Collectors;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import  l2s.Phantoms.ai.tasks.party.CastPartyRecallTask;
import  l2s.Phantoms.enums.PartyState;
import  l2s.Phantoms.enums.PhantomType;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.geodata.GeoEngine;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Skill;
import  l2s.gameserver.templates.StatsSet;
import  l2s.gameserver.data.xml.holder.SkillHolder;

public class PhantomPartyObject extends PhantomDefaultPartyAI
{
	public PhantomPartyObject(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void doBattleAction()
	{
		try
		{
			if (getPartyLeader().getPhantomType() == PhantomType.PHANTOM_BOT_HUNTER && !getSubTask() && !getPartyLeader().isInPeaceZone() && getPartyState() == PartyState.mission_to_kill)
			{
				List<Player> players = getPartyLeader().getAroundPlayers(2500, 500).stream().filter(d -> d != null && !d.isDead() &&  (d.getPvpFlag() != 0 && (getPartyLeader().getClan() == null ? true : getPartyLeader().getClan() != d.getPlayer().getClan()) // не бить свой клан
																																		&& (getPartyLeader().getAlliance() == null ? true : getPartyLeader().getAlliance() != d.getPlayer().getAlliance()) // не бить свой али
																																		&& (getPartyLeader().getParty() == null ? true : getPartyLeader().getParty() != d.getPlayer().getParty()))).collect(Collectors.toList());
				if (players.size() == 0 && !getPRTask())
				{
					initPRTask(new CastPartyRecallTask(getPartyId()), Rnd.get(20, 60) * 1000);
					return;
				}
			}
			// пытаемся воскресить сопартийцев
			Player deadman = getDeadPartyMember();
			if (deadman != null)
			{
				Player resurrecter = getAnyResurrectMan();
				if (resurrecter != null && !resurrecter.isCastingNow() && !resurrecter.phantom_params.getClassAI().getResurrectSkills().getAllSkills().isEmpty())
				{
					resurrecter.abortAttack(true, false);
					resurrecter.abortCast(true, false);
					resurrecter.phantom_params.setResTarget(deadman);
					resurrecter.phantom_params.getPhantomAI().castResurrectSkill(deadman);
				}
				else
				{
					Player res_player = getMemberWithMaxHp();
					if (res_player != null)
					{
						Player dead_healer = getAnyDeathHealer();
						if (dead_healer != null && !res_player.isCastingNow())
						{
							res_player.abortAttack(true, false);
							res_player.abortCast(true, false);
							res_player.phantom_params.setResTarget(dead_healer);
							res_player.setTarget(dead_healer);
							res_player.getAI().Cast(SkillHolder.getInstance().getSkill(2049, 1), dead_healer);
						}
					}
				}
			}
			// если умерли почти все
			if (getDeadPartyMembersCount() >= _all_members.size() / 2)
			{
				Player resurrecter = getAnyResurrectMan();
				if (resurrecter != null && resurrecter.getSkillById(1254) != null && !resurrecter.isCastingNow())
				{
					Skill skill = resurrecter.getSkillById(1254);
					if (!resurrecter.isSkillDisabled(skill))
						resurrecter.getAI().Cast(skill, resurrecter, false, true);
				}
			}
			/*
			 * if (_is_moving != 1 && Rnd.chance(getRegroupToLeaderChance())) { _is_moving = 1; regroup(); }
			 */
			/*
			 * if(_is_moving != 1 && Rnd.chance(getRandomMoveChance())) { _is_moving = 1; Location loc = getRandomMove(_partyLeader, 200, 400); moveToLocation(loc); }
			 */
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void doPeaceAction()
	{
		if (getPartyLeader().getPhantomType() == PhantomType.PHANTOM_BOT_HUNTER)
		{
			abortSubTask();
		}
	}
	
	@Override
	public void onPartyMemberDebuffed(Player member, Skill skill)
	{
		Player healer = getAnyHealer();
		if (healer == null) // не прогрузился, или нет вообще
			return;
		Skill sk = healer.getSkillById(1409); // cleanse
		if (sk != null)
		{
			healer.phantom_params.getPhantomAI().abortAITask();
			healer.abortCast(true, false);
			healer.getAI().Cast(sk, member, false, true);
			healer.phantom_params.getPhantomAI().startAITask(500);
		}
	}
	
	@Override
	public void onPartyMemberAttacked(Player member, Creature attacker)
	{
		PhantomDefaultAI ai = member.phantom_params.getPhantomAI();
		if (ai == null)
			return;
		if (!GeoEngine.canSeeTarget(member, attacker, false))
			return;
		
		if (attacker.isSiegeGuard()) 
		{
			if (getCurrentPointCoordinate()!=null && getCurrentPointCoordinate().getRnd() <= 20)
				return;
			if( attacker.getDistance3D(member) > 200 && !GeoEngine.canMoveToCoord(member.getX(), member.getY(), member.getZ(), attacker.getX(), attacker.getY(), attacker.getZ(), member.getGeoIndex()))
				return;
		} 

		if (getPartyState() != PartyState.battle && member.getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER)
			changePartyState(PartyState.battle);
		
		// хиллер убегает к защитникам
		if (ai.isHealer() || ai.isSupport())
		{
			Player defender = member.phantom_params.getPhantomPartyAI().getAnyTank();
			if (defender == null)
				defender = member.phantom_params.getPhantomPartyAI().getAnyMelee();
			if (defender == null)
				defender = member.phantom_params.getPhantomPartyAI().getAnyMember();
			if (defender != null)
			{
				if (member.phantom_params.getResTarget() == null)
					member.moveToLocation(defender.getLoc(), Rnd.get(60, 160), true);
			}
			takeMainAssist(attacker);
		}
		// выбрать хиллера
		Player healer = null;
		if (ai.isHealer())
			healer = member;
		else
			healer = getAnyHealer();
		if (healer != null)
		{
			// назначаем хилеру нового мембера для хила
			healer.phantom_params.setLockedHealerTarget(member);
		}
		// берем ассист, если не взят
		if (!isMainAssistTaken())
			takeMainAssist(attacker);
		// даем вторичный ассист для сапорта (целью может быть только другой сапорт)
		setSubAssist(attacker);
		// если у мембера мало НР и он далеко от хиллеров - отбегаем на подхил
		if (member != healer && member.getCurrentHpPercents() < 50 && member.getDistance(healer) > 600)
		{
			if (member.phantom_params.getResTarget() == null)
				member.moveToLocation(healer.getLoc(), 150, true);
		}
	}
	
	@Override
	public void getAndSetTarget()
	{
		Player asister = this.getPartyAssister();
		// поменяем асистера если попало на хила\сапорта
		if (asister == null || asister.phantom_params.getPhantomAI().isHealer() 
																																|| asister.phantom_params.getPhantomAI().isDisabler() || asister.phantom_params.getPhantomAI().isSupport() 
																																)
			selectAsister();
		if (asister != null && !asister.isDead())
		{
			// даем вторичный ассист для сапорта (целью может быть только другой сапорт)
			setSubAssist(getPartyTarget());
			for (Player member : getAllMembers())
			{
				if (getPartyTarget() != member.phantom_params.getLockedTarget())
					member.phantom_params.delayAssistChange();
			}
		}
		else // если асистер умер - сменим асистера
			selectAsister();
	}
	
	public boolean selectAsister()
	{
		Player asister = this.getAnyTank();
		if (asister == null || asister.isDead())
			asister = this.getAnyMelee();
		if (asister == null || asister.isDead())
			asister = this.getAnyNuker();
		if (asister != null)
		{
			this.setPartyAssister(asister);
			return true;
		}
		return false;
	}
	
	public int getRadiusTargetSearch()
	{
		// TODO Auto-generated method stub
		return 900;
	}
}
