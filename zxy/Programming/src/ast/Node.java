package ast;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
    public ASTNode astNode;
    public boolean nodeType;//false for action node,true for control node
    public List<Node> edges = new ArrayList<>();
    public Set<String> involvedObjects = new HashSet<>();
}
