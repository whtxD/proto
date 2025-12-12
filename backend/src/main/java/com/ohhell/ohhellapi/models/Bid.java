package com.ohhell.ohhellapi.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;  // ✅ Relación REAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;    // ✅ Relación REAL

    @Column(name = "bid_amount", nullable = false)
    private int bidAmount;

    @Column(name = "tricks_won", nullable = false)
    private int tricksWon = 0;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructor vacío (requerido por JPA)
    public Bid() {}

    // Constructor para crear apuesta
    public Bid(Player player, Round round, int bidAmount) {
        this.player = player;
        this.round = round;
        this.bidAmount = bidAmount;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Round getRound() { return round; }
    public void setRound(Round round) { this.round = round; }

    public int getBidAmount() { return bidAmount; }
    public void setBidAmount(int bidAmount) { this.bidAmount = bidAmount; }

    public int getTricksWon() { return tricksWon; }
    public void setTricksWon(int tricksWon) { this.tricksWon = tricksWon; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Métodos de negocio
    public void incrementTricksWon() { this.tricksWon++; }
    public boolean isBidMet() { return bidAmount == tricksWon; }
}