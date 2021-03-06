package org.kuali.ole.deliver.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Interval;
import org.kuali.ole.OLEConstants;
import org.kuali.ole.deliver.bo.OleCirculationDesk;
import org.kuali.ole.deliver.calendar.bo.*;
import org.kuali.ole.deliver.drools.FixedDateUtil;
import org.kuali.ole.deliver.service.ParameterValueResolver;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KRADServiceLocator;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by pvsubrah on 10/3/15.
 */
public class LoanDateTimeUtil extends ExceptionDateLoanDateTimeUtil {
    private String policyId;
    private Map<String, OleCalendar> calendarMap;
    private OleCalendar activeCalendar;
    private BusinessObjectService businessObjectService;
    private Boolean nonWorkingHoursCheck = false;

    public Date calculateDateTimeByPeriod(String loanPeriod, OleCirculationDesk oleCirculationDesk) {
        Date loanDueDate;

        loanDueDate = getLoanDueDate(loanPeriod);

        if (null != loanDueDate && null != oleCirculationDesk) {
            OleCalendar activeCalendar = getActiveCalendar(loanDueDate, oleCirculationDesk.getCalendarGroupId());
            setActiveCalendar(activeCalendar);

            if (null != activeCalendar) {
                loanDueDate = calculateDueDate(loanDueDate);
            }
        }

        return loanDueDate;
    }

    private Date calculateDueDate(Date loanDueDate) {
        OleCalendarExceptionPeriod oleCalendarExceptionPeriod = doesDateFallInExceptionPeriod(getActiveCalendar(), loanDueDate);

        if (null == oleCalendarExceptionPeriod) {
            OleCalendarExceptionDate exceptionDate = isDateAnExceptionDate(getActiveCalendar(), loanDueDate);
            if (null == exceptionDate) {
                loanDueDate = handleNonWorkingHoursWorkflow(loanDueDate, getActiveCalendar().getOleCalendarWeekList());
            } else {
                if (StringUtils.isEmpty(exceptionDate.getOpenTime()) && StringUtils.isEmpty(exceptionDate.getCloseTime())) {
                    //Holiday workflow;
                    Date followingDay = DateUtils.addDays(loanDueDate, 1);
                    loanDueDate = calculateDueDate(followingDay);
                } else {
                    // Partial hours workflow
                    loanDueDate = handleExceptionDayWithPartialHours(loanDueDate, exceptionDate);
                }
            }
        } else {
            List<OleCalendarExceptionPeriodWeek> oleCalendarExceptionPeriodWeekList = oleCalendarExceptionPeriod.getOleCalendarExceptionPeriodWeekList();
            //If the week list is empty i.e its a holiday period;
            if (CollectionUtils.isEmpty(oleCalendarExceptionPeriodWeekList)) {
                Timestamp endDate = oleCalendarExceptionPeriod.getEndDate();
                Date followingDay = DateUtils.addDays(endDate, 1);
                loanDueDate = calculateDueDate(followingDay);
            } else {
                loanDueDate = handleNonWorkingHoursWorkflow(loanDueDate, oleCalendarExceptionPeriodWeekList);
            }
        }
        return loanDueDate;
    }

    private Date handleExceptionDayWithPartialHours(Date loanDueDate, OleCalendarExceptionDate exceptionDate) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(exceptionDate.getDate());
        exceptionDate.setStartDay(String.valueOf(instance.get(Calendar.DAY_OF_WEEK)-1));
        exceptionDate.setEndDay(String.valueOf(instance.get(Calendar.DAY_OF_WEEK)-1));

        List oleBaseCalendarWeekList = new ArrayList<>();
        oleBaseCalendarWeekList.add(exceptionDate);
        loanDueDate = handleNonWorkingHoursWorkflow(loanDueDate, oleBaseCalendarWeekList);

