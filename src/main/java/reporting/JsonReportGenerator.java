package reporting;

import entry.Fact;
import entry.Settings;
import entry.StrutsAction;
import exceptions.ReportingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Project;
import project.entry.Module;
import utils.Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class JsonReportGenerator extends AbstractReportGenerator{
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonReportGenerator.class);

    @Override
    public void initialize(Project project, Settings settings) {
        super.initialize(project, settings);
    }

    @Override
    public void write() throws ReportingException {
        List<String> Routes = new ArrayList<>();
        try {
            String out = this.settings.getOutPutDirectory();
//            String projectPath = this.command.getProjectPath();
            Utils.mkDir(out);
            String filePath = out + File.separator  + "test.json";
            JSONObject json = new JSONObject();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
            String dataStr = df.format(date);
            json.put("date", dataStr);
            String prevProjectName = null;
            String extractedName = "";
            // 如果是projects添加根路由，如果是单项目不加根路由
            Set<Map.Entry<Module, List<Fact>>> projectsChain = this.project.getAllModulesAndFactChains();
            JSONArray projectsArray = new JSONArray();

            for (Map.Entry<Module, List<Fact>> entry : projectsChain) {
                List<StrutsAction> actions = this.project.getActionChainByModule(entry.getKey());
                String projectName = entry.getKey().getName();
                projectsArray.put(projectName);
                if (projectName != null && projectName.length() > 0){
                    int lastSlashIndex = projectName.lastIndexOf('/');
                    if (lastSlashIndex != -1 && lastSlashIndex < projectName.length() - 1) {  // projectName包含/b/a，截取最后一个/后的内容
                        extractedName = projectName.substring(lastSlashIndex + 1);
                        extractedName = "/" + extractedName; // 在项目根目录前加上/
                    } else if (lastSlashIndex != -1 && lastSlashIndex == projectName.length() -1) { // projectName为/b/a/
                        int secondLastSlashIndex = projectName.substring(0, lastSlashIndex).lastIndexOf('/');
                        if (secondLastSlashIndex != -1) {
                            extractedName = projectName.substring(secondLastSlashIndex + 1, lastSlashIndex);
                            extractedName = "/" + extractedName; // 在项目根目录前加上/
                        }
                    }
                }

                if (this.project.getModuleCount() == 1){
                    extractedName = "";
                }

                List<Fact> factChain = entry.getValue();
                for (Fact fact : factChain) {
                    if (!Objects.equals(prevProjectName, projectName)){
                        try{
                            String routes = String.join("", fact.getRoutes());
                            String className = fact.getClassName();
                            String methodName = fact.getMethod();

                            if (fact.getFactName().equals("factAnalyzer.StrutsXmlFactAnalyzer")){
                                /**
                                 * 1. 如果格式为：/carQuery/*_*.action	{1}Action	{2}， 遍历actions
                                 * 2. 如果格式为：/portal/portal_*.action	portalAction	{1} 找到actions中这个action对应的路由
                                 * 3. 如果格式为：ccsAction!* ccsShareAction {1} 同2
                                 */
                                if (className != null && className.contains("{1}")){
                                    if (methodName != null &&methodName.contains("{2}")){
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
                                } else if (methodName !=null && methodName.contains("{1}")) {
                                    for (StrutsAction action: actions){
                                        String acName = action.getActionName();
                                        if (getSimpleName(acName).equals(getSimpleName(className))){
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
                                // StrutsActionFactAnalyzer的结果不遍历，这是用于html的
                                continue;
                            }else {
                                Routes.add(extractedName + routes);
                            }
                        }catch (Exception e){
                            LOGGER.info(e.getMessage());
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

            List<String> jspPaths = this.project.getJSPPaths();
            if (jspPaths != null && !jspPaths.isEmpty()) {
                JSONArray jspPathsArray = new JSONArray();
                for (String path : jspPaths) {
                    jspPathsArray.put(path);
                }
                json.put("jspPaths", jspPathsArray);
            }

            String jsonString = json.toString(4);
            jsonString = jsonString.replace("\\/", "/");
            Utils.fileWriter(filePath, jsonString);
        }catch (Exception e){
            LOGGER.info(e.getMessage());
        }
    }

    public static String getSimpleName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return className.substring(lastDotIndex + 1);
        } else {
            return className; // 如果没有`.`，则返回原始className
        }
    }
}
