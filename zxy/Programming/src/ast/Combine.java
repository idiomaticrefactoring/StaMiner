package ast;

import java.util.ArrayList;
import java.util.List;

public class Combine {

    public static void main(String[] args) {
        List<Character> data = new ArrayList<Character>();
        data.add('a');
        data.add('b');
        data.add('c');
        data.add('d');
        Combine t = new Combine();

        t.combinerSelect(data, new ArrayList<Character>(), data.size(), 5);
    }

    public <E> void combinerSelect(List<E> data, List<E> workSpace, int n, int k) {
        List<E> copyData;
        List<E> copyWorkSpace;

        if(workSpace.size() == k) {
            for(Object c : workSpace)
                System.out.print(c);
            System.out.println();
        }

        for(int i = 0; i < data.size(); i++) {
            copyData = new ArrayList<E>(data);
            copyWorkSpace = new ArrayList<E>(workSpace);

            copyWorkSpace.add(copyData.get(i));
            for(int j = i; j >=  0; j--)
                copyData.remove(j);
            combinerSelect(copyData, copyWorkSpace, n, k);
        }

    }

}
