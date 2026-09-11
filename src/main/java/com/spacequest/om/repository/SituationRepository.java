package com.spacequest.om.repository;

import com.spacequest.om.entity.SituationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SituationRepository extends JpaRepository<SituationEntity, String> {
    List<SituationEntity> findByChainIdIsNull();
    List<SituationEntity> findByChainIdIsNotNullAndChainOrder(Integer chainOrder);
    long count();
}
