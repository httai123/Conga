package com.viettel.vds.cdp.translator.services.helpers;

public class SqlTemplate {
    public static final String TR_BASIC_QUERY =
            "SELECT $PROJECTION \n" +
                    "FROM $TABLE \n" +
                    "WHERE $PARTITION_NAME >= $LIMIT_ABOVE \n" +
                    "      AND $PARTITION_NAME <= $LIMIT_UPPER \n" +
                    "      AND $WINDOW_CONDITION";

    public static final String ST_BASIC_QUERY =
            "SELECT $PROJECTION \n" +
                    "FROM $TABLE \n" +
                    "WHERE $PARTITION_NAME = $LIMIT_ABOVE";

    public static final String COMPONENT_QUERY =
            "SELECT $PROJECTION \n" +
                    "FROM $SRC \n" +
                    "WHERE $FILTER";

    public static final String WINDOW_COMPONENT_QUERY =
            "SELECT $PROJECTION \n" +
                    "FROM $SRC \n" +
                    "WHERE $FILTER \n" +
                    "GROUP BY $GROUP_BY \n" +
                    "HAVING COUNT(*) >= $CANDIDATE";

    public static final String HAVE_CTA_QUERY =
            "WITH $CTE \n" +
                    "$MAIN_QUERY";

    public static final String SUBQUERY_IN_CTA =
            "$ALIAS AS (\n" +
                    "    $QUERY\n" +
                    ")";

    public static final String AGG_QUERY =
            "SELECT $PROJECTION, \n" +
                    "       $AGG_FUNC($AGG_COL) AS $AGG_ALIAS \n" +
                    "FROM $SRC \n" +
                    "WHERE $FILTER \n" +
                    "GROUP BY $PROJECTION";

    public static final String IN_OP =
            "$COL IN (\n" +
                    "    $VALUES\n" +
                    ")";

    public static final String RELATIONAL_CLAUSE =
            "$FIELD $OP $VALUE";

    private SqlTemplate() {
    }
}
