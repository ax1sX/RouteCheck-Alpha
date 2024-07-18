package project.entry;

import soot.SootClass;
import utils.Utils;

import java.util.*;

public class Module {

    private String name;
    private String classPath;
    private Set<String> classSet;

    private Map<SootClass, String> sootClassAndPathMap;

    private List<Config> configs;

    private Map<String, Config> configMap = new HashMap<>();

    private List<Jar> jars;

    private Map<String, Jar> jarMap = new HashMap<>();


    public Module(String classPath) {
        this.name = Utils.getModulePath(classPath);
        this.classPath = classPath;
        this.classSet = new HashSet<>();
        this.configs = new ArrayList<>();
        this.sootClassAndPathMap = new HashMap<>();
        this.jars = new ArrayList<>();
    }


    public Map<String, Jar> getJarMap() {
        return jarMap;
    }

    public String getName() {
        return name;
    }

    public String getClassPath() {
        return this.classPath;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Map<String, Config> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, Config> configMap) {
        this.configMap = configMap;
    }

    public void setConfigMap(String key, Config config) {
        this.configMap.put(key, config);
    }


    public List<Jar> getJars() {
        return jars.size() > 0 ? jars :  new ArrayList<Jar>();
    }

    public void addJar(Jar jar) {
        this.jars.add(jar);
    }

    public void setJars(List<Jar> jars) {
        this.jars = jars;
    }


    public void setJarMap(String key, Jar jar) {
        this.jarMap.put(key, jar);
    }


    public void addClass(String classPath) {
        this.classSet.add(classPath);
    }

    public List<String> getAllClass() {
        return new ArrayList<>(this.classSet);
    }

    public void addSootClassAndPath(SootClass sootClass, String classPath){
        this.sootClassAndPathMap.put(sootClass, classPath);
    }

    public String getClassBySootClass(SootClass sootClass){
        return this.sootClassAndPathMap.get(sootClass);
    }

    public List<SootClass> getAllSootClass(){
        return new ArrayList<SootClass>(this.sootClassAndPathMap.keySet());
    }

    public List<Config> getConfigs() {
        return configs.size() > 0 ? configs : new ArrayList<Config>();
    }

    public void addConfig(Config config) {
        this.configs.add(config);
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }


}
