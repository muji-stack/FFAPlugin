package com.yourname.ffa.data;

import java.util.UUID;

public class PlayerStats {
    
    private UUID uuid;
    private String playerName;
    private int kills;
    private int deaths;
    private int currentStreak;
    private int bestStreak;
    
    public PlayerStats(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.kills = 0;
        this.deaths = 0;
        this.currentStreak = 0;
        this.bestStreak = 0;
    }
    
    public PlayerStats(UUID uuid, String playerName, int kills, int deaths, int currentStreak, int bestStreak) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.kills = kills;
        this.deaths = deaths;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public void addKill() {
        this.kills++;
        this.currentStreak++;
        if (this.currentStreak > this.bestStreak) {
            this.bestStreak = this.currentStreak;
        }
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public void addDeath() {
        this.deaths++;
        this.currentStreak = 0;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }
    
    public int getBestStreak() {
        return bestStreak;
    }
    
    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }
    
    public double getKDRatio() {
        if (deaths == 0) {
            return kills;
        }
        return Math.round((double) kills / deaths * 100.0) / 100.0;
    }
}
