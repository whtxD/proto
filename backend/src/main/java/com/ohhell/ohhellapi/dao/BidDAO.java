package com.ohhell.ohhellapi.dao;

import com.ohhell.ohhellapi.models.Bid;
import com.ohhell.ohhellapi.models.Round;
import com.ohhell.ohhellapi.models.Player;
import jakarta.persistence.*;
import java.util.List;

@Stateless  // Para que sea inyectable y maneje transacciones
public class BidDAO {

    @PersistenceContext(unitName = "ohhellPU")
    private EntityManager em;

    // CRUD básico
    public Bid findById(Long id) {
        return em.find(Bid.class, id);
    }

    public List<Bid> findByRound(Round round) {
        return em.createQuery(
                        "SELECT b FROM Bid b WHERE b.round = :round ORDER BY b.timestamp",
                        Bid.class)
                .setParameter("round", round)
                .getResultList();
    }

    public Bid findByPlayerAndRound(Player player, Round round) {
        try {
            return em.createQuery(
                            "SELECT b FROM Bid b WHERE b.player = :player AND b.round = :round",
                            Bid.class)
                    .setParameter("player", player)
                    .setParameter("round", round)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Bid save(Bid bid) {
        if (bid.getId() == null) {
            em.persist(bid);  // INSERT
            return bid;
        } else {
            return em.merge(bid);  // UPDATE
        }
    }

    public void delete(Bid bid) {
        em.remove(em.contains(bid) ? bid : em.merge(bid));
    }

    // Consultas específicas
    public int getTotalBidAmountForRound(Round round) {
        Long result = em.createQuery(
                        "SELECT SUM(b.bidAmount) FROM Bid b WHERE b.round = :round",
                        Long.class)
                .setParameter("round", round)
                .getSingleResult();
        return result != null ? result.intValue() : 0;
    }

    public int countBidsInRound(Round round) {
        Long count = em.createQuery(
                        "SELECT COUNT(b) FROM Bid b WHERE b.round = :round",
                        Long.class)
                .setParameter("round", round)
                .getSingleResult();
        return count != null ? count.intValue() : 0;
    }
}