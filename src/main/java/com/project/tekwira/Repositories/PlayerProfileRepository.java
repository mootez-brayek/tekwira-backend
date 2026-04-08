package com.project.tekwira.Repositories;

import com.project.tekwira.Models.PlayerProfile;
import com.project.tekwira.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Integer> {
    Optional<PlayerProfile> findByUser(User user);
}
