import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends SimpleChannelInboundHandler<ChatMessage>{

    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static final AttributeKey<String> nickAttr = AttributeKey.newInstance("nickname");                      // 유니크한 키 값을 가지는 자료구조? or 채널 마다 부여하는 특성??
    private static final NicknameProvider nicknameProvider = new NicknameProvider();

    /*

     */
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception{

        if(ctx.channel().isActive()){
            helo(ctx.channel());
        }

    }

    /*
        채널 활성화 되었을 때 작동하는 함수
        닉네임 설정, 전체 채널에 클라이언트 등록 등 기능을 수해
     */
    private void helo(Channel ch){

        if(nickname(ch) != null)    return;

        String nick = nicknameProvider.reserve();       // 클라이언트에게 지정되지 않은 닉네임 하나 가져옴

        if(nick == null){

            ch.writeAndFlush(M("ERR", "sorry, no more names for you"))      // 빈 닉네임이 없음
                    .addListener(ChannelFutureListener.CLOSE);

        } else {

            bindNickname(ch, nick);             // 채널에(클라이언트에게) 닉네임 부여

            channels.forEach(c -> ch.write(M("HAVE", nickname(c))));        // 내가 서버에 접속하게 되면 전체 채널에 누가 접속해있는 지 메세지 보내줌
            channels.writeAndFlush(M("JOIN", nick));                        // 내가 접속했다고 내 닉네임을 클라이언트(ex. telnet)에 전송
            channels.add(ch);                                                       // 전체 채널에 접속한 클라이언트 추가
            ch.writeAndFlush(M("HELO", nick));

        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){

        cause.printStackTrace();
        if(!ctx.channel().isActive()){

            ctx.writeAndFlush(M("ERR", null, cause.getMessage()))
                    .addListener(ChannelFutureListener.CLOSE);

        }

    }

    /*
    채널 버퍼에 값이 들어올 때 발생함
    여기서 ChatMessage 안에 command를 확인하고 command마다 다른 기능을 수행한다.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessage msg) throws Exception {

        if ("PING".equals(msg.command)) {
            // TODO: [실습3-1] PING 명령어에 대한 응답을 내보냅니다
            ctx.write(M("PONG", "admin", "Hello World"));
        } else if ("QUIT".equals(msg.command)) {
            // TODO: [실습3-2] QUIT 명령어를 처리하고 BYE를 응답합니다. 연결도 끊습니다.
            ctx.write(M("BYE", null, null));
            channels.remove(ctx.channel());
            ReferenceCountUtil.release(msg);        // 릴리즈 해도 되는지 모르겠음
            ctx.close();
        } else if ("SEND".equals(msg.command)) {
            // TODO: [실습3-3] 클라이언트로부터 대화 텍스트가 왔습니다. 모든 채널에 FROM 메시지를 방송합니다.
            channels.write(M("FROM", ctx.channel().attr(nickAttr).get(), msg.text));
        } else if ("NICK".equals(msg.command)) {
            changeNickname(ctx, msg);
        } else {
            ctx.write(M("ERR", null, "unknown command -> " + msg.command));
        }
        ReferenceCountUtil.release(msg);
    }

    // 아직 안써봄
    private void changeNickname(ChannelHandlerContext ctx, ChatMessage msg) {

        String newNick = msg.text.replace(" ", "_").replace(":", "-");
        String prev = nickname(ctx);

        if (!newNick.equals(prev) && nicknameProvider.available(newNick)) {

            nicknameProvider.release(prev).reserve(newNick);
            bindNickname(ctx.channel(), newNick);
            channels.writeAndFlush(M("NICK", prev, newNick));

        } else {
            ctx.write(M("ERR", null, "couldn't change"));
        }
    }


    /* String... args는 인수가 여러개 들어올 수 있게 설정해줌
    * M("a", "b")든 M("a")든 M("a", "b", "c")든 받을 수 있다. 대신 함수 정의에서 구분해줘야함
    */
    private ChatMessage M(String... args) {

        switch (args.length){
            case 1:
                return new ChatMessage(args[0]);
            case 2:
                return new ChatMessage(args[0], args[1]);
            case 3:
                ChatMessage m = new ChatMessage(args[0], args[1]);
                m.text = args[2];
                return m;
                default:
                    throw new IllegalArgumentException();
        }

    }

    // 채널에 대화명을 지정합니다.
    private void bindNickname(Channel c, String nickname) {
        // 채널에는 AttributeKey라는 것을 설정할 수 잇는데 각 채널마다 유니크한 값을 가질 수 있다. 채널마다 지역변수를 하나씩 들고 있다고 생각하면 좋음
        // set(nickname)은 AttributeKey를 설정한 키의 값(벨류)를 지정한다.
        // nickname은 개별 사용자의 닉네임
        c.attr(nickAttr).set(nickname);
    }


    // 채널에 지정된 대화명을 가져옵니다.
    private String nickname(Channel c) {
        // 지정한 채널의 키에 대한 값을 가져온다. 여기서는 채널에 지정된 닉네임의 값을 가져옴
        // 그냥 사용자 닉네임을 가져옴
        return c.attr(nickAttr).get();
    }

    // nickname(Channel)과 같지만 편의를 위한 메소드입니다.
    private String nickname(ChannelHandlerContext ctx) {
        return nickname(ctx.channel());
    }

}
