package reporting;

import entry.Fact;
import entry.Settings;
import entry.StrutsAction;
import exceptions.ReportingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import project.entry.Project;
import project.entry.Projects;
import utils.Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonReportGenerator extends AbstractReportGenerator{

    @Override
    public void initialize(Projects projects, Settings settings) {
        super.initialize(projects, settings);
    }

    @Override
    public void write() throws ReportingException {
        List<String> Routes = new ArrayList<>();
        try {
            String out = this.settings.getOutPutDirectory();
            Utils.mkDir(out);
            String filePath = out + File.separator  + "test.json";
//            Map<String, Object> context = new HashMap<>();
            JSONObject json = new JSONObject();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
            String dataStr = df.format(date);
            json.put("date", dataStr);
            String prevProjectName = null;
            String extractedName = null;
            // 如果是projects添加根路由，如果是单项目不加根路由
            Set<Map.Entry<Project, List<Fact>>> projectsChain = this.projects.getAllProjectsAndFactChains();
//            Set<Map.Entry<Project, List<StrutsAction>>> actionsChain = this.projects.getAllActionChains();
            JSONArray projectsArray = new JSONArray();

            for (Map.Entry<Project, List<Fact>> entry : projectsChain) {
                List<StrutsAction> actions = this.projects.getActionChain(entry.getKey());
                String projectName = entry.getKey().getName(); // projectName包含/xx/xx/a，截取最后一个/后的内容
//                json.put("project", projectName);
                projectsArray.put(projectName);
                int lastSlashIndex = projectName.lastIndexOf('/');
                if (lastSlashIndex != -1 && lastSlashIndex < projectName.length() - 1) {
                    extractedName = projectName.substring(lastSlashIndex + 1);
                    extractedName = "/" + extractedName; // 在项目根目录前加上/
                }
                List<Fact> factChain = entry.getValue();
                for (Fact fact : factChain) {
                    if (!Objects.equals(prevProjectName, projectName)){
//                        String routes = fact.getRoutes().toString();
                        String routes = String.join("", fact.getRoutes());
                        String className = fact.getClassName();
                        String methodName = fact.getMethod();

                        if (fact.getFactName().equals("factAnalyzer.StrutsXmlFactAnalyzer")){
                            /**
                             * 1. 如果格式为：/carQuery/*_*.action	{1}Action	{2}， 遍历actions
                             * 2. 如果格式为：/portal/portal_*.action	portalAction	{1} 找到actions中这个action对应的路由
                             */
                            if (className.contains("{1}")){
                                if (methodName.contains("{2}")){
                                    for (StrutsAction action: actions){
                                        String acName = action.getActionName();
                                        int index = acName.lastIndexOf("Action");
                                        if (index != -1) {
                                            acName = acName.substring(0, index); // 截取Action前的内容
                                        }
                                        List<String> acMethods = action.getActionMethod();
                                        for (String method: acMethods){
                                            String replacedRoute = routes.replaceFirst("\\*", acName).replaceFirst("\\*", method);
//                                            String replacedRoute = routes.replace("*", acName).replace("*", method);
                                            Routes.add(extractedName + replacedRoute);
                                        }
                                    }
                                }
                            } else if (methodName.contains("{1}")) {
                                for (StrutsAction action: actions){
                                    String acName = action.getActionName();
                                    if (acName.equals(className)){
                                        List<String> acMethods = action.getActionMethod();
                                        for (String method: acMethods){
                                            String replacedRoute2 = routes.replace("*", method);
                                            Routes.add(extractedName + replacedRoute2);
                                        }
                                    }

                                }
                            }else { // 不包含{},直接提取路由
                                Routes.add(routes);
                            }

                        } else if (fact.getFactName().equals("factAnalyzer.StrutsActionFactAnalyzer")) {
                            // UnionStrutsActionFactAnalyzer的结果不遍历，这是用于html的
                            continue;
                        }else {
                            Routes.add(extractedName + routes);
                        }

                    }
                }
                prevProjectName = projectName;
                JSONArray routesArray = new JSONArray();
                for (String route : Routes) {
                    routesArray.put(route);
                }
                json.put("project", projectsArray);
                json.put("Routes", routesArray);

            }
            /**
             * {
             *     "project": "xxx",
             *     "Routes": [],
             * }
             *
             */


//            context.put("project", this.project.getName());
//            List<Map> facts = new ArrayList<>();
//            this.factChain.forEach(fact -> {
//                facts.add(Utils.objectToMap(fact));
//            });
//            context.put("facts", facts);
//            context.put("date", dataStr);
//            JSONObject jsonObject = new JSONObject(context);
            String jsonString = json.toString(4);
            jsonString = jsonString.replace("\\/", "/");
            Utils.fileWriter(filePath, jsonString);
        }catch (Exception e){
            throw new ReportingException("");
        }
    }
}
