package project.entry;

import soot.SootClass;

import java.util.*;

public class Project {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private Set<SootClass> classes;

    public Map<SootClass, String> getClassesToPath() {
        return classesToPath;
    }

    public void setClassesToPath(SootClass sootClass, String classPath) {
        this.classesToPath.put(sootClass, classPath);
    }
    public void setClassesToPath(Map<SootClass, String> classesToPath) {
        this.classesToPath = classesToPath;
    }

    private Map<SootClass, String> classesToPath = new HashMap<>();

    private Collection<Config> configs;

    public Map<String, Config> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, Config> configMap) {
        this.configMap = configMap;
    }

    public void setConfigMap(String key, Config config) {
        this.configMap.put(key, config);
    }

    private Map<String, Config> configMap = new HashMap<>();

    private String service;

    private String frameWork;

    public Collection<Jar> getJars() {
        return jars.size() >0 ? jars : new ArrayList<>();
    }

    public void addJar(Jar jar){
        this.jars.add(jar);
    }

    public void setJars(Collection<Jar> jars) {
        this.jars = jars;
    }

    private Collection<Jar> jars;

    public Map<String, Jar> getJarMap() {
        return jarMap;
    }

    public void setJarMap(String key, Jar jar) {
        this.jarMap.put(key, jar);
    }

    private Map<String, Jar> jarMap = new HashMap<>();

    public Project(){
        this.configs = new ArrayList<>();
        this.classes = new HashSet<>();
        this.service = "";
        this.frameWork = "";
        this.jars = new ArrayList<>();
    }

    public Project(String name, Set<SootClass> classes, Collection<Config> configs, String service, String frameWork) {
        this.name = name;
        this.classes = classes;
        this.configs = configs;
        this.service = service;
        this.frameWork = frameWork;
    }

    public Set<SootClass> getClasses() {
        return classes.size() > 0 ? classes : new HashSet<>();
    }

    public void setClasses(Set<SootClass> classes) {
        this.classes = classes;
    }

    public Collection<Config> getConfigs() {
        return configs.size()> 0 ? configs:  new ArrayList<>();
    }

    public void addConfig(Config config){
        this.configs.add(config);
    }

    public void setConfigs(Collection<Config> configs) {
        this.configs = configs;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getFrameWork() {
        return frameWork;
    }

    public void setFrameWork(String frameWork) {
        this.frameWork = frameWork;
    }
}
