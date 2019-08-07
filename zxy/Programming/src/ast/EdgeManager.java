package ast;

import java.util.ArrayList;
import java.util.List;

public class EdgeManager {
    private List<Edge> edges = new ArrayList<>();
    public void addEdge(String a,String b)
    {
        boolean result = true;
        for(int i = 0;i < this.edges.size();i++)
        {
            if(this.edges.get(i).nameA.equals(a) && this.edges.get(i).nameB.equals(b))
            {
                result = false;
                break;
            }
        }
        if(result)
        {
            Edge newEdge = new Edge(a,b);
            this.edges.add(newEdge);
        }
    }

    public boolean existA(String a)
    {
        for(int i = 0;i < this.edges.size();i++)
        {
            if(this.edges.get(i).nameA.equals(a) || this.edges.get(i).nameB.equals(a))
                return true;
        }
        return false;
    }

    public void addEdgeManager(EdgeManager edgeManager)
    {
        for(int i = 0;i < edgeManager.edges.size();i++)
        {
            String nameA = edgeManager.edges.get(i).nameA;
            String nameB = edgeManager.edges.get(i).nameB;
            boolean result = false;
            for(int j = 0;j < this.edges.size();j++)
            {
                if(this.edges.get(j).nameA.equals(nameA) && this.edges.get(j).nameB.equals(nameB))
                {
                    result = true;
                    break;
                }
            }
            if(!result)
                this.edges.add(edgeManager.edges.get(i));
        }
    }

    public void display()
    {
        for(int i = 0;i < this.edges.size();i++)
        {
            System.out.println("[" + this.edges.get(i).nameA + ',' + this.edges.get(i).nameB + "]");
        }
    }
}
