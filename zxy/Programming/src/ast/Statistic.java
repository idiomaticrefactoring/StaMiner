package ast;

import java.util.ArrayList;
import java.util.List;

public class Statistic {
    public String javaFileName = null;
    public List<Data> dataList = new ArrayList<>();

    public void addStatistic(Statistic statistic)
    {
        for(int i = 0;i < statistic.dataList.size();i++)
        {
            int position = -1;
            for(int j = 0;j < this.dataList.size();j++)
            {
                if(this.dataList.get(j).className.equals(statistic.dataList.get(i)))
                {
                    position = j;
                    break;
                }
            }
            if(position != -1)
            {
                Data data1 = this.dataList.get(position);
                Data data2 = statistic.dataList.get(i);
                for(int m = 0;m < data2.methodNames.size();m++)
                {
                    int p = -1;
                    for(int n = 0;n < data1.methodNames.size();n++)
                    {
                        if(data1.methodNames.get(n).equals(data2.methodNames.get(m)))
                        {
                            p = n;
                            break;
                        }
                    }
                    if(p != -1)
                    {
                        int prev = data1.methodUsages.get(p);
                        data1.methodUsages.set(p, prev + data2.methodUsages.get(m));
                        data1.amount += data2.methodUsages.get(m);
                    }
                    else
                    {
                        data1.methodNames.add(data2.methodNames.get(m));
                        data1.methodUsages.add(data2.methodUsages.get(m));
                        data1.amount += data2.methodUsages.get(m);
                    }
                }
            }
            else
            {
                this.dataList.add(statistic.dataList.get(i));
            }
        }
    }

    public int existClass(String className)
    {
        if(className == null)
            return -2;
        for(int i = 0;i < this.dataList.size();i++)
        {
            if(this.dataList.get(i).className == null)
                assert false;
            if(this.dataList.get(i).className.equals(className))
                return i;
        }
        return -1;
    }

    public void addMethod(int position,String className,String methodName)
    {
        if(!(className != null && className.contains("java.")))
            return;
        if(position != -1)
        {
            int p = -1;
            for(int i = 0;i < this.dataList.get(position).methodNames.size();i++)
            {
                if(this.dataList.get(position).methodNames.get(i).equals(methodName))
                {
                    p = i;
                    break;
                }
            }
            if(p != -1)
            {
                int before = this.dataList.get(position).methodUsages.get(p);
                this.dataList.get(position).methodUsages.set(p,before + 1);
            }
            else
            {
                this.dataList.get(position).methodNames.add(methodName);
                this.dataList.get(position).methodUsages.add(1);
            }
            this.dataList.get(position).amount++;
        }
        else
        {
            Data data = new Data();
            data.className = className;
            data.methodNames.add(methodName);
            data.methodUsages.add(1);
            data.amount = 1;
            this.dataList.add(data);
        }
    }

    public void display()
    {
        System.out.println("\n**********\n");
        System.out.println("路径: " + this.javaFileName);
        for(int i = 0;i < this.dataList.size();i++)
        {
            System.out.println("类: " + this.dataList.get(i).className + " , 共 " + this.dataList.get(i).amount + "次使用");
            for(int j = 0;j < this.dataList.get(i).methodNames.size();j++)
            {
                System.out.println("方法: " + this.dataList.get(i).methodNames.get(j) + " " + this.dataList.get(i).methodUsages.get(j) + " 次");
            }
        }
        System.out.println("\n**********\n");
    }
}
