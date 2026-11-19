package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.AddMemberDto;
import com.project.tekwira.Dto.CreateTeamDto;
import com.project.tekwira.Models.Team;
import com.project.tekwira.Services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasRole('PLAYER')")
    public Team createTeam(@RequestBody CreateTeamDto dto) {
        return teamService.createTeam(dto.getName());
    }

    @PostMapping("/{teamId}/members")
    @PreAuthorize("hasRole('PLAYER')")
    public Team addMember(@PathVariable Long teamId,
                          @RequestBody AddMemberDto dto) {
        return teamService.addMember(teamId, dto.getUsername());
    }
}
