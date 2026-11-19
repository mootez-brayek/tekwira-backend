package com.project.tekwira.Services.implementations;

import com.project.tekwira.Models.Role;
import com.project.tekwira.Models.Team;
import com.project.tekwira.Models.User;
import com.project.tekwira.Repositories.TeamRepository;
import com.project.tekwira.Repositories.UserRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public Team createTeam(String teamName) {
        User owner = JwtUtil.getCurrentUser();

        Team team = new Team();
        team.setName(teamName);
        team.setTeamOwner(owner);

        team.getMembers().add(owner);

        return teamRepository.save(team);
    }

    @Override
    public Team addMember(Long teamId, String username) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException("Team not found"));

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.PLAYER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already in team");
        }

        if (team.getMembers().contains(user)) {
            throw new RuntimeException("User already in team");
        }
        team.getMembers().add(user);
        return teamRepository.save(team);
    }
}
