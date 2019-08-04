
import java.util.*;

// 交叉队列
public class Test {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String line;
        while (scan.hasNextLine()) {
            line = scan.nextLine().trim();
            fun(line);
        }
    }

    public static void fun(String line) {
        String[] numString = line.split(",");
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < numString.length; i++) {
            if (!map.containsKey(numString[i])) map.put(numString[i], 1);
            else map.put(numString[i], map.get(numString[i]) + 1);
        }
        for(Map.Entry<String, Integer> entry:map.entrySet()){
            if(entry.getValue()==1){
                System.out.println(entry.getKey());
                break;
            }
        }
    }
}
