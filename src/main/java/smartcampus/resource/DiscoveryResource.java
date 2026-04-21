package smartcampus.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces("application/json")
public class DiscoveryResource {

    @GET
    public Map<String, Object> getInfo() {

        Map<String, Object> data = new HashMap<>();

        data.put("version", "v1");
        data.put("name", "Smart Campus Sensor & Room Management API");
        data.put("description", "RESTful API for managing campus rooms and sensors");
        data.put("contact", "smartcampus-admin@westminster.ac.uk");

        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        data.put("resources", links);

        return data;
    }
}