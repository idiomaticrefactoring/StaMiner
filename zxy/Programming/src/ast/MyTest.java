package ast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyTest {
    static public List<Integer> list = new ArrayList<>();
    int a = 0;
    public void myFunc()
    {
        int c = this.a;
    }
    public MyTest getMe()
    {
        return this;
    }
    public int getA()
    {
        return this.a;
    }
    public static void main(String[] args)
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        MyTest myTest = new MyTest();
        int n;
        try{
            n = new Integer(bufferedReader.readLine());
            n = myTest.getMe().getA();
            for(int i = 0;i < n;i++)
                myTest.list.add(new Integer(bufferedReader.readLine()));
            System.out.println(myTest.list);
            do{
                System.out.println("TEST");
                n--;
            }while(n > 0);
            for(Integer x:list)
            {
                System.out.println(x);
            }
            int b = myTest.a;
            int c = myTest.getMe().a;
            for(int i = 0;i < list.size();i++)
                System.out.println(list.get(i));
            if(n > 0)
                System.out.println("FIRST");
            else
                System.out.println("SECOND");
            switch (n)
            {
                case 0:System.out.println(n);break;
                case 1:System.out.println(n);break;
                default:System.out.println(n);break;
            }
            while(myTest.getMe().a > 0)
            {
                System.out.println("TEST");
                n--;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
