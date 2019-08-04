package ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

/**
 * OK
 * 从上到下访问Java文件, 以一个method为单位, 统计该method内部的API使用序列
 * ASTNodeVisitor
 * Class: ClassName
 * APIn: MethodName
 */

public class ASTNodeVisitor extends ASTVisitor {

    // API -> Class list    存储API的类名
    private List<String> APIClassList = new ArrayList<>();
    // API -> Name list     存储API的方法名
    private List<String> APINameList = new ArrayList<>();

    /**
     * 访问API方法调用节点: 填充某Java类的API方法在一个method中的混合使用序列
     * int a = test.func();
     *
     * @param node 方法调用节点
     * @return
     */
    @Override
    public boolean visit(MethodInvocation node) {

        String APIClass="CurrentClass";                                    //API所属的类
        String APIName = node.getName().toString();                        //API方法的名称

        // 判断该API是否属于当前类, 确定API所属的Class, 方法调用节点的表达式
        Expression expression = node.getExpression();
        // 如果API所在的表达式存在并且有类型绑定, 说明该API是外部类定义的, 否则说明API是本类定义的非静态方法
        if (expression != null && expression.resolveTypeBinding() != null)
            APIClass = expression.resolveTypeBinding().getBinaryName(); // API类

        // 添加新的调用信息到临时列表中
        APIClassList.add(APIClass);
        APINameList.add(APIName);

        return true;
    }

    public List<String> getAPIClassList() {
        return APIClassList;
    }

    public List<String> getAPINameList() {
        return APINameList;
    }
}
