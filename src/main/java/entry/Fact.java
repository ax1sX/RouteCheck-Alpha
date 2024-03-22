package entry;

import utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Fact{

    private String md5;
    private String className;
    private List<String> routes;
    private String description;
    private int credibility;

    public String getFactName() {
        return factName;
    }

    public void setFactName(String factName) {
        this.factName = factName;
    }

    private String factName;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private String method;

    public Fact(){
        this.routes = new ArrayList<>();
    }
    public Fact(String md5, String className, List<String> routes, String description, int credibility) {
        this.md5 = md5;
        this.className = className;
        this.routes = routes;
        this.description = description;
        this.credibility = credibility;
    }

    public String getMD5() {
        return Utils.getMD5Str(this.className + this.method + String.join("", this.routes));
    }


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public void setRoute(String route){
        this.routes.add(route);
    }
    public void setRoutes(List<String> routes) {
        this.routes = routes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCredibility() {
        return credibility;
    }

    public void setCredibility(int credibility) {
        this.credibility = credibility;
    }
}
