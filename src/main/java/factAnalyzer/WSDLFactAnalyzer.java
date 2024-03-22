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
import java.util.List;

@FactAnalyzerAnnotations(
        name = "WSDLFactAnalyzer"
)
public class WSDLFactAnalyzer extends SOAPFactAnalyzer{

    public WSDLFactAnalyzer(){
        super(WSDLFactAnalyzer.class.getName(), "config", "");
    }

    @Override
    public void prepare(Object object) {
        Config config = (Config) object;
        if(config.getSuffix().equals("wsdl")){
            this.setEnable(true);
        }else {
            this.setEnable(false);
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析*.wsdl
            SAXBuilder saxBuilder = new SAXBuilder();
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child ->{
                if(child.getName().equals("service")){
                    List<Element> grandson = child.getChildren();
                    grandson.forEach(gs ->{
                        if(gs.getName().equals("port")){
                            List<Element> greatGrandSon = gs.getChildren();
                            greatGrandSon.forEach(ggs -> {
                                if(ggs.getName().equals("address")){
                                    Fact fact = new Fact();
                                    fact.setRoute( ggs.getAttributeValue("location"));
                                    fact.setCredibility(3);
                                    fact.setDescription(String.format("从%s文件中提取", config.getFilePath()));
                                    factChain.add(fact);
                                }
                            });
                        }
                    });
                }

            });
        }catch (Exception e){
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        InputStream is = new FileInputStream(new File("D:\\工作\\文档\\源码\\久其财务报表\\ACRDataTransport.wsdl"));
        Document document = saxBuilder.build(is);
        Element rootElement = document.getRootElement();
        List<Element> children = rootElement.getChildren();
        children.forEach(child ->{
            if(child.getName().equals("service")){
                List<Element> grandson = child.getChildren();
                grandson.forEach(gs ->{
                    if(gs.getName().equals("port")){
                    List<Element> greatGrandSon = gs.getChildren();
                    greatGrandSon.forEach(ggs -> {
                        if(ggs.getName().equals("address")){

                            ggs.getAttributeValue("location");
                        }
                    });
                    }
                });
            }

        });
    }
}
