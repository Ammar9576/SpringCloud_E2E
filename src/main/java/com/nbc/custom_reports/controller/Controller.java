package com.nbc.custom_reports.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@ApiIgnore
public class Controller {
	
	@RequestMapping(value = "/")
	public String welcome() {
		return "Welcome to convergence";
	}
}
