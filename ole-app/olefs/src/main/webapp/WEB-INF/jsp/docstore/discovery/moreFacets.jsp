<%--
   - Copyright 2011 The Kuali Foundation.
   - 
   - Licensed under the Educational Community License, Version 2.0 (the "License");
   - you may not use this file except in compliance with the License.
   - You may obtain a copy of the License at
   - 
   - http://www.opensource.org/licenses/ecl2.php
   - 
   - Unless required by applicable law or agreed to in writing, software
   - distributed under the License is distributed on an "AS IS" BASIS,
   - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   - See the License for the specific language governing permissions and
   - limitations under the License.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	import="org.kuali.ole.docstore.discovery.*" pageEncoding="UTF-8"%>
<%@ page import="org.kuali.ole.docstore.discovery.web.struts.form.DiscoveryForm"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:useBean id="discoveryForm"	class="org.kuali.ole.docstore.discovery.web.struts.form.DiscoveryForm" scope="session" />
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

	<script type="text/javascript" src="script/jquery/jquery-1.4.3.min.js"></script>
    <link type="text/css" rel="Stylesheet" href="./css/blackbird.css" />
 <script type="text/javascript" src="script/SoftXpath.js"></script>
    <style type="text/css">
    	body {
    		background-color:#FFFFFF;
    	}
    	.searchFacetsDiv {
    		width:450px !important;
    	}
    </style>   

<div style="margin:1cm">
<h3>Limit your search</h3>
<form id="moreFacets" name="moreFacets">
<textarea id="resultsXml"  style="display: none" 
 name="resultsXml" rows="5" cols="80" >
  <c:out value="${discoveryForm.searchResult}"/>
</textarea>

<input type="text" id="fieldName" name="fieldName" style="display: none" />
</form>
<table>
    <tr>
        <td></td>
        <td align="right">
            <div>
                <%
                    StringBuffer sortDisplay = new StringBuffer();
                    StringBuffer pageDisplay = new StringBuffer();
                    String sort = discoveryForm.getFacetSort();
                    if (sort == null) {
                        sort = "lex";
                    }
                    String pageSize = discoveryForm.getFacetPageSize();
                    if (pageSize == null) {
                        pageSize = "25";
                    }

                    if (pageSize.equals("25")) {
                        pageDisplay.append("<span>25</span> | ");
                        pageDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetPageSize=50\" style=\"text-decoration: underline\">50</a> |");
                        pageDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetPageSize=100\" style=\"text-decoration: underline\">100</a>");
                    }
                    else if (pageSize.equals("50")) {
                        pageDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetPageSize=25\" style=\"text-decoration: underline\">25</a> |");
                        pageDisplay.append("<span>50</span> | ");
                        pageDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetPageSize=100\" style=\"text-decoration: underline\">100</a>");
                    }
                    else {
                        pageDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetPageSize=25\" style=\"text-decoration: underline\">25</a> |");
                        pageDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetPageSize=50\" style=\"text-decoration: underline\">50</a> |");
                        pageDisplay.append("<span>100</span>");
                    }

                    if (sort.equals("count")) {
                        sortDisplay.append("<span>Relevance</span> | ");
                        sortDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetSort=lex\" style=\"text-decoration: underline\">Alphabet</a>");
                    }
                    else {
                        sortDisplay.append(
                                "<a href=\"discovery.do?searchType=moreFacets&facetSort=count\" style=\"text-decoration: underline\">Relevance</a> | ");
                        sortDisplay.append("<span>Alphabet</span>");
                    }
                %>

                Display <%=pageDisplay.toString()%>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <br>
                <br>

                Sort <%=sortDisplay.toString()%>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

            </div>
        </td>
    </tr>
    <tr>
        <td>
            <div>
                <%
                    StringBuffer prefixBuffer = new StringBuffer();
                    String[] prefix = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F",
                                       "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                                       "W", "X", "Y", "Z"};
                    if (discoveryForm.getFacetPrefix().length() == 0) {
                        prefixBuffer.append("<span>ALL</span> | ");
                    }
                    else {
                        prefixBuffer.append("<a href=\"discovery.do?searchType=moreFacets&facetPrefix=\" style=\"text-decoration: underline\">ALL</a> | ");
                    }
                    for (int i = 0; i < prefix.length; i++) {
                        if (discoveryForm.getFacetPrefix().equalsIgnoreCase(prefix[i])) {
                            prefixBuffer.append("<span>");
                            prefixBuffer.append(prefix[i]);
                            prefixBuffer.append("</span>");
                        }
                        else {
                            prefixBuffer.append("<a href=\"discovery.do?searchType=moreFacets&facetPrefix=");
                            prefixBuffer.append(prefix[i].toLowerCase());
                            prefixBuffer.append("\" style=\"text-decoration: underline\">" + prefix[i] + "</a>");
                        }
                        if (prefix.length != i) {
                            prefixBuffer.append(" | ");
                        }
                    }
                %>
                <br>
                <br>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

                <%=prefixBuffer.toString()%>

            </div>
        </td>
    </tr>
    <tr>
        <td>
            <div id="searchFacetsDiv" class="searchFacetsDiv">

            </div>
        </td>
    </tr>
    <tr>
        <td></td>
        <td align="right">
            <div style="border-bottom-right-radius:500 " align="right">
                <a href="discovery.do?searchType=moreFacets&facetPage=previous" id="previous"
                   style="text-decoration: underline">Previous Page</a>
                <a href="discovery.do?searchType=moreFacets&facetPage=next" id="next"
                   style="text-decoration: underline"> Next Page</a>
                <br/><br/><br/><br/><br/><br/>
            </div>
        </td>
    </tr>
