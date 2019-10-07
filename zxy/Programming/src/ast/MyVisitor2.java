package ast;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class MyVisitor2 extends ASTVisitor {
    public ApiSequence apiSequence = new ApiSequence();

    private String getFatherMethod(ASTNode astNode)
    {
        ASTNode p = astNode;
        while(p != null && p.getNodeType() != 31)
        {
            p = p.getParent();
        }
        if(p != null)
        {
            MethodDeclaration methodDeclaration = (MethodDeclaration)p;
            return methodDeclaration.getName().toString();
        }
        return "null";
    }

    @Override
    public boolean visit(MethodInvocation node)
    {
        ApiSequence results = new ApiSequence();
        //解析参数
        for(int i = 0;i < node.arguments().size();i++)
        {
            MyVisitor2 newMyVisitor = new MyVisitor2();
            ((ASTNode)node.arguments().get(i)).accept(newMyVisitor);
            results.addAll(newMyVisitor.apiSequence);
        }
        //获取API节点信息
        ActionNode actionNode = new ActionNode();
        actionNode.actionNodeType = 1;
        actionNode.calleeName = node.getName().toString();
        boolean cascadingCall = false;
        if(node.getExpression() == null)
        {
            actionNode.objectName = "null";
            actionNode.className = "null";
        }
        else {
            if(node.getExpression().getNodeType() == 32)
                cascadingCall = true;
            actionNode.objectName = node.getExpression().toString();
            actionNode.involvedObjects.add(actionNode.objectName);
            if(node.getExpression().resolveTypeBinding() == null)
                actionNode.className = "null";
            else
            {
                actionNode.className = node.getExpression().resolveTypeBinding().getQualifiedName();
            }
        }
        //actionNode.fatherControlNode = getFatherNode(node);
        actionNode.astNode = node;
        //合并
        results.add(actionNode.className,actionNode.calleeName,getFatherMethod(node));
        //级联调用
        if(cascadingCall)
        {
            MyVisitor2 newMyVisitor = new MyVisitor2();
            node.getExpression().accept(newMyVisitor);
            //methodGroum = MyVisitor.sequentialMergeGroum(newMyVisitor.groum,methodGroum);
            ApiSequence apiSequenceTemp = new ApiSequence();
            apiSequenceTemp.addAll(newMyVisitor.apiSequence);
            apiSequenceTemp.addAll(results);
            results = apiSequenceTemp;
        }
        //添加到主序列上
        this.apiSequence.addAll(results);

        return false;
    }

    @Override
    public boolean visit(ClassInstanceCreation node)
    {
        ApiSequence results = new ApiSequence();
        //解析参数
        for(int i = 0;i < node.arguments().size();i++)
        {
            MyVisitor2 newMyVisitor = new MyVisitor2();
            ((ASTNode)node.arguments().get(i)).accept(newMyVisitor);
            results.addAll(newMyVisitor.apiSequence);
        }
        ActionNode actionNode = new ActionNode();
        actionNode.actionNodeType = 0;
        actionNode.calleeName = new String("NEW");
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
            actionNode.objectName = "null";
        actionNode.className = node.getType().toString();
        //actionNode.fatherControlNode = getFatherNode(node);
        actionNode.astNode = node;

        //Groum classGroum = new Groum();
        //classGroum.Nodes.add(actionNode);

        //合并
        results.add(actionNode.className,actionNode.calleeName,getFatherMethod(node));

        //添加到主序列上
        this.apiSequence.addAll(results);

        return false;
    }

    @Override
    public boolean visit(DoStatement node)
    {
        //将do-while 转化为 while
        //解析第一个statement
        MyVisitor2 newMyVisitor = new MyVisitor2();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor);
        ApiSequence statementAPI = newMyVisitor.apiSequence;

        //解析expression
        MyVisitor2 newMyVisitor2 = new MyVisitor2();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor2);
        ApiSequence expressionAPI = newMyVisitor2.apiSequence;

        //创建WHILE控制节点
        ControlNode doNode = new ControlNode();
        doNode.controlNodeType = 2;
        //第二遍扫描再填充controlStructure
        doNode.astNode = node;
        ApiSequence doAPI = new ApiSequence();
        //doGroum.Nodes.add(doNode);

        //解析第二个statement
        MyVisitor2 newMyVisitor3 = new MyVisitor2();
        Statement statement2 = node.getBody();
        statement2.accept(newMyVisitor3);
        ApiSequence statementAPI2 = newMyVisitor3.apiSequence;

        //合并
        ApiSequence finalAPI = new ApiSequence();
        finalAPI.addAll(expressionAPI);
        finalAPI.addAll(statementAPI);

        //添加到主groum上
        this.apiSequence.addAll(finalAPI);

        return false;
    }

    @Override
    public boolean visit(EnhancedForStatement node)
    {
        //解析expression
        MyVisitor2 newMyVisitor = new MyVisitor2();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor);
        ApiSequence expressionAPI = newMyVisitor.apiSequence;

        //创建FOR控制节点
        ControlNode enForNode = new ControlNode();
        enForNode.controlNodeType = 3;
        //第二遍扫描再填充controlStructure
        enForNode.astNode = node;
        //Groum enGroum = new Groum();
        //enGroum.Nodes.add(enForNode);

        //解析Statement
        MyVisitor2 newMyVisitor2 = new MyVisitor2();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor2);
        ApiSequence statementAPI = newMyVisitor2.apiSequence;

        //合并
        ApiSequence finalAPI = new ApiSequence();
        finalAPI.addAll(expressionAPI);
        finalAPI.addAll(statementAPI);

        //添加到主groum上
        this.apiSequence.addAll(finalAPI);

        return false;
    }

    @Override
    public boolean visit(ForStatement node)
    {
        //解析initializers
        ApiSequence initAPI = new ApiSequence();
        List initializers = node.initializers();
        for(int i = 0;i < initializers.size();i++)
        {
            MyVisitor2 newMyVisitor = new MyVisitor2();
            ((ASTNode)initializers.get(i)).accept(newMyVisitor);
            initAPI.addAll(newMyVisitor.apiSequence);
        }

        //解析expression
        MyVisitor2 newMyVisitor = new MyVisitor2();
        Expression expression = node.getExpression();
        if(expression != null)
            expression.accept(newMyVisitor);
        ApiSequence expressionAPI = newMyVisitor.apiSequence;

        //创建FOR控制节点
        ControlNode forNode = new ControlNode();
        forNode.controlNodeType = 3;
        //第二遍扫描再填充controlStructure
        forNode.astNode = node;
        //forGroum.Nodes.add(forNode);

        //解析statement
        MyVisitor2 newMyVisitor2 = new MyVisitor2();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor2);
        ApiSequence statementAPI = newMyVisitor2.apiSequence;

        //解析updaters
        ApiSequence results = new ApiSequence();
        List updaters = node.updaters();
        for(int i = 0;i < updaters.size();i++)
        {
            MyVisitor2 newMyVisitor3 = new MyVisitor2();
            ((ASTNode)updaters.get(i)).accept(newMyVisitor3);
            results.addAll(newMyVisitor3.apiSequence);
        }

        //合并
        ApiSequence finalAPI = new ApiSequence();
        finalAPI.addAll(initAPI);
        finalAPI.addAll(expressionAPI);
        finalAPI.addAll(statementAPI);
        finalAPI.addAll(results);

        //添加到主groum上
        this.apiSequence.addAll(finalAPI);

        return false;
    }

    @Override
    public boolean visit(IfStatement node)
    {
        //解析expression
        Expression expression = node.getExpression();
        MyVisitor2 newMyVisitor = new MyVisitor2();
        expression.accept(newMyVisitor);
        ApiSequence expressionAPI = newMyVisitor.apiSequence;

        //创建IF控制节点
        ControlNode ifNode = new ControlNode();
        ifNode.controlNodeType = 0;
        //第二遍扫描再填充controlStructure
        ifNode.astNode = node;
        Groum ifGroum = new Groum();
        //ifGroum.Nodes.add(ifNode);

        //解析thenStatement
        MyVisitor2 newMyVisitor2 = new MyVisitor2();
        Statement thenStatement = node.getThenStatement();
        thenStatement.accept(newMyVisitor2);
        ApiSequence thenAPI = newMyVisitor2.apiSequence;

        //解析elseStatement
        MyVisitor2 newMyVisitor3 = new MyVisitor2();
        Statement elseStatement = node.getElseStatement();
        if(elseStatement != null)
            elseStatement.accept(newMyVisitor3);
        ApiSequence elseAPI = newMyVisitor3.apiSequence;

        //合并
        ApiSequence finalAPI = new ApiSequence();
        finalAPI.addAll(expressionAPI);
        finalAPI.addAll(thenAPI);
        finalAPI.addAll(elseAPI);

        //添加到主groum上
        this.apiSequence.addAll(finalAPI);
        return false;
    }

    @Override
    public boolean visit(SwitchStatement node)
    {
        //解析expression
        MyVisitor2 newMyVisitor = new MyVisitor2();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor);
        ApiSequence expressionAPI = newMyVisitor.apiSequence;

        //创建SWITCH控制节点
        ControlNode switchNode = new ControlNode();
        switchNode.controlNodeType = 1;
        //第二遍扫描再填充controlStructure
        switchNode.astNode = node;
        Groum switchGroum = new Groum();
        //switchGroum.Nodes.add(switchNode);

        //解析statements
        ApiSequence statementsAPI = new ApiSequence();
        List Statements = node.statements();
        for(int i = 0;i < Statements.size();i++) {
            MyVisitor2 newMyVisitor2 = new MyVisitor2();
            ((ASTNode) Statements.get(i)).accept(newMyVisitor2);
            statementsAPI.addAll(newMyVisitor2.apiSequence);
        }

        //合并
        ApiSequence finalAPI = new ApiSequence();
        finalAPI.addAll(expressionAPI);
        finalAPI.addAll(statementsAPI);

        //添加到主groum上
        this.apiSequence.addAll(finalAPI);
        return false;
    }

    @Override
    public boolean visit(WhileStatement node)
    {
        //解析expression部分
        MyVisitor2 newMyVisitor = new MyVisitor2();
        Expression expression = node.getExpression();
        expression.accept(newMyVisitor);
        ApiSequence expressionAPI = newMyVisitor.apiSequence;

        //插入while节点
        ControlNode whileNode = new ControlNode();
        whileNode.controlNodeType = 2;
        //第二遍扫描再填充controlStructure
        whileNode.astNode = node;
        Groum whileGroum = new Groum();
        //whileGroum.Nodes.add(whileNode);

        //解析statement部分
        MyVisitor2 newMyVisitor2 = new MyVisitor2();
        Statement statement = node.getBody();
        statement.accept(newMyVisitor2);
        ApiSequence statementAPI = newMyVisitor2.apiSequence;

        //合并
        ApiSequence finalAPI = new ApiSequence();
        finalAPI.addAll(expressionAPI);
        finalAPI.addAll(statementAPI);

        //添加到主groum上
        this.apiSequence.addAll(finalAPI);
        return false;
    }

    public void displayAPI()
    {
        apiSequence.display();
    }
    public void displayJson(String className)
    {
        apiSequence.toJson(className);
    }
}
