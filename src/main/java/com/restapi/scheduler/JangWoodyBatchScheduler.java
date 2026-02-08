package com.restapi.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.restapi.RestApiTest02Application;
import com.restapi.service.JangWoodyService;

@Component
public class JangWoodyBatchScheduler {
	private static final Logger log = LoggerFactory.getLogger(JangWoodyBatchScheduler.class);
	private static final String HOLIDAY_YES = "Y";
	private static final String HOLIDAY_NO = "N";

	private final JangWoodyService jangWoodyService;

	public JangWoodyBatchScheduler(JangWoodyService jangWoodyService) {
		this.jangWoodyService = jangWoodyService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void runBatchOnStartup() {
		log.info("Holiday batch triggered at startup");
		runDailyBatch();
	}

	@Scheduled(cron = "0 10 0 * * ?", zone = "Asia/Seoul")
	public void runDailyBatch() {
		log.info("Holiday batch started");
		try {
			log.info("Holiday batch in progress: retrieving holiday information");
			String result = jangWoodyService.getHolidayInfo();
			if (HOLIDAY_YES.equals(result) || HOLIDAY_NO.equals(result)) {
				RestApiTest02Application.setHoliday(result);
			} else {
				RestApiTest02Application.setHoliday(HOLIDAY_NO);
			}
			log.info("Holiday batch finished with result: {}", RestApiTest02Application.getHoliday());
		} catch (Exception e) {
			log.error("Holiday batch failed", e);
		}
	}
}