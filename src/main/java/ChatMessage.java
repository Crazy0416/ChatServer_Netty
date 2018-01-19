import java.util.Objects;

public class ChatMessage {

    public final String command;
    public final String nickname;
    public String text;

    public ChatMessage(String command){
        this(command, null, null);
    }

    public ChatMessage(String command, String nickname){
        this(command, nickname, null);
    }

    public ChatMessage(String command, String nickname, String text){

        this.command = command;
        this.nickname = nickname;
        this.text = text;

    }

    public String toString(){           // ChatMessage를 String으로 변환할 시에 사용

        StringBuilder b = new StringBuilder();
        b.append(command);

        if(nickname != null)    b.append(":").append(nickname);
        if(text != null)        b.append(" ").append(text);

        return b.toString();

    }

    public static ChatMessage parse(String line){       // String으로 클라이언트에서 날라올 때 ChatMessage로 바꿔서 서버에서 사용

        if(line.contains("\n"))     throw new IllegalArgumentException();

        String[] tokens = line.split("\\s", 2);
        String command = tokens[0];
        ChatMessage m;

        if(command.contains(":")){
            String[] t = command.split(":", 2);
            m = new ChatMessage(t[0], t[1]);
        } else{
            m = new ChatMessage(command);
        }

        if(tokens.length > 1) m.text = tokens[1];

        return m;

    }

    @Override
    public boolean equals(Object o){        // 비교

        if (o == null || !(o instanceof ChatMessage)) return false;

        ChatMessage oc = (ChatMessage)o;

        return this == o ||
                Objects.equals(command, oc.command)
                        && Objects.equals(nickname, oc.nickname)
                        && Objects.equals(text, oc.text);

    }

}
