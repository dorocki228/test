package events.impl.ctf;

import events.CaptureTeamFlagEvent;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.objects.SpawnableObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VISTALL
 * @date 20:38/03.04.2012
 */
public class CtfFlagObject implements SpawnableObject, FlagItemAttachment
{
	private static final Logger _log = LoggerFactory.getLogger(CtfFlagObject.class);
	private ItemInstance _item;
	private Location _location;

	private Event _event;
	private TeamType _teamType;

    public CtfFlagObject(Location location, TeamType teamType)
    {
        _location = location;
        _teamType = teamType;
    }

    @Override
    public void spawnObject(Event event)
    {
        if(_item != null)
        {
            _log.info("CtfFlagObject: can't spawn twice: " + event);
            return;
        }
        _item = ItemFunctions.createItem(9819);
        _item.setAttachment(this);
        _item.dropMe(null, _location);
        _item.setReflection(event.getReflection());
        _item.setDropTime(0);

        _event = event;
    }

    @Override
    public void despawnObject(Event event)
    {
        if(_item == null)
            return;

        Player owner = GameObjectsStorage.getPlayer(_item.getOwnerId());
        if(owner != null)
        {
            owner.getInventory().destroyItem(_item);
            owner.sendDisarmMessage(_item);
        }

        _item.setAttachment(null);
        _item.setJdbcState(JdbcEntityState.UPDATED);
        _item.delete();

        _item.deleteMe();
        _item = null;

        _event = null;
    }

    @Override
    public void respawnObject(Event event)
    {

    }

    @Override
    public void refreshObject(Event event)
    {

    }

    @Override
    public void onLogout(Player player)
    {
        onDeath(player, null);
    }

    @Override
    public void onDeath(Player owner, Creature killer)
    {
        owner.getInventory().removeItem(_item);

        _item.setLocation(ItemInstance.ItemLocation.VOID);
        _item.setJdbcState(JdbcEntityState.UPDATED);
        _item.update();

        owner.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_DROPPED_S1).addItemName(_item.getItemId()));

        _item.dropMe(null, owner.getLoc());
        _item.setReflection(owner.getReflection());
        _item.setDropTime(0);
    }

	@Override
	public void onLeaveSiegeZone(Player p0)
	{
	}

    @Override
    public boolean canPickUp(Player player)
    {
        if(player.getActiveWeaponFlagAttachment() != null || player.isMounted())
            return false;
        CaptureTeamFlagEvent event = player.getEvent(CaptureTeamFlagEvent.class);
        if(event != _event)
            return false;
        if(player.getTeam() == TeamType.NONE || player.getTeam() == _teamType)
            return false;
        return true;
    }

    @Override
    public void pickUp(Player player)
    {
        player.getInventory().equipItem(_item);

        CaptureTeamFlagEvent event = player.getEvent(CaptureTeamFlagEvent.class);
		event.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_ACQUIRED_THE_FLAG).addName(player), _teamType.revert());
    }

    @Override
    public boolean canAttack(Player player)
    {
        player.sendPacket(SystemMsg.THAT_WEAPON_CANNOT_PERFORM_ANY_ATTACKS);
        return false;
    }

    @Override
	public boolean canCast(Player player, Skill skill)
    {
		SkillEntry[] skills = player.getActiveWeaponTemplate().getAttachedSkills();
        if(!ArrayUtils.contains(skills, skill))
        {
            player.sendPacket(SystemMsg.THAT_WEAPON_CANNOT_USE_ANY_OTHER_SKILL_EXCEPT_THE_WEAPONS_SKILL);
            return false;
        }
        else
            return true;
    }

    @Override
    public void setItem(ItemInstance item)
    {
        // ignored
    }

    public Location getLocation()
    {
        GameObject owner = getOwner();
        if(owner != null)
            return owner.getLoc();
        else if(_item != null)
            return _item.getLoc();
        else
            return _location;
    }

    public GameObject getOwner()
    {
        return _item == null ? null : GameObjectsStorage.getPlayer(_item.getOwnerId());
    }

    public Event getEvent()
    {
        return _event;
    }

    public TeamType getTeamType()
    {
        return _teamType;
    }
}
