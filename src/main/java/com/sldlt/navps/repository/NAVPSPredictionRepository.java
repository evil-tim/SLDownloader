package com.sldlt.navps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Component;

import com.sldlt.navps.entity.NAVPSPrediction;

@Component
public interface NAVPSPredictionRepository extends JpaRepository<NAVPSPrediction, Long>, QuerydslPredicateExecutor<NAVPSPrediction> {

}
