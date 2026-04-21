package smartcampus.resource;

import smartcampus.model.Sensor;
import smartcampus.model.SensorReading;
import smartcampus.repository.DataStore;
import smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.ArrayList;
import java.util.List;

@Produces("application/json")
public class SensorReadingResource {

    @GET
    public Response get(@PathParam("id") String id) {

        Sensor sensor = DataStore.sensors.get(id);

        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }

        return Response.ok(DataStore.readings.getOrDefault(id, new ArrayList<>())).build();
    }

    @POST
    @Consumes("application/json")
    public Response add(@PathParam("id") String id, SensorReading reading) {

        Sensor sensor = DataStore.sensors.get(id);

        if (sensor == null) {
            return Response.status(404)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }

        // Check if sensor is in MAINTENANCE mode
        if ("MAINTENANCE".equals(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is in MAINTENANCE mode and cannot accept readings");
        }

        // Add the reading to the sensor's history
        DataStore.readings
                .computeIfAbsent(id, k -> new ArrayList<>())
                .add(reading);

        // Update the sensor's current value
        sensor.setCurrentValue(reading.getValue());

        return Response.status(201).build();
    }
}