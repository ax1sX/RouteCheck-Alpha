package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import exceptions.FactAnalyzerException;
import soot.SootClass;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import utils.Utils;
import entry.Fact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

@FactAnalyzerAnnotations(
        name = "HttpServletFactAnalyzer"
)
public class HttpServletFactAnalyzer extends AbstractFactAnalyzer{

    public HttpServletFactAnalyzer() {
        super(HttpServletFactAnalyzer.class.getName(), "class", "");
    }

    @Override
    public void prepare(Object object) {
        // TODO： nothing
    }

    private boolean hasSuperClass(SootClass sootClass){
        SootClass sc = sootClass.getSuperclass();
        if(sc.getName().equals("javax.servlet.http.HttpServlet")){
            return true;
        }
        if(sc.hasSuperclass()){
            return hasSuperClass(sc);
        }
        return false;
    }


    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Fact fact = new Fact();
            SootClass sootClass = (SootClass) object;
            // TODO: 1.判断是否使用注解; 2.判断是否继承HttpServlet
            AtomicBoolean hasWebServlet = new AtomicBoolean(false);
            VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
            if(visibilityAnnotationTag != null && visibilityAnnotationTag.hasAnnotations()){
                ArrayList<AnnotationTag> annotationTags =  visibilityAnnotationTag.getAnnotations();
                annotationTags.forEach(a -> {
                    if(a.getType().equals("Ljavax/servlet/annotation/WebServlet;")){
                        a.getElems().forEach(e ->{
                            if(e.getClass().toString().contains("AnnotationArrayElem")){
                                if(e.getName().equals("urlPatterns") || e.toString().contains("/")){
                                    AnnotationArrayElem annotationArrayElem = (AnnotationArrayElem) e;
                                    annotationArrayElem.getValues().forEach(v ->{
                                        AnnotationStringElem annotationStringElem = (AnnotationStringElem) v;
                                        String route = annotationStringElem.getValue();
                                        fact.setRoute(route);
                                    });
                                    fact.setClassName(sootClass.getName());
                                    fact.setDescription("类文件中使用注解：" + a.toString());
                                    fact.setCredibility(3);
                                    fact.setMethod("do*");
                                    fact.setFactName(getName());
                                    factChain.add(fact);
                                    hasWebServlet.set(true);
                                }
                            }
                        });
                    }
                });
            }

            if(!hasWebServlet.get() && sootClass.hasSuperclass()){
                if(hasSuperClass(sootClass)){
                    fact.setClassName(sootClass.getName());
                    fact.setCredibility(1);
                    fact.setDescription("类文件继承（直接或间接）javax.servlet.http.HttpServlet");
                    fact.setMethod("do*");
                    fact.setFactName(getName());
                    factChain.add(fact);
                }
            }
        }catch (Exception e){
            throw new FactAnalyzerException(e.getMessage());
        }
    }
}
