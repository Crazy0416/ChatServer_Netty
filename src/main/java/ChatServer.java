import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ResourceLeakDetector;

public class ChatServer {

    public static void main(String[] args) throws Exception{

        NettyStartupUtil.runServer(8030, pipeline->
            pipeline.addLast(new LineBasedFrameDecoder(1024, true, true))     // \n과 \r\n을 구분함??
                    .addLast(new StringDecoder(CharsetUtil.UTF_8), new StringEncoder(CharsetUtil.UTF_8))        // ByteBuf객체를 UTF-8 String으로 변환시켜줌  보통 LineBasedFrameDecoder과 같이 쓴다.
                    .addLast(new ChatMessageCodec(), new LoggingHandler(LogLevel.INFO))                         // String을 ChatMessage로, ChatMessage를 String으로 변환
                    .addLast(new ChatServerHandler())
        );

    }

}
