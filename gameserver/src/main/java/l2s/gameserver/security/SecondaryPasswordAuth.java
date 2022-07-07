package l2s.gameserver.security;

import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.dao.AccountVariablesDAO;
import l2s.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordAckPacket;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordCheckPacket;
import l2s.gameserver.network.l2.s2c.Ex2NDPasswordVerifyPacket;
import l2s.gameserver.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecondaryPasswordAuth
{
	private static final Logger _log;
	private final GameClient _activeClient;
	private String _password;
	private int _wrongAttempts;
	private boolean _authed;
	private static final String VAR_PWD = "secauth_pwd";
	private static final String VAR_WTE = "secauth_wte";

	public SecondaryPasswordAuth(GameClient activeClient)
	{
		_activeClient = activeClient;
		_password = null;
		_wrongAttempts = 0;
		_authed = false;
		loadPassword();
	}

	private void loadPassword()
	{
		String accountName = _activeClient.getLogin();
		String password = AccountVariablesDAO.getInstance().select(accountName, VAR_PWD);
		if(password != null)
		{
			_password = password;
			String wrongAttempts = AccountVariablesDAO.getInstance().select(accountName, VAR_WTE);
			if(wrongAttempts != null)
				_wrongAttempts = Integer.parseInt(wrongAttempts);
		}
	}

	public boolean savePassword(String password)
	{
		if(passwordExist())
		{
			_log.warn("[SecondaryPasswordAuth]" + _activeClient.getLogin() + " forced savePassword");
			_activeClient.closeNow(false);
			return false;
		}
		if(!validatePassword(password))
		{
			_activeClient.sendPacket(new Ex2NDPasswordAckPacket(1));
			return false;
		}
		password = cryptPassword(password);
		String accountName = _activeClient.getLogin();
		AccountVariablesDAO.getInstance().insert(accountName, VAR_PWD, password);
		_password = password;
		return true;
	}

	public boolean insertWrongAttempt(int attempts)
	{
		String accountName = _activeClient.getLogin();
		AccountVariablesDAO.getInstance().insert(accountName, VAR_WTE, Integer.toString(attempts));
		return true;
	}

	public boolean changePassword(String oldPassword, String newPassword)
	{
		if(!passwordExist())
		{
			_log.warn("[SecondaryPasswordAuth]" + _activeClient.getLogin() + " forced changePassword");
			_activeClient.closeNow(false);
			return false;
		}
		if(!checkPassword(oldPassword, true))
			return false;
		if(!validatePassword(newPassword))
		{
			_activeClient.sendPacket(new Ex2NDPasswordAckPacket(1));
			return false;
		}
		newPassword = cryptPassword(newPassword);
		String accountName = _activeClient.getLogin();
		AccountVariablesDAO.getInstance().insert(accountName, VAR_PWD, newPassword);
		_password = newPassword;
		_authed = false;
		return true;
	}

	public boolean checkPassword(String password, boolean skipAuth)
	{
		password = cryptPassword(password);
		if(password.equals(_password))
		{
			if(!skipAuth)
			{
				_authed = true;
				_activeClient.sendPacket(new Ex2NDPasswordVerifyPacket(0, _wrongAttempts));
			}
			insertWrongAttempt(0);
			return true;
		}
		++_wrongAttempts;
		if(_wrongAttempts < Config.EX_SECOND_AUTH_MAX_ATTEMPTS)
		{
			_activeClient.sendPacket(new Ex2NDPasswordVerifyPacket(1, _wrongAttempts));
			insertWrongAttempt(_wrongAttempts);
			return false;
		}
		int banExpire = (int) (System.currentTimeMillis() / 1000L) + Config.EX_SECOND_AUTH_BAN_TIME * 60;
		int accessLvl = Config.EX_SECOND_AUTH_BAN_TIME > 0 ? 0 : -100;
		String msg = _activeClient.getLogin() + " - (" + _activeClient.getIpAddr() + ") has inputted the wrong password " + _wrongAttempts + " times in row.";
		GameServer.getInstance().getAuthServerCommunication().sendPacket(new ChangeAccessLevel(_activeClient.getLogin(), accessLvl, banExpire, msg));
		_log.warn(msg);
		insertWrongAttempt(0);
		_activeClient.close(new Ex2NDPasswordVerifyPacket(2, Config.EX_SECOND_AUTH_MAX_ATTEMPTS));
		return false;
	}

	public boolean passwordExist()
	{
		return _password != null;
	}

	public void openDialog()
	{
		if(passwordExist())
			_activeClient.sendPacket(new Ex2NDPasswordCheckPacket(1));
		else
			_activeClient.sendPacket(new Ex2NDPasswordCheckPacket(0));
	}

	public boolean isAuthed()
	{
		return _authed;
	}

	private String cryptPassword(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			return Base64.getEncoder().encodeToString(hash);
		}
		catch(NoSuchAlgorithmException e)
		{
			_log.error("[SecondaryPasswordAuth] Unsupported Algorythm", e);
		}
		catch(UnsupportedEncodingException e2)
		{
			_log.error("[SecondaryPasswordAuth] Unsupported Encoding", e2);
		}
		return null;
	}

	private boolean validatePassword(String password)
	{
		if(!Strings.isDigit(password))
			return false;
		if(password.length() < 6 || password.length() > 8)
			return false;
		_wrongAttempts = 0;
		return true;
	}

	static
	{
		_log = LoggerFactory.getLogger(SecondaryPasswordAuth.class);
	}
}
