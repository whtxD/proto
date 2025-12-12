// REEMPLAZA todo el contenido por ESTO:
package com.ohhell.ohhellapi.dao;

import com.ohhell.ohhellapi.models.Trick;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

public class TrickDAO {

    @PersistenceContext(unitName = "ohhellPU")
    private EntityManager em;

    public Trick findById(Long id) {
        return em.find(Trick.class, id);
    }

    public List<Trick> findByRoundId(Long roundId) {
        return em.createQuery(
                        "SELECT t FROM Trick t WHERE t.roundId = :roundId",
                        Trick.class)
                .setParameter("roundId", roundId)
                .getResultList();
    }

    public Trick save(Trick trick) {
        if (trick.getId() == null) {
            em.persist(trick);
            return trick;
        } else {
            return em.merge(trick);
        }
    }

    public void delete(Long id) {
        Trick trick = findById(id);
        if (trick != null) {
            em.remove(trick);
        }
    }
}