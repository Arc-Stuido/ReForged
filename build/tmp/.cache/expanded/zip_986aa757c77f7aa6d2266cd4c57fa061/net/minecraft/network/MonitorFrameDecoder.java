package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MonitorFrameDecoder extends ChannelInboundHandlerAdapter {
    private final BandwidthDebugMonitor monitor;

    public MonitorFrameDecoder(BandwidthDebugMonitor p_331226_) {
        this.monitor = p_331226_;
    }

    @Override
    public void channelRead(ChannelHandlerContext p_328985_, Object p_332208_) {
        if (p_332208_ instanceof ByteBuf bytebuf) {
            this.monitor.onReceive(bytebuf.readableBytes());
        }

        p_328985_.fireChannelRead(p_332208_);
    }
}