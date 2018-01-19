import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class ChatMessageCodec extends MessageToMessageCodec<String, ChatMessage>{       // String을 ChatMessage로 디코딩, ChatMessage를 String으로 인코딩한다.

    @Override
    protected void encode(ChannelHandlerContext ctx, ChatMessage msg, List<Object> out) throws Exception {
        out.add(msg + "\n");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String line, List<Object> out) throws Exception{
        out.add(ChatMessage.parse(line));
    }

}
