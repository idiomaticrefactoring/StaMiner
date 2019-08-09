package ast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyScanner {
    private JSONParser jsonParser = new JSONParser();
    public Groum groum = new Groum();
    public String className;
    public String functionName;

    public void Scan(String location)
    {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(new File(location)));
            Iterator iterator = jsonObject.keySet().iterator();
            className = (String)iterator.next();
            JSONObject funcJsonObject = (JSONObject)jsonObject.get(className);
            iterator = funcJsonObject.keySet().iterator();
            functionName = (String)iterator.next();
            JSONObject groumJsonObject = (JSONObject)funcJsonObject.get(functionName);
            iterator = groumJsonObject.keySet().iterator();
            JSONObject edgesJsonObject = (JSONObject) groumJsonObject.get((String)iterator.next());
            JSONArray vertxsJsonArray = (JSONArray) groumJsonObject.get((String)iterator.next());

            //先建立节点的列表
            Iterator vertxsIterator = vertxsJsonArray.iterator();
            List<JSONArray> edgesArrayIn = new ArrayList<>();
            List<String> nameIn = new ArrayList<>();
            List<JSONArray> edgesArrayOut = new ArrayList<>();
            List<String> nameOut = new ArrayList<>();
            while(vertxsIterator.hasNext())
            {
                JSONArray singleJsonArray = (JSONArray) vertxsIterator.next();
                Iterator singleIterator = singleJsonArray.iterator();
                String nodeName = (String)singleIterator.next();
                String nodeClassName = (String)singleIterator.next();
                String nodeMethodName = (String)singleIterator.next();
                boolean nodeType = (boolean)singleIterator.next();//false Action; true Control
                JSONArray nodeEdgeJsonArrayIn = (JSONArray) singleIterator.next();
                JSONArray nodeEdgeJsonArrayOut = (JSONArray) singleIterator.next();

                //处理
                if(nodeEdgeJsonArrayIn != null)
                {
                    nameIn.add(nodeName);
                    edgesArrayIn.add(nodeEdgeJsonArrayIn);
                }
                if(nodeEdgeJsonArrayOut != null)
                {
                    nameOut.add(nodeName);
                    edgesArrayOut.add(nodeEdgeJsonArrayOut);
                }
                if(nodeType)
                {
                    //控制节点
                    ControlNode controlNode = new ControlNode();
                    controlNode.nodeName = nodeName;
                    controlNode.nodeType = true;
                    switch (nodeClassName)
                    {
                        case "IF":controlNode.controlNodeType = 0;break;
                        case "SWITCH":controlNode.controlNodeType = 1;break;
                        case "WHILE":controlNode.controlNodeType = 2;break;
                        case "For":controlNode.controlNodeType = 3;break;
                        default:assert false;
                    }
                    this.groum.Nodes.add(controlNode);
                }
                else
                {
                    //行为节点
                    ActionNode actionNode = new ActionNode();
                    actionNode.nodeName = nodeName;
                    actionNode.nodeType = false;
                    actionNode.className = nodeClassName;
                    actionNode.calleeName = nodeMethodName;
                    this.groum.Nodes.add(actionNode);
                }
            }

            //添加第一部分的边
            Iterator edgeIterator = edgesJsonObject.keySet().iterator();
            while(edgeIterator.hasNext())
            {
                String nameA = (String)edgeIterator.next();
                String nameB = (String)edgesJsonObject.get(nameA);

                Node nodeA = null;
                Node nodeB = null;
                for(int i = 0;i < this.groum.Nodes.size();i++)
                {
                    if(this.groum.Nodes.get(i).nodeName.equals(nameA))
                    {
                        nodeA = this.groum.Nodes.get(i);
                    }
                    else if(this.groum.Nodes.get(i).nodeName.equals(nameB))
                    {
                        nodeB = this.groum.Nodes.get(i);
                    }
                }
                if(nodeA == null || nodeB == null)
                {
                    assert false;
                }
                nodeA.edges.add(nodeB);
            }

            //添加第二部分的边
            for(int i = 0;i < edgesArrayIn.size();i++)
            {
                String nameB = nameIn.get(i);
                String nameA = null;
                JSONArray jsonArray = edgesArrayIn.get(i);
                Iterator iterator1 = jsonArray.iterator();
                while(iterator1.hasNext())
                {
                    nameA = (String)iterator1.next();
                    Node nodeA = null;
                    Node nodeB = null;
                    for(int j = 0;j < this.groum.Nodes.size();j++)
                    {
                        if(this.groum.Nodes.get(j).nodeName.equals(nameA))
                        {
                            nodeA = this.groum.Nodes.get(j);
                        }
                        else if(this.groum.Nodes.get(j).nodeName.equals(nameB))
                        {
                            nodeB = this.groum.Nodes.get(j);
                        }
                    }
                    if(nodeA == null || nodeB == null)
                    {
                        assert false;
                    }
                    nodeA.edges.add(nodeB);

                    //添加控制结构
                    ControlNode controlNode = (ControlNode)nodeB;
                    boolean exist = false;
                    for(int j = 0;j < controlNode.controlStructure.size();j++)
                    {
                        if(controlNode.controlStructure.get(j) == nodeA)
                        {
                            exist = true;
                            break;
                        }
                    }
                    if(!exist)
                    {
                        controlNode.controlStructure.add(nodeA);
                    }
                }
            }

            for(int i = 0;i < edgesArrayOut.size();i++)
            {
                String nameA = nameOut.get(i);
                String nameB = null;
                JSONArray jsonArray = edgesArrayOut.get(i);
                Iterator iterator1 = jsonArray.iterator();
                while(iterator1.hasNext())
                {
                    nameB = (String)iterator1.next();
                    Node nodeA = null;
                    Node nodeB = null;
                    for(int j = 0;j < this.groum.Nodes.size();j++)
                    {
                        if(this.groum.Nodes.get(j).nodeName.equals(nameA))
                        {
                            nodeA = this.groum.Nodes.get(j);
                        }
                        else if(this.groum.Nodes.get(j).nodeName.equals(nameB))
                        {
                            nodeB = this.groum.Nodes.get(j);
                        }
                    }
                    if(nodeA == null || nodeB == null)
                    {
                        assert false;
                    }
                    nodeA.edges.add(nodeB);

                    //添加控制结构
                    ControlNode controlNode = (ControlNode)nodeA;
                    boolean exist = false;
                    for(int j = 0;j < controlNode.controlStructure.size();j++)
                    {
                        if(controlNode.controlStructure.get(j) == nodeB)
                        {
                            exist = true;
                            break;
                        }
                    }
                    if(!exist)
                    {
                        controlNode.controlStructure.add(nodeB);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
