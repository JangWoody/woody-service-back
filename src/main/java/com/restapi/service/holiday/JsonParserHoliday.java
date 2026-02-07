package com.restapi.service.holiday;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParserHoliday {
	public static ArrayList<String> parseHolidayJson(String json) throws Exception {

		ObjectMapper mapper = new ObjectMapper();
		ApiRoot root = mapper.readValue(json, ApiRoot.class);
		ApiResponse response = root.getResponse();

		int totalCount = response.getBody().getTotalCount();
		List<HolidayItem> list = response.getBody().getItems().getItem();

		ArrayList<String> holidayArr = null;
		if (totalCount > 0) {
			for (int i = 0; i < list.size(); i++) {
				HolidayItem item = list.get(i);
				if (item.getIsHoliday().equals("Y")) {
					if (holidayArr == null)
						holidayArr = new ArrayList<>();
					holidayArr.add(String.valueOf(item.getLocdate()));
				}
			}
		}
		return holidayArr;
	}
}
