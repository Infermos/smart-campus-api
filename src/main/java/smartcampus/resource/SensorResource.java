package smartcampus.resource;

import smartcampus.model.Sensor;
import smartcampus.repository.DataStore;
import smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.Collection;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces("application/json")
public class SensorResource {

    @POST
    @Consumes("application/json")
    public Response create(Sensor sensor) {

        // Validate that the referenced room exists
        if (!DataStore.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room ID does not exist: " + sensor.getRoomId());
        }

        DataStore.sensors.put(sensor.getId(), sensor);

        // Add sensor ID to the room's sensor list
        DataStore.rooms.get(sensor.getRoomId())
                .getSensorIds().add(sensor.getId());

        return Response.status(201).entity(sensor).build();
    }

    @GET
    public Collection<Sensor> getAll(@QueryParam("type") String type) {

        if (type == null) {
            return DataStore.sensors.values();
        }

        // Filter by sensor type
        return DataStore.sensors.values()
                .stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    @Path("/{id}/readings")
    public SensorReadingResource getReadings() {
        return new SensorReadingResource();
    }
}