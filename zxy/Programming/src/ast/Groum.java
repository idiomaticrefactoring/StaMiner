package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Groum {
    public List<Node> Nodes = new ArrayList<>();

    public boolean isValid()
    {
        if(this.Nodes.size() == 1)
            return true;
        List<Integer> color = new ArrayList<>();
        for(int i = 0;i < this.Nodes.size();i++)
            color.add(0);   //white
        for(int i = 0;i < this.Nodes.size();i++)
        {
            if(color.get(i) == 0)
            {
                boolean result = DFS(i,color);
                if(!result)
                    return false;
            }
        }
        return true;
    }

    private boolean DFS(int startNode,List<Integer> color)
    {
        color.set(startNode,1);     //gray
        for(int i = 0;i < this.Nodes.get(startNode).edges.size();i++)
        {
            int neighbour = -1;
            for(int j = 0;j < this.Nodes.size();j++)
            {
                if(this.Nodes.get(j) == this.Nodes.get(startNode).edges.get(i))
                {
                    neighbour = j;
                    break;
                }
            }
            if(neighbour == -1)
                assert false;
            if(color.get(neighbour) == 0)
            {
                boolean result = DFS(neighbour,color);
                if(!result)
                    return false;
            }
            else if(color.get(neighbour) == 1)
            {
                //找到BE，成环
                return false;
            }
        }
        color.set(startNode,2);     //black
        return true;
    }
}
