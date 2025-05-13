package com.viettel.vds.cdp.translator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.viettel.vds.cdp.translator.enums.FrequentlyAggregate;
import com.viettel.vds.cdp.translator.enums.LogicalQuantifier;
import com.viettel.vds.cdp.translator.enums.TimeRangeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class SelectedTimeModel {
    private int[] timeRange;
    private TimeRangeType timeRangeType;
    private FrequentlyAggregate frequency;
    private LogicalQuantifier logicalQuantifier;
}
