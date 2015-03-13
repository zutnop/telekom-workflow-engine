package ee.telekom.workflow.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController{

    @RequestMapping("/")
    public String serveIndex1(){
        return "redirect:/console/status";
    }

    @RequestMapping("/console")
    public String serveIndex2(){
        return "redirect:/console/status";
    }

    @RequestMapping("/console/")
    public String serveIndex3(){
        return "redirect:/console/status";
    }

    @RequestMapping(value = "/login")
    public String serveLoginPage(){
        return "login";
    }

}
