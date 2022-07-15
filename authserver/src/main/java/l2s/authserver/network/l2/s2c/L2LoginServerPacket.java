/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package l2s.authserver.network.l2.s2c;


import com.google.common.flogger.FluentLogger;
import l2s.authserver.network.l2.L2LoginClient;
import l2s.commons.net.nio.impl.SendablePacket;

public abstract class L2LoginServerPacket extends SendablePacket<L2LoginClient>
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	public final boolean write()
	{
		try
		{
			writeImpl();
			return true;
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Client: %s - Failed writing: %s!", getClient(), getClass().getSimpleName() );
		}
		return false;
	}

	protected abstract void writeImpl();
}
