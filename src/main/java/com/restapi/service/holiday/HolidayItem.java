package com.restapi.service.holiday;

import lombok.Data;

@Data
public class HolidayItem {
    private String dateKind;
    private String dateName;
    private String isHoliday;
    private long locdate;
    private int seq;
}
