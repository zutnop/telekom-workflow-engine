package ee.telekom.workflow.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;

@Controller
public class IndexController{

    @Autowired
    private WorkflowEngineConfiguration configuration;

    @RequestMapping("/")
    public String serveIndex1(){
        return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/status";
    }

    @RequestMapping("/console")
    public String serveIndex2(){
        return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/status";
    }

    @RequestMapping("/console/")
    public String serveIndex3(){
        return "redirect:" + configuration.getConsoleMappingPrefix() + "/console/status";
    }

    @RequestMapping(value = "/login")
    public String serveLoginPage(){
        return "login";
    }

}
