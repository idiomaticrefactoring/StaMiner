package ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;


import java.util.ArrayList;

//获得所有的方法名
public class FuncNameVis extends ASTVisitor {
    public ArrayList<String> funNamelist = new ArrayList<>();
    @Override
    public boolean visit(MethodDeclaration node) {
        funNamelist.add(node.getName().toString());
        return true;
    }

}

