package org.kuali.ole.deliver.drools.rules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.ole.deliver.bo.drools.DroolsRuleBo;
import org.kuali.ole.deliver.drools.DroolsConstants;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pvsubrah on 7/8/15.
 */
public class IsActiveRuleFormulator extends RuleFormulatorUtil implements RuleFormulator {

    GeneralChecksDroolFileGenerator droolFileGenerator;

    public GeneralChecksDroolFileGenerator getDroolFileGenerator() {
        return droolFileGenerator;
    }

    public void setDroolFileGenerator(GeneralChecksDroolFileGenerator droolFileGenerator) {
        this.droolFileGenerator = droolFileGenerator;
    }

    @Override
    public Map formulateRuleMap(DroolsRuleBo droolsRuleBo) {
        return super.formulateRule(droolsRuleBo);
    }

    @Override
    public String formulateRules(DroolsRuleBo droolsRuleBo) {
        return "";
    }

    @Override
    protected String getTemplateFileName() {
        return "general-checks/active-indicator.txt";
    }

    @Override
    protected void processExtraRules(DroolsRuleBo droolsRuleBo, Map map) {
        //Empty
    }


    @Override
    public boolean isInterested(String value) {
        return value.equals(DroolsConstants.GENERAL_CHECK_RULE_TYPE.IS_ACTIVE);
    }

    @Override
    public boolean isInterestedForParameters(String value) {
        return false;
    }

    @Override
    public boolean isInterestedForThenCustomRules(String value) {
        return false;
    }


}
