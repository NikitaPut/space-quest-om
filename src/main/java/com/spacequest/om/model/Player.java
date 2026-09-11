package com.spacequest.om.model;
import java.util.ArrayList;
import java.util.List;

public class Player {
    public String name;
    public String role;
    public String color;
    public int x, y;
    public int burns = 0;
    
    // Способности
    public boolean abilityUsed = false; // Использована ли уникальная способность
    public boolean rerollAvailable = false; // Для Главного Инженера
    public boolean autoSolveAvailable = false; // Для Программиста
    public boolean shieldActive = false; // Защита от КЗ (Техник безопасности / Термоусадка)
    public boolean skipNextTurn = false; // Пропуск хода (Флюс)
    public int diceRoll = 0; // Последний бросок кубика
    
    // Предметы
    public List<String> tools = new ArrayList<>();
    
    public Player(String name, String role, String color) { 
        this.name = name; 
        this.role = role; 
        this.color = color;
    }
    
    public void resetAbilities() {
        rerollAvailable = false;
        autoSolveAvailable = false;
    }
}
