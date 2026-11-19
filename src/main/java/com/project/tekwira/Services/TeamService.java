package com.project.tekwira.Services;

import com.project.tekwira.Models.Team;

public interface TeamService {
    Team createTeam(String teamName);

    Team addMember(Long teamId, String username);
}
