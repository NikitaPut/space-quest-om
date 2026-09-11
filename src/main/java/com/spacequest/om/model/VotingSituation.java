package com.spacequest.om.model;

import com.spacequest.om.entity.SituationEntity;
import java.util.List;
import java.util.ArrayList;

public class VotingSituation {
    public String id;
    public String title;
    public String description;
    public List<String> options = new ArrayList<>();
    public List<String> consequences = new ArrayList<>();
    public List<String> effects = new ArrayList<>();
    public String chainId;
    public String nextSituationId;
    public int turnsUntilNext;
    public String condition;
    
    public static VotingSituation fromEntity(SituationEntity e) {
        if (e == null) return null;
        VotingSituation v = new VotingSituation();
        v.id = e.id;
        v.title = e.title;
        v.description = e.description;
        v.options = e.options != null ? e.options : new ArrayList<>();
        v.consequences = e.consequences != null ? e.consequences : new ArrayList<>();
        v.effects = e.effects != null ? e.effects : new ArrayList<>();
        v.chainId = e.chainId;
        v.nextSituationId = e.nextSituationId;
        v.turnsUntilNext = e.turnsUntilNext != null ? e.turnsUntilNext : 0;
        v.condition = e.condition;
        return v;
    }
}
