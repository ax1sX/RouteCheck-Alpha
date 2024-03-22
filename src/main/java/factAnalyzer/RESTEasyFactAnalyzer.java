package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.Map;

@FactAnalyzerAnnotations(
        name = "RESTEasyFactAnalyzer"
)
public class RESTEasyFactAnalyzer extends JAXRSFactAnalyzer{

    public RESTEasyFactAnalyzer(){
        super(RESTEasyFactAnalyzer.class.getName(), "class", "");
    }
    @Override
    public void prepare(Object object) {
        Map<String, Jar> jarMap = this.getProject().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if(jarMap.containsKey("resteasy-jaxrs") && visibilityAnnotationTag != null){
            this.setEnable(true);
        }else{
            this.setEnable(false);
        }
    }
}
