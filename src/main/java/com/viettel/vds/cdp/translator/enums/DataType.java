package com.viettel.vds.cdp.translator.enums;

public enum DataType {
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    DECIMAL,
    STRING {
        @Override
        public String getValue(Object value) {
            if (value.toString().isEmpty()) {
                return "";
            }
            return String.format("'%s'", value);
        }
    },
    DATE {
        @Override
        public String getValue(Object value) {
            if (value.toString().isEmpty()) {
                return "";
            }
            try {
                //parse value to long
                long day = Long.parseLong(value.toString());
                String op = day < 0 ? "-" : "+";
                //convert miniSecond to day
                day = Math.abs(day);
                if (day == 0) {
                    return "CURDATE()";
                }
                return String.format("CURDATE() %s INTERVAL %d DAY", op, day);
            } catch (NumberFormatException e) {
                return String.format("'%s'", value);
            }
        }
    },
    TIMESTAMP {
        @Override
        public String getValue(Object value) {
            if (value.toString().isEmpty()) {
                return "";
            }
            try {
                // Parse value to long
                long seconds = Long.parseLong(value.toString());
                String op = seconds < 0 ? "-" : "+";
                // Convert milliSecond to second
                seconds = Math.abs(seconds);
                if (seconds == 0) {
                    return "CURRENT_TIMESTAMP()";
                }
                return String.format("CURRENT_TIMESTAMP() %s INTERVAL %d SECOND", op, seconds);
            } catch (NumberFormatException e) {
                return String.format("'%s'", value);
            }
        }
    };

    public String getValue(Object value) {
        return value.toString();
    }
}