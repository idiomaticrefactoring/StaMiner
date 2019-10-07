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
    public List<String> variableList;

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

    public void sequenceBuilding(int k)
    {
        for(int i = 0;i < k;i++)
        {
            List<String> result = new ArrayList<>();
            for(int j = 0;j < this.edgeManager.edges.size();j++)
            {
                if(this.edgeManager.edges.get(j).A.nodeType == false)
                {
                    ActionNode actionNode = (ActionNode)this.edgeManager.edges.get(j).A;
                    if(actionNode.className != null && actionNode.className.equals(variableList.get(i)))
                    {
                        String displayString;
                        displayString = actionNode.className + '.' + actionNode.calleeName;
                        result.add(displayString);
                    }
                }
                if(this.edgeManager.edges.get(j).B.nodeType == false)
                {
                    ActionNode actionNode = (ActionNode)this.edgeManager.edges.get(j).B;
                    if(actionNode.className != null && actionNode.className.equals(variableList.get(i)))
                    {
                        String displayString;
                        displayString = actionNode.className + '.' + actionNode.calleeName;
                        result.add(displayString);
                    }
                }
            }
            for(int j = 0;j < result.size();j++)
            {
                System.out.print(result.get(j) + ' ');
            }
            System.out.println(',');
        }
    }

    public void usageExtracting(int k)
    {
        initVariableSet();
        //集合A为variableSet的前k个元素
        List<String> A = new ArrayList<>(variableSet);
        this.variableList = A;
        List<ActionNode> K = new ArrayList<>();
        if(k > A.size())
        {
            k = A.size();
            //return;
        }
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
            if(!edgeManager.existA(K.get(i)))
            {
                System.out.println("find in");
                edgeManager.addEdgeManager(Find(K.get(i),A));
                System.out.println("find out");
            }
        }

        edgeManager.display();
        sequenceBuilding(k);
    }

    private EdgeManager Find(Node a,List<String> A)
    {
        System.out.println("find begin");
        List<Node> B = Get(a,A);
        System.out.println(B);
        EdgeManager result = new EdgeManager();
        for(int i = 0;i < B.size();i++)
        {
            Node nameA = a;
            Node nameB = B.get(i);
            result.addEdge(nameA,nameB);
            /*
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
                Node nameA2;
                Node nameB2;
                nameA2 = nameB;
                nameB2 = this.groum.Nodes.get(MyVisitor.getGroumId(father,this.groum));
                if(bToC)
                {
                    result.addEdge(nameA2,nameB2);
                }
                else
                    result.addEdge(nameB2,nameA2);
            }
            */
            Node nameB2 = null;
            for(int j = 0;j < this.groum.Nodes.size();j++)
            {
                if(this.groum.Nodes.get(j).nodeType)
                {
                    ControlNode controlNode = (ControlNode)this.groum.Nodes.get(j);
                    for(int m = 0;m < controlNode.controlStructure.size();m++)
                    {
                        if(controlNode.controlStructure.get(m) == nameB)
                        {
                            nameB2 = controlNode;
                            break;
                        }
                    }
                }
                if(nameB2 != null)
                    break;
            }
            if(nameB2 != null)
            {
                boolean bToC = false;
                for(int j = 0;j < B.get(i).edges.size();j++)
                {
                    if(B.get(i).edges.get(j) == nameB2)
                    {
                        bToC = true;
                        break;
                    }
                }
                Node nameA2;
                nameA2 = nameB;
                if(bToC)
                {
                    result.addEdge(nameA2,nameB2);
                }
                else
                    result.addEdge(nameB2,nameA2);
            }
            result.addEdgeManager(Find(B.get(i),A));
        }

        System.out.println("find end");
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
