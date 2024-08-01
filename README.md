2023年初我和@suizhibo完成了RouteCheck的雏形，目的是用于分析和提取源码中所有的访问路由。

磨磨唧唧一直没改完，先发布了一版适合扫国内java源码的。这个版本的限制是要求源码中采用的是包含`/WEB-INF/`的目录格式。

现在使用还是需要传递个`-sp`参数，来包含`settings.yaml`的路径。这个在后续的版本中会合并到jar中。
该配置文件主要用来定义需要用到的分析器、路由的输出位置和格式。
```
factAnalyzers:
  default: [ApacheCXFFactAnalyzer, ApacheWinkFactAnalyzer, GuiceServletFactAnalyzer,
            JerseyFactAnalyzer, RESTEasyFactAnalyzer, RestletFactAnalyzer, JAXRSFactAnalyzer,
            SpringBeanFactAnalyzer,SpringMVCAnnotationFactAnalyzer,
            StrutsXmlFactAnalyzer, WSDDFactAnalyzer, WSDLFactAnalyzer, WebXmlFactAnalyzer, SOAPUnionFactAnalyzer,
            StrutsActionFactAnalyzer, HttpServletFactAnalyzer,
            UnionWebServiceFactAnalyzer, UnionServletFactAnalyzer]
outPutDirectory: ./output
tempDirectory: ./output
reportType: all
```

# Usage

```text
.______        ______    __    __  .___________. _______   ______  __    __   _______   ______  __  ___ 
|   _  \      /  __  \  |  |  |  | |           ||   ____| /      ||  |  |  | |   ____| /      ||  |/  / 
|  |_)  |    |  |  |  | |  |  |  | `---|  |----`|  |__   |  ,----'|  |__|  | |  |__   |  ,----'|  '  /  
|      /     |  |  |  | |  |  |  |     |  |     |   __|  |  |     |   __   | |   __|  |  |     |    <   
|  |\  \----.|  `--'  | |  `--'  |     |  |     |  |____ |  `----.|  |  |  | |  |____ |  `----.|  .  \  
| _| `._____| \______/   \______/      |__|     |_______| \______||__|  |__| |_______| \______||__|\__\ 
                                                                                                        
usage: java -jar RouteCheck.jar [-h] [-pf <arg>] [-pp <arg>]
 -h,--help                    打印命令行帮助信息
 -pf,--package-prefix <arg>   项目的包名特征前缀,在解析jar时作为匹配特征
 -pp,--project-path <arg>     项目路径

```
Default Usage
```text
 java -jar RouteCheck.jar -pp /红海ehr/201910161726_RedseaPlatform -pf red.sea
```

RouteCheck supports two output formats: HTML and JSON.

HTML
![html示例图片](./img/img.png)

JSON
![json示例图片](./img/json.png)

