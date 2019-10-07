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
        Node startNode = this.Nodes.get(0);
        Stack<Node> stack = new Stack<>();
        stack.push(startNode);
        while(!stack.empty())
        {
            Node outNode = stack.pop();
            for(int i = 0;i < outNode.edges.size();i++)
            {
                if(stack.contains(outNode.edges.get(i)))
                    return false;
                else
                {
                    stack.push(outNode.edges.get(i));
                }
            }
        }
        return true;
    }
}
