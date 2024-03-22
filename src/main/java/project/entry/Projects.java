package project.entry;

import entry.Fact;

import java.util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Projects {
    private Map<Project, List<Fact>> projectFactMap;

    public Projects() {
        projectFactMap = new HashMap<>();
    }

    public void addProject(Project project) {
        projectFactMap.put(project, null); // 初始化为 null
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

    public static void main(String[] args) {

    }
}
