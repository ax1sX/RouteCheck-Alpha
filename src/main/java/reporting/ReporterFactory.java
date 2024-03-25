package reporting;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReporterFactory {
    public static Map<String, ReportGenerator> getReportGenerator(String type){
        if(type.toLowerCase(Locale.ROOT).equals("html")){
            Map<String, ReportGenerator> generatorHtmlMap = new HashMap<>();
            generatorHtmlMap.put("html", new HtmlReportGenerator());
            return generatorHtmlMap;
        }

        if(type.toLowerCase(Locale.ROOT).equals("json")){
            Map<String, ReportGenerator> generatorJsonMap = new HashMap<>();
            generatorJsonMap.put("json", new JsonReportGenerator());
            return generatorJsonMap;
        }

        if(type.toLowerCase(Locale.ROOT).equals("all")){
            Map<String, ReportGenerator> generatorMap = new HashMap<>();
            generatorMap.put("html", new HtmlReportGenerator());
            generatorMap.put("json", new JsonReportGenerator());
            return generatorMap;
        }
        return null;
    }
}
