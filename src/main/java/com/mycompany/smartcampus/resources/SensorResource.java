/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

/**
 *
 * @author Hiffan
 */
import com.mycompany.smartcampus.exception.LinkedResourceNotFoundException;
import com.mycompany.smartcampus.model.Room;
import com.mycompany.smartcampus.model.Sensor;
import com.mycompany.smartcampus.model.SensorReading;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Path("/sensors")
public class SensorResource {
    
    private static final Map<String, Sensor> sensorStore = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> readingsStore = new ConcurrentHashMap<>();
    
    static {
        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "ACTIVE", 450.0, "LAB-101");
        sensorStore.put(sensor1.getId(), sensor1);
        sensorStore.put(sensor2.getId(), sensor2);
        
        Room room1 = RoomResource.getRoomStore().get("LIB-301");
        Room room2 = RoomResource.getRoomStore().get("LAB-101");
        if (room1 != null) room1.addSensor(sensor1.getId());
        if (room2 != null) room2.addSensor(sensor2.getId());
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(sensorStore.values());
        
        if (type != null && !type.isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        
        return sensors;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Sensor ID is required")
                    .build();
        }
        
        if (sensor.getRoomId() == null || !RoomResource.getRoomStore().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Cannot create sensor. Room with ID '" + sensor.getRoomId() + "' does not exist."
            );
        }
        
        if (sensorStore.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Sensor with ID " + sensor.getId() + " already exists")
                    .build();
        }
        
        sensorStore.put(sensor.getId(), sensor);
        
        Room room = RoomResource.getRoomStore().get(sensor.getRoomId());
        room.addSensor(sensor.getId());
        
        readingsStore.put(sensor.getId(), new ArrayList<>());
        
        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }
    
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorStore.get(sensorId);
        
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor not found")
                    .build();
        }
        
        return Response.ok(sensor).build();
    }
    
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        if (!sensorStore.containsKey(sensorId)) {
            throw new NotFoundException("Sensor not found");
        }
        return new SensorReadingResource(sensorId);
    }
    
    public static Map<String, Sensor> getSensorStore() {
        return sensorStore;
    }
    
    public static Map<String, List<SensorReading>> getReadingsStore() {
        return readingsStore;
    }
}
