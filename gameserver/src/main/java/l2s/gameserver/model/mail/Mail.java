package l2s.gameserver.model.mail;

import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.dao.MailDAO;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;

import java.util.HashSet;
import java.util.Set;

public class Mail implements JdbcEntity, Comparable<Mail>
{
	public enum SenderType
	{
		NORMAL,
		NEWS_INFORMER,
		NONE,
		BIRTHDAY,
		UNKNOWN,
		SYSTEM,
		MENTOR,
		PRESENT;

		public static SenderType[] VALUES = values();
	}

	private static final long serialVersionUID = -8704970972611917153L;

    private static final int MAX_SYSTEM_PARAMS_COUNT = 8;

	public static final int DELETED = 0;
	public static final int READED = 1;
	public static final int REJECTED = 2;

	private static final MailDAO _mailDAO = MailDAO.getInstance();

	private int messageId;
	private int senderId;
	private String senderName;
    private String senderHwid;
	private int receiverId;
	private String receiverName;
	private int expireTime;
	private String topic;
	private String body;
	private long price;
	private SenderType _type = SenderType.NORMAL;
	private boolean isUnread;
    private boolean _isReturned;
	private final Set<ItemInstance> attachments = new HashSet<>();

	private int _systemTopic;
    private int _systemBody;
    private int[] _systemParams = new int[MAX_SYSTEM_PARAMS_COUNT];

	private JdbcEntityState _state = JdbcEntityState.CREATED;

	public int getMessageId()
	{
		return messageId;
	}

	public void setMessageId(int messageId)
	{
		this.messageId = messageId;
	}

	public int getSenderId()
	{
		return senderId;
	}

	public void setSenderId(int senderId)
	{
		this.senderId = senderId;
	}

	public String getSenderName()
	{
		return senderName;
	}

	public void setSenderName(String senderName)
	{
		this.senderName = senderName;
	}

	public String getSenderHwid()
	{
		return senderHwid;
	}

	public void setSenderHwid(String senderHwid)
	{
		this.senderHwid = senderHwid;
	}

	public int getReceiverId()
	{
		return receiverId;
	}

	public void setReceiverId(int receiverId)
	{
		this.receiverId = receiverId;
	}

	public String getReceiverName()
	{
		return receiverName;
	}

	public void setReceiverName(String receiverName)
	{
		this.receiverName = receiverName;
	}

	public int getExpireTime()
	{
		return expireTime;
	}

	public void setExpireTime(int expireTime)
	{
		this.expireTime = expireTime;
	}

	public String getTopic()
	{
		return topic;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public boolean isPayOnDelivery()
	{
		return price > 0L;
	}

	public long getPrice()
	{
		return price;
	}

	public void setPrice(long price)
	{
		this.price = price;
	}

	public boolean isUnread()
	{
		return isUnread;
	}

	public void setUnread(boolean isUnread)
	{
		this.isUnread = isUnread;
	}

    public boolean isReturned()
    {
        return _isReturned;
    }

    public void setReturned(boolean value)
    {
        _isReturned = value;
    }

	public Set<ItemInstance> getAttachments()
	{
		return attachments;
	}

	public void addAttachment(ItemInstance item)
	{
		attachments.add(item);
	}

    public boolean isReturnable()
    {
        return _type == SenderType.NORMAL && !attachments.isEmpty() && !_isReturned;
    }

    public int getSystemTopic()
    {
        return _systemTopic;
    }

    public void setSystemTopic(int systemTopic)
    {
        _systemTopic = systemTopic;
    }

    public void setSystemTopic(SystemMsg systemTopic)
    {
        _systemTopic = systemTopic.getId();
    }

    public int getSystemBody()
    {
        return _systemBody;
    }

    public void setSystemBody(int systemBody)
    {
        _systemBody = systemBody;
    }

    public void setSystemBody(SystemMsg systemBody)
    {
        _systemBody = systemBody.getId();
    }

    public int[] getSystemParams()
    {
        return _systemParams;
    }

    public String getSystemParamsToString()
    {
        String result = "";
        for(int param : _systemParams)
            result = result + param + ";";
        return result;
    }

    public void setSystemParam(int i, int val)
    {
        _systemParams[i] = val;
    }

    public void setSystemParams(String val)
    {
        if(val == null || val.isEmpty())
            return;
        String[] params = val.split(";");
        int length = Math.min(params.length, MAX_SYSTEM_PARAMS_COUNT);

        for(int i = 0; i < length; ++i)
        {
            String param = params[i];
            if(param == null || param.isEmpty())
                continue;
            setSystemParam(i, Integer.parseInt(param));
        }
    }

	@Override
	public boolean equals(Object o)
	{
		if(o == this)
			return true;
		if(o == null)
			return false;
		if(o.getClass() != getClass())
			return false;
		return ((Mail) o).getMessageId() == getMessageId();
	}

    @Override
    public int hashCode()
    {
        return 13 * getMessageId() + 11700;
    }

	@Override
	public void setJdbcState(JdbcEntityState state)
	{
		_state = state;
	}

	@Override
	public JdbcEntityState getJdbcState()
	{
		return _state;
	}

	@Override
    public void save()
	{
		_mailDAO.save(this);
	}

	@Override
    public void update()
	{
		_mailDAO.update(this);
	}

	@Override
    public void delete()
	{
		_mailDAO.delete(this);
	}

	public Mail reject()
	{
		Mail mail = new Mail();
		mail.setSenderId(getReceiverId());
		mail.setSenderName(getReceiverName());
		mail.setReceiverId(getSenderId());
		mail.setReceiverName(getSenderName());
        mail.setSenderHwid(getSenderHwid());
		mail.setTopic(getTopic());
		mail.setBody(getBody());
		synchronized (getAttachments())
		{
			getAttachments().forEach(mail::addAttachment);
			getAttachments().clear();
		}
		mail.setType(SenderType.NEWS_INFORMER);
		mail.setUnread(true);
        mail.setReturned(true);
        mail.setPrice(getPrice());

		return mail;
	}

	public Mail reply()
	{
		Mail mail = new Mail();
		mail.setSenderId(getReceiverId());
		mail.setSenderName(getReceiverName());
		mail.setReceiverId(getSenderId());
		mail.setReceiverName(getSenderName());
		mail.setTopic("[Re]" + getTopic());
		mail.setBody(getBody());
		mail.setType(SenderType.NEWS_INFORMER);
		mail.setUnread(true);
		return mail;
	}

	@Override
	public int compareTo(Mail o)
	{
		return o.getMessageId() - getMessageId();
	}

	public SenderType getType()
	{
		return _type;
	}

	public void setType(SenderType type)
	{
		_type = type;
	}
}