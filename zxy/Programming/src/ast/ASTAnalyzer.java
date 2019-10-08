package ast;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.json.simple.JSONObject;

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

    /**
     * 从一个Java文件中提取相关信息
     *
     * @param path 一个Java文件的路径
     */
    public JSONObject extractInfoFormAST(String path) {

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

        // 建立AST访问器
        ASTNodeVisitor visitor = new ASTNodeVisitor();
        MyVisitor myVisitor = new MyVisitor();
        //MyVisitor2 myVisitor = new MyVisitor2();
        //MyVisitor3 myVisitor = new MyVisitor3();
        compUnit.accept(myVisitor);

        //myVisitor.secondScan();
        return myVisitor.classJson;
        //myVisitor.displayGroum();

        //if(!myVisitor.groum.isValid())
        //{
        //    System.out.println("invalid groum");
        //    assert false;
        //}

        //UsageExtracting usageExtracting = new UsageExtracting(myVisitor.groum);
        //usageExtracting.usageExtracting(5);

        //MyScanner myScanner = new MyScanner();
        //myScanner.Scan("test_save.json");

        //UsageExtracting usageExtracting = new UsageExtracting(myScanner.groum);
        //usageExtracting.usageExtracting(12);

    }

    public static List<String> getAllObjectFileName(String path)
    {
        File file = new File(path);
        File[] fileList = file.listFiles();
        List<String> nameList = new ArrayList<>();
        if(fileList == null)
            return nameList;
        for(int i = 0;i < fileList.length;i++)
        {
            if(fileList[i].isFile() && fileList[i].getName().contains(".java"))
            {
                nameList.add(fileList[i].getAbsolutePath());
            }
            if(fileList[i].isDirectory())
            {
                List<String> subList = getAllObjectFileName(fileList[i].getAbsolutePath());
                nameList.addAll(subList);
            }
        }
        return nameList;
    }

    public static void main(String[] args) {


        try {
            PrintStream ps = new PrintStream(new FileOutputStream("result/swipe-android-master.json"));
            System.setOut(ps);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ASTAnalyzer analyzer = new ASTAnalyzer();
        String rootPath = "/Users/njucszxy/Documents/GitHub/StaMiner/data/java-projects/swipe-android-master";
        List<String> filePaths = getAllObjectFileName(rootPath);
        //Statistic result = new Statistic();
        //result.javaFileName = rootPath;


        JSONObject result = new JSONObject();

        for(int i = 0;i < filePaths.size();i++)
        {
            //Statistic temp = analyzer.extractInfoFormAST(filePaths.get(i));  // 提取API信息
            File file = new File(filePaths.get(i));
            String fileName = file.getName();
            int pointAt = fileName.indexOf('.');
            String className = fileName.substring(0,pointAt);
            //JSONObject jsonObject = analyzer.extractInfoFormAST(filePaths.get(i)).displayJson(className);
            //result.put(className,jsonObject);
            //analyzer.extractInfoFormAST(filePaths.get(i)).displayJson(className);
            //result.addStatistic(temp);

            //if(className.equals("Utils") || className.equals("Array2DHashSet") || className.equals("IntervalSet"))
            //    continue;
            //System.out.println("**********");
            //System.out.println(className);
            JSONObject classJson = analyzer.extractInfoFormAST(filePaths.get(i));
            result.put(className,classJson);
            //System.out.println("**********");
        }
        System.out.println(result);
    }
}
