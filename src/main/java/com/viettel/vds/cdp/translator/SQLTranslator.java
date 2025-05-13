package com.viettel.vds.cdp.translator;

import com.viettel.vds.cdp.translator.exceptions.SQLTranslatorException;
import com.viettel.vds.cdp.translator.model.Item;
import com.viettel.vds.entities.Constants;
import com.viettel.vds.entities.cdp.AttributeRule;
import com.viettel.vds.entities.cdp.Operator;
import com.viettel.vds.entities.cdp.Ruleset;
import com.viettel.vds.model.mongo.cdp.RulesetOperators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Refactor lại từ {@link SQLTranslator}, dùng class này để có thể tích hợp với các module CDP
 */
public class SQLTranslator implements ISQLTranslator {
    private final List<Item> items;

    public SQLTranslator(List<RulesetOperators> operators) {
        if (operators == null || operators.isEmpty()) {
            throw new SQLTranslatorException("operators is null or empty");
        }
        items = new ArrayList<>();
        for (RulesetOperators o : operators) {
            items.add(new Item(o.getDataType(), o.getCode(), o.getName(), o.getSqlOperator(), o.getMapping()));
        }
    }

    @SuppressWarnings("java:S3776")
    private String translate(AttributeRule rule) {
        Operator operator = rule.getOperator();
        if (operator == null) {
            return "";
        }

        // Get the operator value from the DataFrame
        String code = operator.getCode();
        String dataType = operator.getDataType();

        //  get item satisfied with code, dataType, valueOption, valueType
        Item item = items.stream()
                .filter(o -> code.equals(o.code) && dataType.equals(o.dataType))
                .findFirst().orElse(null);

        if (item == null) {
            throw new IllegalArgumentException(Constants.INVALID_OPERATOR_MSG + " dataType: " + dataType + ", code: " + code);
        }
        String mapping = item.sql_condition_mapping;

        List<Object> values = rule.getValue();
        String value = "";
        String value1 = "";
        long value2 = 0L;

        if (values != null) {
            if ("Date".equals(dataType)) {
                try {
                    value1 = "CURDATE()";
                    value2 = Long.parseLong(String.valueOf(values.get(0)));
                } catch (NumberFormatException e) {
                    value1 = values.stream().map(v -> String.format("'%s'", v)).collect(Collectors.joining(","));
                }
                if ("equalTo".equals(code) || "notEqualTo".equals(code)) {
                    value = values.stream().map(v -> String.format("'%s'", v)).collect(Collectors.joining(","));
                }
            } else if ("Timestamp".equals(dataType)) {
                try {
                    value1 = "CURRENT_TIMESTAMP()";
                    value2 = Long.parseLong(String.valueOf(values.get(0)));
                } catch (NumberFormatException e) {
                    value1 = values.stream().map(v -> String.format("'%s'", v)).collect(Collectors.joining(","));
                }
                if ("equalTo".equals(code) || "notEqualTo".equals(code)) {
                    value = values.stream().map(v -> String.format("'%s'", v)).collect(Collectors.joining(","));
                }
            } else if ("String".equals(dataType)) {
                if ("like".equals(code) || "notLike".equals(code)) {
                    value = values.stream().map(String::valueOf).collect(Collectors.joining(","));
                } else {
                    value = values.stream().map(v -> String.format("'%s'", v)).collect(Collectors.joining(","));
                }
            } else {
                value = values.stream().map(String::valueOf).collect(Collectors.joining(","));
            }
        }

        return mapping
                .replace("{col_name}", rule.getDb() + "." + rule.getTable() + "." + rule.getAttribute())
                .replace("{value}", value)
                .replace("{value1}", value1)
                .replace("{value2}", String.valueOf(value2));
    }

    @Override
    public String translate(Ruleset ruleset) {
        StringBuilder sb = new StringBuilder();

        String logical = ruleset.getLogical();
        List<AttributeRule> attributeRules = ruleset.getAttributeRules();
        List<Ruleset> autoRules = ruleset.getAutoRules();

        if (attributeRules != null) {
            attributeRules.forEach(attributeRule -> sb.append(" ( ")
                    .append(translate(attributeRule))
                    .append(" ) ")
                    .append(logical));
        }

        if (autoRules != null) {
            autoRules.forEach(rs -> sb.append(" ( ")
                    .append(translate(rs))
                    .append(" ) ")
                    .append(logical));
        }

        String sql = sb.toString();
        if (sql.endsWith(logical)) {
            sql = sql.substring(0, sql.length() - logical.length() - 1);
        }

        return sql;
    }

    public String basicRulesetToSQL(Ruleset ruleset) {
        return "select * from " + translate(ruleset);
    }
}
