package l2s.gameserver.model.actor.listener;

import l2s.Phantoms.listener.StopEffectListener;
import l2s.commons.listener.Listener;
import l2s.gameserver.listener.actor.player.*;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.ArtifactInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.RequestActionUse;

import java.util.OptionalInt;

public class PlayerListenerList extends CharListenerList
{
	public PlayerListenerList(Player actor)
	{
		super(actor);
	}

	@Override
	public Player getActor()
	{
		return (Player) actor;
	}

	public void onEnter()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerEnterListener.class.isInstance(listener))
					((OnPlayerEnterListener) listener).onPlayerEnter(getActor());
	}

	public void onExit()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerExitListener.class.isInstance(listener))
					((OnPlayerExitListener) listener).onPlayerExit(getActor());
	}

	public void onTeleport(int x, int y, int z, Reflection reflection)
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnTeleportListener.class.isInstance(listener))
					((OnTeleportListener) listener).onTeleport(getActor(), x, y, z, reflection);
	}

	public void onTeleported()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnTeleportedListener.class.isInstance(listener))
					((OnTeleportedListener) listener).onTeleported(getActor());
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnTeleportedListener.class.isInstance(listener))
					((OnTeleportedListener) listener).onTeleported(getActor());
	}

	public void onPartyInvite()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyInviteListener.class.isInstance(listener))
					((OnPlayerPartyInviteListener) listener).onPartyInvite(getActor());
	}

	public void onPartyLeave()
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerPartyLeaveListener.class.isInstance(listener))
					((OnPlayerPartyLeaveListener) listener).onPartyLeave(getActor());
	}

	public void onSummonServitor(Servitor servitor)
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPlayerSummonServitorListener.class.isInstance(listener))
					((OnPlayerSummonServitorListener) listener).onSummonServitor(getActor(), servitor);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPlayerSummonServitorListener.class.isInstance(listener))
					((OnPlayerSummonServitorListener) listener).onSummonServitor(getActor(), servitor);
	}

	public void onSocialAction(RequestActionUse.Action action)
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnSocialActionListener.class.isInstance(listener))
					((OnSocialActionListener) listener).onSocialAction(getActor(), getActor().getTarget(), action);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnSocialActionListener.class.isInstance(listener))
					((OnSocialActionListener) listener).onSocialAction(getActor(), getActor().getTarget(), action);
	}

	public void onLevelChange(int oldLvl, int newLvl)
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnLevelChangeListener.class.isInstance(listener))
					((OnLevelChangeListener) listener).onLevelChange(getActor(), oldLvl, newLvl);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnLevelChangeListener.class.isInstance(listener))
					((OnLevelChangeListener) listener).onLevelChange(getActor(), oldLvl, newLvl);
	}

	public void onClassChange(ClassId oldClass, ClassId newClass, boolean onRestore)
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnClassChangeListener.class.isInstance(listener))
					((OnClassChangeListener) listener).onClassChange(getActor(), oldClass, newClass, onRestore);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnClassChangeListener.class.isInstance(listener))
					((OnClassChangeListener) listener).onClassChange(getActor(), oldClass, newClass, onRestore);
	}

	public void onActiveClass(ClassId classId, SubClass newActiveSub, boolean onRestore) {
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnActiveClassListener.class.isInstance(listener))
					((OnActiveClassListener) listener).onActiveClass(getActor(), classId, newActiveSub, onRestore);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnActiveClassListener.class.isInstance(listener))
					((OnActiveClassListener) listener).onActiveClass(getActor(), classId, newActiveSub, onRestore);
	}

	public void onPickupItem(ItemInstance item)
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnPickupItemListener.class.isInstance(listener))
					((OnPickupItemListener) listener).onPickupItem(getActor(), item);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnPickupItemListener.class.isInstance(listener))
					((OnPickupItemListener) listener).onPickupItem(getActor(), item);
	}

	public void onQuestFinish(int questId)
	{
		if(!CharListenerList.global.getListeners().isEmpty())
			for(Listener<Creature> listener : CharListenerList.global.getListeners())
				if(OnQuestFinishListener.class.isInstance(listener))
					((OnQuestFinishListener) listener).onQuestFinish(getActor(), questId);
		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnQuestFinishListener.class.isInstance(listener))
					((OnQuestFinishListener) listener).onQuestFinish(getActor(), questId);
	}

	public void onOlympiadFinishBattle(boolean winner)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnOlympiadFinishBattleListener.class.isInstance(listener))
					((OnOlympiadFinishBattleListener) listener).onOlympiadFinishBattle(getActor(), winner);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnOlympiadFinishBattleListener.class.isInstance(listener))
					((OnOlympiadFinishBattleListener) listener).onOlympiadFinishBattle(getActor(), winner);
	}

	public void onFishing(OptionalInt fish)
	{
		if(!global.getListeners().isEmpty())
			for(Listener<Creature> listener : global.getListeners())
				if(OnFishingListener.class.isInstance(listener))
					((OnFishingListener) listener).onFishing(getActor(), fish);

		if(!getListeners().isEmpty())
			for(Listener<Creature> listener : getListeners())
				if(OnFishingListener.class.isInstance(listener))
					((OnFishingListener) listener).onFishing(getActor(), fish);
	}

	public void onArtifactCapture(ArtifactInstance artifact)
	{
		if(!global.getListeners().isEmpty())
			global.getListeners().stream()
					.filter(OnArtifactCaptureListener.class::isInstance)
					.forEach(listener -> ((OnArtifactCaptureListener) listener).onArtifactCapture(getActor(), artifact));
		if(!getListeners().isEmpty())
			getListeners().stream()
					.filter(OnArtifactCaptureListener.class::isInstance)
					.forEach(listener -> ((OnArtifactCaptureListener) listener).onArtifactCapture(getActor(), artifact));
	}

	public void onFortressCapture()
	{
		if(!global.getListeners().isEmpty())
			global.getListeners().stream()
					.filter(OnFortressCaptureListener.class::isInstance)
					.forEach(listener -> ((OnFortressCaptureListener) listener).onFortressCapture(getActor()));
		if(!getListeners().isEmpty())
			getListeners().stream()
					.filter(OnFortressCaptureListener.class::isInstance)
					.forEach(listener -> ((OnFortressCaptureListener) listener).onFortressCapture(getActor()));
	}

	public void onReflectionEnter(Reflection reflection)
	{
		if(!global.getListeners().isEmpty())
			global.getListeners().stream()
					.filter(OnPlayerReflectionListener.class::isInstance)
					.forEach(listener -> ((OnPlayerReflectionListener) listener).onPlayerEnterReflection(getActor(), reflection));
		if(!getListeners().isEmpty())
			getListeners().stream()
					.filter(OnPlayerReflectionListener.class::isInstance)
					.forEach(listener -> ((OnPlayerReflectionListener) listener).onPlayerEnterReflection(getActor(), reflection));
	}

	public void onReflectionExit(Reflection reflection)
	{
		if(!global.getListeners().isEmpty())
			global.getListeners().stream()
					.filter(OnPlayerReflectionListener.class::isInstance)
					.forEach(listener -> ((OnPlayerReflectionListener) listener).onPlayerExitReflection(getActor(), reflection));
		if(!getListeners().isEmpty())
			getListeners().stream()
					.filter(OnPlayerReflectionListener.class::isInstance)
					.forEach(listener -> ((OnPlayerReflectionListener) listener).onPlayerExitReflection(getActor(), reflection));
	}

	public void onEnchant(ItemInstance item)
	{
		if(!global.getListeners().isEmpty())
			global.getListeners().stream()
					.filter(OnPlayerEnchantListener.class::isInstance)
					.forEach(listener -> ((OnPlayerEnchantListener) listener).onEnchant(getActor(), item));
		if(!getListeners().isEmpty())
			getListeners().stream()
					.filter(OnPlayerEnchantListener.class::isInstance)
					.forEach(listener -> ((OnPlayerEnchantListener) listener).onEnchant(getActor(), item));
	}

	public void stopEffect(int skill)
	{
		if (!global.getListeners().isEmpty())
		{
			global.getListeners().stream().filter(StopEffectListener.class::isInstance).forEach(listener->
			{
				((StopEffectListener) listener).stopEffect(getActor(), skill);
			});
		}
		
		if (!getListeners().isEmpty())
		{
			getListeners().stream().filter(StopEffectListener.class::isInstance).forEach(listener->
			{
				((StopEffectListener) listener).stopEffect(getActor(),skill);
			});
		}
	}
}
