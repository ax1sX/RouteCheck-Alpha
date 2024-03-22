package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import project.entry.Config;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@FactAnalyzerAnnotations(
        name = "WebXmlFactAnalyzer"
)

/*
https://docs.oracle.com/cd/E13222_01/wls/docs81/webapp/web_xml.html
*/
public class WebXmlFactAnalyzer extends AbstractFactAnalyzer {

    static Map<String, Element> servlets = new HashMap<>();
    static Map<String, Set<Element>> servletMappings = new HashMap<>();

    public WebXmlFactAnalyzer() {
        super(WebXmlFactAnalyzer.class.getName(), "config", "");
    }

    @Override
    public void prepare(Object object) {
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析web.xml
            try{
                // TODO: 判断是否包含<web-app>标签
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setEntityResolver(new NoOpEntityResolver());
                InputStream is = new FileInputStream(new File(filePath));
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                if(rootElement.getName().equals("web-app")){
                    this.setEnable(true);
                }
            }catch (Exception ex){

            }
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            servlets.clear();
            servletMappings.clear();
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析web.xml
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver());
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child ->{
                if(child.getName().equals("servlet")){
                    String servletName = child.getChildText("servlet-name", child.getNamespace());
                    servlets.put(servletName, child);
                }else if(child.getName().equals("servlet-mapping")){
                    String servletName = child.getChildText("servlet-name", child.getNamespace());
                    Set<Element> values = servletMappings.getOrDefault(servletName, new HashSet<Element>());
                    values.add(child);
                    servletMappings.put(servletName, values);
                }
            });
            if (servlets.size() > 0 && servletMappings.size() > 0) {
                servlets.forEach((name, servlet) -> {
                    Set<Element> servletMapping = servletMappings.getOrDefault(name, new HashSet<>());
                    servletMapping.forEach(sm ->{
                        Fact fact = new Fact();
                        String servletClass = servlet.getChildText("servlet-class", servlet.getNamespace());
                        fact.setClassName(servletClass);
                        fact.setRoute(sm.getChildText("url-pattern", sm.getNamespace()));
                        fact.setDescription(String.format("从文件%s中提取出servlet和servlet-mapping", config.getFilePath()));
                        fact.setCredibility(3);
                        /*考虑到这个doget dopost之类的这些方法解析不到，就默认设置了do*，没有把这个字段设置为空*/
                        // TODO: 这里不应该设置成do*
                        fact.setMethod("do*");
                        fact.setFactName(getName());
                        factChain.add(fact);
                    });
                });
            }
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        // TODO: 解析web.xml
        SAXBuilder saxBuilder = new SAXBuilder(false);
        InputStream is = new FileInputStream(new File("C:\\Users\\ss\\Desktop\\test\\web.xml"));
        Document document = saxBuilder.build(is);
        Element rootElement = document.getRootElement();
    }
}
