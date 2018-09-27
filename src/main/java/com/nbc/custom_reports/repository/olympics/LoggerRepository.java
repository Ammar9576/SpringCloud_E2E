package com.nbc.custom_reports.repository.olympics;

import org.springframework.stereotype.Repository;

import com.nbc.custom_reports.domain.PayloadErrorLogger;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface LoggerRepository extends JpaRepository<PayloadErrorLogger,Serializable> {

}
