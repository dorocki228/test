package l2s.gameserver.model.snapshot;


import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SnapshotPlayer {
	private final List<Abnormal> abnormals;
	private final Location returnLoc;
	private final double currentHp;
	private final double currentMp;
	private final double currentCp;
	private final int defaultTitleColor;
	private final String defaultTitle;
	private final String defaultName;
	private final int defaultNameColor;
	private final Reflection reflection;

	public SnapshotPlayer(Player player) {
		this.currentCp = player.getCurrentCp();
		this.currentHp = player.getCurrentHp();
		this.currentMp = player.getCurrentMp();
		final Collection<Abnormal> abnormals = player.getAbnormalList().getEffects();
		this.abnormals = new ArrayList<>(abnormals.size());
		for(final Abnormal effect : abnormals) 
		{
			if(effect.isOffensive()) {
				continue;
			}
			final Abnormal e = effect.getTemplate().getEffect(effect.getEffector(), effect.getEffected(), effect.getSkill());
			if(e != null) 
			{
				e.setDuration(effect.getDuration());
				e.setTimeLeft(effect.getTimeLeft());
				this.abnormals.add(e);
			}
		}
		this.returnLoc = player.getLoc();
		this.defaultTitleColor = player.getTitleColor();
		this.defaultTitle = player.getTitle();
		this.defaultName = player.getName();
		this.defaultNameColor = player.getNameColor();
		this.reflection = player.getReflection();
	}

	public List<Abnormal> getAbnormals() {
		return abnormals;
	}

	public Location getReturnLoc() {
		return returnLoc;
	}

	public double getCurrentHp() {
		return currentHp;
	}

	public double getCurrentMp() {
		return currentMp;
	}

	public double getCurrentCp() {
		return currentCp;
	}

	public int getDefaultTitleColor() {
		return defaultTitleColor;
	}

	public String getDefaultTitle() {
		return defaultTitle;
	}

	public String getDefaultName() {
		return defaultName;
	}

	public int getDefaultNameColor() {
		return defaultNameColor;
	}

	public Reflection getReflection() {
		return reflection;
	}
}
