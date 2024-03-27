package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import entry.StrutsAction;
import exceptions.FactAnalyzerException;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.tagkit.*;

import java.util.*;

@FactAnalyzerAnnotations(
        name = "StrutsActionFactAnalyzer"
)

public class StrutsActionFactAnalyzer extends AbstractFactAnalyzer{
    static Map<String, List<String>> ActionMethods = new HashMap<>();
    private final String PATTERN = "Lorg/springframework/stereotype/Controller;";

    public StrutsActionFactAnalyzer(String name, String type, String description) {
        super(name, type, description);
    }

    public StrutsActionFactAnalyzer(){
        super(StrutsActionFactAnalyzer.class.getName(), "class", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        // 空着
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain, Collection<StrutsAction> actionChain) throws FactAnalyzerException {
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");

        if (visibilityAnnotationTag == null) {
            extractSuperclass(sootClass, factChain, actionChain);
        } else {
            String sootClassVisibilityAnnotationTagString = visibilityAnnotationTag.toString();
            if (sootClassVisibilityAnnotationTagString.contains(PATTERN)) {
                extractAnnotations(sootClass, visibilityAnnotationTag.getAnnotations(), factChain, actionChain);
            }
        }
    }

    private void extractSuperclass(SootClass sootClass, Collection<Fact> factChain, Collection<StrutsAction> actionChain) {
        SootClass tmpClass = sootClass;
        String targetSuperclass = "com.opensymphony.xwork2.ActionSupport";
        while (!tmpClass.getName().equals("java.lang.Object")) {
            try {
                if (tmpClass.getName().equals(targetSuperclass)) {
                    // getName: com.hikvision.cms.modules.ops.point.action.OpsPointAction; getShortName: OpsPointAction
                    extractFactAndAction(sootClass.getShortName(), sootClass, factChain, actionChain);
                    break;
                }
                tmpClass = tmpClass.getSuperclass();
            } catch (Exception var5) {
                return;
            }
        }
    }

    private void extractAnnotations(SootClass sootClass, ArrayList<AnnotationTag> annotations, Collection<Fact> factChain, Collection<StrutsAction> actionChain) {
        for (AnnotationTag annotationTag : annotations) {
            if (annotationTag.getType().equals("Lorg/springframework/stereotype/Controller;")) {
                Collection<AnnotationElem> elems = annotationTag.getElems();
                String className = null;
                if (!elems.isEmpty()) {
                    for (AnnotationElem elem : elems) {
                        if (elem instanceof AnnotationStringElem) {
                            String value = ((AnnotationStringElem) elem).getValue();
                            if (value != null && value.contains("Action")) {
                                className = value;
                            }
                        }
                    }
                } else {
                    className = sootClass.getName();
                }
                extractFactAndAction(className, sootClass, factChain, actionChain);
            }
        }
    }

    private void extractFactAndAction(String className, SootClass sootClass, Collection<Fact> factChain, Collection<StrutsAction> actionChain) {
        List<String> targetMethods = GetPublicMethods(sootClass);
        Fact fact = new Fact();
        fact.setClassName(className);
        fact.setRoute("-");
        fact.setDescription("提取出Struts2相关Action");
        fact.setCredibility(3);
        fact.setMethod(targetMethods.toString());
        fact.setFactName(getName());
        factChain.add(fact);

        StrutsAction strutsAction = new StrutsAction();
        strutsAction.setActionName(className);
        strutsAction.setActionMethod(targetMethods);
        actionChain.add(strutsAction);
    }


    @Override
    public void prepare(Object object) {
        SootClass sootClass = (SootClass) object;
        String className = sootClass.getName();
        // className是含包名的类名，截取最后一个 . 后的内容，判断是否包含Action
        int lastIndex = className.lastIndexOf(".");
        if (lastIndex != -1 && lastIndex < className.length() - 1) {
            String suffix = className.substring(lastIndex + 1);
            // 判断截取的字符串是否包含 "Action" 字段
            if (suffix.endsWith("Action")) {
                this.setEnable(true);
            }else {
                this.setEnable(false);
            }
        } else {
            this.setEnable(false);
        }
    }

    private List<String> GetPublicMethods(SootClass sootClass){
        List<String> ClassMethods = new ArrayList<>();
        List<String> TargetMethods = new ArrayList<>();

        for (SootField field : sootClass.getFields()) { // 获取类中字段的名称
            String fieldName = field.getName();
            ClassMethods.add("set" + fieldName);
            ClassMethods.add("get" + fieldName);
        }

        for (SootMethod method : sootClass.getMethods()) { // 获取修饰符为public，并且不是setter和getter的方法
            if (!method.isPublic()) {
                continue;
            }

            boolean isExcludeMethod = ClassMethods.stream()
                    .anyMatch(clm -> method.getName().equalsIgnoreCase(clm) || method.getName().equals("init") || method.getName().equals("<init>"));


            if (!isExcludeMethod){
                TargetMethods.add(method.getName());
            }

        }
        return TargetMethods;
    }

    //    @Override
//    public void analysis(Object object, Collection<Fact> factChain, Collection<StrutsAction> actionChain) throws FactAnalyzerException {
//        SootClass sootClass = (SootClass) object;
//        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
//        if(visibilityAnnotationTag == null){
//            SootClass tmpClass = sootClass;
//            String targetSuperclass = "com.opensymphony.xwork2.ActionSupport";
//            while(!tmpClass.getName().equals("java.lang.Object") ) {
//                try {
//                    if (tmpClass.getName().equals(targetSuperclass)) {
//                        List<String> TargetMethods = GetPublicMethods(sootClass);
//                        Fact fact = new Fact();
//                        fact.setClassName(sootClass.getName());
//                        fact.setRoute("-");
//                        fact.setDescription("提取出Struts2相关Action");
//                        fact.setCredibility(3);
//                        fact.setMethod(TargetMethods.toString());
//                        fact.setFactName(getName());
//                        factChain.add(fact);
//                        // 增加StrutsAction，便于后续和StrutsXmlFactAnalyzer做乘积
//                        StrutsAction strutsAction = new StrutsAction();
//                        strutsAction.setActionName(sootClass.getName());
//                        strutsAction.setActionMethod(TargetMethods);
//                        actionChain.add(strutsAction);
//                    }
//                    tmpClass = tmpClass.getSuperclass();
//                } catch (Exception var5) {
//                    return;
//                }
//            }
//        }else {
//            String sootClassVisibilityAnnotationTagString = visibilityAnnotationTag.toString();
//            if(sootClassVisibilityAnnotationTagString.contains(PATTERN)){
//                for (Tag tag : sootClass.getTags()) {
//                    if (tag instanceof VisibilityAnnotationTag) {
//                        ArrayList<AnnotationTag> annotations= visibilityAnnotationTag.getAnnotations();
//                        for(AnnotationTag annotationTag: annotations){
//                            if (annotationTag.getType().equals("Lorg/springframework/stereotype/Controller;")){
//                                Collection<AnnotationElem> elems= annotationTag.getElems();
//                                String className = null;
//                                // 用于解析类注解@Controller("xxAction") 如果只是@Controller，则elems.isEmpty()为空
//                                if (!elems.isEmpty()){ //
//                                    for (AnnotationElem elem : elems) {
//                                        if (elem instanceof AnnotationStringElem){
//                                            String value = ((AnnotationStringElem) elem).getValue(); // Action名称
//                                            if (value != null && value.contains("Action")) { // 如果是struts的Action类
//                                                className = value;
//                                            }
//                                        }
//                                    }
//                                }else { //如果只是@Controller, 获取Action的类名，而不是获取@Controller中的命名
//                                    className = sootClass.getName();
//                                }
//                                List<String> TargetMethods = GetPublicMethods(sootClass);
//                                Fact fact = new Fact();
//                                fact.setClassName(className);
//                                fact.setRoute("-");
//                                fact.setDescription("提取出Struts2相关Action");
//                                fact.setCredibility(3);
//                                fact.setMethod(TargetMethods.toString());
//                                fact.setFactName(getName());
//                                factChain.add(fact);
//                                // 增加StrutsAction，便于后续和StrutsXmlFactAnalyzer做乘积
//                                StrutsAction strutsAction = new StrutsAction();
//                                strutsAction.setActionName(className);
//                                strutsAction.setActionMethod(TargetMethods);
//                                actionChain.add(strutsAction);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}