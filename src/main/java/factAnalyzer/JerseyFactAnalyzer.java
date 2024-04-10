package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.Collection;
import java.util.Map;

@FactAnalyzerAnnotations(
        name = "JerseyFactAnalyzer"
)
public class JerseyFactAnalyzer extends JAXRSFactAnalyzer{

    public JerseyFactAnalyzer(){
        super(JerseyFactAnalyzer.class.getName(), "class", "");
    }

    @Override
    public void prepare(Object object) {
        Map<String, Jar> jarMap = this.getProject().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if(jarMap.containsKey("jersey") && visibilityAnnotationTag != null){
            this.setEnable(true);
        }else{
            this.setEnable(false);
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        super.analysis(object, factChain);
    }
}
