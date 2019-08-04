package ast;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class ControlNode extends Node {
    public int controlNodeType = -1;//-1 for undefined,0 for if,1 for switch,2 for while,3 for 'for'
    public List<Node> controlStructure = new ArrayList<>();

    ControlNode()
    {
        this.nodeType = true;
    }
}
