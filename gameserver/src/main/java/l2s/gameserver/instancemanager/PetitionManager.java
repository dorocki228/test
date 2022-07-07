package l2s.gameserver.instancemanager;

import l2s.gameserver.Config;
import l2s.gameserver.handler.petition.IPetitionHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.GmListTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class PetitionManager implements IPetitionHandler
{
	private static final Logger _log;
	private static final PetitionManager _instance;
	private final AtomicInteger _nextId;
	private final Map<Integer, Petition> _pendingPetitions;
	private final Map<Integer, Petition> _completedPetitions;

	public static final PetitionManager getInstance()
	{
		return _instance;
	}

	private PetitionManager()
	{
		_nextId = new AtomicInteger();
		_pendingPetitions = new ConcurrentHashMap<>();
		_completedPetitions = new ConcurrentHashMap<>();
		_log.info("Initializing PetitionManager");
	}

	public int getNextId()
	{
		return _nextId.incrementAndGet();
	}

	public void clearCompletedPetitions()
	{
		int numPetitions = getPendingPetitionCount();
		getCompletedPetitions().clear();
		_log.info("PetitionManager: Completed petition data cleared. " + numPetitions + " petition(s) removed.");
	}

	public void clearPendingPetitions()
	{
		int numPetitions = getPendingPetitionCount();
		getPendingPetitions().clear();
		_log.info("PetitionManager: Pending petition queue cleared. " + numPetitions + " petition(s) removed.");
	}

	public boolean acceptPetition(Player respondingAdmin, int petitionId)
	{
		if(!isValidPetition(petitionId))
			return false;
		Petition currPetition = getPendingPetitions().get(petitionId);
		if(currPetition.getResponder() != null)
			return false;
		currPetition.setResponder(respondingAdmin);
		currPetition.setState(PetitionState.In_Process);
		currPetition.sendPetitionerPacket(new SystemMessage(406));
		currPetition.sendResponderPacket(new SystemMessage(389).addNumber(currPetition.getId()));
		currPetition.sendResponderPacket(new SystemMessage(394).addString(currPetition.getPetitioner().getName()));
		return true;
	}

	public boolean cancelActivePetition(Player player)
	{
		for(Petition currPetition : getPendingPetitions().values())
		{
			if(currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
				return currPetition.endPetitionConsultation(PetitionState.Petitioner_Cancel);
			if(currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return currPetition.endPetitionConsultation(PetitionState.Responder_Cancel);
		}
		return false;
	}

	public void checkPetitionMessages(Player petitioner)
	{
		if(petitioner != null)
			for(Petition currPetition : getPendingPetitions().values())
			{
				if(currPetition == null)
					continue;
				if(currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
					for(SayPacket2 logMessage : currPetition.getLogMessages())
						petitioner.sendPacket(logMessage);
			}
	}

	public boolean endActivePetition(Player player)
	{
		if(!player.isGM())
			return false;
		for(Petition currPetition : getPendingPetitions().values())
		{
			if(currPetition == null)
				continue;
			if(currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
				return currPetition.endPetitionConsultation(PetitionState.Completed);
		}
		return false;
	}

	protected Map<Integer, Petition> getCompletedPetitions()
	{
		return _completedPetitions;
	}

	protected Map<Integer, Petition> getPendingPetitions()
	{
		return _pendingPetitions;
	}

	public int getPendingPetitionCount()
	{
		return getPendingPetitions().size();
	}

	public int getPlayerTotalPetitionCount(Player player)
	{
		if(player == null)
			return 0;
		int petitionCount = 0;
		for(Petition currPetition : getPendingPetitions().values())
		{
			if(currPetition == null)
				continue;
			if(currPetition.getPetitioner() == null || currPetition.getPetitioner().getObjectId() != player.getObjectId())
				continue;
			++petitionCount;
		}
		for(Petition currPetition : getCompletedPetitions().values())
		{
			if(currPetition == null)
				continue;
			if(currPetition.getPetitioner() == null || currPetition.getPetitioner().getObjectId() != player.getObjectId())
				continue;
			++petitionCount;
		}
		return petitionCount;
	}

	public boolean isPetitionInProcess()
	{
		for(Petition currPetition : getPendingPetitions().values())
		{
			if(currPetition == null)
				continue;
			if(currPetition.getState() == PetitionState.In_Process)
				return true;
		}
		return false;
	}

	public boolean isPetitionInProcess(int petitionId)
	{
		if(!isValidPetition(petitionId))
			return false;
		Petition currPetition = getPendingPetitions().get(petitionId);
		return currPetition.getState() == PetitionState.In_Process;
	}

	public boolean isPlayerInConsultation(Player player)
	{
		if(player != null)
			for(Petition currPetition : getPendingPetitions().values())
			{
				if(currPetition == null)
					continue;
				if(currPetition.getState() != PetitionState.In_Process)
					continue;
				if(currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId() || currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
					return true;
			}
		return false;
	}

	public boolean isPetitioningAllowed()
	{
		return Config.PETITIONING_ALLOWED;
	}

	public boolean isPlayerPetitionPending(Player petitioner)
	{
		if(petitioner != null)
			for(Petition currPetition : getPendingPetitions().values())
			{
				if(currPetition == null)
					continue;
				if(currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == petitioner.getObjectId())
					return true;
			}
		return false;
	}

	private boolean isValidPetition(int petitionId)
	{
		return getPendingPetitions().containsKey(petitionId);
	}

	public boolean rejectPetition(Player respondingAdmin, int petitionId)
	{
		if(!isValidPetition(petitionId))
			return false;
		Petition currPetition = getPendingPetitions().get(petitionId);
		if(currPetition.getResponder() != null)
			return false;
		currPetition.setResponder(respondingAdmin);
		return currPetition.endPetitionConsultation(PetitionState.Responder_Reject);
	}

	public boolean sendActivePetitionMessage(Player player, String messageText)
	{
		for(Petition currPetition : getPendingPetitions().values())
		{
			if(currPetition == null)
				continue;
			if(currPetition.getPetitioner() != null && currPetition.getPetitioner().getObjectId() == player.getObjectId())
			{
				SayPacket2 cs = new SayPacket2(player.getObjectId(), ChatType.PETITION_PLAYER, player.getName(), messageText);
				currPetition.addLogMessage(cs);
				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
			if(currPetition.getResponder() != null && currPetition.getResponder().getObjectId() == player.getObjectId())
			{
				SayPacket2 cs = new SayPacket2(player.getObjectId(), ChatType.PETITION_GM, player.getName(), messageText);
				currPetition.addLogMessage(cs);
				currPetition.sendResponderPacket(cs);
				currPetition.sendPetitionerPacket(cs);
				return true;
			}
		}
		return false;
	}

	public void sendPendingPetitionList(Player activeChar)
	{
		StringBuilder htmlContent = new StringBuilder(600 + getPendingPetitionCount() * 300);
		htmlContent.append("<html><body><center><table width=270><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center>Petition Menu</center></td><td width=45><button value=\"Back\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br><table width=\"270\"><tr><td><table width=\"270\"><tr><td><button value=\"Reset\" action=\"bypass -h admin_reset_petitions\" width=\"80\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td align=right><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"80\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br></td></tr>");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if(getPendingPetitionCount() == 0)
			htmlContent.append("<tr><td>There are no currently pending petitions.</td></tr>");
		else
			htmlContent.append("<tr><td><font color=\"LEVEL\">Current Petitions:</font><br></td></tr>");
		boolean color = true;
		int petcount = 0;
		for(Petition currPetition : getPendingPetitions().values())
		{
			if(currPetition == null)
				continue;
			htmlContent.append("<tr><td width=\"270\"><table width=\"270\" cellpadding=\"2\" bgcolor=").append(color ? "131210" : "444444").append("><tr><td width=\"130\">").append(dateFormat.format(new Date(currPetition.getSubmitTime())));
			htmlContent.append("</td><td width=\"140\" align=right><font color=\"").append(currPetition.getPetitioner().isOnline() ? "00FF00" : "999999").append("\">").append(currPetition.getPetitioner().getName()).append("</font></td></tr>");
			htmlContent.append("<tr><td width=\"130\">");
			if(currPetition.getState() != PetitionState.In_Process)
				htmlContent.append("<table width=\"130\" cellpadding=\"2\"><tr><td><button value=\"View\" action=\"bypass -h admin_view_petition ").append(currPetition.getId()).append("\" width=\"50\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td><button value=\"Reject\" action=\"bypass -h admin_reject_petition ").append(currPetition.getId()).append("\" width=\"50\" height=\"21\" back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
			else
				htmlContent.append("<font color=\"").append(currPetition.getResponder().isOnline() ? "00FF00" : "999999").append("\">").append(currPetition.getResponder().getName()).append("</font>");
			htmlContent.append("</td>").append(currPetition.getTypeAsString()).append("<td width=\"140\" align=right>").append(currPetition.getTypeAsString()).append("</td></tr></table></td></tr>");
			color = !color;
			if(++petcount > 10)
			{
				htmlContent.append("<tr><td><font color=\"LEVEL\">There is more pending petition...</font><br></td></tr>");
				break;
			}
		}
		htmlContent.append("</table></center></body></html>");
		HtmlMessage htmlMsg = new HtmlMessage(0);
		htmlMsg.setHtml(htmlContent.toString());
		activeChar.sendPacket(htmlMsg);
	}

	public int submitPetition(Player petitioner, String petitionText, int petitionType)
	{
		Petition newPetition = new Petition(petitioner, petitionText, petitionType);
		int newPetitionId = newPetition.getId();
		getPendingPetitions().put(newPetitionId, newPetition);
		String msgContent = petitioner.getName() + " has submitted a new petition.";
		GmListTable.broadcastToGMs(new SayPacket2(petitioner.getObjectId(), ChatType.HERO_VOICE, "Petition System", msgContent));
		return newPetitionId;
	}

	public void viewPetition(Player activeChar, int petitionId)
	{
		if(!activeChar.isGM())
			return;
		if(!isValidPetition(petitionId))
			return;
		Petition currPetition = getPendingPetitions().get(petitionId);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		HtmlMessage html = new HtmlMessage(0);
		html.setFile("admin/petition.htm");
		html.replace("%petition%", String.valueOf(currPetition.getId()));
		html.replace("%time%", dateFormat.format(new Date(currPetition.getSubmitTime())));
		html.replace("%type%", currPetition.getTypeAsString());
		html.replace("%petitioner%", currPetition.getPetitioner().getName());
		html.replace("%online%", currPetition.getPetitioner().isOnline() ? "00FF00" : "999999");
		html.replace("%text%", currPetition.getContent());
		activeChar.sendPacket(html);
	}

	@Override
	public void handle(Player player, int id, String txt)
	{
		if(GmListTable.getAllVisibleGMs().isEmpty())
		{
			player.sendPacket(new SystemMessage(702));
			return;
		}
		if(!getInstance().isPetitioningAllowed())
		{
			player.sendPacket(new SystemMessage(381));
			return;
		}
		if(getInstance().isPlayerPetitionPending(player))
		{
			player.sendPacket(new SystemMessage(390));
			return;
		}
		if(getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING)
		{
			player.sendPacket(new SystemMessage(602));
			return;
		}
		int totalPetitions = getInstance().getPlayerTotalPetitionCount(player) + 1;
		if(totalPetitions > Config.MAX_PETITIONS_PER_PLAYER)
		{
			player.sendPacket(new SystemMessage(733));
			return;
		}
		if(txt.length() > 255)
		{
			player.sendPacket(new SystemMessage(971));
			return;
		}
		if(id >= PetitionType.values().length)
		{
			_log.warn("PetitionManager: Invalid petition type : " + id);
			return;
		}
		int petitionId = getInstance().submitPetition(player, txt, id);
		player.sendPacket(new SystemMessage(389).addNumber(petitionId));
		player.sendPacket(new SystemMessage(730).addNumber(totalPetitions).addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions));
		player.sendPacket(new SystemMessage(601).addNumber(getInstance().getPendingPetitionCount()));
	}

	static
	{
		_log = LoggerFactory.getLogger(PetitionManager.class.getName());
		_instance = new PetitionManager();
	}

	public enum PetitionState
	{
		Pending,
		Responder_Cancel,
		Responder_Missing,
		Responder_Reject,
		Responder_Complete,
		Petitioner_Cancel,
		Petitioner_Missing,
		In_Process,
		Completed
    }

	public enum PetitionType
	{
		Immobility,
		Recovery_Related,
		Bug_Report,
		Quest_Related,
		Bad_User,
		Suggestions,
		Game_Tip,
		Operation_Related,
		Other
    }

	private class Petition
	{
		private final long _submitTime;
		private long _endTime;
		private final int _id;
		private final PetitionType _type;
		private PetitionState _state;
		private final String _content;
		private final List<SayPacket2> _messageLog;
		private final int _petitioner;
		private int _responder;

		public Petition(Player petitioner, String petitionText, int petitionType)
		{
			_submitTime = System.currentTimeMillis();
			_endTime = -1L;
			_state = PetitionState.Pending;
			_messageLog = new ArrayList<>();
			_id = getNextId();
			_type = PetitionType.values()[petitionType - 1];
			_content = petitionText;
			_petitioner = petitioner.getObjectId();
		}

		protected boolean addLogMessage(SayPacket2 cs)
		{
			return _messageLog.add(cs);
		}

		protected List<SayPacket2> getLogMessages()
		{
			return _messageLog;
		}

		public boolean endPetitionConsultation(PetitionState endState)
		{
			setState(endState);
			_endTime = System.currentTimeMillis();
			if(getResponder() != null && getResponder().isOnline())
				if(endState == PetitionState.Responder_Reject)
					getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
				else
				{
					getResponder().sendPacket(new SystemMessage(395).addString(getPetitioner().getName()));
					if(endState == PetitionState.Petitioner_Cancel)
						getResponder().sendPacket(new SystemMessage(391).addNumber(getId()));
				}
			if(getPetitioner() != null && getPetitioner().isOnline())
				getPetitioner().sendPacket(new SystemMessage(387));
			getCompletedPetitions().put(getId(), this);
			return getPendingPetitions().remove(getId()) != null;
		}

		public String getContent()
		{
			return _content;
		}

		public int getId()
		{
			return _id;
		}

		public Player getPetitioner()
		{
			return GameObjectsStorage.getPlayer(_petitioner);
		}

		public Player getResponder()
		{
			return GameObjectsStorage.getPlayer(_responder);
		}

		public long getEndTime()
		{
			return _endTime;
		}

		public long getSubmitTime()
		{
			return _submitTime;
		}

		public PetitionState getState()
		{
			return _state;
		}

		public String getTypeAsString()
		{
			return _type.toString().replace("_", " ");
		}

		public void sendPetitionerPacket(L2GameServerPacket responsePacket)
		{
			if(getPetitioner() == null || !getPetitioner().isOnline())
				return;
			getPetitioner().sendPacket(responsePacket);
		}

		public void sendResponderPacket(L2GameServerPacket responsePacket)
		{
			if(getResponder() == null || !getResponder().isOnline())
			{
				endPetitionConsultation(PetitionState.Responder_Missing);
				return;
			}
			getResponder().sendPacket(responsePacket);
		}

		public void setState(PetitionState state)
		{
			_state = state;
		}

		public void setResponder(Player responder)
		{
			if(getResponder() != null)
				return;
			_responder = responder.getObjectId();
		}
	}
}
