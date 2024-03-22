package factAnalyzer;

import entry.Fact;
import exceptions.FactAnalyzerException;
import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import utils.Utils;

import java.util.*;


public class JAXRSFactAnalyzer extends AbstractFactAnalyzer {
    protected final String PATTERN = "Lorg/springframework/stereotype/Controller;";
    protected final String PATTERNPATH = "Ljavax/ws/rs/Path;";

    public JAXRSFactAnalyzer() {
        super(JAXRSFactAnalyzer.class.getName(), "class", "");
    }

    public JAXRSFactAnalyzer(String name, String type, String description) {
        super(name, type, description);
    }

    protected Collection<String> findRoute(ArrayList<AnnotationTag> annotationTags) {
        Set<String> route = new HashSet<>();
        annotationTags.forEach(a -> {
            if (a.getType().equals(PATTERNPATH)) {
                a.getElems().forEach(e -> {
                    try {
                        if (e.getClass().toString().contains("AnnotationStringElem")) {
                            if (e.getName().equals("value")) {
                                AnnotationStringElem annotationStringElem = (AnnotationStringElem) e;
                                route.add(annotationStringElem.getValue());
                            }
                        }
                    } catch (Exception ex) {
                    }
                });
            }
        });
        return route;
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            SootClass sootClass = (SootClass) object;
            VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
            if (visibilityAnnotationTag == null) return;
            String sootClassVisibilityAnnotationTagString = visibilityAnnotationTag.toString();
            ArrayList<AnnotationTag> annotationTags = visibilityAnnotationTag.getAnnotations();
            // TODO: 提取多重继承关系中的类注解的RequestMapping
            Set<String> prefix = (Set<String>) findRoute(annotationTags);
            if (sootClassVisibilityAnnotationTagString.contains(PATTERN) ||
                    sootClassVisibilityAnnotationTagString.contains(PATTERNPATH)) {
                List<SootMethod> sootMethodList = sootClass.getMethods();
                sootMethodList.forEach(sootMethod -> {
                    try {
                        VisibilityAnnotationTag visibilityAnnotationTagTemp =
                                (VisibilityAnnotationTag) sootMethod.getTag("VisibilityAnnotationTag");
                        if (visibilityAnnotationTagTemp != null) {
                            String sootMethodVisibilityAnnotationTagString = visibilityAnnotationTagTemp.toString();
                            boolean isMat = sootMethodVisibilityAnnotationTagString.contains(PATTERNPATH);
                            if (isMat) {
                                // 提取路由，创建新事实
                                ArrayList<AnnotationTag> annotationTagsTemp = visibilityAnnotationTagTemp.getAnnotations();
                                Set<String> suffix = (Set<String>) findRoute(annotationTagsTemp);
                                Fact fact = new Fact();
                                fact.setClassName(sootClass.getName());
                                fact.setDescription("类文件中使用注解：" + annotationTags.toString() + "\n"
                                        + annotationTagsTemp.toString());
                                if (prefix.size() > 0) {
                                    prefix.forEach(p -> {
                                        suffix.forEach(s -> {
                                            if (s.startsWith("/") || p.endsWith("/")) {
                                                fact.setRoute(p + s);
                                            } else {
                                                fact.setRoute(p + "/" + s);
                                            }

                                        });
                                    });
                                } else {
                                    suffix.forEach(s -> {
                                        fact.setRoute(s);
                                    });
                                }
                                fact.setMethod(sootMethod.getName());
                                fact.setCredibility(3);
                                fact.setFactName(this.getName());
                                factChain.add(fact);
                            }
                        }
                    } catch (Exception ex) {
                    }
                });
            }
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    @Override
    public void prepare(Object object) {

    }
}
