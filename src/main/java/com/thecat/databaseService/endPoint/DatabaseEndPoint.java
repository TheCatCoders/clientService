package com.thecat.databaseService.endPoint;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.thecat.databaseService.entities.User;
import com.thecat.databaseService.entities.UserJson;
import com.thecat.databaseService.services.impl.DatabaseService;

/**
 * API use to manage interaction with the database
 *
 * @author froberge
 * @since December 2017
 */
@Path("/db")
public class DatabaseEndPoint {


    /**
     * select action 
     *
     * @param {@link UserJson} user
     * @return {@link UserJson}
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path( "/select" )
    public Response select(UserJson user) {
        if (user != null) {
            User u = DatabaseService.getInstance().select(user.getEmailAdr(), user.getPassword());

            if (u == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else {
                return Response.ok()
                        .entity(parseResponse(u))
                        .build();
            }
        } else {
            return Response.status(Response.Status.NO_CONTENT)
                    .entity("No user specify")
                    .build();
        }
    }

    /**
     * register action 
     *
     * @param {@link UserJson} user
     * @return
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path( "/register" )
    public Response register(UserJson user) {
        if (user != null) {
            boolean b = DatabaseService.getInstance().register(user);

            if (b ) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity( "Could not Register, please try again" )
                        .build();
            }
        } else {
            return Response.status(Response.Status.NO_CONTENT)
                    .entity("No user specify")
                    .build();
        }
    }

    
    /**
     * Parse the the response to a proper JSON element.
     *
     * @param u {@link User}
     * @return {@link UserJson}
     */
    private UserJson parseResponse(User u) {
        UserJson uj = new UserJson();
        uj.setAge(Integer.toString(u.getAge()));
        uj.setEmailAdr(u.getEmailAddress());
        uj.setGender(u.getGender().name());
        uj.setPassword(u.getPassword());
        uj.setUsername(u.getName());

        return uj;
    }
}
