package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import utils.Utils;

import java.util.*;

//https://www.cnblogs.com/lyh233/p/12047942.html

@FactAnalyzerAnnotations(
        name = "SpringMVCAnnotationFactAnalyzer"
)
public class SpringMVCAnnotationFactAnalyzer extends SpringFactAnalyzer{

    private final String PATTERN = "Lorg/springframework/stereotype/Controller;";
    private final String PATTERNMAP = "Lorg/springframework/web/bind/annotation";

    public SpringMVCAnnotationFactAnalyzer(){
        super(SpringMVCAnnotationFactAnalyzer.class.getName(), "class", "");
    }

    /*
    处理根路由
     */
    private Collection<String> findRoute(ArrayList<AnnotationTag> annotationTags){
        Set<String> route = new HashSet<>();
        annotationTags.forEach(a -> {
            if(a.getType().startsWith(PATTERNMAP) && a.getType().contains("Mapping")){
                a.getElems().forEach(e ->{
                    try{
                        if(e.getClass().toString().contains("AnnotationArrayElem")){
                            if(e.getName().equals("path") || ((AnnotationArrayElem) e).getValues().toString().contains("/")){
                                AnnotationArrayElem annotationArrayElem = (AnnotationArrayElem) e;
                                annotationArrayElem.getValues().forEach(v ->{
                                    AnnotationStringElem annotationStringElem = (AnnotationStringElem) v;
                                    route.add(annotationStringElem.getValue());
                                });
                            }
                        }}catch (Exception ex){}
                });
            }
        });
        return route;
    }


    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if(visibilityAnnotationTag == null)return;
        String sootClassVisibilityAnnotationTagString = visibilityAnnotationTag.toString();
        ArrayList<AnnotationTag> annotationTags =  visibilityAnnotationTag.getAnnotations();
        // TODO: 提取多重继承关系中的类注解的RequestMapping
        Set<String> prefix = (Set<String>) findRoute(annotationTags);
        if(sootClassVisibilityAnnotationTagString.contains(PATTERN) ||
                (sootClassVisibilityAnnotationTagString.contains(PATTERNMAP) && sootClassVisibilityAnnotationTagString.contains("Mapping"))){
            List<SootMethod> sootMethodList = sootClass.getMethods();
            sootMethodList.forEach(sootMethod -> {
                VisibilityAnnotationTag visibilityAnnotationTagTemp =
                        (VisibilityAnnotationTag) sootMethod.getTag("VisibilityAnnotationTag");
                if(visibilityAnnotationTagTemp != null){
                    String sootMethodVisibilityAnnotationTagString = visibilityAnnotationTagTemp.toString();
                    boolean isMat = sootMethodVisibilityAnnotationTagString.contains(PATTERNMAP);
                    if(isMat) {
                        // 提取路由，创建新事实
                        ArrayList<AnnotationTag> annotationTagsTemp = visibilityAnnotationTagTemp.getAnnotations();
                        Set<String> suffix = (Set<String>) findRoute(annotationTagsTemp);
                        Fact fact = new Fact();
                        fact.setClassName(sootClass.getName());
//                        fact.setDescription("类文件中使用注解：" + annotationTags.toString() + "\n"
//                                +annotationTagsTemp.toString());
                        fact.setDescription("类文件中使用注解");

                        /*
                        路由基本形态：/prefix/suffix。
                        如果没有suffix，路由:/prefix
                        如果没有prefix，路由:/suffix
                        如果都没有，路由:/
                         */
                        if (prefix.size() > 0) {
                            prefix.forEach(p -> {
                                if (suffix.isEmpty()){
                                    fact.setRoute(prefix.stream().findFirst().get());
                                }else {
                                    suffix.forEach(s -> {
                                        if(p.equals("/")){
                                            fact.setRoute(s);
                                        }else {
                                            fact.setRoute(p + s);
                                        }

                                    });
                                }

                            });
                        }else{
                            suffix.forEach(s -> {
                                fact.setRoute(s);
                            });
                        }

                        fact.setMethod(sootMethod.getName());
                        fact.setCredibility(3);
                        fact.setFactName(getName());
                        factChain.add(fact);
                    }
                }
            });
        }
    }

    @Override
    public void prepare(Object object) {

    }
}
