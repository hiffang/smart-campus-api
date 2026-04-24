/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampus.resources;

import com.mycompany.smartcampus.exception.RoomNotEmptyException;
import jakarta.ws.rs.Path;
import com.mycompany.smartcampus.model.Room;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hiffan
 */

@Path("/rooms")
public class RoomResource {
    
    private static final Map<String, Room> roomStore = new HashMap<>();
    
    static {
        Room room1 = new Room("LIB-301", "Library Quiet Study", 30);
        Room room2 = new Room("LAB-101", "Computer Lab A", 40);
        roomStore.put(room1.getId(), room1);
        roomStore.put(room2.getId(), room2);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getAllRooms() {
        return new ArrayList<>(roomStore.values());
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room ID required")
                    .build();
        }
        
        if (roomStore.containsKey(room.getId())){
            return Response.status(Response.Status.CONFLICT)
                    .entity("Room with ID" + room.getId() + "already exists")
                    .build();
        }
        
        roomStore.put(room.getId(), room);
        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }
    
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomStore.get(roomId);
        
        if(room == null ){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room Not FOund")
                    .build();
        }
        
        return Response.ok(room).build();   
    }
    
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = roomStore.get(roomId);
        
        if (room == null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        
        if(!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
            "Cannot delete room " + roomId + ". It contains " + 
                room.getSensorIds().size() + " active sensor(s)."
            );
        }
        
        roomStore.remove(roomId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    
    public static Map<String, Room> getRoomStore() {
        return roomStore;
    }
    
}
