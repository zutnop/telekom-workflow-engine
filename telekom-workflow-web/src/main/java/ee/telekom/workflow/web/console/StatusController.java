package ee.telekom.workflow.web.console;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import ee.telekom.workflow.web.console.model.MbeanAttributeModel;

@Controller
@RequestMapping("/console")
public class StatusController{

    private static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    @Autowired
    private MBeanServer mbeanServer;

    @RequestMapping("/status")
    public String viewEngineStatus( Model model ){
        try{
            Map<String, List<MbeanAttributeModel>> mbeans = new TreeMap<>();
            appendMbeanInformation( mbeans, "ee.telekom.workflowengine:*" );
            appendMbeanInformation( mbeans, "com.hazelcast:*" );
            model.addAttribute( "mbeans", mbeans );
        }
        catch( Exception e ){
            log.error( "MBeans querying failed", e );
            model.addAttribute( "error", "MBeans querying failed" );
        }
        return "console/status";
    }

    private void appendMbeanInformation( Map<String, List<MbeanAttributeModel>> mbeans, String selector ) throws Exception{
        Set<ObjectName> foundObjectNames = mbeanServer.queryNames( new ObjectName( selector ), null );

        for( ObjectName objectName : foundObjectNames ){
            MBeanInfo mbeanInfo = mbeanServer.getMBeanInfo( objectName );
            String name = objectName.getKeyProperty( "name" );
            if( name == null || mbeans.containsKey(name) ){
                name = name + ":" + mbeanInfo.getDescription();
            }
            List<MbeanAttributeModel> attributes = new ArrayList<>();
            mbeans.put( name, attributes );

            for( MBeanAttributeInfo attributeInfo : mbeanInfo.getAttributes() ){
                MbeanAttributeModel attribute = new MbeanAttributeModel();
                attribute.setName( attributeInfo.getName() );
                attribute.setDescription( attributeInfo.getDescription() );
                Object attr = null;
                try {
                    attr = mbeanServer.getAttribute(objectName, attribute.getName());
                } catch (JMException jme) {
                    log.warn("Error while getting attribute: " + attribute.getName(), jme);
                }
                attribute.setValue( attr );
                attributes.add( attribute );
            }
        }
    }

}
