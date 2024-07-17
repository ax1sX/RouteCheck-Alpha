package project.entry;

import entry.Fact;
import entry.StrutsAction;

import java.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {
    private Map<Module, List<Fact>> moduleFactMap;
    private Map<String, Module> moduleMap;
    private Map<Module, List<StrutsAction>> moduleActionMap;
    private List<String> jspPaths;
    private String projectName;
    private String service;
    private String frameWork;

    public Project(String name) {
        this.projectName = name;
        this.service = "";
        this.frameWork = "";
        // 需要初始化，否则调用属性相关方法时会报错 NullPointerException
        moduleFactMap = new HashMap<>();
        moduleActionMap = new HashMap<>();
        moduleMap = new HashMap<>();
        jspPaths = new ArrayList<>();
    }

    public String getName(){
        return this.projectName;
    }

    public void addModule(Module module) {
        moduleMap.put(module.getName(), module);
        moduleFactMap.put(module, null);
        moduleActionMap.put(module, null);
    }

    public void addFactChainByModule(Module module, List<Fact> factChain) {
        moduleFactMap.put(module, factChain);
    }

    public List<Fact> getFactChainByModule(Module module) {
        return moduleFactMap.get(module);
    }

    public boolean isEmpty() {
        return moduleFactMap.isEmpty();
    }

    public List<Module> getAllModule() {
        return new ArrayList<>(moduleMap.values());
    }

    public Module getModuleByName(String moduleName) {
        return moduleMap.getOrDefault(moduleName, null);
    }

    public  void addModuleByName(Module module){
        moduleMap.put(module.getName(), module);
    }

    public Set<Map.Entry<Module, List<Fact>>> getAllModulesAndFactChains() {
        return moduleFactMap.entrySet();
    }

    public void addActionChainByModule(Module module, List<StrutsAction> actionChain) {
        moduleActionMap.put(module, actionChain);
    }

    public Set<Map.Entry<Module, List<StrutsAction>>> getAllActionChains() {
        return moduleActionMap.entrySet();
    }

    public List<StrutsAction> getActionChainByModule(Module module) {
        return moduleActionMap.get(module);
    }

    public void addJSPPath(String path) {
        jspPaths.add(path);
    }

    // 获取 JSP 文件路径列表
    public List<String> getJSPPaths() {
        return jspPaths;
    }

    public void setJSPPaths(List<String> jspPaths) {
        this.jspPaths = jspPaths;
    }

    public int getModuleCount() {
        return moduleMap.size();
    }

}
