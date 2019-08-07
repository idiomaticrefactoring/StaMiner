package ast;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsageExtracting {
    public Groum groum;
    public EdgeManager edgeManager = new EdgeManager();
    public Set<String> variableSet = new HashSet<>();

    public UsageExtracting(Groum groum)
    {
        this.groum = groum;         //不能对groum进行修改
    }

    private void initVariableSet()
    {
        for(int i = 0;i < groum.Nodes.size();i++)
        {
            if(groum.Nodes.get(i).nodeType == false)
            {
                ActionNode actionNode = (ActionNode)groum.Nodes.get(i);
                if(actionNode.className != null)
                    variableSet.add(actionNode.className);
            }
        }
    }

    public void usageExtracting(int k)
    {
        initVariableSet();
        //集合A为variableSet的前k个元素
        List<String> A = new ArrayList<>(variableSet);
        List<ActionNode> K = new ArrayList<>();
        for(int i = 0;i < k;i++)
        {
            for(int j = 0;j < groum.Nodes.size();j++)
            {
                if(groum.Nodes.get(j).nodeType == false)
                {
                    ActionNode actionNode = (ActionNode)groum.Nodes.get(j);
                    if(actionNode.className != null && actionNode.className.equals(A.get(i)))
                    {
                        K.add(actionNode);
                        break;
                    }
                }
            }
        }
        for(int i = 0;i < K.size();i++)
        {
            if(!edgeManager.existA(K.get(i).objectName))
            {
                edgeManager.addEdgeManager(Find(K.get(i),A));
            }
        }
        edgeManager.display();
    }

    private EdgeManager Find(Node a,List<String> A)
    {
        List<Node> B = Get(a,A);
        EdgeManager result = new EdgeManager();
        for(int i = 0;i < B.size();i++)
        {
            String nameA = MyVisitor.getNodeName(a);
            String nameB = MyVisitor.getNodeName(B.get(i));
            result.addEdge(nameA,nameB);
            ASTNode father = MyVisitor.getFatherNode(B.get(i).astNode);
            if(father != null)
            {
                boolean bToC = false;
                for(int j = 0;j < B.get(i).edges.size();j++)
                {
                    if(B.get(i).edges.get(j).astNode == father)
                    {
                        bToC = true;
                        break;
                    }
                }
                String nameA2;
                String nameB2;
                nameA2 = nameB;
                nameB2 = MyVisitor.getNodeName( this.groum.Nodes.get(MyVisitor.getGroumId(father,this.groum)));
                if(bToC)
                {
                    result.addEdge(nameA2,nameB2);
                }
                else
                    result.addEdge(nameB2,nameA2);
            }
            result.addEdgeManager(Find(B.get(i),A));
        }
        return result;
    }

    private List<Node> Get(Node a,List<String> A)
    {
        List<Node> C = new ArrayList<>();
        List<Node> B = a.edges;
        for(int i = 0;i < B.size();i++)
        {
            boolean result = false;
            if(B.get(i).nodeType == false)
            {
                ActionNode actionNode = (ActionNode)B.get(i);
                if(A.contains(actionNode.objectName))
                {
                    result = true;
                }
            }
            else
                result = true;
            if(result)
            {
                C.add(B.get(i));
            }
            else
            {
                List<Node> C2 = Get(B.get(i),A);
                for(int m = 0;m < C2.size();m++)
                {
                    boolean flag = false;
                    for(int n = 0;n < C.size();n++)
                    {
                        if(C.get(n) == C2.get(m))
                        {
                            flag = true;
                            break;
                        }
                    }
                    if(!flag)
                        C.add(C2.get(m));
                }
            }
        }
        return C;
    }
}
