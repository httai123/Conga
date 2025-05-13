package com.viettel.vds.cdp.translator;

import com.viettel.vds.entities.cdp.Ruleset;

public interface ISQLTranslator {
    public String translate(Ruleset ruleset);
    public String basicRulesetToSQL(Ruleset ruleset);
}
