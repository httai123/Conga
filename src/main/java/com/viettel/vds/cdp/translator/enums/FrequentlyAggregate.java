package com.viettel.vds.cdp.translator.enums;

import com.viettel.vds.cdp.translator.services.helpers.DateTimeHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public enum FrequentlyAggregate {
    DAILY {
        @Override
        public int calDif(int start, int end) {
            return DateTimeHelper.calculateDateDifference(start, end) + 1;
        }

        @Override
        public String getSqlCurrentTime() {
            return "CURDATE()";
        }

        @Override
        public String getSqlString(String timeString, int interval, ToSqlStringOptions options) {
            if (interval == 0) {
                return timeString;
            }
            return String.format(
                    "%s $INTERVAL_STRING"
                            .replace(intervalStringToken, getIntervalString(interval, "DAY")),
                    timeString
            );
        }
    },
    WEEKLY {
        @Override
        public int calDif(int start, int end) {
            return DateTimeHelper.calculateWeekDifference(start, end) + 1;
        }

        @Override
        public String getSqlCurrentTime() {
            return "YEARWEEK(CURDATE())";
        }

        @Override
        public String getSqlString(String timeString, int interval, ToSqlStringOptions options) {
            String day = options.startDay ? "' Monday'" : "' Sunday'";
            String formatString = "STR_TO_DATE(CONCAT(%s, $TEMPLATE_DAY), '%%x%%v %%W') $INTERVAL_STRING"
                    .replace("$TEMPLATE_DAY", day)
                    .replace(intervalStringToken, getIntervalString(interval, "WEEK"));
            return String.format(
                    formatString,
                    timeString
            );
        }
    },
    MONTHLY {
        @Override
        public int calDif(int start, int end) {
            return DateTimeHelper.calculateMonthDifference(start, end) + 1;
        }

        @Override
        public String getSqlCurrentTime() {
            return "DATE_FORMAT(CURDATE(), '%Y%m')";
        }

        @Override
        public String getSqlString(String timeString, int interval, ToSqlStringOptions options) {
            if (options.startDay) {
                return String.format(
                        "CONCAT(%s, '01') $INTERVAL_STRING"
                                .replace(intervalStringToken, getIntervalString(interval, "MONTH")),
                        timeString
                );
            }
            return String.format(
                    "LAST_DAY(CONCAT(%s, '01') $INTERVAL_STRING)"
                            .replace(intervalStringToken, getIntervalString(interval, "MONTH")),
                    timeString
            );
        }
    };

    final String intervalStringToken = "$INTERVAL_STRING";

    String getIntervalString(int interval, String unit) {
        String operatorInterval = interval > 0 ? "-" : "+";
        return interval == 0 ? "" : "$OPERATOR INTERVAL $INTERVAL_VALUE $UNIT"
                .replace("$OPERATOR", operatorInterval)
                .replace("$UNIT", unit)
                .replace("$INTERVAL_VALUE", String.valueOf(Math.abs(interval)));
    }

    public abstract int calDif(int start, int end);

    public int calDifRela(int start, int end) {
        return end - start + 1;
    }

    public abstract String getSqlCurrentTime();

    public abstract String getSqlString(String timeString, int interval, ToSqlStringOptions options);

    public String getSqlStringWrapper(String timeString, int interval, ToSqlStringOptions options) {
        boolean greaterThanCurrent = options.greaterThanCurrent;
        String sqlString = getSqlString(timeString, interval, options);
        sqlString = "DATE_FORMAT($SQL_STRING, '%Y%m%d')"
                .replace("$SQL_STRING", sqlString);
        return greaterThanCurrent ? "LEAST($SQL_STRING, DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d'))"
                .replace("$SQL_STRING", sqlString) : sqlString;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ToSqlStringOptions {
        private boolean startDay;
        private boolean greaterThanCurrent;
    }

}
