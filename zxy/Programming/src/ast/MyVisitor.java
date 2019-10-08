package ast;

import org.eclipse.jdt.core.dom.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyVisitor extends ASTVisitor {
    public Groum groum = new Groum();
    public JSONObject classJson = new JSONObject();

    public static String getNodeName(Node node)
    {
        if(node.nodeName != null)
            return node.nodeName;
        if(node.nodeType == false)
        {
            ActionNode actionNode = (ActionNode)node;
            return actionNode.objectName + actionNode.astNode.hashCode();
        }
        else
            return getControlName((ControlNode)node);
    }

    private static String getControlName(ControlNode controlNode)
    {
        String name = new String();
        switch (controlNode.controlNodeType)
        {
            case 0:name = "if";break;
            case 1:name = "switch";break;
            case 2:name = "while";break;
            case 3:name = "for";break;
            default:assert false;
        }
        name += controlNode.astNode.hashCode();
        return name;
    }

    public static Groum parallelMergeGroum(Groum groum,Groum anotherGroum)
    {
        Groum newGroum = new Groum();
        newGroum.Nodes.addAll(groum.Nodes);
        newGroum.Nodes.addAll(anotherGroum.Nodes);
        return newGroum;
    }

    public static Groum sequentialMergeGroum(Groum groum,Groum anotherGroum)
    {
        Groum newGroum = new Groum();
        newGroum.Nodes.addAll(groum.Nodes);
        List<Node> sourceNode = new ArrayList<>();
        for(int i = 0;i < anotherGroum.Nodes.size();i++)
        {
            if(isZeroInDegree(anotherGroum.Nodes.get(i),anotherGroum))
                sourceNode.add(anotherGroum.Nodes.get(i));
        }
        for(int i = 0;i < newGroum.Nodes.size();i++)
        {
            if(isZeroOutDegree(newGroum.Nodes.get(i)))
            {
                for(int j = 0;j < sourceNode.size();j++)
                {
                    newGroum.Nodes.get(i).edges.add(sourceNode.get(j));
                }
            }
        }
        newGroum.Nodes.addAll(anotherGroum.Nodes);
        return newGroum;
    }

    private static boolean isZeroInDegree(Node node,Groum groum)
    {
        for(int i = 0;i < groum.Nodes.size();i++)
        {
            for(int j = 0;j < groum.Nodes.get(i).edges.size();j++)
                if(groum.Nodes.get(i).edges.get(j) == node)
                    return false;
        }
        return true;
    }

    private static boolean isZeroOutDegree(Node node)
    {
        if(node.edges.isEmpty())
            return true;
        else
            return false;
    }

    private int getId(ASTNode node)
    {
        for(int i = 0;i < this.groum.Nodes.size();i++)
            if(this.groum.Nodes.get(i).astNode == node)
            {
                return i;
            }
        return -1;
    }

    public static int getGroumId(ASTNode node,Groum groum)
    {
        for(int i = 0;i < groum.Nodes.size();i++)
            if(groum.Nodes.get(i).astNode == node)
            {
                return i;
            }
        return -1;
    }

    private int getId(Node node)
    {
        for(int i = 0;i < this.groum.Nodes.size();i++)
        {
            if(this.groum.Nodes.get(i).nodeType == node.nodeType)
            {
                if((node.nodeType && (ControlNode)node == this.groum.Nodes.get(i)) || (!node.nodeType && (ActionNode)node == this.groum.Nodes.get(i)))
                    return i;
            }
        }
        return -1;
    }

    public static ASTNode getFatherNode(ASTNode node)
    {
        ASTNode parent = node.getParent();
        //判断是否在某个控制块中被解析过
        while(parent != null) {
            if (parent.getNodeType() == 61 ||
                    parent.getNodeType() == 19 ||
                    parent.getNodeType() == 70 ||
                    parent.getNodeType() == 24 ||
                    parent.getNodeType() == 25 ||
                    parent.getNodeType() == 49
            ) {
                break;
            }
            parent = parent.getParent();
        }
        return parent;
    }

    private boolean checkNode2(ASTNode node)
    {
        ASTNode parent = node.getParent();
        //判断是否为import或package语句的子语句
        while(parent != null)
        {
            if (parent.getNodeType() == 26 || parent.getNodeType() == 35)
                return false;
            parent = parent.getParent();
        }
        return true;
    }

    private static void addAllObjects(Set<String> involvedObjects,Groum groum)
    {
        if(groum == null)
            return;
        for(int i = 0;i < groum.Nodes.size();i++)
            involvedObjects.addAll(groum.Nodes.get(i).involvedObjects);
    }

    @Override
    public boolean visit(DoStatement node)
    {
        //将do-while 转化为 while
        //解析第一个statement
        MyVisitor newMyVisitor = new MyVisitor();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor);
        Groum statementGroum = newMyVisitor.groum;

        //解析expression
        MyVisitor newMyVisitor2 = new MyVisitor();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor2);
        Groum expressionGroum = newMyVisitor2.groum;

        //创建WHILE控制节点
        ControlNode doNode = new ControlNode();
        doNode.controlNodeType = 2;
            //第二遍扫描再填充controlStructure
        doNode.astNode = node;
        Groum doGroum = new Groum();
        //doGroum.Nodes.add(doNode);

        //解析第二个statement
        MyVisitor newMyVisitor3 = new MyVisitor();
        Statement statement2 = node.getBody();
        statement2.accept(newMyVisitor3);
        Groum statementGroum2 = newMyVisitor3.groum;

        //数据流分析
        addAllObjects(doNode.involvedObjects,statementGroum);
        addAllObjects(doNode.involvedObjects,expressionGroum);
        addAllObjects(doNode.involvedObjects,statementGroum2);
        doGroum.Nodes.add(doNode);

        //合并
        Groum finalGroum;
        finalGroum = MyVisitor.sequentialMergeGroum(statementGroum,expressionGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,doGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,statementGroum2);

        //添加到主groum上
        this.groum = MyVisitor.sequentialMergeGroum(this.groum, finalGroum);

        return false;
    }

    @Override
    public boolean visit(MethodDeclaration node)
    {
        //获得groum
        MyVisitor newMyvisitor = new MyVisitor();
        if(node.getBody() != null)
        {
            node.getBody().accept(newMyvisitor);
            newMyvisitor.secondScan();
        }
        Groum result = newMyvisitor.groum;

        //后续操作
        if(!result.isValid())
            assert false;

        JSONObject methodJson = new JSONObject();
        JSONArray edgesJson = new JSONArray();
        for(int i = 0;i < result.Nodes.size();i++)
        {
            Node tempNode = result.Nodes.get(i);
            for(int j = 0;j < tempNode.edges.size();j++)
            {
                JSONArray singleJson = new JSONArray();
                singleJson.add(getNodeName(tempNode));
                singleJson.add(getNodeName(tempNode.edges.get(j)));
                edgesJson.add(singleJson);
            }
        }
        methodJson.put("edges",edgesJson);

        JSONArray vertxsJson = new JSONArray();
        for(int i = 0;i < result.Nodes.size();i++)
        {
            JSONArray singleJson = new JSONArray();
            singleJson.add(getNodeName(result.Nodes.get(i)));
            if(result.Nodes.get(i).nodeType)
            {
                ControlNode controlNode = (ControlNode)result.Nodes.get(i);
                String name;
                switch (controlNode.controlNodeType)
                {
                    case 0:name = "IF";break;
                    case 1:name = "SWITCH";break;
                    case 2:name = "WHILE";break;
                    case 3:name = "FOR";break;
                    default:name = "null";assert false;
                }
                singleJson.add(name);
                singleJson.add(true);
            }
            else
            {
                ActionNode actionNode = (ActionNode)result.Nodes.get(i);
                String name = "";
                if(actionNode.className == null)
                    name += "null";
                else
                    name += actionNode.className;
                name += '.';
                if(actionNode.calleeName == null)
                    name += "null";
                else
                    name += actionNode.calleeName;
                singleJson.add(name);
                singleJson.add(false);
            }
            vertxsJson.add(singleJson);
        }
        methodJson.put("vertxs",vertxsJson);
        this.classJson.put(node.getName(),methodJson);

        //获得api sequence


        return false;
    }


    @Override
    public boolean visit(MethodInvocation node)
    {
        List<Groum> results = new ArrayList<>();
        //解析参数
        for(int i = 0;i < node.arguments().size();i++)
        {
            MyVisitor newMyVisitor = new MyVisitor();
            ((ASTNode)node.arguments().get(i)).accept(newMyVisitor);
            results.add(newMyVisitor.groum);
        }
        //合并groum
        for(int i = 1;i < results.size();i++)
        {
            results.set(0,MyVisitor.parallelMergeGroum(results.get(0),results.get(i)));
        }
        ActionNode actionNode = new ActionNode();
        actionNode.actionNodeType = 1;
        actionNode.calleeName = node.getName().toString();
        boolean cascadingCall = false;
        if(node.getExpression() == null)
        {
            actionNode.objectName = null;
            actionNode.className = null;
        }
        else {
            if(node.getExpression().getNodeType() == 32)
                cascadingCall = true;
            actionNode.objectName = node.getExpression().toString();
            actionNode.involvedObjects.add(actionNode.objectName);
            if(node.getExpression().resolveTypeBinding() == null)
                actionNode.className = null;
            else
            {
                actionNode.className = node.getExpression().resolveTypeBinding().getQualifiedName();
            }
        }
        actionNode.fatherControlNode = getFatherNode(node);
        actionNode.astNode = node;

        Groum methodGroum = new Groum();

        //数据流分析
        if(results.size() != 0) {
            addAllObjects(actionNode.involvedObjects, results.get(0));
        }

        //判断是否是java api
        //if(actionNode.className != null && actionNode.className.contains("java."))
            methodGroum.Nodes.add(actionNode);

        //合并
        if(results.size() != 0) {
            results.set(0, MyVisitor.sequentialMergeGroum(results.get(0), methodGroum));
            methodGroum = results.get(0);
        }

        //级联调用
        if(cascadingCall)
        {
            MyVisitor newMyVisitor = new MyVisitor();
            node.getExpression().accept(newMyVisitor);
            methodGroum = MyVisitor.sequentialMergeGroum(newMyVisitor.groum,methodGroum);
        }

        //添加到主groum上
        this.groum = MyVisitor.sequentialMergeGroum(this.groum, methodGroum);

        return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation node)
    {
        List<Groum> results = new ArrayList<>();
        //解析参数
        for(int i = 0;i < node.arguments().size();i++)
        {
            MyVisitor newMyVisitor = new MyVisitor();
            ((ASTNode)node.arguments().get(i)).accept(newMyVisitor);
            results.add(newMyVisitor.groum);
        }
        //合并groum
        for(int i = 1;i < results.size();i++)
        {
            results.set(0,MyVisitor.parallelMergeGroum(results.get(0),results.get(i)));
        }
        ActionNode actionNode = new ActionNode();
        actionNode.actionNodeType = 0;
        actionNode.calleeName = new String("INIT");
        if(node.getParent().getNodeType() == 59)
        {
            VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)node.getParent();
            actionNode.objectName = variableDeclarationFragment.getName().toString();
            actionNode.involvedObjects.add(actionNode.objectName);
        }
        else if(node.getParent().getNodeType() == 7)
        {
            Assignment assignment = (Assignment)node.getParent();
            actionNode.objectName = assignment.getLeftHandSide().toString();
            actionNode.involvedObjects.add(actionNode.objectName);
        }
        else
            actionNode.objectName = null;
        actionNode.className = node.getType().toString();
        actionNode.fatherControlNode = getFatherNode(node);
        actionNode.astNode = node;

        Groum classGroum = new Groum();

        //判断是否是java api
        //if(actionNode.className != null && actionNode.className.contains("java."))
            classGroum.Nodes.add(actionNode);

        //合并
        if(results.size() != 0) {
            results.set(0, MyVisitor.sequentialMergeGroum(results.get(0), classGroum));
            this.groum = MyVisitor.sequentialMergeGroum(this.groum, results.get(0));
        }
        else
        {
            this.groum = MyVisitor.sequentialMergeGroum(this.groum, classGroum);
        }

        return false;
    }

    @Override
    public boolean visit(EnhancedForStatement node)
    {
        //解析expression
        MyVisitor newMyVisitor = new MyVisitor();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor);
        Groum expressionGroum = newMyVisitor.groum;

        //创建FOR控制节点
        ControlNode enForNode = new ControlNode();
        enForNode.controlNodeType = 3;
            //第二遍扫描再填充controlStructure
        enForNode.astNode = node;
        Groum enGroum = new Groum();
        //enGroum.Nodes.add(enForNode);

        //解析Statement
        MyVisitor newMyVisitor2 = new MyVisitor();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor2);
        Groum statementGroum = newMyVisitor2.groum;

        //数据流分析
        addAllObjects(enForNode.involvedObjects,expressionGroum);
        addAllObjects(enForNode.involvedObjects,statementGroum);
        enGroum.Nodes.add(enForNode);

        //合并
        Groum finalGroum;
        finalGroum = MyVisitor.sequentialMergeGroum(expressionGroum,enGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,statementGroum);

        //添加到主groum上
        this.groum = MyVisitor.sequentialMergeGroum(this.groum, finalGroum);

        return false;
    }



    @Override
    public boolean visit(FieldAccess node)
    {
        if(checkNode2(node))
        {
            ActionNode actionNode = new ActionNode();
            actionNode.actionNodeType = 2;
            actionNode.calleeName = node.getName().toString();
            actionNode.objectName = node.getExpression().toString();
            actionNode.involvedObjects.add(actionNode.objectName);
            if(node.getExpression().resolveTypeBinding() != null)
                actionNode.className = node.getExpression().resolveTypeBinding().getQualifiedName();
            actionNode.fatherControlNode = getFatherNode(node);
            actionNode.astNode = node;
            Groum actionGroum = new Groum();
            actionGroum.Nodes.add(actionNode);

            //添加到主groum上
            this.groum = MyVisitor.sequentialMergeGroum(this.groum, actionGroum);
        }
        return true;
    }

    @Override
    public boolean visit(QualifiedName node)
    {
        if(checkNode2(node))
        {
            ActionNode actionNode = new ActionNode();
            actionNode.actionNodeType = 2;
            actionNode.calleeName = node.getName().toString();
            actionNode.objectName = node.getQualifier().toString();
            actionNode.involvedObjects.add(actionNode.objectName);
            actionNode.className = null;
            actionNode.fatherControlNode = getFatherNode(node);
            actionNode.astNode = node;
            Groum actionGroum = new Groum();
            actionGroum.Nodes.add(actionNode);

            //添加到主groum上
            this.groum = MyVisitor.sequentialMergeGroum(this.groum, actionGroum);
        }
        return true;
    }




    @Override
    public boolean visit(ForStatement node)
    {
        //解析initializers
        Groum initGroum = new Groum();
        List initializers = node.initializers();
        for(int i = 0;i < initializers.size();i++)
        {
            MyVisitor newMyVisitor = new MyVisitor();
            ((ASTNode)initializers.get(i)).accept(newMyVisitor);
            initGroum = MyVisitor.sequentialMergeGroum(initGroum,newMyVisitor.groum);
        }

        //解析expression
        MyVisitor newMyVisitor = new MyVisitor();
        Expression expression = node.getExpression();
        if(expression != null)
            expression.accept(newMyVisitor);
        Groum expressionGroum = newMyVisitor.groum;

        //创建FOR控制节点
        ControlNode forNode = new ControlNode();
        forNode.controlNodeType = 3;
            //第二遍扫描再填充controlStructure
        forNode.astNode = node;
        Groum forGroum = new Groum();
        //forGroum.Nodes.add(forNode);

        //解析statement
        MyVisitor newMyVisitor2 = new MyVisitor();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor2);
        Groum statementGroum = newMyVisitor2.groum;

        //解析updaters
        Groum updatersGroum = new Groum();
        List updaters = node.updaters();
        for(int i = 0;i < updaters.size();i++)
        {
            MyVisitor newMyVisitor3 = new MyVisitor();
            ((ASTNode)updaters.get(i)).accept(newMyVisitor3);
            updatersGroum = MyVisitor.sequentialMergeGroum(updatersGroum,newMyVisitor3.groum);
        }

        //数据流分析
        addAllObjects(forNode.involvedObjects,initGroum);
        addAllObjects(forNode.involvedObjects,expressionGroum);
        addAllObjects(forNode.involvedObjects,statementGroum);
        addAllObjects(forNode.involvedObjects,updatersGroum);
        forGroum.Nodes.add(forNode);

        //合并
        Groum finalGroum;
        finalGroum = MyVisitor.sequentialMergeGroum(initGroum,expressionGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,forGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,statementGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,updatersGroum);

        //添加到主groum上
        this.groum = MyVisitor.sequentialMergeGroum(this.groum, finalGroum);
        return false;
    }

    @Override
    public boolean visit(IfStatement node)
    {
        //解析expression
        Expression expression = node.getExpression();
        MyVisitor newMyVisitor = new MyVisitor();
        expression.accept(newMyVisitor);
        Groum expressionGroum = newMyVisitor.groum;

        //创建IF控制节点
        ControlNode ifNode = new ControlNode();
        ifNode.controlNodeType = 0;
            //第二遍扫描再填充controlStructure
        ifNode.astNode = node;
        Groum ifGroum = new Groum();
        //ifGroum.Nodes.add(ifNode);

        //解析thenStatement
        MyVisitor newMyVisitor2 = new MyVisitor();
        Statement thenStatement = node.getThenStatement();
        thenStatement.accept(newMyVisitor2);
        Groum thenGroum = newMyVisitor2.groum;

        //解析elseStatement
        MyVisitor newMyVisitor3 = new MyVisitor();
        Statement elseStatement = node.getElseStatement();
        if(elseStatement != null)
            elseStatement.accept(newMyVisitor3);
        Groum elseGroum = newMyVisitor3.groum;

        //数据流分析
        addAllObjects(ifNode.involvedObjects,expressionGroum);
        addAllObjects(ifNode.involvedObjects,thenGroum);
        addAllObjects(ifNode.involvedObjects,elseGroum);
        ifGroum.Nodes.add(ifNode);

        //合并
        Groum finalGroum;
        finalGroum = MyVisitor.sequentialMergeGroum(expressionGroum,ifGroum);
        Groum tempGroum = MyVisitor.parallelMergeGroum(thenGroum,elseGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,tempGroum);

        //添加到主groum上
        this.groum = MyVisitor.sequentialMergeGroum(this.groum, finalGroum);
        return false;
    }

    @Override
    public boolean visit(SwitchStatement node)
    {
        //解析expression
        MyVisitor newMyVisitor = new MyVisitor();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor);
        Groum expressionGroum = newMyVisitor.groum;

        //创建SWITCH控制节点
        ControlNode switchNode = new ControlNode();
        switchNode.controlNodeType = 1;
            //第二遍扫描再填充controlStructure
        switchNode.astNode = node;
        Groum switchGroum = new Groum();
        //switchGroum.Nodes.add(switchNode);

        //解析statements
        Groum statementsGroum = new Groum();
        List Statements = node.statements();
        for(int i = 0;i < Statements.size();i++) {
            MyVisitor newMyVisitor2 = new MyVisitor();
            ((ASTNode) Statements.get(i)).accept(newMyVisitor2);
            statementsGroum = MyVisitor.parallelMergeGroum(statementsGroum,newMyVisitor2.groum);
        }

        //数据流分析
        addAllObjects(switchNode.involvedObjects,expressionGroum);
        addAllObjects(switchNode.involvedObjects,statementsGroum);
        switchGroum.Nodes.add(switchNode);

        //合并
        Groum finalGroum;
        finalGroum = MyVisitor.sequentialMergeGroum(expressionGroum,switchGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,statementsGroum);

        //添加到主groum上
        this.groum = MyVisitor.sequentialMergeGroum(this.groum, finalGroum);
        return false;
    }


    @Override
    public boolean visit(WhileStatement node)
    {
        //解析expression部分
        MyVisitor newMyVisitor = new MyVisitor();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor);
        Groum expressionGroum = newMyVisitor.groum;

        //插入while节点
        ControlNode whileNode = new ControlNode();
        whileNode.controlNodeType = 2;
            //第二遍扫描再填充controlStructure
        whileNode.astNode = node;
        Groum whileGroum = new Groum();
        //whileGroum.Nodes.add(whileNode);

        //解析statement部分
        MyVisitor newMyVisitor2 = new MyVisitor();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor2);
        Groum statementGroum = newMyVisitor2.groum;

        //数据流分析
        addAllObjects(whileNode.involvedObjects,expressionGroum);
        addAllObjects(whileNode.involvedObjects,statementGroum);
        whileGroum.Nodes.add(whileNode);

        //合并
        Groum finalGroum;
        finalGroum = MyVisitor.sequentialMergeGroum(expressionGroum,whileGroum);
        finalGroum = MyVisitor.sequentialMergeGroum(finalGroum,statementGroum);

        //添加到主groum上
        this.groum = MyVisitor.sequentialMergeGroum(this.groum, finalGroum);
        return false;
    }


    @Override
    public boolean visit(Assignment node)
    {
        MyVisitor myVisitor1 = new MyVisitor();
        Expression rightExpression = node.getRightHandSide();
        rightExpression.accept(myVisitor1);

        MyVisitor myVisitor2 = new MyVisitor();
        Expression leftExpression = node.getLeftHandSide();
        leftExpression.accept(myVisitor2);

        //数据流分析
        if(leftExpression.getNodeType() == 22 || leftExpression.getNodeType() == 40)
        {
            if(myVisitor2.groum.Nodes.size() == 0)
                assert false;
            addAllObjects(myVisitor2.groum.Nodes.get(0).involvedObjects,myVisitor1.groum);
        }

        //添加到主groum上
        Groum finalGroum;
        finalGroum = MyVisitor.sequentialMergeGroum(myVisitor1.groum,myVisitor2.groum);

        this.groum = MyVisitor.sequentialMergeGroum(this.groum, finalGroum);

        return false;
    }



    public void displayGroum()
    {
        int javaAPI = 0;
        System.out.println("**********\n");
        for(int i = 0;i < this.groum.Nodes.size();i++)
        {
            if(this.groum.Nodes.get(i).nodeType)
            {
                System.out.println("ID: " + i);
                System.out.println("Groum类型: 控制节点");
                System.out.print("节点类型: ");
                ControlNode controlNode = (ControlNode)this.groum.Nodes.get(i);
                switch (controlNode.controlNodeType)
                {
                    case 0:System.out.println("if");break;
                    case 1:System.out.println("switch");break;
                    case 2:System.out.println("while");break;
                    case 3:System.out.println("for");break;
                    default:assert false;break;
                }
                //输出控制框架内的节点号
                System.out.println("控制框架内节点: ");
                for(int j = 0;j < controlNode.controlStructure.size();j++)
                {
                    //System.out.print(getId(controlNode.controlStructure.get(j).astNode) + ", ");
                    System.out.print(getId(controlNode.controlStructure.get(j)) + ", ");
                }
            }
            else
            {
                System.out.println("ID: " + i);
                System.out.println("Groum类型: 行为节点");
                System.out.print("节点类型: ");
                ActionNode actionNode = (ActionNode)this.groum.Nodes.get(i);
                switch (actionNode.actionNodeType)
                {
                    case 0:System.out.println("constructor");break;
                    case 1:System.out.println("method");break;
                    case 2:System.out.println("field");break;
                    default:assert false;break;
                }
                System.out.println("内容: " + actionNode.className + '.' + actionNode.calleeName);
                System.out.println("绑定对象: " + actionNode.objectName);

                if(actionNode.className != null && actionNode.className.contains("java."))
                    javaAPI++;
            }

            //打印边
            System.out.println("边: ");
            for(int j = 0;j < this.groum.Nodes.get(i).edges.size();j++)
                System.out.println(i + " -> " + getId(this.groum.Nodes.get(i).edges.get(j)));

            //打印变量
            System.out.println("关联变量: ");
            System.out.println(this.groum.Nodes.get(i).involvedObjects);

            System.out.println("\n**********\n");
        }

        System.out.println("共有" + javaAPI + "个JAVA API");
    }

    public Statistic getGroumStatistic(String path)
    {
        Statistic statistic = new Statistic();
        statistic.javaFileName = path;
        for(int i = 0;i < this.groum.Nodes.size();i++)
        {
            if(!this.groum.Nodes.get(i).nodeType)
            {
                ActionNode actionNode = (ActionNode)this.groum.Nodes.get(i);
                int position = statistic.existClass(actionNode.className);
                if(position != -2)
                {
                    statistic.addMethod(position,actionNode.className,actionNode.calleeName);
                }
            }
        }
        return statistic;
    }

    public void secondScan()
    {
        //第二遍扫描填充控制流框架
        for(int i = 0;i < this.groum.Nodes.size();i++)
        {
            if(!this.groum.Nodes.get(i).nodeType)
            {
                ActionNode actionNode = (ActionNode)this.groum.Nodes.get(i);
                int id = getId(actionNode.fatherControlNode);
                if(id == -1)
                    continue;
                ControlNode controlNode = (ControlNode)this.groum.Nodes.get(id);
                controlNode.controlStructure.add(actionNode);
            }
            for(int j = i+1;j < this.groum.Nodes.size();j++)
            {
                Set<String> result = new HashSet<>();
                result.addAll(this.groum.Nodes.get(i).involvedObjects);
                result.retainAll(this.groum.Nodes.get(j).involvedObjects);
                if(result.size() != 0)
                    this.groum.Nodes.get(i).edges.add(this.groum.Nodes.get(j));
            }
        }
    }
}
