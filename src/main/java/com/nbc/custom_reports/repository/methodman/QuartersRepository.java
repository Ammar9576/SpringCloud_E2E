package com.nbc.custom_reports.repository.methodman;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nbc.custom_reports.domain.methodman.Quarters;


@Repository
public interface QuartersRepository extends JpaRepository<Quarters,Serializable> {
	
	
	List<Quarters> findByCalendarId(Long calendarId);

}
