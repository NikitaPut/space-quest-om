package com.spacequest.om.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "situations")
public class SituationEntity {
    @Id
    @Column(name = "situation_id")
    public String id;
    
    public String title;
    
    @Column(length = 2000)
    public String description;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "situation_options", joinColumns = @JoinColumn(name = "situation_id"))
    @Column(name = "option_text", length = 1000)
    @OrderColumn(name = "option_index")
    public List<String> options = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "situation_consequences", joinColumns = @JoinColumn(name = "situation_id"))
    @Column(name = "consequence_text", length = 2000)
    @OrderColumn(name = "option_index")
    public List<String> consequences = new ArrayList<>();
    
    public String chainId;
    public Integer chainOrder;
    public String nextSituationId;
    public Integer turnsUntilNext;
    public String condition;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "situation_effects", joinColumns = @JoinColumn(name = "situation_id"))
    @Column(name = "effect_code", length = 2000)
    @OrderColumn(name = "option_index")
    public List<String> effects = new ArrayList<>();
}
