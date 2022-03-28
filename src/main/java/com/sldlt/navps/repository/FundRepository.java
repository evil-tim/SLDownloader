package com.sldlt.navps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Component;

import com.sldlt.navps.entity.Fund;

@Component
public interface FundRepository extends JpaRepository<Fund, Long>, QuerydslPredicateExecutor<Fund> {

    Fund findOneByCode(String code);

}
