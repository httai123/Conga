package com.viettel.vds.cdp.translator.services.helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateTimeHelper {
    private DateTimeHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static int calculateMonthDifference(int startDate, int endDate) {
        int startYear = startDate / 100;
        int startMonth = startDate % 100;
        int endYear = endDate / 100;
        int endMonth = endDate % 100;

        return (endYear - startYear) * 12 + (endMonth - startMonth);
    }

    public static int calculateDateDifference(int startDate, int endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate startDateObj = LocalDate.parse(
                String.valueOf(startDate),
                formatter
        );
        LocalDate endDateObj = LocalDate.parse(String.valueOf(endDate), formatter);

        return (int) ChronoUnit.DAYS.between(startDateObj, endDateObj);
    }

    public static int calculateWeekDifference(int startDate, int endDate) {
        String startDateString = String.valueOf(startDate);
        String endDateString = String.valueOf(endDate);

        int startYear = Integer.parseInt(startDateString.substring(0, 4));
        int startWeek = Integer.parseInt(startDateString.substring(4, 6));
        int endYear = Integer.parseInt(endDateString.substring(0, 4));
        int endWeek = Integer.parseInt(endDateString.substring(4, 6));

        LocalDate startDateObj = LocalDate.of(startYear, 1, 1)
                .with(WeekFields.of(Locale.getDefault()).weekOfYear(), startWeek);
        LocalDate endDateObj = LocalDate.of(endYear, 1, 1)
                .with(WeekFields.of(Locale.getDefault()).weekOfYear(), endWeek);

        return (int) ChronoUnit.WEEKS.between(startDateObj, endDateObj);
    }
}
