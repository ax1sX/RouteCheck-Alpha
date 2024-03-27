package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@FactAnalyzerAnnotations(
        name = "UnionWebServiceFactAnalyzer"
)
public class UnionWebServiceFactAnalyzer extends AbstractFactAnalyzer{
    private static final Logger LOGGER = LoggerFactory.getLogger(UnionWebServiceFactAnalyzer.class);
    public UnionWebServiceFactAnalyzer(String name, String type, String description) {
        super(name, type, description);
    }

    public UnionWebServiceFactAnalyzer() {
        super(UnionWebServiceFactAnalyzer.class.getName(), "union", "");
    }


    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        Map<String, Fact> Rootdict = new HashMap<>(); // 存放路由和类名
        Map<String, Fact> Apidict = new HashMap<>();

        // TODO：ClassName中存在\n和空格，导致需要replace
        for (Fact fact : factChain) {
            try{
                String className = fact.getClassName();
                String factName = fact.getFactName();
                if (className != null && Objects.equals(className.trim().replace("\\s", ""), "org.apache.cxf.transport.servlet.CXFServlet")) {
                    Rootdict.put(fact.getRoutes().toString(), fact);
                } else if (factName != null && Objects.equals(factName.trim().replace("\\s", ""), "factAnalyzer.ApacheCXFFactAnalyzer")) {
                    Apidict.put(fact.getRoutes().toString(), fact);
                }
            }catch (Exception e){
                LOGGER.info(e.getMessage());
            }

        }
        if (!Rootdict.isEmpty()) {
            if (!Apidict.isEmpty()){
                for (Map.Entry<String, Fact> Rootentry : Rootdict.entrySet()) {
                    for (Map.Entry<String, Fact> Apientry :  Apidict.entrySet()) {
                        // 将路由、类名分别拼接起来
                        String combinedRoute = combinePaths(Rootentry.getKey(), Apientry.getKey());
                        String combinedClass = Rootentry.getValue().getClassName().trim() + "&" + Apientry.getValue().getClassName().trim();
                        Fact fact = new Fact();
                        fact.setRoute(combinedRoute);
                        fact.setClassName(combinedClass);
                        fact.setDescription("合并ApacheCXFFactAnalyzer和WebXmlFactAnalyzer中的WebService路由");
                        fact.setCredibility(3);
                        fact.setMethod("—");
                        fact.setFactName(getName());
                        factChain.add(fact);
                        factChain.remove(Apientry.getValue()); // 既然要合并，把这个ApacheCXFFactAnalyzer的Fact从FactChain中删除
                    }
                }

            }
        }
    }

    @Override
    public void prepare(Object object) {

    }

    private String combinePaths(String basePath, String additionalPath) {
        // 如果基础路径为空，直接返回附加路径
        basePath = basePath.trim().replaceAll("\\s", "").replaceAll("\\[|\\]", "");
        additionalPath = additionalPath.trim().replaceAll("\\s", "").replaceAll("\\[|\\]", "");
        if (basePath.isEmpty()) {
            return additionalPath;
        }

        // 如果附加路径为空，直接返回基础路径
        if (additionalPath.isEmpty()) {
            return basePath;
        }

        // 去除基础路径末尾的*号
        if (basePath.endsWith("*")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        // 去除基础路径末尾的斜杠
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        // 去除附加路径开头的斜杠和通配符
        if (additionalPath.startsWith("/")) {
            additionalPath = additionalPath.substring(1);
        }

        // 合并路径并返回结果
        return basePath + "/" + additionalPath;
    }

}
