package smartcampus.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.Date;

@Path("/logs")
public class LogViewerResource {
    
    @GET
    @Produces("text/html")
    public String getLogs() {
        try {
            String logFile = System.getProperty("java.io.tmpdir") + "smart-campus-logs.txt";
            
            StringBuilder html = new StringBuilder();
            html.append("<html><head><title>Smart Campus Logs</title>");
            html.append("<meta http-equiv='refresh' content='2'>");
            html.append("<style>body{font-family:monospace;background:#1e1e1e;color:#d4d4d4;padding:20px;}");
            html.append("pre{background:#252526;padding:15px;border-radius:5px;}</style>");
            html.append("</head><body>");
            html.append("<h1>Smart Campus API - Live Logs</h1>");
            html.append("<p><strong>Log File:</strong> " + logFile + "</p>");
            html.append("<p><strong>Time:</strong> " + new Date() + "</p>");
            html.append("<hr>");
            
            File file = new File(logFile);
            if (!file.exists()) {
                html.append("<p style='color:red;'>Log file does not exist yet!</p>");
                html.append("<p>Filter has NOT been loaded.</p>");
            } else {
                html.append("<h2>Log Content:</h2>");
                html.append("<pre>");
                
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    html.append(line).append("\n");
                }
                br.close();
                
                html.append("</pre>");
            }
            
            html.append("</body></html>");
            return html.toString();
            
        } catch (Exception e) {
            return "<html><body><h1>Error</h1><pre>" + e.toString() + "</pre></body></html>";
        }
    }
}