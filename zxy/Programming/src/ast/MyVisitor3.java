package ast;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.json.simple.JSONObject;

public class MyVisitor3 extends ASTVisitor {
    public MethodAnnotation annotationList = new MethodAnnotation();

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
    public boolean visit(Javadoc node)
    {
        String fatherMethod = getFatherMethod(node);
        String comment;
        if(node.isDocComment())
            comment = node.toString();
        else
            comment = "null";
        annotationList.add(fatherMethod,comment);
        return false;
    }

    public void display(String fileName)
    {
        annotationList.display(fileName);
    }
    public JSONObject displayJson(String fileName)
    {
        return annotationList.toJson(fileName);
    }
}
