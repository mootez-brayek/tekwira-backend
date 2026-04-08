package com.project.tekwira.Models;

import lombok.Getter;

@Getter
public enum FieldType {
    SIX_A_SIDE(12),
    SEVEN_A_SIDE(14);

    private final int totalPlayers;
    FieldType(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }
}
