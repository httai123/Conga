WITH arpu_n_monthly_week_neg_4_neg_2_all AS (SELECT msisdn
                                             FROM C360_FLAT_SERVICES
                                             WHERE PARTITION_DATE IN (
                                                                      LEAST(DATE_FORMAT(
                                                                                    STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                                    INTERVAL 2 WEEK,
                                                                                    '%Y%m%d'),
                                                                            DATE_FORMAT(CURDATE(), '%Y%m%d')),
                                                                      DATE_FORMAT(
                                                                              STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                              INTERVAL 3 WEEK,
                                                                              '%Y%m%d'),
                                                                      DATE_FORMAT(
                                                                              STR_TO_DATE(CONCAT(YEARWEEK(CURDATE()), ' Sunday'), '%x%v %W') -
                                                                              INTERVAL 4 WEEK,
                                                                              '%Y%m%d')
                                                 )
                                               AND arpu_n_monthly IS NOT NULL
                                             GROUP BY msisdn
                                             HAVING COUNT(*) >= 3),
     credit_score_monthly_monthly_neg_1_0_ex
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
             HAVING COUNT(*) >= 1),
     electricity_last_amount_bill_weekly_202452 AS (SELECT msisdn
                                                    FROM C360_FLAT_SERVICES
                                                    WHERE PARTITION_DATE = LEAST(
                                                            DATE_FORMAT(
                                                                    STR_TO_DATE(CONCAT(202452, ' Sunday'), '%x%v %W'),
                                                                    '%Y%m%d'), DATE_FORMAT(CURDATE(), '%Y%m%d'))
                                                      AND electricity_last_amount_bill_weekly > 500000),
     cal_internet_television_count_sms_received_daily_neg_7 AS (SELECT msisdn
                                                                FROM C360_FLAT_SERVICES
                                                                WHERE PARTITION_DATE = DATE_FORMAT(CURDATE() - INTERVAL 7 DAY, '%Y%m%d')
                                                                  AND cal_internet_television_count_sms_received_daily >
                                                                      10),
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
     water_amount_bill_daily
         AS (SELECT msisdn
             FROM C360_FLAT_SERVICES
             WHERE PARTITION_DATE >= DATE_FORMAT(20250226, '%Y%m%d')
               AND PARTITION_DATE <= DATE_FORMAT(20250227, '%Y%m%d')
               AND water_amount_bill_daily <
                   300000
             GROUP BY msisdn
             HAVING COUNT(*) >= 2),
     id_1741589800082 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM water_amount_bill_daily
                                INTERSECT
                                SELECT msisdn
                                FROM credit_score_monthly_monthly_neg_1_0_ex) AS t),
     id_1741589800080 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM electricity_last_amount_bill_weekly_202452
                                UNION
                                SELECT msisdn
                                FROM id_1741589800082) AS t)
SELECT msisdn
FROM (SELECT msisdn
      FROM cus_type_n_monthly_202412_202501
      INTERSECT
      SELECT msisdn
      FROM cal_internet_television_count_sms_received_daily_neg_7
      INTERSECT
      SELECT msisdn
      FROM id_1741589800080
      INTERSECT
      SELECT msisdn
      FROM arpu_n_monthly_week_neg_4_neg_2_all) AS t