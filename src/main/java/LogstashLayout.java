import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Plugin(name = "LogstashLayout", category = "Core", elementType = "layout", printObject = true)
public class LogstashLayout extends AbstractStringLayout {

    private ObjectMapper mapper = new ObjectMapper();
    private ObjectWriter objectWriter = mapper.writer();
    private String appName;
    private boolean includeMDC;

    protected LogstashLayout(String appName, boolean includeMDC, Charset charset) {
        super(charset);
        this.includeMDC = includeMDC;
        this.appName = appName;
    }

    /**
     Example:

     {
     ...
     "_source": {
         "server": "X",
         "level": "INFO",
         "message": "Test message",
         "type": "log4j2",
         "@timestamp": "2017-05-03T05:45:28.241Z",
         "appname": "test_app",
         "thread_name": "testThread-63318",
         "level_value": 20000,
         "@version": 1,
         "host": "123.456.789.123",
         "logger_name": "app.foo"
     }
     ...
     }

     */

    public String toSerializable(LogEvent logEvent) {
        try {
            // Grep data from event
            ObjectNode node = mapper.createObjectNode();

            String timestampAsString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .format(new Date(logEvent.getTimeMillis()));


            // Put in default values
            node.put("level", logEvent.getLevel().toString()); // level
            node.put("level_value", logEvent.getLevel().intLevel()); // level_value
            node.put("logger_name", logEvent.getLoggerName()); // logger_name
            node.put("type", "log4j2");
            node.put("appname", this.appName); // Append "appname"
            node.put("@timestamp", timestampAsString);
            node.put("@version", "1");
            node.put("thread_name", logEvent.getThreadName());
            node.put("message", logEvent.getMessage().toString());
            // What about host, sever and port?

            if (this.includeMDC == true) {
                Map<String, String> map = logEvent.getContextMap();

                for (Map.Entry<String, String> entry : map.entrySet()) {
                    node.put(entry.getKey(), entry.getValue());
                }
            }

            return objectWriter.writeValueAsString(node);
        } catch (JsonProcessingException e) {
		    System.out.println("Error 1: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    @PluginFactory
    public static LogstashLayout createLayout(
            @PluginAttribute(value = "appname") String appName,
			@PluginAttribute(value = "includeMDC") boolean includeMDC,
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
        return new LogstashLayout(appName, includeMDC, charset);
    }
}
