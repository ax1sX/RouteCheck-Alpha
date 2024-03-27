package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Config;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@FactAnalyzerAnnotations(
        name = "StrutsXmlFactAnalyzer"
)
public class StrutsXmlFactAnalyzer extends AbstractFactAnalyzer{
    private static final Logger LOGGER = LoggerFactory.getLogger(StrutsXmlFactAnalyzer.class);

    static Map<String, Element> actionMap = new HashMap<>();

    public StrutsXmlFactAnalyzer() {
        super(StrutsXmlFactAnalyzer.class.getName(), "config", "");
    }


    /**
     *     此方法解析的是package标签下，默认解析action标签的name、class、method属性。三种常见形式
     *     <package name="emap-findcar-package" extends="emap-default" namespace="/gis/ibuilding/page/queryMachine">
     *         <action name="*_*" class="{1}Action" method="{2}">
     *             <result name="j_{1}_{2}" type="json">
     *                 <param name="root">returnMessage</param>
     *             </result>
     *         </action>
     *     </package>
     *
     *     <action name="dishConfig">
     *          <result>/modules/ccs/dishConfig.jsp</result>
     *      </action>
     *
     *      <action name="ccsAction!*" class="ccsShareAction" method="{1}"></action>
     */
    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析struts2.xml
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver());
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> packages = rootElement.getChildren();
            packages.forEach(pg ->{
                if(pg.getName().equals("package")){
                    List<Element> actions = pg.getChildren();
                    String nameSpace = pg.getAttributeValue("namespace");
                    // 有些package标签只配置interceptor、global-results等，不存在action。这种往往没有namespace属性
                    if (nameSpace == null){return;}
                    actions.forEach(action -> {
                        if (action.getName().equals("action")){
                            try {
                                String actionName = action.getAttributeValue("name");
                                actionMap.put(actionName, action);
                                /**
                                 * 处理特例 namespace="/", action name="*_*"
                                 */
                                if (actionName != null && !nameSpace.equals("/")) {
                                    actionName = "/" + actionName;
                                }
                                String route = nameSpace + actionName + ".action";
                                String clazz = action.getAttributeValue("class");
                                String method = action.getAttributeValue("method");
                                Fact fact = new Fact();
                                fact.setMethod(method);
                                fact.setClassName(clazz);
                                fact.setRoute(route);
                                fact.setDescription(String.format("从%s中发现%s", config.getFilePath(),
                                        action));
                                fact.setCredibility(3);
                                fact.setFactName(getName());
                                factChain.add(fact);
                            }catch (Exception e){
                                LOGGER.info(e.getMessage());
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }

    }

    @Override
    public void prepare(Object object) {
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析struts2.xml
            try{
                // TODO: 判断是否包含<struts>标签
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setEntityResolver(new NoOpEntityResolver());
                InputStream is = new FileInputStream(new File(filePath));
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                if(rootElement.getName().equals("struts")){
                    this.setEnable(true);
                }
            }catch (Exception ex){

            }
        }

    }

    public static void main(String[] args) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        InputStream is = new FileInputStream(new File("D:\\工作\\专项工具\\RouteCheck\\config\\struts2.xml"));
        Document document = saxBuilder.build(is);
        Element rootElement = document.getRootElement();
        List<Element> packages = rootElement.getChildren();
        packages.forEach(pg ->{
            if(pg.getName().equals("package")){
                List<Element> actions = pg.getChildren();
                String nameSpace = pg.getAttributeValue("namespace");
                actions.forEach(action -> {
                    String actionName = action.getAttributeValue("name");
                    String route = nameSpace + actionName + ".action";
                    String clazz = action.getAttributeValue("class");
                    if(clazz == null){
                        clazz = "ActionSupport";
                    }
                    String method = action.getAttributeValue("method");



                });
            }
        });
    }
}
