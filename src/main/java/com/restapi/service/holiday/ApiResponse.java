package com.restapi.service.holiday;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
	private Header header;
	private Body body;
}