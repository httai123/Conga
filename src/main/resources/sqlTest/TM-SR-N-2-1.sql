WITH cal_internet_television_count_all_interactive_weekly_neg_3_neg_0 AS (SELECT msisdn
                                                                          FROM C360_FLAT_SERVICES
                                                                          WHERE PARTITION_DATE IN (
                                                                                                   LEAST(DATE_FORMAT(
                                                                                                                 STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W'),
                                                                                                                 '%Y%m%d'),
                                                                                                         DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                                                   DATE_FORMAT(
                                                                                                           STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                                           INTERVAL 1 WEEK,
                                                                                                           '%Y%m%d'),
                                                                                                   DATE_FORMAT(
                                                                                                           STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                                           INTERVAL 2 WEEK,
                                                                                                           '%Y%m%d'),
                                                                                                   DATE_FORMAT(
                                                                                                           STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                                           INTERVAL 3 WEEK,
                                                                                                           '%Y%m%d'))
                                                                            AND
                                                                              cal_internet_television_count_all_interactive_weekly >=
                                                                              10
                                                                          GROUP BY msisdn
                                                                          HAVING COUNT(*) >= 1),

     cal_internet_television_count_sms_received_weekly_neg_3_0 AS (SELECT msisdn
                                                                   FROM C360_FLAT_SERVICES
                                                                   WHERE PARTITION_DATE IN (
                                                                                            LEAST(DATE_FORMAT(
                                                                                                          STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W'),
                                                                                                          '%Y%m%d'),
                                                                                                  DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                                            DATE_FORMAT(
                                                                                                    STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                                    INTERVAL 1 WEEK,
                                                                                                    '%Y%m%d'),
                                                                                            DATE_FORMAT(
                                                                                                    STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                                    INTERVAL 2 WEEK,
                                                                                                    '%Y%m%d'),
                                                                                            DATE_FORMAT(
                                                                                                    STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                                    INTERVAL 3 WEEK,
                                                                                                    '%Y%m%d')
                                                                       )
                                                                     AND (
                                                                       cal_internet_television_count_sms_received_weekly <
                                                                       4)
                                                                   GROUP BY msisdn
                                                                   HAVING COUNT(*) >= 1)
SELECT msisdn
FROM (SELECT msisdn
      FROM cal_internet_television_count_sms_received_weekly_neg_3_0
      INTERSECT
      SELECT msisdn
      FROM cal_internet_television_count_all_interactive_weekly_neg_3_neg_0) AS t