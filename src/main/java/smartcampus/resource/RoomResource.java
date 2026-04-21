package smartcampus.resource;

import smartcampus.model.Room;
import smartcampus.repository.DataStore;
import smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.Collection;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

@Path("/rooms")
@Produces("application/json")
public class RoomResource {

    private static final String LOG_FILE = "C:\\smart-campus-filter-debug.txt";

    @GET
    public Collection<Room> getAll() {
        writeToFile(">>> RoomResource: GET /rooms called");
        System.err.println(">>> RoomResource: GET /rooms called");
        return DataStore.rooms.values();
    }

    @POST
    @Consumes("application/json")
    public Response create(Room room) {
        writeToFile(">>> RoomResource: POST /rooms called - Creating room: " + room.getId());
        System.err.println(">>> RoomResource: POST /rooms called - Creating room: " + room.getId());
        
        DataStore.rooms.put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    @GET
    @Path("/{id}")
    public Response getOne(@PathParam("id") String id) {
        writeToFile(">>> RoomResource: GET /rooms/" + id + " called");
        System.err.println(">>> RoomResource: GET /rooms/" + id + " called");
        
        Room room = DataStore.rooms.get(id);
        if (room == null) {
            return Response.status(404).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        writeToFile(">>> RoomResource: DELETE /rooms/" + id + " called");
        System.err.println(">>> RoomResource: DELETE /rooms/" + id + " called");

        Room room = DataStore.rooms.get(id);

        // Check if room has sensors before deletion
        if (room != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room with active sensors");
        }

        DataStore.rooms.remove(id);
        return Response.ok().build();
    }
    
    private static void writeToFile(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}