</table>
</div>
<script type="text/javascript">
//var fieldName = opener.document.getElementById("selectedFacetName").value;
var facetName = opener.document.getElementById("selectedFacetName").value;
//var resultsXml = opener.document.getElementById("resultsXml").value;
 var resultsXml = document.getElementById("resultsXml").value;
        resultsXml = resultsXml.trim();
var languagesInfoXml;
var resultsXPathObj = new SoftXpath();
resultsXPathObj.registerNamespace('','');
if(!resultsXPathObj.loadXML(resultsXml)){
	alert('Loading Results xml failed!!!');
}
var languageXPathObj = new SoftXpath();
languageXPathObj.registerNamespace('','');
if(!languageXPathObj.load("languages2.xml")){
	alert('Loading Languages xml failed!!!');
}

function getLanguageName(languageCode){
	var languageName=null;
	if(languageCode.length == 3){
		var filterQuery = '//lang[@code=\''+languageCode+'\']';
		var languageNodes = languageXPathObj.selectNodes(filterQuery);
			if ((languageNodes != null) && (languageNodes.length > 0)) {
				languageName=languageNodes[0].text;
			}
	}
	else{
		languageName=languageCode;
	}
	if(languageName==null){
		languageName=languageCode;
	}
	return languageName;
}
function isFacetSelected(termsList,facetValue){
	
	var flag=false;
	for(y=0;y<termsList.length;y++){
		if(termsList[y]==facetValue){
			flag= true;
		break;
		}
	}
	return flag;
	
}


var facetHtmlText = [];
var i = 0;


var fieldNameTemp;

var terms = "";
var termsNodes = resultsXPathObj.selectNodes("//lst[@name='responseHeader']//str[@name='terms']");
if((termsNodes != null || termsNodes != "") && termsNodes.length > 0){
	terms = termsNodes[0].text;
}
var termsList="";
var sortarray = new Array();
if(terms.length>0){
	termsList=terms.split("|");
	}
fieldNameTemp=facetName.substring(0,facetName.indexOf("_"));
if(fieldNameTemp == "PublicationDate"){
	fieldNameTemp = "Publication Date";
}
facetHtmlText[i++] = "<h3>" +fieldNameTemp+ "</h3>";

var facetFieldNameXPath = "//lst[@name='facet_fields']/lst[@name='" + facetName + "']/int/@name";
var facetFieldNamesList = resultsXPathObj.selectNodes(facetFieldNameXPath);
if((facetFieldNamesList != "" || facetFieldNamesList != null)&& facetFieldNamesList.length > 0){
}
var count=resultsXPathObj.selectNodes("//lst[@name='facet_fields']/lst[@name='" + facetName + "']/int").length;

