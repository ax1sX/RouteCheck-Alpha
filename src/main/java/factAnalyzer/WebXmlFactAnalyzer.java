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

public class WebXmlFactAnalyzer extends AbstractFactAnalyzer {

    static Map<String, Element> servlets = new HashMap<>();
    static Map<String, Set<Element>> servletMappings = new HashMap<>();
    static Map<String, Element> filters = new HashMap<>();
    static Map<String, Set<Element>> filterMappings = new HashMap<>();
    static List<String> AxisUrls = new ArrayList<>();


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
            filters.clear();
            filterMappings.clear();
            AxisUrls.clear();

            Config config = (Config) object;
            String filePath = config.getFilePath();

            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver());
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();

            for (Object childObj : rootElement.getChildren()) {
                if (childObj instanceof Element) {
                    Element child = (Element) childObj;
                    String childName = child.getName();
                    switch (childName) {
                        case "servlet":
                            processServlet(child);
                            break;
                        case "servlet-mapping":
                            processServletMapping(child);
                            break;
                        case "filter":
                            processFilter(child);
                            break;
                        case "filter-mapping":
                            processFilterMapping(child);
                            break;
                        default:
                            // Handle other cases if needed
                    }
                }
            }
            processElements(servlets, servletMappings, "从文件%s中提取出servlet和servlet-mapping", "servlet-class", factChain, config);
            processElements(filters, filterMappings, "从文件%s中提取出filter和filter-mapping", "filter-class", factChain, config);


        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    private void processServlet(Element servlet) {
        String servletName = servlet.getChildText("servlet-name", servlet.getNamespace());
        servlets.put(servletName, servlet);
    }

    private void processFilter(Element filter) {
        String filterName = filter.getChildText("filter-name", filter.getNamespace());
        filters.put(filterName, filter);
    }

    private void processMapping(Element mapping, Map<String, Set<Element>> mappings, String nameTag) {
        String name = mapping.getChildText(nameTag, mapping.getNamespace()); // 获取filter-name或servlet-name标签的值
        String urlPattern = mapping.getChildText("url-pattern", mapping.getNamespace());
        if (urlPattern != null && urlPattern.startsWith("/") && !urlPattern.equals("/*") && !urlPattern.equals("/")) {
            Set<Element> values = mappings.computeIfAbsent(name, k -> new HashSet<>());
            values.add(mapping);
        }
        if (name.equals("AxisServlet")) {
            AxisUrls.add(urlPattern);
        }
    }

    private void processServletMapping(Element servletMapping) {
        processMapping(servletMapping, servletMappings, "servlet-name");
    }

    private void processFilterMapping(Element filterMapping) {
        processMapping(filterMapping, filterMappings, "filter-name");
    }


    private void processElements(Map<String, Element> elements, Map<String, Set<Element>> mappings, String descriptionTemplate, String classNameKey, Collection<Fact> factChain, Config config) {
        if (elements.isEmpty() || mappings.isEmpty()) return;

        elements.forEach((name, element) -> {
            Set<Element> elementMappings = mappings.getOrDefault(name, new HashSet<>());
            elementMappings.forEach(mapping -> {
                Fact fact = new Fact();
                String className = element.getChildText(classNameKey, element.getNamespace());
                fact.setClassName(className);
                fact.setRoute(mapping.getChildText("url-pattern", mapping.getNamespace()));
                fact.setDescription(String.format(descriptionTemplate, config.getFilePath()));
                fact.setCredibility(3);
                fact.setMethod("—");
                fact.setFactName(getName());
                factChain.add(fact);
            });
        });
    }
}
