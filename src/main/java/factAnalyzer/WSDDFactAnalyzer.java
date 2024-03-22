package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import project.entry.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FactAnalyzerAnnotations(
        name = "WSDDFactAnalyzer"
)

public class WSDDFactAnalyzer extends SOAPFactAnalyzer{
    static Map<String, Element> services = new HashMap<>();

    public WSDDFactAnalyzer(){
        super(WSDDFactAnalyzer.class.getName(), "config", "");
    }
    @Override
    public void prepare(Object object) {
        Config config = (Config) object;
        if(config.getSuffix().equals("wsdd")){
            this.setEnable(true);
        }else{
            this.setEnable(false);
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析*.wsdd
            SAXBuilder saxBuilder = new SAXBuilder();
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child ->{
                if(child.getName().equals("service")){
                    String serviceName = child.getAttributeValue("name");
                    services.put(serviceName, child);
                }

            });
        }catch (Exception e){
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {

        // TODO: 解析*.wsdd
        SAXBuilder saxBuilder = new SAXBuilder();
        InputStream is = new FileInputStream(new File("D:\\工作\\文档\\源码\\久其财务报表\\server-config.wsdd"));
        Document document = saxBuilder.build(is);
        Element rootElement = document.getRootElement();
        List<Element> children = rootElement.getChildren();
        children.forEach(child ->{
            if(child.getName().equals("service")){
                String serviceName = child.getAttributeValue("name");
                services.put(serviceName, child);
            }
        });

    }

}
