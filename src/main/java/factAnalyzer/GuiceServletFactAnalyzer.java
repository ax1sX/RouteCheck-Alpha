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
                            String classFilePath = getModule().getClassBySootClass(sootClass);
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
        Map<String, Jar> jarMap = this.getModule().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if (jarMap.containsKey("guice-servlet") && visibilityAnnotationTag != null) {
            this.setEnable(true);
        } else {
            this.setEnable(false);
        }
    }
}
