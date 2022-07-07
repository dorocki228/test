package l2s.authserver.network.l2;

import l2s.authserver.Config;
import l2s.authserver.accounts.Account;
import l2s.authserver.crypt.LoginCrypt;
import l2s.authserver.crypt.ScrambledKeyPair;
import l2s.authserver.network.l2.s2c.AccountKicked;
import l2s.authserver.network.l2.s2c.AccountKicked.AccountKickedReason;
import l2s.authserver.network.l2.s2c.L2LoginServerPacket;
import l2s.authserver.network.l2.s2c.LoginFail;
import l2s.authserver.network.l2.s2c.LoginFail.LoginFailReason;
import l2s.commons.net.nio.impl.MMOClient;
import l2s.commons.net.nio.impl.MMOConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;

public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
	private static final Logger _log = LoggerFactory.getLogger(L2LoginClient.class);
	private static final int PROTOCOL_VERSION = 50721;
	private LoginClientState _state;
	private LoginCrypt _loginCrypt;
	private ScrambledKeyPair _scrambledPair;
	private byte[] _blowfishKey;
	private String _login;
	private SessionKey _skey;
	private Account _account;
	private final String _ipAddr;
	private int _sessionId;
	private boolean _passwordCorrect;

	public L2LoginClient(MMOConnection<L2LoginClient> con)
	{
		super(con);
		_state = LoginClientState.CONNECTED;
		_scrambledPair = Config.getScrambledRSAKeyPair();
		_blowfishKey = Config.getBlowfishKey();
		(_loginCrypt = new LoginCrypt()).setKey(_blowfishKey);
		_sessionId = con.hashCode();
		_ipAddr = getConnection().getSocket().getInetAddress().getHostAddress();
		_passwordCorrect = false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret;
		try
		{
			ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
		}
		catch(IOException e)
		{
			_log.error("", e);
			closeNow(true);
			return false;
		}
		if(!ret)
			closeNow(true);
		return ret;
	}

	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		int offset = buf.position();
		try
		{
			size = _loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch(IOException e)
		{
			_log.error("", e);
			return false;
		}
		buf.position(offset + size);
		return true;
	}

	public LoginClientState getState()
	{
		return _state;
	}

	public void setState(LoginClientState state)
	{
		_state = state;
	}

	public byte[] getBlowfishKey()
	{
		return _blowfishKey;
	}

	public byte[] getScrambledModulus()
	{
		return _scrambledPair.getScrambledModulus();
	}

	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair.getKeyPair().getPrivate();
	}

	public String getLogin()
	{
		return _login;
	}

	public void setLogin(String login)
	{
		_login = login;
	}

	public Account getAccount()
	{
		return _account;
	}

	public void setAccount(Account account)
	{
		_account = account;
	}

	public SessionKey getSessionKey()
	{
		return _skey;
	}

	public void setSessionKey(SessionKey skey)
	{
		_skey = skey;
	}

	public void setSessionId(int val)
	{
		_sessionId = val;
	}

	public int getSessionId()
	{
		return _sessionId;
	}

	public void setPasswordCorrect(boolean val)
	{
		_passwordCorrect = val;
	}

	public boolean isPasswordCorrect()
	{
		return _passwordCorrect;
	}

	public void sendPacket(L2LoginServerPacket lsp)
	{
		if(isConnected())
			getConnection().sendPacket(lsp);
	}

	public void close(LoginFailReason reason)
	{
		if(isConnected())
			getConnection().close(new LoginFail(reason));
	}

	public void close(AccountKickedReason reason)
	{
		if(isConnected())
			getConnection().close(new AccountKicked(reason));
	}

	public void close(L2LoginServerPacket lsp)
	{
		if(isConnected())
			getConnection().close(lsp);
	}

	@Override
	public void onDisconnection()
	{
		_state = LoginClientState.DISCONNECTED;
		_skey = null;
		_loginCrypt = null;
		_scrambledPair = null;
		_blowfishKey = null;
	}

	@Override
	public String toString()
	{
		switch(_state)
		{
			case AUTHED:
			{
				return "[ Account : " + getLogin() + " IP: " + getIpAddress() + "]";
			}
			default:
			{
				return "[ State : " + getState() + " IP: " + getIpAddress() + "]";
			}
		}
	}

	public String getIpAddress()
	{
		return _ipAddr;
	}

	@Override
	protected void onForcedDisconnection()
	{}

	public int getProtocol()
	{
		return PROTOCOL_VERSION;
	}

	public enum LoginClientState
	{
		CONNECTED,
		AUTHED_GG,
		AUTHED,
		DISCONNECTED
    }
}
