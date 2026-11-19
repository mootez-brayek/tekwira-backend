package com.project.tekwira.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"reservation_id", "event_type"}
    )
)
public class ReliabilityTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PlayerProfile playerProfile;

    @ManyToOne(optional = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReliabilityEventType eventType;

    @Column(nullable = false)
    private int pointsChange;

    @Column(nullable = false)
    private int previousScore;

    @Column(nullable = false)
    private int newScore;

    private LocalDateTime createdAt;
}
