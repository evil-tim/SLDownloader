package com.sldlt.navps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Component;

import com.sldlt.navps.entity.NAVPSEntry;

@Component
public interface NAVPSEntryRepository extends JpaRepository<NAVPSEntry, Long>, QuerydslPredicateExecutor<NAVPSEntry> {

}
