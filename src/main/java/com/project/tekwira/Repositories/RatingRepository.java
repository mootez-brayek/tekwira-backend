package com.project.tekwira.Repositories;

import com.project.tekwira.Dto.RatingStats;
import com.project.tekwira.Models.Rating;
import com.project.tekwira.Models.Stadium;
import com.project.tekwira.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByPlayerAndStadium(User player, Stadium stadium);

    List<Rating> findByStadium(Stadium stadium);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.stadium.id = :stadiumId")
    Double getAverageRating(Long stadiumId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.stadium.id = :stadiumId")
    Long getRatingsCount(Long stadiumId);

    @Query("""
    SELECT new com.project.tekwira.Dto.RatingStats(
        r.stadium.id,
        AVG(r.score),
        COUNT(r)
    )
    FROM Rating r
    WHERE r.stadium.id = :stadiumId
    GROUP BY r.stadium.id
""")
    RatingStats getRatingStatsForStadium(@Param("stadiumId") Long stadiumId);

    @Query("""
        SELECT AVG(r.score)
        FROM Rating r
        WHERE r.stadium.id = :stadiumId
    """)
    Double getAverageScoreByStadium(@Param("stadiumId") Long stadiumId);


    @Query("""
    SELECT r.stadium.id,
           COALESCE(AVG(r.score), 0),
           COUNT(r)
    FROM Rating r
    WHERE r.stadium.id IN :ids
    GROUP BY r.stadium.id
""")
    List<Object[]> getStatsForStadiums(@Param("ids") List<Long> ids);

    boolean existsByPlayerAndStadium(User player, Stadium stadium);
}
