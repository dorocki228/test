package npc.model;

import java.util.Collection;
import java.util.Set;
import java.util.StringTokenizer;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.actor.instances.player.SubClassInfo;
import l2s.gameserver.model.actor.instances.player.SubClassList;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;

public final class SubClassManagerInstance extends NpcInstance
{

	public SubClassManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player))
			return;

		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();

		if("subclass".equals(cmd))
		{
			if(!player.getSummons().isEmpty())
			{
				showChatWindow(player, "subclass/no_servitor.htm");
				return;
			}

			if(player.isTransformed())
			{
				showChatWindow(player, "subclass/no_transform.htm");
				return;
			}

			if((player.getWeightPenalty() >= 3) || ((player.getInventoryLimit() * 0.8) < player.getInventory().getSize()))
			{
				showChatWindow(player, "subclass/no_weight.htm");
				return;
			}

			if(player.getLevel() < 40)
			{
				showChatWindow(player, "subclass/no_level.htm");
				return;
			}

			String cmd2 = st.nextToken();

			if("add".equals(cmd2))
			{
				if(player.getSubClassList().size() >= SubClassList.MAX_SUB_COUNT)
				{
					showChatWindow(player, "subclass/add_no_limit.htm");
					return;
				}

				if(!st.hasMoreTokens())
				{
					StringBuilder availSubList = new StringBuilder();
					Set<ClassId> availSubClasses = SubClassInfo.getAvailableSubClasses(player);

					for(ClassId subClsId : availSubClasses)
					{
						availSubList.append("<a action=\"bypass -h npc_%objectId%_subclass_add_" + subClsId.getId() + "\">" + HtmlUtils.htmlClassName(subClsId.getId()) + "</a><br>");
					}

					showChatWindow(player, "subclass/add_list.htm", "<?ADD_SUB_LIST?>", availSubList.toString());
					return;
				}

				int addSubClassId = Integer.parseInt(st.nextToken());

				if(!st.hasMoreTokens())
				{
					String addSubConfirm = "<a action=\"bypass -h npc_%objectId%_subclass_add_" + addSubClassId + "_confirm\">" + HtmlUtils.htmlClassName(addSubClassId) + "</a>";
					showChatWindow(player, "subclass/add_confirm.htm", "<?ADD_SUB_CONFIRM?>", addSubConfirm);
					return;
				}

				String cmd3 = st.nextToken();

				if("confirm".equals(cmd3))
				{
					if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(player))
					{
						SystemMsg msg = SystemMsg.C1_DOES_NOT_MEET_THE_PARTICIPATION_REQUIREMENTS_SUBCLASS_CHARACTER_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD;
						player.sendPacket(new SystemMessage(msg).addName(player));
						return;
					}

					if(player.addSubClass(addSubClassId, true, 0, 0))
					{
						player.rewardSkills(false, true, true);
						player.sendSkillList();
						//						player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
						showChatWindow(player, "subclass/add_success.htm");
						return;
					}

					showChatWindow(player, "subclass/add_error.htm");
					return;
				}
			}
			else if("change".equals(cmd2))
			{
				if(!player.getSubClassList().haveSubClasses())
				{
					showChatWindow(player, "subclass/change_no_subs.htm");
					return;
				}

				if(Config.ENABLE_OLYMPIAD && Olympiad.isRegisteredInComp(player))
				{
					player.sendMessage("You cannot change subclass when you registered in Olympiad.");
					return;
				}

				if(!st.hasMoreTokens())
				{
					StringBuilder mySubList = new StringBuilder();
					Collection<SubClass> subClasses = player.getSubClassList().values();

					for(SubClass sub : subClasses)
					{
						if(sub == null || sub.isActive())
						{
							continue;
						}

						int classId = sub.getClassId();
						if(sub.isBase())
							mySubList.append("<font color=LEVEL><a action=\"bypass -h npc_%objectId%_subclass_change_" + classId + "\">" + HtmlUtils.htmlClassName(classId) + "</a>(Base)</font><br>");
						else
							mySubList.append("<a action=\"bypass -h npc_%objectId%_subclass_change_" + classId + "\">" + HtmlUtils.htmlClassName(classId) + "</a><br>");

					}

					showChatWindow(player, "subclass/change_list.htm", "<?CHANGE_SUB_LIST?>", mySubList.toString());
					return;
				}

				int subId = Integer.parseInt(st.nextToken());

				SubClass sc = player.getSubClassList().getByClassId(subId);
				if(sc != null && !sc.isActive())
				{
					player.cancelReApplyTasks();
					player.setActiveSubClass(subId, false, false);
					showChatWindow(player, "subclass/change_success.htm");
				}

				return;
			}
			else if("cancel".equals(cmd2))
			{
				if(!player.getSubClassList().haveSubClasses())
				{
					showChatWindow(player, "subclass/cancel_no_subs.htm");
					return;
				}

				if(!st.hasMoreTokens())
				{
					StringBuilder mySubList = new StringBuilder();
					Collection<SubClass> subClasses = player.getSubClassList().values();

					for(SubClass sub : subClasses)
					{
						if(sub == null)
						{
							continue;
						}

						if(sub.isBase())
						{
							continue;
						}

						int classId = sub.getClassId();
						mySubList.append("<a action=\"bypass -h npc_%objectId%_subclass_cancel_" + classId + "\">" + HtmlUtils.htmlClassName(classId) + "</a><br>");
					}

					showChatWindow(player, "subclass/cancel_list.htm", "<?CANCEL_SUB_LIST?>", mySubList.toString());
					return;
				}

				int cancelClassId = Integer.parseInt(st.nextToken());

				if(!st.hasMoreTokens())
				{
					StringBuilder availSubList = new StringBuilder();
					Set<ClassId> availSubClasses = SubClassInfo.getAvailableSubClasses(player);

					for(ClassId classId : availSubClasses)
					{
						if(classId.getId() == player.getBaseClassId())
							continue;
						if(player.getActiveClassId() == classId.getId())
							continue;

						availSubList.append("<a action=\"bypass -h npc_%objectId%_subclass_cancel_" + cancelClassId + "_" + classId.getId() + "\">" + HtmlUtils.htmlClassName(classId.getId()) + "</a><br>");
					}

					showChatWindow(player, "subclass/cancel_change_list.htm", "<?CANCEL_CHANGE_SUB_LIST?>", availSubList.toString());
					return;
				}

				int newSubClassId = Integer.parseInt(st.nextToken());

				if(!st.hasMoreTokens())
				{
					String newSubConfirm = "<a action=\"bypass -h npc_%objectId%_subclass_cancel_" + cancelClassId + "_" + newSubClassId + "_confirm\">" + HtmlUtils.htmlClassName(newSubClassId) + "</a>";
					showChatWindow(player, "subclass/cancel_confirm.htm", "<?CANCEL_SUB_CONFIRM?>", newSubConfirm);
					return;
				}

				String cmd3 = st.nextToken();

				if("confirm".equals(cmd3))
				{
					if(player.modifySubClass(cancelClassId, newSubClassId, false))
					{
						player.rewardSkills(false, true, true);
						player.sendSkillList();
						//						player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
						showChatWindow(player, "subclass/add_success.htm");
						return;
					}

					showChatWindow(player, "subclass/add_error.htm");
					return;
				}
			}
			else if("CancelRequest".equals(cmd2))
			{
				showChatWindow(player, "subclass/cancelrequest.htm");
				return;
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	private void showChatWindow(Player player, String filename, Object... objects)
	{
		showChatWindow(player, filename, false, objects);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
		{
			showChatWindow(player, "subclass/subclass.htm");
			return;
		}

		super.showChatWindow(player, val, firstTalk, replace);
	}
}
