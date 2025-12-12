package com.ohhell.ohhellapi.resources;

import com.ohhell.ohhellapi.services.BidService;
import com.ohhell.ohhellapi.models.Bid;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;

@Path("/v1/games/{gameId}/rounds/{roundNumber}/bids")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BidResource {

    @Inject
    private BidService bidService;

    // DTO para la petición de apuesta
    public static class BidRequest {
        private Long playerId;
        private Integer bidValue;

        // Getters y Setters
        public Long getPlayerId() { return playerId; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }

        public Integer getBidValue() { return bidValue; }
        public void setBidValue(Integer bidValue) { this.bidValue = bidValue; }
    }

    /**
     * GET: Obtener todas las apuestas de una ronda
     */
    @GET
    public Response getAllBids(
            @PathParam("gameId") Long gameId,
            @PathParam("roundNumber") int roundNumber) {

        try {
            // Nota: Necesitarás un método en BidService que encuentre el roundId
            // basado en gameId y roundNumber
            // Por ahora asumimos que roundNumber es el ID de la ronda
            Long roundId = Long.valueOf(roundNumber);

            List<Bid> bids = bidService.getBidsByRound(roundId);
            return Response.ok(bids).build();

        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * POST: Hacer una apuesta
     */
    @POST
    public Response makeBid(
            @PathParam("gameId") Long gameId,
            @PathParam("roundNumber") int roundNumber,
            BidRequest bidRequest) {

        try {
            // Validaciones básicas del request
            if (bidRequest == null ||
                    bidRequest.getPlayerId() == null ||
                    bidRequest.getBidValue() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"playerId y bidValue son requeridos\"}")
                        .build();
            }

            // Convertir roundNumber a roundId (ajusta según tu lógica)
            Long roundId = Long.valueOf(roundNumber);

            // DELEGAR TODO AL SERVICE
            Bid bid = bidService.placeBid(
                    bidRequest.getPlayerId(),
                    roundId,
                    bidRequest.getBidValue()
            );

            return Response.status(Response.Status.CREATED)
                    .entity(bid)
                    .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error interno: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * GET: Obtener apuesta específica de un jugador
     */
    @GET
    @Path("/player/{playerId}")
    public Response getPlayerBid(
            @PathParam("gameId") Long gameId,
            @PathParam("roundNumber") int roundNumber,
            @PathParam("playerId") Long playerId) {

        try {
            Long roundId = Long.valueOf(roundNumber);
            Bid bid = bidService.getBidByPlayerInRound(playerId, roundId);

            if (bid == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Apuesta no encontrada\"}")
                        .build();
            }

            return Response.ok(bid).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}