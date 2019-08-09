package ast;

import java.util.ArrayList;
import java.util.List;

public class EdgeManager {
    public List<Edge> edges = new ArrayList<>();
    public void addEdge(Node a,Node b)
    {
        boolean result = true;
        for(int i = 0;i < this.edges.size();i++)
        {
            if(this.edges.get(i).A == a && this.edges.get(i).B == b)
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

    public boolean existA(Node a)
    {
        for(int i = 0;i < this.edges.size();i++)
        {
            if(this.edges.get(i).A == a || this.edges.get(i).B == a)
                return true;
        }
        return false;
    }

    public void addEdgeManager(EdgeManager edgeManager)
    {
        for(int i = 0;i < edgeManager.edges.size();i++)
        {
            Node nameA = edgeManager.edges.get(i).A;
            Node nameB = edgeManager.edges.get(i).B;
            boolean result = false;
            for(int j = 0;j < this.edges.size();j++)
            {
                if(this.edges.get(j).A == nameA && this.edges.get(j).B == nameB)
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
            System.out.println("[" + MyVisitor.getNodeName(this.edges.get(i).A) + ',' + MyVisitor.getNodeName(this.edges.get(i).B) + "]");
        }
    }
}
