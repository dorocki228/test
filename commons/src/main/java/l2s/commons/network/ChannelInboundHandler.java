/*
 * Copyright (C) 2004-2015 L2J Unity
 *
 * This file is part of L2J Unity.
 *
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2s.commons.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @param <T>
 *
 * @author Nos
 * @author Java-man
 */
public abstract class ChannelInboundHandler<T extends ChannelInboundHandler<?>> extends SimpleChannelInboundHandler<IIncomingPacket<T>> {
    private Channel _channel;

    private int _failedPackets = 0;
    private int _unknownPackets = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        _channel = ctx.channel();
    }

    public void setConnectionState(IConnectionState connectionState) {
        _channel.attr(IConnectionState.CONNECTION_STATE_ATTRIBUTE_KEY).set(connectionState);
    }

    public IConnectionState getConnectionState() {
        return _channel != null ? _channel.attr(IConnectionState.CONNECTION_STATE_ATTRIBUTE_KEY).get() : null;
    }

    public void onPacketReadFail() {
        if (_failedPackets++ >= 5) {
            NetworkLogger.INSTANCE.getLogger().atWarning().log("Too many client packet fails, connection closed : %s", toString());
            if (_channel != null) {
                _channel.close();
            }
        }
    }

    public void onUnknownPacket() {
        if (_unknownPackets++ >= 5) {
            NetworkLogger.INSTANCE.getLogger().atWarning().log("Too many client unknown packets, connection closed : %s", toString());
            if (_channel != null) {
                _channel.close();
            }
        }
    }
}
