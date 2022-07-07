package l2s.authserver.network.l2.c2s;

import l2s.authserver.Config;
import l2s.authserver.GameServerManager;
import l2s.authserver.IpBanManager;
import l2s.authserver.accounts.Account;
import l2s.authserver.accounts.SessionManager;
import l2s.authserver.crypt.PasswordHash;
import l2s.authserver.database.DatabaseFactory;
import l2s.authserver.database.dao.AccountLogDAO;
import l2s.authserver.network.gamecomm.as2gs.GetAccountInfo;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import l2s.authserver.network.l2.L2LoginClient;
import l2s.authserver.network.l2.s2c.LoginFail;
import l2s.authserver.network.l2.s2c.LoginFail.LoginFailReason;
import l2s.authserver.network.l2.s2c.LoginOk;

import javax.crypto.Cipher;

public class RequestAuthLogin extends L2LoginClientPacket
{
	private final AccountLogDAO accountLogDAO = DatabaseFactory.getInstance().getJdbi().onDemand(AccountLogDAO.class);

	private final byte[] _raw1;
	private final byte[] _raw2;
	private boolean _newAuthMethod;

	public RequestAuthLogin()
	{
		_raw1 = new byte[128];
		_raw2 = new byte[128];
		_newAuthMethod = false;
	}

	@Override
	protected void readImpl()
	{
		if(_buf.remaining() >= _raw1.length + _raw2.length)
		{
			_newAuthMethod = true;
            readB(_raw1);
            readB(_raw2);
		}
		if(_buf.remaining() >= _raw1.length)
		{
            readB(_raw1);
			readD();
			readD();
			readD();
			readD();
			readD();
			readD();
			readH();
			readC();
		}
	}

	@Override
	protected void runImpl() throws Exception
	{
		L2LoginClient client = getClient();
		byte[] decUser = null;
		byte[] decPass = null;
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(2, client.getRSAPrivateKey());
			decUser = rsaCipher.doFinal(_raw1, 0, 128);
			if(_newAuthMethod)
				decPass = rsaCipher.doFinal(_raw2, 0, _raw2.length);
		}
		catch(Exception e)
		{
			client.closeNow(true);
			return;
		}
		String user = null;
		String password = null;
		if(_newAuthMethod)
		{
			user = new String(decUser, 78, 32).trim().toLowerCase();
			password = new String(decPass, 92, 16).trim();
		}
		else
		{
			user = new String(decUser, 94, 14).trim().toLowerCase();
			password = new String(decUser, 108, 16).trim();
		}
		int currentTime = (int) (System.currentTimeMillis() / 1000L);
		Account account = new Account(user);
		account.restore();
		String passwordHash = Config.DEFAULT_CRYPT.encrypt(password);
		if(account.getPasswordHash() == null)
		{
			if(!Config.AUTO_CREATE_ACCOUNTS || !user.matches(Config.ANAME_TEMPLATE) || !password.matches(Config.APASSWD_TEMPLATE))
			{
				client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
				return;
			}
			account.setAllowedIP("");
			account.setAllowedHwid("");
			account.setPasswordHash(passwordHash);
			account.save();
		}
		boolean passwordCorrect = account.getPasswordHash().equals(passwordHash);
		if(!passwordCorrect)
			for(PasswordHash c : Config.LEGACY_CRYPT)
				if(c.compare(password, account.getPasswordHash()))
				{
					passwordCorrect = true;
					account.setPasswordHash(passwordHash);
					break;
				}
		if(!IpBanManager.getInstance().tryLogin(client.getIpAddress(), passwordCorrect))
		{
			client.closeNow(false);
			return;
		}
		client.setPasswordCorrect(passwordCorrect);
		if(!Config.CHEAT_PASSWORD_CHECK && !passwordCorrect)
		{
			client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
			return;
		}
		if(account.getAccessLevel() < 0)
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		if(account.getBanExpire() > currentTime)
		{
			client.close(LoginFailReason.REASON_ACCESS_FAILED);
			return;
		}
		if(!account.isAllowedIP(client.getIpAddress()))
		{
			client.close(LoginFailReason.REASON_ATTEMPTED_RESTRICTED_IP);
			return;
		}
		for(GameServerConnection gs : GameServerManager.getInstance().getGameServers())
			if(gs.getGameServerDescription().getProtocol() >= 2 && gs.isAuthed())
				gs.sendPacket(new GetAccountInfo(user));
		account.setLastAccess(currentTime);
		account.setLastIP(client.getIpAddress());

		accountLogDAO.insert(account);

		SessionManager.Session session = SessionManager.getInstance().openSession(account);
		client.setAuthed(true);
		client.setLogin(user);
		client.setAccount(account);
		client.setSessionKey(session.getSessionKey());
		client.setState(L2LoginClient.LoginClientState.AUTHED);
		client.sendPacket(new LoginOk(client.getSessionKey()));
	}
}
