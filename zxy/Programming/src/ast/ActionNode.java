package ast;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;

public class ActionNode extends Node {
    public int actionNodeType = -1;//-1 for undefined, 0 for constructor,1 for method,2 for field
    public String className;//this may be null
    public String calleeName;
    public String objectName;//this may be null
    public ASTNode fatherControlNode;//this may be null
    ActionNode()
    {
        this.nodeType = false;
    }
}
