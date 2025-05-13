WITH cal_tuition_count_evt_monthly_neg_3_0 AS (SELECT msisdn
                                               FROM C360_FLAT_SERVICES
                                               WHERE PARTITION_DATE IN (
                                                                        LEAST(DATE_FORMAT(
                                                                                      LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                      '%Y%m%d'),
                                                                              DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                        DATE_FORMAT(
                                                                                LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                '%Y%m%d'),
                                                                        DATE_FORMAT(
                                                                                LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                '%Y%m%d'),
                                                                        DATE_FORMAT(
                                                                                LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                '%Y%m%d')
                                                   )
                                                 AND (cal_tuition_count_evt_monthly >=
                                                      2)
                                               GROUP BY msisdn
                                               HAVING COUNT(*) >= 1),
     cal_tuition_count_urlopen_monthly_neg_3_0 AS (SELECT msisdn
                                                   FROM C360_FLAT_SERVICES
                                                   WHERE PARTITION_DATE IN (
                                                                            LEAST(DATE_FORMAT(
                                                                                          LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                          '%Y%m%d'),
                                                                                  DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                            DATE_FORMAT(
                                                                                    LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                    '%Y%m%d'),
                                                                            DATE_FORMAT(
                                                                                    LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                    '%Y%m%d'),
                                                                            DATE_FORMAT(
                                                                                    LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                    '%Y%m%d')
                                                       )
                                                     AND (cal_tuition_count_urlopen_monthly >=
                                                          2)
                                                   GROUP BY msisdn
                                                   HAVING COUNT(*) >= 1),
     datetime_tuition_lastest_potential_moment_url_monthly_neg_3_0 AS (SELECT msisdn
                                                                       FROM C360_FLAT_SERVICES
                                                                       WHERE PARTITION_DATE IN (
                                                                                                LEAST(DATE_FORMAT(
                                                                                                              LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                                              '%Y%m%d'),
                                                                                                      DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                                        '%Y%m%d'),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                                        '%Y%m%d'),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                                        '%Y%m%d')
                                                                           )
                                                                         AND (
                                                                           datetime_tuition_lastest_potential_moment_url_monthly !=
                                                                           CURDATE() - INTERVAL 45 DAY)
                                                                       GROUP BY msisdn
                                                                       HAVING COUNT(*) >= 1),
     amount_sum_tuition_next_bill_monthly_neg_3_0 AS (SELECT msisdn
                                                      FROM C360_FLAT_SERVICES
                                                      WHERE PARTITION_DATE IN (
                                                                               LEAST(DATE_FORMAT(
                                                                                             LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                             '%Y%m%d'),
                                                                                     DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                               DATE_FORMAT(
                                                                                       LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                       '%Y%m%d'),
                                                                               DATE_FORMAT(
                                                                                       LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                       '%Y%m%d'),
                                                                               DATE_FORMAT(
                                                                                       LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                       '%Y%m%d')
                                                          )
                                                        AND amount_sum_tuition_next_bill_monthly >
                                                            5000
                                                      GROUP BY msisdn
                                                      HAVING COUNT(*) >= 1),
     datetime_tuition_lastest_potential_moment_evt_monthly_neg_3_0 AS (SELECT msisdn
                                                                       FROM C360_FLAT_SERVICES
                                                                       WHERE PARTITION_DATE IN (
                                                                                                LEAST(DATE_FORMAT(
                                                                                                              LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                                              '%Y%m%d'),
                                                                                                      DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                                        '%Y%m%d'),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                                        '%Y%m%d'),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                                        '%Y%m%d')
                                                                           )
                                                                         AND (
                                                                           datetime_tuition_lastest_potential_moment_evt_monthly !=
                                                                           CURRENT_TIMESTAMP() - INTERVAL 45 SECOND)
                                                                       GROUP BY msisdn
                                                                       HAVING COUNT(*) >= 1),
     date_tuition_next_bill_monthly_neg_3_0 AS (SELECT msisdn
                                                FROM C360_FLAT_SERVICES
                                                WHERE PARTITION_DATE IN (
                                                                         LEAST(DATE_FORMAT(
                                                                                       LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                       '%Y%m%d'),
                                                                               DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                         DATE_FORMAT(
                                                                                 LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                 '%Y%m%d'),
                                                                         DATE_FORMAT(
                                                                                 LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                 '%Y%m%d'),
                                                                         DATE_FORMAT(
                                                                                 LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                 '%Y%m%d')
                                                    )
                                                  and (date_tuition_next_bill_monthly <=
                                                       CURDATE() - INTERVAL 45 DAY)
                                                GROUP BY msisdn
                                                HAVING COUNT(*) >= 1),
     cal_tuition_count_sms_received_monthly_neg_3_0 AS (SELECT msisdn
                                                        FROM C360_FLAT_SERVICES
                                                        WHERE PARTITION_DATE IN (
                                                                                 LEAST(DATE_FORMAT(
                                                                                               LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                               '%Y%m%d'),
                                                                                       DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                                 DATE_FORMAT(
                                                                                         LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                         '%Y%m%d'),
                                                                                 DATE_FORMAT(
                                                                                         LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                         '%Y%m%d'),
                                                                                 DATE_FORMAT(
                                                                                         LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                         '%Y%m%d')
                                                            )
                                                          AND (cal_tuition_count_sms_received_monthly >=
                                                               10)
                                                        GROUP BY msisdn
                                                        HAVING COUNT(*) >= 1),
     datetime_tuition_lastest_potential_moment_sms_monthly_neg_3_0 AS (SELECT msisdn
                                                                       FROM C360_FLAT_SERVICES
                                                                       WHERE PARTITION_DATE IN (
                                                                                                LEAST(DATE_FORMAT(
                                                                                                              LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01')),
                                                                                                              '%Y%m%d'),
                                                                                                      DATE_FORMAT(CURDATE() - INTERVAL 1 DAY, '%Y%m%d')),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 1 MONTH),
                                                                                                        '%Y%m%d'),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 2 MONTH),
                                                                                                        '%Y%m%d'),
                                                                                                DATE_FORMAT(
                                                                                                        LAST_DAY(CONCAT(DATE_FORMAT(CURDATE(), '%Y%m'), '01') - INTERVAL 3 MONTH),
                                                                                                        '%Y%m%d')
                                                                           )
                                                                         and (
                                                                           datetime_tuition_lastest_potential_moment_sms_monthly !=
                                                                           CURRENT_TIMESTAMP() - INTERVAL 45 SECOND)
                                                                       GROUP BY msisdn
                                                                       HAVING COUNT(*) >= 1),
     id_1741688899787 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM datetime_tuition_lastest_potential_moment_url_monthly_neg_3_0
                                INTERSECT
                                SELECT msisdn
                                FROM cal_tuition_count_evt_monthly_neg_3_0) AS t),
     id_1741688899784 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM datetime_tuition_lastest_potential_moment_evt_monthly_neg_3_0
                                INTERSECT
                                SELECT msisdn
                                FROM cal_tuition_count_urlopen_monthly_neg_3_0) AS t),
     id_1741688899783 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM id_1741688899784
                                UNION
                                SELECT msisdn
                                FROM id_1741688899787) AS t),
     id_1741688899780 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM date_tuition_next_bill_monthly_neg_3_0
                                INTERSECT
                                SELECT msisdn
                                FROM amount_sum_tuition_next_bill_monthly_neg_3_0) AS t),
     id_1741688899777 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM cal_tuition_count_sms_received_monthly_neg_3_0
                                INTERSECT
                                SELECT msisdn
                                FROM datetime_tuition_lastest_potential_moment_sms_monthly_neg_3_0) AS t),
     id_1741688899776 AS (SELECT msisdn
                          FROM (SELECT msisdn
                                FROM id_1741688899777
                                UNION
                                SELECT msisdn
                                FROM id_1741688899780) AS t)
SELECT msisdn
FROM (SELECT msisdn
      FROM id_1741688899776
      INTERSECT
      SELECT msisdn
      FROM id_1741688899783) AS t