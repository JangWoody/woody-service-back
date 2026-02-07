package com.restapi.controller;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.restapi.RestApiTest02Application;
import com.restapi.service.JangWoodyService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class JangWoodyController {

	private static final String DOWNLOAD_PAGE = """
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<title>EAI Tool Download</title>
			</head>
			<body>
				<h1>EAI Tool Download</h1>
				<ul>
					<li><a href="/ChannelSchdMgr">1. PO 채널 스케쥴 매니저</a></li>
					<li><a href="/CCInfo">2. PO 채널 정보 조회</a></li>
					<li><a href="/XSDGenerator">3. PO XSD Generator</a></li>
				</ul>
			</body>
			</html>
			""";

	private final JangWoodyService jangWoodyService;

	public JangWoodyController(JangWoodyService jangWoodyService) {
		this.jangWoodyService = jangWoodyService;
	}

	@GetMapping(value = "/download", produces = MediaType.TEXT_HTML_VALUE)
	public String getDownloadPage() {
		return DOWNLOAD_PAGE;
	}

	@GetMapping("/weather")
	public String weather() {
		return "날씨확인용 단축어를 통해 실행하세요";
	}

	@RequestMapping(
    	value = "/holiday",
    	method = {RequestMethod.GET, RequestMethod.POST},
    	produces = MediaType.TEXT_PLAIN_VALUE
	)
	public String getHolidayInfo() {
		return RestApiTest02Application.getHoliday();
	}

	@PostMapping("/weather")
	public String postWeather(HttpServletRequest request) {
		String userKey = request.getParameter("userKey");
		String callIP = request.getParameter("callIP");
		String callTime = request.getParameter("callTime");
		String coX = request.getParameter("coX");
		String coY = request.getParameter("coY");
		String locate = request.getParameter("locate");
		return jangWoodyService.getWeather(userKey, callIP, callTime, coX, coY, locate);
	}

	@PostMapping("/news10")
	public String getHotNews() {
		return jangWoodyService.getNewsTopTen();
	}

	@GetMapping("/DT_Gen")
	public ResponseEntity<UrlResource> getDtGen(HttpServletRequest request) throws MalformedURLException {
		return createDownloadResponse(request, "DT_Gen", "C:\\FTP\\DT_Generator.zip");
	}

	@GetMapping("/ChannelSchdMgr")
	public ResponseEntity<UrlResource> getChannelScheduleManager(HttpServletRequest request)
			throws MalformedURLException {
		return createDownloadResponse(request, "ChannelSchdMgr", "C:\\FTP\\ChannelScheduleManager.zip");
	}

	@GetMapping("/CCInfo")
	public ResponseEntity<UrlResource> getCCInformation(HttpServletRequest request) throws MalformedURLException {
		return createDownloadResponse(request, "CCInfo", "C:\\FTP\\ShowCCInformation.zip");
	}

	@GetMapping("/XSDGenerator")
	public ResponseEntity<UrlResource> getXSDGenerator(HttpServletRequest request) throws MalformedURLException {
		return createDownloadResponse(request, "XSDGenerator", "C:\\FTP\\XSDGenerator.zip");
	}

	private ResponseEntity<UrlResource> createDownloadResponse(HttpServletRequest request, String serviceName,
			String filePath) throws MalformedURLException {
		jangWoodyService.callIPandService(serviceName, request.getRemoteAddr() + "\t" + request.getRemoteHost());

		UrlResource resource = new UrlResource("file:" + filePath);
		String encodedUploadFileName = UriUtils.encode(Paths.get(filePath).getFileName().toString(),
				StandardCharsets.UTF_8);
		String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(resource);
	}
}
