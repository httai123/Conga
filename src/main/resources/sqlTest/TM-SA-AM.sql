WITH water_amount_bill_daily_20250226_20250227
         AS (SELECT msisdn
             FROM C360_FLAT_SERVICES
             WHERE PARTITION_DATE >= DATE_FORMAT(20250226, '%Y%m%d')
               AND PARTITION_DATE <= DATE_FORMAT(20250227, '%Y%m%d')
               AND water_amount_bill_daily >
                   500000
             GROUP BY msisdn
             HAVING COUNT(*) >= 2),
     datetime_water_earliest_potential_moment_evt_weekly_202452 AS (SELECT msisdn
                                                                    FROM C360_FLAT_SERVICES
                                                                    WHERE PARTITION_DATE = LEAST(
                                                                            DATE_FORMAT(
                                                                                    STR_TO_DATE(CONCAT(202452, ' Sunday'), '%x%v %W'),
                                                                                    '%Y%m%d'),
                                                                            DATE_FORMAT(CURDATE(), '%Y%m%d'))
                                                                      AND datetime_water_earliest_potential_moment_evt_weekly IS NOT NULL),
     electricity_last_amount_bill_daily_20250115 AS (SELECT msisdn
                                                     FROM C360_FLAT_SERVICES
                                                     WHERE PARTITION_DATE = DATE_FORMAT(20250115, '%Y%m%d')
                                                       AND (electricity_last_amount_bill_daily > 200000)),
     credit_score_monthly_neg_1_0
         AS (SELECT msisdn
             FROM C360_FLAT_SERVICES
             WHERE PARTITION_DATE IN (
                                      LEAST(DATE_FORMAT(LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                        '%Y%m%d'), DATE_FORMAT(CURDATE(), '%Y%m%d')),
                                      DATE_FORMAT(
                                              LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                              '%Y%m%d')
                 )
               AND credit_score_monthly >
                   700
             GROUP BY msisdn
             HAVING COUNT(*) >= 2),
     cal_internet_television_count_sms_received_daily_neg_1 AS (SELECT msisdn
                                                                FROM C360_FLAT_SERVICES
                                                                WHERE PARTITION_DATE = DATE_FORMAT(CURDATE() - INTERVAL 7 DAY, '%Y%m%d')
                                                                  AND cal_internet_television_count_sms_received_daily >
                                                                      10),
     datetime_water_earliest_potential_moment_sms_weekly_neg_2 AS (SELECT msisdn
                                                                   FROM C360_FLAT_SERVICES
                                                                   WHERE PARTITION_DATE = LEAST(DATE_FORMAT(
                                                                                                        STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                                        INTERVAL 2 WEEK,
                                                                                                        '%Y%m%d'),
                                                                                                DATE_FORMAT(CURDATE(), '%Y%m%d'))
                                                                     AND
                                                                       datetime_water_earliest_potential_moment_sms_weekly >
                                                                       '2025-02-15T00:00:00Z'),
     cus_type_n_monthly_202412_202501
         AS (SELECT msisdn
             FROM C360_FLAT_SERVICES
             WHERE PARTITION_DATE IN (
                                      LEAST(DATE_FORMAT(LAST_DAY(CONCAT(202501, '01')), '%Y%m%d'),
                                            DATE_FORMAT(CURDATE(), '%Y%m%d')),
                                      DATE_FORMAT(LAST_DAY(CONCAT(202501, '01') - INTERVAL 1 MONTH), '%Y%m%d')
                 )
               AND cus_type_n_monthly LIKE
                   '%'
             GROUP BY msisdn
             HAVING COUNT(*) >= 2),
     id_1741589801335 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM electricity_last_amount_bill_daily_20250115
                                INTERSECT
                                SELECT msisdn
                                FROM credit_score_monthly_neg_1_0) AS t),
     id_1741589801333 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM water_amount_bill_daily_20250226_20250227
                                UNION
                                SELECT msisdn
                                FROM id_1741589801335) AS t)
SELECT msisdn
FROM (SELECT msisdn
      FROM cus_type_n_monthly_202412_202501
      INTERSECT
      SELECT msisdn
      FROM cal_internet_television_count_sms_received_daily_neg_1
      INTERSECT
      SELECT msisdn
      FROM id_1741589801333
      INTERSECT
      SELECT msisdn
      FROM datetime_water_earliest_potential_moment_evt_weekly_202452
      INTERSECT
      SELECT msisdn
      FROM datetime_water_earliest_potential_moment_sms_weekly_neg_2) AS t