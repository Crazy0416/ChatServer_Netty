import java.util.*;

public class NicknameProvider {

    private final Queue<String> pool;
    private final Set<String> preset;
    private final Set<String>   occupied = new HashSet<>();


    public NicknameProvider() {

        List<String> names = Arrays.asList(             // 닉네임 자료구조
                "Mark", "Tim", "Evan", "Bill", "Larry",
                "Paul", "Eric", "David", "Martin", "Matz",
                "Rich", "John", "Rob", "Ken", "Joe",
                "Simon", "Roberto", "Niklaus", "Alan", "Richard",
                "James", "Kyrie", "Michale", "Stephen", "Derrik",
                "Kevin", "Russel", "LeBron", "Kobe", "Chris",
                "Tony", "Blake", "Dwayne", "Carmelo"
        );

        preset = new HashSet<>(names);                  // 아직 쓰지 않은 사용자 닉네임들 저장
        Collections.shuffle(names);                     // 닉네임을 랜덤으로 섞음??
        pool = new LinkedList<>(names);                 // 아직 쓰지 않은 사용자 닉네임들 저장

    }

    public synchronized boolean available(String nickname){
        return !preset.contains(nickname) && !occupied.contains(nickname);
    }

    public synchronized String reserve(){

        String n = pool.poll();                 // 아직 쓰지 않은 사용자 닉네임들 중 하나 가져옴
        if (n != null)  occupied.add(n);        // occupied에 추가(사용된 닉네임 저장하는 공간인듯)

        return n;

    }

    public synchronized void reserve(String custom) {

        if (!available(custom)) throw new RuntimeException("not available name");
        occupied.add(custom);

    }

    public synchronized NicknameProvider release(String nick){          // 사용한 닉네임 다시 돌려놓음

        occupied.remove(nick);

        if(preset.contains(nick) && !pool.contains(nick))   pool.add(nick);

        return this;

    }

}
