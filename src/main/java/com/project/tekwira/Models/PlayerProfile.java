package com.project.tekwira.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;

    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrongFoot strongFoot;

    private int goals = 0;

    private int reservationCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    private String bio;

    @Column(nullable = false)
    private int reliabilityScore = 50;

    @Column(nullable = false)
    private int completedReservations = 0;

    @Column(nullable = false)
    private int cancelledReservations = 0;

    @Column(nullable = false)
    private int noShowReservations = 0;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

}
