package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Element;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.tagkit.*;

import java.util.*;

@FactAnalyzerAnnotations(
        name = "UnionStrutsActionFactAnalyzer"
)
public class UnionStrutsActionFactAnalyzer extends AbstractFactAnalyzer{
    static Map<String, List<String>> ActionMethods = new HashMap<>();
    private final String PATTERN = "Lorg/springframework/stereotype/Controller;";

    public UnionStrutsActionFactAnalyzer(String name, String type, String description) {
        super(name, type, description);
    }

    public UnionStrutsActionFactAnalyzer(){
        super(UnionStrutsActionFactAnalyzer.class.getName(), "class", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
//        ActionMethods.clear();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if(visibilityAnnotationTag == null)return;
        String sootClassVisibilityAnnotationTagString = visibilityAnnotationTag.toString();
        if(sootClassVisibilityAnnotationTagString.contains(PATTERN)){
            for (Tag tag : sootClass.getTags()) {
                if (tag instanceof VisibilityAnnotationTag) {
                    ArrayList<AnnotationTag> annotations= visibilityAnnotationTag.getAnnotations();
                    for(AnnotationTag annotationTag: annotations){
                        if (annotationTag.getType().equals("Lorg/springframework/stereotype/Controller;")){
                            Collection<AnnotationElem> elems= annotationTag.getElems();
                            for (AnnotationElem elem : elems) {
                                if (elem instanceof AnnotationStringElem){
                                    String value = ((AnnotationStringElem) elem).getValue(); // Action名称
                                    if (value != null && value.contains("Action")) { // 如果是struts的Action类
                                        List<String> TargetMethods = GetPublicMethods(sootClass);
//                                        ActionMethods.put(value,TargetMethods);
                                        Fact fact = new Fact();
                                        fact.setClassName(value);
                                        fact.setRoute("-");
                                        fact.setDescription("提取出Struts2相关Action");
                                        fact.setCredibility(3);
                                        /*考虑到这个doget dopost之类的这些方法解析不到，就默认设置了do*，没有把这个字段设置为空*/
                                        // TODO: 这里不应该设置成do*
                                        fact.setMethod(TargetMethods.toString());
                                        fact.setFactName(getName());
                                        factChain.add(fact);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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
            if (suffix.contains("Action")) {
                this.setEnable(true);
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

//            boolean isExcludeMethod = false;
//            for (String clm : ClassMethods) {
//                if (method.getName().equalsIgnoreCase(clm)) {
//                    isExcludeMethod = true;
//                    break;
//                }
//                if (method.getName().equals("init")){
//                    isExcludeMethod=true;
//                    break;
//                }
//                if (method.getName().equals("<init>")){
//                    isExcludeMethod=true;
//                    break;
//                }
//            }
            boolean isExcludeMethod = ClassMethods.stream()
                    .anyMatch(clm -> method.getName().equalsIgnoreCase(clm) || method.getName().equals("init") || method.getName().equals("<init>"));


            if (!isExcludeMethod){
                TargetMethods.add(method.getName());
            }

        }
        return TargetMethods;
    }
}
