package com.spacequest.om.model;
import java.util.ArrayList;
import java.util.List;

public class Cell {
    public int x, y;
    public CellType type;
    public boolean hasKZ = false;
    public List<String> playerColors = new ArrayList<>(); // Список цветов фишек на этой клетке
    
    public Cell(int x, int y, CellType type) { 
        this.x = x; 
        this.y = y; 
        this.type = type; 
    }
    public void setType(CellType type) { this.type = type; }
}
