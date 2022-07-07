package l2s.gameserver.handler.admincommands.impl;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.CharacterVariable;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.HtmlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static final Logger _log;

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanGmEdit)
			return false;
		switch(command)
		{
			case admin_changelvl:
			{
				if(wordList.length == 2)
				{
					int lvl = Integer.parseInt(wordList[1]);
					if(activeChar.getTarget().isPlayer())
						((Player) activeChar.getTarget()).setAccessLevel(lvl);
					break;
				}
				if(wordList.length == 3)
				{
					int lvl = Integer.parseInt(wordList[2]);
					Player player = GameObjectsStorage.getPlayer(wordList[1]);
					if(player != null)
						player.setAccessLevel(lvl);
					break;
				}
				break;
			}
			case admin_remove_access:
			{
				if(activeChar.getTarget().isPlayer())
					((Player) activeChar.getTarget()).setPlayerAccess(null);
				break;
			}
			case admin_moders:
			{
				showModersPannel(activeChar);
				break;
			}
			case admin_moders_add:
			{
				if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
				{
					activeChar.sendMessage("Incorrect target. Please select a player.");
					showModersPannel(activeChar);
					return false;
				}
				Player modAdd = activeChar.getTarget().getPlayer();
				if(Config.gmlist.containsKey(modAdd.getObjectId()))
				{
					activeChar.sendMessage("Error: Moderator " + modAdd.getName() + " already in server access list.");
					showModersPannel(activeChar);
					return false;
				}
				String newFName = "m" + modAdd.getObjectId() + ".xml";
				Path source = Paths.get("config/GMAccess.d/template/moderator.xml");
				Path target = Paths.get("config/GMAccess.d/" + newFName);
				try
				{
					Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
				}
				catch(IOException e)
				{
					e.printStackTrace();
					activeChar.sendMessage("Error: Failed to copy access-file.");
					showModersPannel(activeChar);
					return false;
				}
				try
				{
					BufferedReader in = new BufferedReader(new FileReader("config/GMAccess.d/" + newFName, StandardCharsets.UTF_8));
					String str;
                    String res = "";
                    while((str = in.readLine()) != null)
						res = res + str + "\n";
					in.close();
					res = res.replaceFirst("ObjIdPlayer", String.valueOf(modAdd.getObjectId()));
					Files.write(target, res.getBytes(StandardCharsets.UTF_8));
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Error: Failed to modify object ID in access-file.");
					File fDel = new File("config/GMAccess.d/" + newFName);
					if(fDel.exists())
						fDel.delete();
					showModersPannel(activeChar);
					return false;
				}
				File af = new File("config/GMAccess.d/" + newFName);
				if(!af.exists())
				{
					activeChar.sendMessage("Error: Failed to read access-file for " + modAdd.getName());
					showModersPannel(activeChar);
					return false;
				}
				Config.loadGMAccess(af);
				modAdd.setPlayerAccess(Config.gmlist.get(modAdd.getObjectId()));
				activeChar.sendMessage("Moderator " + modAdd.getName() + " added.");
				showModersPannel(activeChar);
				break;
			}
			case admin_moders_del:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Please specify moderator object ID to delete moderator.");
					showModersPannel(activeChar);
					return false;
				}
				int oid = Integer.parseInt(wordList[1]);
				if(!Config.gmlist.containsKey(oid))
				{
					activeChar.sendMessage("Error: Moderator with object ID " + oid + " not found in server access lits.");
					showModersPannel(activeChar);
					return false;
				}
				Config.gmlist.remove(oid);
				Player modDel = GameObjectsStorage.getPlayer(oid);
				if(modDel != null)
					modDel.setPlayerAccess(null);
				String fname = "m" + oid + ".xml";
				File f = new File("config/GMAccess.d/" + fname);
				if(!f.exists() || !f.isFile() || !f.delete())
				{
					activeChar.sendMessage("Error: Can't delete access-file: " + fname);
					showModersPannel(activeChar);
					return false;
				}
				if(modDel != null)
					activeChar.sendMessage("Moderator " + modDel.getName() + " deleted.");
				else
					activeChar.sendMessage("Moderator with object ID " + oid + " deleted.");
				showModersPannel(activeChar);
				break;
			}
			case admin_penalty:
			{
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //penalty charName [count] [reason]");
					return false;
				}
				int count = 1;
				if(wordList.length > 2)
					count = Integer.parseInt(wordList[2]);
				String reason = "\u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430";
				if(wordList.length > 3)
					reason = wordList[3];
				int oId = 0;
				Player player2 = GameObjectsStorage.getPlayer(wordList[1]);
				if(player2 != null && player2.getPlayerAccess().CanBanChat)
				{
					oId = player2.getObjectId();
					int oldPenaltyCount = 0;
					String oldPenalty = player2.getVar("penaltyChatCount");
					if(oldPenalty != null)
						oldPenaltyCount = Integer.parseInt(oldPenalty);
					player2.setVar("penaltyChatCount", String.valueOf(oldPenaltyCount + count), -1L);
				}
				else
				{
					oId = mysql.simple_get_int("obj_Id", "characters", "`char_name`='" + wordList[1] + "'");
					if(oId > 0)
					{
						String val = CharacterVariablesDAO.getInstance().getVarFromPlayer(oId, "penaltyChatCount");
						int oldCount = 0;
						if(val != null && !val.isEmpty())
							oldCount = Integer.parseInt(val);
						CharacterVariablesDAO.getInstance().insert(oId, new CharacterVariable("penaltyChatCount", String.valueOf(oldCount + count), -1L));
					}
				}
				if(oId <= 0)
					break;
				if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
				{
					Announcements.announceToAll(activeChar + " \u043e\u0448\u0442\u0440\u0430\u0444\u043e\u0432\u0430\u043b \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u0430 " + wordList[1] + " \u043d\u0430 " + count + ", \u043f\u0440\u0438\u0447\u0438\u043d\u0430: " + reason + ".");
					break;
				}
				Announcements.shout(activeChar, activeChar + " \u043e\u0448\u0442\u0440\u0430\u0444\u043e\u0432\u0430\u043b \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u0430 " + wordList[1] + " \u043d\u0430 " + count + ", \u043f\u0440\u0438\u0447\u0438\u043d\u0430: " + reason + ".", ChatType.CRITICAL_ANNOUNCE);
				break;
			}
		}
		return true;
	}

	private static void showModersPannel(Player activeChar)
	{
		HtmlMessage reply = new HtmlMessage(5);
		String html = "Moderators managment panel.<br>";
		File dir = new File("config/GMAccess.d/");
		if(!dir.exists() || !dir.isDirectory())
		{
			html += "Error: Can't open permissions folder.";
			reply.setHtml(html);
			activeChar.sendPacket(reply);
			return;
		}
		html += "<p align=right>";
		html += "<button width=120 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_moders_add\" value=\"Add modrator\">";
		html += "</p><br>";
		html += "<center><font color=LEVEL>Moderators:</font></center>";
		html += "<table width=285>";
		for(File f : dir.listFiles())
			if(!f.isDirectory() && f.getName().startsWith("m"))
				if(f.getName().endsWith(".xml"))
				{
					int oid = Integer.parseInt(f.getName().substring(1, 10));
					String pName = getPlayerNameByObjId(oid);
					boolean on = false;
					if(pName == null || pName.isEmpty())
						pName = String.valueOf(oid);
					else
						on = GameObjectsStorage.getPlayer(pName) != null;
					html += "<tr>";
					html = html + "<td width=140>" + pName;
					html += on ? " <font color=\"33CC66\">(on)</font>" : "";
					html += "</td>";
					html = html + "<td width=45><button width=50 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_moders_log " + oid + "\" value=\"Logs\"></td>";
					html = html + "<td width=45><button width=20 height=20 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\" action=\"bypass -h admin_moders_del " + oid + "\" value=\"X\"></td>";
					html += "</tr>";
				}
		html += "</table>";
		reply.setHtml(html);
		activeChar.sendPacket(reply);
	}

	private static String getPlayerNameByObjId(int oid)
	{
		String pName = null;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `char_name` FROM `characters` WHERE `obj_Id`=\"" + oid + "\" LIMIT 1");
			rset = statement.executeQuery();
			if(rset.next())
				pName = rset.getString(1);
		}
		catch(Exception e)
		{
			_log.warn("SQL Error: " + e);
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return pName;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	static
	{
		_log = LoggerFactory.getLogger(AdminChangeAccessLevel.class);
	}

	private enum Commands
	{
		admin_changelvl,
		admin_remove_access,
		admin_moders,
		admin_moders_add,
		admin_moders_del,
		admin_penalty
	}
}
