package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Bonux
 * @author Java-man
 **/
public class ElementalTeleporterInstance extends NpcInstance {
	enum Handlers {
		ignis_teleport(-2017072305) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				handleTeleport(player, npc, reply, TELEPORT_LOC1, "ignis");
			}
		},
		ignis_enter_instance(-2017072302) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				//handleEnterReflection(player, npc, reply,
				//		INSTANCE_ID1, EPIC_INSTANCE_ID1, "ignis");
			}
		},
		nebula_teleport(-2017072405) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				handleTeleport(player, npc, reply, TELEPORT_LOC2, "nebula");
			}
		},
		nebula_enter_instance(-2017072402) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				handleEnterReflection(player, npc, reply,
						INSTANCE_ID2, EPIC_INSTANCE_ID2, "nebula");
			}
		},
		procella_teleport(-2017072505) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				handleTeleport(player, npc, reply, TELEPORT_LOC3, "procella");
			}
		},
		procella_enter_instance(-2017072502) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				handleEnterReflection(player, npc, reply,
						INSTANCE_ID3, EPIC_INSTANCE_ID3, "procella");
			}
		},
		petram_teleport(-2017072605) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				handleTeleport(player, npc, reply, TELEPORT_LOC4, "petram");
			}
		},
		petram_enter_instance(-2017072602) {
			@Override
			void execute(Player player, NpcInstance npc, long reply) {
				handleEnterReflection(player, npc, reply,
						INSTANCE_ID4, EPIC_INSTANCE_ID4, "petram");
			}
		};

		private final int ask;

		Handlers(int ask) {
			this.ask = ask;
		}

		abstract void execute(Player player, NpcInstance npc, long reply);

		static Optional<Handlers> find(int ask) {
			return Arrays.stream(values()).filter(handler -> handler.ask == ask).findAny();
		}
	}

	private static final Location TELEPORT_LOC1 = new Location(180200, -111736, -5824);
	private static final int INSTANCE_ID1 = 195;
	private static final int EPIC_INSTANCE_ID1 = 195;

	private static final Location TELEPORT_LOC2 = new Location(79736, 256088, -9328);
	private static final int INSTANCE_ID2 = 196;
	private static final int EPIC_INSTANCE_ID2 = 196;

	private static final Location TELEPORT_LOC3 = new Location(186040, 192712, -3568);
	private static final int INSTANCE_ID3 = 197;
	private static final int EPIC_INSTANCE_ID3 = 197;

    private static final Location TELEPORT_LOC4 = new Location(176938, -50796, -3392);
    private static final int INSTANCE_ID4 = 198;
    private static final int EPIC_INSTANCE_ID4 = 198;

    public ElementalTeleporterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public void onMenuSelect(Player player, int ask, long reply, int state) {
		Handlers.find(ask).ifPresentOrElse(handler ->
				handler.execute(player, this, reply),
				() -> super.onMenuSelect(player, ask, reply, state));
    }

	private static void handleTeleport(Player player, NpcInstance npc, long reply,
									   Location teleportLoc, String bossName) {
		if (reply == 1) {
			player.teleToLocation(teleportLoc, ReflectionManager.MAIN);
		} else if (reply == 2) {
			npc.showChatWindow(player, "default/" + npc.getNpcId() + "-" + bossName + ".htm", false);
		}
	}

	private static void handleEnterReflection(Player player, NpcInstance npc, long reply,
											  int instanceId, int epicInstanceId, String bossName) {
		if (!player.isInParty()) {
			npc.showChatWindow(player, "default/" + npc.getNpcId()
					+ "-no_solo_" + bossName + ".htm", false);
			return;
		}

		if (reply == 1) {
			Reflection r = player.getActiveReflection();
			if (r != null) {
				if (player.canReenterInstance(instanceId))
					player.teleToLocation(r.getTeleportLoc(), r);
				return;
			}

			/*Party party = player.getParty();
			CommandChannel commandChannel = party != null ? party.getCommandChannel() : null;
			if(commandChannel == null)
			{
				showChatWindow(player, "default/" + getNpcId() + "-no_group.htm", false);
				return;
			}*/

			if (player.canEnterInstance(instanceId)) {
				ReflectionUtils.enterReflection(player, instanceId);
				return;
			}
		} else if (reply == 2) {
			player.sendMessage("Disabled.");

			/*Reflection r = player.getActiveReflection();
			if (r != null) {
				if (player.canReenterInstance(epicInstanceId))
					player.teleToLocation(r.getTeleportLoc(), r);
				return;
			}

			if (player.canEnterInstance(epicInstanceId)) {
				ReflectionUtils.enterReflection(player, epicInstanceId);
				return;
			}*/
		}
	}
}
