package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import project.entry.Jar;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.VisibilityAnnotationTag;
import utils.DeCompilerUtil;
import utils.Utils;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FactAnalyzerAnnotations(
        name = "GuiceServletFactAnalyzer"
)
public class GuiceServletFactAnalyzer extends AbstractFactAnalyzer {

    public GuiceServletFactAnalyzer() {
        super(GuiceServletFactAnalyzer.class.getName(), "class", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        // https://blog.csdn.net/a304096740/article/details/101883424
        try {
            SootClass sootClass = (SootClass) object;
            String tempDirectory = getSettings().getTempDirectory();
            // 当前版本只考虑直接继承关系
            if (sootClass.getSuperclass().getName().equals("com.google.inject.servlet.ServletModule")) {
                try {
                    for (SootMethod sm :
                            sootClass.getMethods()) {
                        if (sm.getName().equals("configureServlets")) {
                            String classFilePath = getProject().getClassesToPath().get(sootClass);
                            DeCompilerUtil.deCompile(new String[]{"-dgs=true", classFilePath, tempDirectory});
                            String newPath = classFilePath.substring(classFilePath.lastIndexOf(File.separator) + 1);
                            newPath = tempDirectory + File.separator + newPath;
                            newPath = newPath.replace(".class", ".java");
                            String javaContent = Utils.fileReader(newPath);
                            String pattern = ".*serve\\(([a-z A-Z\\[\\]{}\"\"/,\\s0-9\\*]*)\\)\\.with.*\\(([/A-Za-z0-9]+\\.class).*";
                            String[] lines = javaContent.split(";");
                            String patternRegex = ".*serveRegex\\((.*?)\\.with.*\\(([/A-Za-z0-9]+\\.class).*";
                            Pattern serve = Pattern.compile(pattern);
                            Pattern serveRegex = Pattern.compile(patternRegex);
                            for (String line :
                                    lines) {
                                Fact fact = new Fact();
                                String route = "";
                                String className = "";
                                Matcher matcherServe = serve.matcher(line);
                                if (matcherServe.find()) {
                                    route = matcherServe.group(1);
                                    className = matcherServe.group(2);
                                }

                                Matcher matcherServeRegex = serveRegex.matcher(line);
                                if (matcherServeRegex.find()) {
                                    route = matcherServeRegex.group(1);
                                    className = matcherServeRegex.group(2);
                                }
                                if (!route.equals("") && !className.equals("")) {
                                    fact.setMethod("do*");
                                    fact.setClassName(className);
                                    fact.setCredibility(3);
                                    fact.setRoute(route);
                                    fact.setDescription(line);
                                    fact.setFactName(this.getName());
                                    factChain.add(fact);
                                }
                            }

                        }
                    }

                } catch (RuntimeException runtimeException) {

                }
            }
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    @Override
    public void prepare(Object object) {
        Map<String, Jar> jarMap = this.getProject().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if (jarMap.containsKey("guice-servlet") && visibilityAnnotationTag != null) {
            this.setEnable(true);
        } else {
            this.setEnable(false);
        }
    }

    public static void main(String[] args) {
//        String content = "package com.ming.user;\n" +
//                "\n" +
//                "import com.google.inject.servlet.ServletModule;\n" +
//                "import com.ming.user.action.UserServlet;\n" +
//                "\n" +
//                "class UserModule$1 extends ServletModule {\n" +
//                "   // $FF: synthetic field\n" +
//                "   final UserModule this$0;\n" +
//                "\n" +
//                "   UserModule$1(UserModule var1) {\n" +
//                "      this.this$0 = var1;\n" +
//                "   }\n" +
//                "\n" +
//                "   protected void configureServlets() {\n" +
//                "      this.serve(\"/UserServlet\", new String[]{\"/UserController\"}).with(UserServlet.class);\n" +
//                "      this.serve(\"/UserServlet1\").with(UserServlet1.class);\n" +
//                "      this.serveRegex(\"(.)*ajax(.)*\").with(UserServlet2.class);\n" +
//                "   }\n" +
//                "}\n";
//        String pattern = ".*serve\\(([a-z A-Z\\[\\]{}\"\"/,\\s0-9\\*]*)\\)\\.with.*\\(([/A-Za-z0-9]+\\.class).*";
//        String patternRegex = ".*serveRegex\\((.*?)\\.with.*\\(([/A-Za-z0-9]+\\.class).*";
//        Pattern r1 = Pattern.compile(pattern);
//        Pattern r = Pattern.compile(patternRegex);
//        String[] lines = content.split(";");
//        for (String line:
//             lines) {
//            Matcher m = r.matcher(line);
//            if(m.find()){
//                String route = m.group(1);
//                String className = m.group(2);
//                System.out.println(route);
//                System.out.println(className);
//            }
//
//            Matcher m1 = r1.matcher(line);
//            if(m1.find()){
//                String route = m1.group(1);
//                String className = m1.group(2);
//                System.out.println(route);
//                System.out.println(className);
//            }
//        }

        DeCompilerUtil.deCompile(new String[]{"-dgs=true", "D:\\工作\\专项工具\\RouteCheck\\output\\UserModule$1.java", "D:\\工作\\专项工具\\RouteCheck\\config"});
    }
}
