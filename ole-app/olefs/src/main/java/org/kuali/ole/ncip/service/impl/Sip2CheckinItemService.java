package org.kuali.ole.ncip.service.impl;

import org.kuali.asr.handler.CheckinItemResponseHandler;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.deliver.service.ParameterValueResolver;
import org.kuali.ole.bo.OLECheckInItem;

/**
 * Created by chenchulakshmig on 8/27/15.
 */
public class Sip2CheckinItemService extends CheckinItemService{

    @Override
    public String prepareResponse() {

        switch (responseFormatType) {
            case ("XML"):
                response = ((CheckinItemResponseHandler) getResponseHandler()).marshalObjectToSIP2Xml(getOleCheckInItem());
                break;
            case ("JSON"):
                response = getResponseHandler().marshalObjectToJson(getOleCheckInItem());
                break;
        }

        return response;
    }

    @Override
    public String getOperatorId(String operatorId) {
        return ParameterValueResolver.getInstance().getParameter(OLEConstants
                .APPL_ID_OLE, OLEConstants.DLVR_NMSPC, OLEConstants.DLVR_CMPNT, operatorId);
    }
}
