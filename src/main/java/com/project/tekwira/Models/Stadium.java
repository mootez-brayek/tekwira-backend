package com.project.tekwira.Models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private Double latitude;
    private Double longitude;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private FieldType fieldType = FieldType.SIX_A_SIDE;
    // 🕒 Working hours
    private LocalTime openingTime;
    private LocalTime closingTime;

    // 💰 Pricing
    @Column(nullable = false)
    private Double pricePerGame;

    @Column(nullable = false)
    private Double subscriptionPrice;

    // 📝 Description
    @Column(length = 1000)
    private String description;

    // 📸 Optional image
    @ElementCollection
    private List<String> imageUrls;
    private String logo;

    // 🕓 Auditing
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private boolean isActive;
    // 🔗 Link to OWNER user
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "stadium")
    private List<Rating> ratings = new ArrayList<>();

}
