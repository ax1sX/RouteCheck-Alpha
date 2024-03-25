package project.entry;

import entry.Fact;
import entry.StrutsAction;

import java.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Projects {
    private Map<Project, List<Fact>> projectFactMap;
    private Map<Project, List<StrutsAction>> projectActionMap;

    public Projects() {
        // 需要初始化，否则调用属性相关方法时会报错 NullPointerException
        projectFactMap = new HashMap<>();
        projectActionMap = new HashMap<>();
    }

    public void addProject(Project project) {
        projectFactMap.put(project, null);
        projectActionMap.put(project, null);
    }

    public void addFactChain(Project project, List<Fact> factChain) {
        projectFactMap.put(project, factChain);
    }

    public List<Fact> getFactChain(Project project) {
        return projectFactMap.get(project);
    }

    public boolean isEmpty() {
        return projectFactMap.isEmpty();
    }

    public Set<Project> getProjects() {
        return projectFactMap.keySet();
    }

    public Set<Map.Entry<Project, List<Fact>>> getAllProjectsAndFactChains() {
        return projectFactMap.entrySet();
    }

    public void addActionChain(Project project, List<StrutsAction> actionChain) {
        projectActionMap.put(project, actionChain);
    }

    public Set<Map.Entry<Project, List<StrutsAction>>> getAllActionChains() {
        return projectActionMap.entrySet();
    }

    public List<StrutsAction> getActionChain(Project project) {
        return projectActionMap.get(project);
    }
}
