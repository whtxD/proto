package com.ohhell.ohhellapi.services;

import com.ohhell.ohhellapi.dao.BidDAO;
import com.ohhell.ohhellapi.dao.RoundDAO;
import com.ohhell.ohhellapi.dao.PlayerDAO;
import com.ohhell.ohhellapi.models.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class BidService {

    @Inject
    private BidDAO bidDAO;

    @Inject
    private RoundDAO roundDAO;

    @Inject
    private PlayerDAO playerDAO;

    /**
     * Método PRINCIPAL: Un jugador hace su apuesta
     */
    @Transactional
    public Bid placeBid(Long playerId, Long roundId, int bidAmount) {
        // 1. Validar que existen
        Player player = playerDAO.findById(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Jugador no encontrado");
        }

        Round round = roundDAO.findById(roundId);
        if (round == null) {
            throw new IllegalArgumentException("Ronda no encontrada");
        }

        // 2. Validar fase de la ronda
        if (!"BIDDING".equals(round.getStatus())) {
            throw new IllegalStateException("La ronda no está en fase de apuestas");
        }

        // 3. Validar que el jugador no ha apostado ya
        Bid existingBid = bidDAO.findByPlayerAndRound(player, round);
        if (existingBid != null) {
            throw new IllegalStateException("El jugador ya ha apostado en esta ronda");
        }

        // 4. Validar rango de apuesta (0 a cartas por jugador)
        if (bidAmount < 0 || bidAmount > round.getCardsPerPlayer()) {
            throw new IllegalArgumentException(
                    "La apuesta debe estar entre 0 y " + round.getCardsPerPlayer()
            );
        }

        // 5. Validar REGLA CRÍTICA de Oh Hell!
        if (!isValidBid(round, bidAmount, player)) {
            throw new IllegalStateException(
                    "Apuesta inválida: La suma de todas las apuestas no puede ser igual al número de cartas"
            );
        }

        // 6. Crear y guardar la apuesta
        Bid bid = new Bid(player, round, bidAmount);
        bidDAO.save(bid);

        // 7. Si todos han apostado, cambiar fase de la ronda
        List<Player> playersInGame = playerDAO.findByGame(round.getGame());
        long activePlayers = playersInGame.stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .count();

        if (bidDAO.countBidsInRound(round) == activePlayers) {
            round.setStatus("PLAYING");
            roundDAO.save(round);
        }

        return bid;
    }

    /**
     * VALIDACIÓN CRÍTICA: Regla del Oh Hell!
     */
    private boolean isValidBid(Round round, int newBidAmount, Player newPlayer) {
        List<Bid> existingBids = bidDAO.findByRound(round);
        List<Player> activePlayers = playerDAO.findByGame(round.getGame()).stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .toList();

        // Calcular suma actual
        int currentSum = existingBids.stream()
                .mapToInt(Bid::getBidAmount)
                .sum();

        int totalPlayers = activePlayers.size();
        int cardsPerPlayer = round.getCardsPerPlayer();

        // Si NO es el último jugador
        if (existingBids.size() < totalPlayers - 1) {
            return true;  // Cualquier apuesta válida es aceptable
        }

        // Si ES el último jugador
        if (existingBids.size() == totalPlayers - 1) {
            int potentialSum = currentSum + newBidAmount;
            return potentialSum != cardsPerPlayer;  // ¡NO puede ser igual!
        }

        return false;  // Ya todos apostaron
    }

    // Otros métodos del servicio
    public List<Bid> getBidsByRound(Long roundId) {
        Round round = roundDAO.findById(roundId);
        return bidDAO.findByRound(round);
    }

    public Bid getBidByPlayerInRound(Long playerId, Long roundId) {
        Player player = playerDAO.findById(playerId);
        Round round = roundDAO.findById(roundId);
        return bidDAO.findByPlayerAndRound(player, round);
    }

    @Transactional
    public void incrementTricksWon(Long bidId) {
        Bid bid = bidDAO.findById(bidId);
        if (bid != null) {
            bid.incrementTricksWon();
            bidDAO.save(bid);
        }
    }
}