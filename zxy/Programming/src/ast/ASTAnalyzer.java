package ast;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Create AST parser with respect to each java file.
 * Get method-level API usage patterns for each software library.
 * Get tokens around each API.
 * <p>
 * AST分析器
 * 为每个JAva文件创建AST解析器
 * 为每个软件库获取方法级别的API使用模式
 * 获取每个API周围的tokens
 */

public class ASTAnalyzer {

    private String[] sources = {"/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home/src"};
    private String[] classpath = {"/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home/jre/lib/rt.jar"};


    public List<String> APIClassList = new ArrayList<>();
    public List<String> APINameList = new ArrayList<>();

    /**
     * 从一个Java文件中提取相关信息
     *
     * @param path 一个Java文件的路径
     */
    public void extractInfoFormAST(String path) {

        // 读取源码文件内容
        byte[] input = null;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path));
            input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
            bufferedInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 建立AST解析器 Java 8版本
        ASTParser astParser = ASTParser.newParser(AST.JLS8); //Java 8
        astParser.setSource(new String(input).toCharArray());
        astParser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
        astParser.setResolveBindings(true);
        Map options = JavaCore.getOptions();
        astParser.setCompilerOptions(options);
        astParser.setBindingsRecovery(true);
        astParser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);
        astParser.setUnitName("any_name");
        CompilationUnit compUnit = (CompilationUnit) (astParser.createAST(null)); // 编译单元

        //System.out.println(compUnit);

        // 建立AST访问器
        ASTNodeVisitor visitor = new ASTNodeVisitor();
        MyVisitor myVisitor = new MyVisitor();
        compUnit.accept(myVisitor);

        myVisitor.secondScan();
        myVisitor.displayGroum();

        UsageExtracting usageExtracting = new UsageExtracting(myVisitor.groum);
        usageExtracting.usageExtracting(2);

        // 获取 API 使用序列 （和其在文件中相应的位置）
        //APIClassList = visitor.getAPIClassList();
        //APINameList = visitor.getAPINameList();
    }

    /**
     * 显示获取的API信息
     */
    public void display() {
        for (int i = 0; i < APIClassList.size(); i++)
            System.out.println(APIClassList.get(i) + ": " + APINameList.get(i));
    }

    public static void main(String[] args) {
        ASTAnalyzer analyzer = new ASTAnalyzer();
        String testFilePath = "src/ast/MyTest.java";
        analyzer.extractInfoFormAST(testFilePath);  // 提取API信息
        //analyzer.display();             // 显示API信息
    }
}