var recordCount = 0;
var sortarray = new Array();
for (var j = 0; j < count; j++) {
	var facetFieldName = "";
	var facetFieldNameNodes = resultsXPathObj.selectNodes("//lst[@name='facet_fields']/lst[@name='" + facetName + "']/int[" + (j+1) + "]/@name");
	if( (facetFieldNameNodes != null || facetFieldNameNodes != "")&&(facetFieldNameNodes.length > 0)){
		facetFieldName = facetFieldNameNodes[0].text;
	}
	var facetFieldValue = resultsXPathObj.selectNodes("//lst[@name='facet_fields']/lst[@name='" + facetName + "']/int[@name='" + facetFieldName + "']");
	var values = "";
	if( (facetFieldValue !=null || facetFieldValue != "") && facetFieldValue.length > 0) {
		values = facetFieldValue[0].text;
	}
	val=facetFieldName+" ("+values+")";
	sortarray.push(val);
//	sortarray.sort();
}
	for(var s=0; s < sortarray.length; s++) {
	var sortedval=sortarray[s];
    	sortedfacet=sortedval.substring(0,sortedval.lastIndexOf("(")-1);
//	sortedfacet= sortedfacet.replace(/^\s+|\s+$/g,"");
				if(isFacetSelected(termsList,sortedfacet)){
                    if(fieldNameTemp=="Publication Date"){
                        var pubval = sortedfacet + sortedval.substring(sortedval.lastIndexOf("("), sortedval.length);
                        facetHtmlText[i++] = "<li>" + getDisplayName(pubval) + "</li>";
                    } else if (sortedfacet != null && getLanguageName(sortedfacet) != null) {
                        facetHtmlText[i++] = "<li>" + getDisplayName(sortedval) + "</li>";
                    }
				}
				else {
					if(fieldNameTemp=="Publication Date"){
						recordCount++;
						 var pubval =  sortedfacet+sortedval.substring(sortedval.lastIndexOf("("),sortedval.length);
						facetHtmlText[i++] = "<li><a href=\"#\" onClick=\"opener.location.href='discovery.do?searchType=facetSearch&facetValue="+ encodeURIComponent(sortedfacet)+"&facetFieldValue="+facetName+"';self.close()\"> "+ getDisplayName(pubval)+ "</a></li>";
					}
					else if(!(sortedfacet==null||sortedfacet == "N/A")&&!(getLanguageName(sortedfacet)==null)){
						if(fieldNameTemp=="Language"){
							recordCount++;
							var langval = getLanguageName(sortedfacet)+ sortedval.substring(sortedval.lastIndexOf("("),sortedval.length);
							facetHtmlText[i++] = "<li><a href=\"#\" onClick=\"opener.location.href='discovery.do?searchType=facetSearch&facetValue="+sortedfacet+"&facetFieldValue="+facetName+"';self.close()\"> "+ getDisplayName(langval)+ "</a></li>";
						}
						else{
							  recordCount++;
							//facetHtmlText[i++] = "<li><a href=\"#\" onClick=\"opener.location.href='discovery.do?searchType=facetSearch&facetValue="+sortedfacet+"&facetFieldValue="+facetName+"';self.close()\"> "+ sortedval + "</a></li>";
						 var sortedfacet1 = sortedfacet;
                            sortedfacet1 = sortedfacet1.replace(/\\/g, "\\\\");
                            sortedfacet1 = sortedfacet1.replace(/\'/g, "\\\'");
                            sortedfacet1 = sortedfacet1.replace(/\"/g, "\\\"");
                            var temp = "<li><a href=\"#\" onClick=\"opener.location.href='discovery.do?searchType=facetSearch&facetValue="
                                + encodeURIComponent(sortedfacet1) + "&facetFieldValue=" + facetName
                                + "';self.close()\"> " + getDisplayName(sortedval) + "</a></li>";
                            temp = temp.replace(/%5C'/g, "%5C%27");
                            facetHtmlText[i++] = temp;
						
						}
					}
				}
			facetHtmlText[i++] = "</div>";								
}

	var pageSize = <%=pageSize%>;
	if(recordCount < pageSize ){
	   document.getElementById("next").style.display='none';
	   }
    var facetOffset = <%=discoveryForm.getFacetOffset()%>
    if(facetOffset == 0){
    document.getElementById("previous").style.display='none';
   }

function getDisplayName(value){
    var dispFacet;
    var index = value.indexOf(" /r/n!@#$");
     if (index > 0) {
        dispFacet = value.substring(index + 9);
     }
     else {
        dispFacet = value.replace(" /r/n!@#$"," ");
     }
    return dispFacet;
	}

$("#searchFacetsDiv").html(facetHtmlText.join(''));


</script>