        return loanDueDate;
    }

    private Date handleNonWorkingHoursWorkflow(Date loanDueDate, List<? extends OleBaseCalendarWeek> oleBaseCalendarWeekList) {
        Map<String, Map<String, String>> openAndClosingTimeForTheGivenDayFromWeekList = getOpenAndClosingTimeForTheGivenDayFromWeekList(loanDueDate, oleBaseCalendarWeekList);
        if (nonWorkingHoursCheck) {
            Map<String, String> openTime = openAndClosingTimeForTheGivenDayFromWeekList.get("openTime");
            Calendar calendar = resolveDateTime(openTime, loanDueDate);
            loanDueDate = calendar.getTime();
            loanDueDate = handleGracePeriod(loanDueDate);
        } else {
            boolean loanDueTimeWithinWorkingHours = isLoanDueTimeWithinWorkingHours(loanDueDate, oleBaseCalendarWeekList);

            if (!loanDueTimeWithinWorkingHours) {
                if (includeNonWorkingHours()) {
                    Date followingDay = DateUtils.addDays(loanDueDate, 1);
                    nonWorkingHoursCheck = true;
                    loanDueDate = calculateDueDate(followingDay);
                } else {
                    Map<String, String> closeTime = openAndClosingTimeForTheGivenDayFromWeekList.get("closeTime");
                    Calendar calendar = resolveDateTime(closeTime, loanDueDate);
                    loanDueDate = calendar.getTime();
                }
            }
        }
        return loanDueDate;
    }


    private Date getLoanDueDate(String loanPeriod) {
        Date loanDueDate = null;

        if (loanPeriod.equalsIgnoreCase(OLEConstants.FIXED_DUE_DATE)) {
            loanDueDate = new FixedDateUtil().getFixedDateByPolicyId(getPolicyId());
        } else {
            StringTokenizer stringTokenizer = new StringTokenizer(loanPeriod, "-");
            String amount = stringTokenizer.nextToken();
            String period = stringTokenizer.nextToken();

            if (period.equalsIgnoreCase("m")) {
                loanDueDate = DateUtils.addMinutes(new Date(), Integer.parseInt(amount));
            } else if (period.equalsIgnoreCase("h")) {
                loanDueDate = DateUtils.addHours(new Date(), Integer.parseInt(amount));
            } else if (period.equalsIgnoreCase("d")) {
                loanDueDate = DateUtils.addDays(new Date(), Integer.parseInt(amount));
            } else if (period.equalsIgnoreCase("w")) {
                loanDueDate = DateUtils.addWeeks(new Date(), Integer.parseInt(amount));
            }
        }
        return loanDueDate;
    }

    private Date handleGracePeriod(Date loanDueDate) {
        Date updatedDate = null;
        String gracePeriod = getGracePeriodForIncludingNonWorkingHours();
        if (StringUtils.isNotBlank(gracePeriod)) {
            StringTokenizer stringTokenizer = new StringTokenizer(gracePeriod, "-");
            String amount = stringTokenizer.nextToken();
            String interval = stringTokenizer.nextToken();
            if (interval.equalsIgnoreCase("m")) {
                updatedDate = DateUtils.addMinutes(loanDueDate, Integer.valueOf(amount));
            } else if (interval.equalsIgnoreCase("h")) {
                updatedDate = DateUtils.addHours(loanDueDate, Integer.valueOf(amount));
            }
        } else {
            updatedDate = loanDueDate;
        }
        return updatedDate;
    }

    public Boolean includeNonWorkingHours() {
        return ParameterValueResolver.getInstance().getParameterAsBoolean(OLEConstants
                .APPL_ID, OLEConstants.DLVR_NMSPC, OLEConstants.DLVR_CMPNT, OLEConstants.CALENDER_FLAG);
    }


    private boolean compareTimes(Map<String, String> openTimeForTheGivenDay, Map<String, String> closingTimeForTheGivenDay, Date loanDueDate) {
        Calendar closeTimeCalendar = resolveDateTime(closingTimeForTheGivenDay, loanDueDate);
        Calendar openTimeCalendar = resolveDateTime(openTimeForTheGivenDay, loanDueDate);
        //Compares for the givne day if the loan due time falls within the closing time
        return (openTimeCalendar.getTime().compareTo(loanDueDate) <= 0 && closeTimeCalendar.getTime().compareTo(loanDueDate) >= 0);
    }

    private Calendar resolveDateTime(Map<String, String> closingTimeForTheGivenDay, Date loanDueDate) {
        String time = closingTimeForTheGivenDay.keySet().iterator().next();
        String timeSession = closingTimeForTheGivenDay.get(time);
        StringTokenizer timeTokenizer = new StringTokenizer(time, ":");
        int hour = Integer.parseInt(timeTokenizer.nextToken());

        Calendar instance = Calendar.getInstance();
        //The date is being set to the loan due date to ensure the comparisons are for the given day.
        instance.setTime(loanDueDate);
        //The hour and minutes are for closing times.
        instance.set(Calendar.HOUR_OF_DAY, hour);
        instance.set(Calendar.MINUTE, Integer.parseInt(timeTokenizer.nextToken()));
        return instance;
    }


    public OleCalendar getActiveCalendar(Date date, String groupId) {
        if (!getCalendarMap().containsKey(groupId)) {
            List<OleCalendar> oleCalendarList = getOleCalendars(groupId);
            for (OleCalendar calendar : oleCalendarList) {
                if (calendarExists(new Timestamp(date.getTime()), calendar.getBeginDate(), calendar.getEndDate())) {
                    getCalendarMap().put(groupId, calendar);
                }
            }
        }
        return getCalendarMap().get(groupId);
    }

    protected List<OleCalendar> getOleCalendars(String groupId) {
        HashMap criteriaMap = new HashMap();
        criteriaMap.put(OLEConstants.CALENDER_ID, groupId);
        return (List<OleCalendar>) getBusinessObjectService().findMatching(OleCalendar.class, criteriaMap);
    }

    public boolean calendarExists(Timestamp date, Timestamp fromDate, Timestamp toDate) {
        Interval interval;
        if (null != fromDate) {
            if (null != toDate) {
                interval = new Interval(fromDate.getTime(), toDate.getTime());
                return interval.contains(date.getTime());
            } else {
                return date.compareTo(fromDate) > 0 ? true : false;
            }
        }

        return false;
    }

    public Map<String, OleCalendar> getCalendarMap() {
        if (null == calendarMap) {
            calendarMap = new HashMap<>();
        }
        return calendarMap;
    }

    public void setCalendarMap(Map<String, OleCalendar> calendarMap) {
        this.calendarMap = calendarMap;
    }

    public BusinessObjectService getBusinessObjectService() {
        if (null == businessObjectService) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public String getGracePeriodForIncludingNonWorkingHours() {
        return ParameterValueResolver.getInstance().getParameter(OLEConstants
                .APPL_ID, OLEConstants.DLVR_NMSPC, OLEConstants.DLVR_CMPNT, OLEConstants.GRACE_PERIOD_FOR_NON_WORKING_HOURS);
    }


    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public boolean isLoanDueTimeWithinWorkingHours(Date loanDueDate, List<? extends OleBaseCalendarWeek> oleBaseCalendarWeekList) {
        Map<String, Map<String, String>> openAndClosingTimeForTheGivenDay = getOpenAndClosingTimeForTheGivenDay(loanDueDate, oleBaseCalendarWeekList);
        return compareTimes(openAndClosingTimeForTheGivenDay.get("openTime"), openAndClosingTimeForTheGivenDay.get("closeTime"), loanDueDate);
    }

    private Map<String, Map<String, String>> getOpenAndClosingTimeForTheGivenDay(Date loanDueDate, List<? extends OleBaseCalendarWeek> oleBaseCalendarWeekList) {
        Map<String, Map<String, String>> openingAndClosingTimeMap;

        openingAndClosingTimeMap = getOpenAndClosingTimeForTheGivenDayFromWeekList(loanDueDate, oleBaseCalendarWeekList);

        return openingAndClosingTimeMap;
    }


    public Map<String, Map<String, String>> getOpenAndClosingTimeForTheGivenDayFromWeekList(Date loanDueDate, List<? extends OleBaseCalendarWeek> oleCalendarWeekList) {
        int day = loanDueDate.getDay();
        Map<String, Map<String, String>> openingAndClosingTimeMap = new HashMap<>();
        Map<String, String> closingTimeMap = new HashMap<>();
        Map<String, String> openingTimeMap = new HashMap<>();

        for (Iterator<? extends OleBaseCalendarWeek> iterator = oleCalendarWeekList.iterator(); iterator.hasNext(); ) {
            OleBaseCalendarWeek OleBaseCalendarWeek = iterator.next();
            String startDay = OleBaseCalendarWeek.getStartDay();
            if (startDay.equals(String.valueOf(day))) {
                resolveOpenAndCloseTimesForCalendarWeek(closingTimeMap, openingTimeMap, OleBaseCalendarWeek);
                break;
            }
            String endDay = OleBaseCalendarWeek.getEndDay();
            //The start day may not always be Sunday (0); Hence the check.
            if (Integer.valueOf(startDay) < Integer.valueOf(endDay)) {
                if (day > Integer.valueOf(startDay) && day <= Integer.valueOf(endDay)) {
                    resolveOpenAndCloseTimesForCalendarWeek(closingTimeMap, openingTimeMap, OleBaseCalendarWeek);
                    break;
                }
            } else {
                if (day < Integer.valueOf(startDay) && day >= Integer.valueOf(endDay)) {
                    resolveOpenAndCloseTimesForCalendarWeek(closingTimeMap, openingTimeMap, OleBaseCalendarWeek);
                    break;
                }
            }
        }
        openingAndClosingTimeMap.put("openTime", openingTimeMap);
        openingAndClosingTimeMap.put("closeTime", closingTimeMap);
        return openingAndClosingTimeMap;
    }

    private void resolveOpenAndCloseTimesForCalendarWeek(Map<String, String> closingTimeMap, Map<String, String> openingTimeMap, OleBaseCalendarWeek oleBaseCalendarWeek) {
        String closeTime = oleBaseCalendarWeek.getCloseTime();
        String closeTimeSession = oleBaseCalendarWeek.getCloseTimeSession();
        closingTimeMap.put(closeTime, closeTimeSession);

        String openTime = oleBaseCalendarWeek.getOpenTime();
        String openTimeSession = oleBaseCalendarWeek.getOpenTimeSession();
        openingTimeMap.put(openTime, openTimeSession);
    }

    public OleCalendar getActiveCalendar() {
        return activeCalendar;
    }

    public void setActiveCalendar(OleCalendar activeCalendar) {
        this.activeCalendar = activeCalendar;
    }
}
