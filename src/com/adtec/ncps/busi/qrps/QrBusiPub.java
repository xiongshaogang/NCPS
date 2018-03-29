package com.adtec.ncps.busi.qrps;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleBinaryOperator;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.bson.util.StringRangeSet;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.AmountUtils;
import com.adtec.ncps.busi.ncp.BusiMsgProc;
import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.FileTool;
import com.adtec.ncps.busi.ncp.PubTool;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.ncp.SysPubDef;
import com.adtec.ncps.busi.ncp.dao.BaseDao;
import com.adtec.ncps.busi.ncp.dao.BookDaoTool;
import com.adtec.ncps.busi.ncp.dao.BookExtDaoTool;
import com.adtec.ncps.busi.qrps.bean.CouponInfo;
import com.adtec.ncps.busi.qrps.bean.QrBook;
import com.adtec.ncps.busi.qrps.dao.QrBookDao;
import com.adtec.starring.datapool.CompSDO;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.global.SysDef;
import com.adtec.starring.log.DBExecuter;
import com.adtec.starring.respool.ResPool;
import com.adtec.starring.struct.dta.DtaInfo;
import com.adtec.starring.util.StringTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.union.sdk.SDKConstants;
import com.union.sdk.SDKUtil;
import com.union.sdk.SignService;

public class QrBusiPub {

	/**
	 *  将info信息转到QrBook对象中
	 * @param type
	 * @param qrBook
	 * @param svrReq
	 */
	public static void parsInfo2Obj(String type, QrBook qrBook ,String svrReq) {
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();
		
		if ("PAYERINFO".equals(type.toUpperCase())) {
		    String accNo = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].accNo");
		    String name = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].name");
		    String payerBankInfo = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].payerBankInfo");
		    String issCode = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].issCode");
		    String acctClass = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].acctClass");
		    String certifTp = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].certifTp");
		    String certifId = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].certifId");
		    String cvn2 = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].cvn2");
		    String expired = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].expired");
		    String cardAttr = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].cardAttr");
		    String mobile = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].mobile");

		    qrBook.setPayer_info_acc_no(accNo);
		    qrBook.setPayer_info_name(name);
		    qrBook.setPayer_info_payer_bank_info(payerBankInfo);
		    qrBook.setPayer_info_iss_code(issCode);
		    qrBook.setPayer_info_acct_class(acctClass);
		    qrBook.setPayer_info_certif_tp(certifTp);
		    qrBook.setPayer_info_certif_id(certifId);
		    qrBook.setPayer_info_cvn2(cvn2);
		    qrBook.setPayer_info_expired(expired);
		    qrBook.setPayer_info_card_attr(cardAttr);
		    qrBook.setPayer_info_mobile(mobile);
		    

		} else if ("COUPONINFO".equals(type.toUpperCase())) {//  优惠信息couponInfo
			String type1 = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].type");
			String spnsrId = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].spnsrId");
			String offstAmt = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].offstAmt");
			String id = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].id");
			String desc = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].desc");
			String addnInfo = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].addnInfo");
			qrBook.setCoupon_info_type(type1);
			qrBook.setCoupon_info_spnsr_id(spnsrId);
			if(offstAmt != null && !offstAmt.trim().isEmpty()) {
				qrBook.setCoupon_info_offst_amt(Double.valueOf(offstAmt));
			}
			qrBook.setCoupon_info_id(id);
			qrBook.setCoupon_info_desc(desc);
			qrBook.setCoupon_info_addninfo(addnInfo);
			
		} else if ("ADDNCOND".equals(type.toUpperCase())) {// addnCond附加处理条件ADDNCOND
		    String currency = (String) EPOper.get(tpID, svrReq+"[0].cd[0].addnCond[0].currency");
		    String pinFreeStr = (String) EPOper.get(tpID, svrReq+"[0].cd[0].addnCond[0].pinFree");
		    String maxAmontStr = (String) EPOper.get(tpID, svrReq+"[0].cd[0].addnCond[0].maxAmont");
		    qrBook.setAddn_cond_currency(currency);
		    if(pinFreeStr != null && !pinFreeStr.trim().isEmpty()) {
		    	qrBook.setAddn_cond_pinfree(Integer.valueOf(pinFreeStr));
		    }
		    if(maxAmontStr != null && !maxAmontStr.trim().isEmpty()) {
		    	qrBook.setAddn_cond_maxamont(Double.valueOf(maxAmontStr));
		    }

		} else if ("PAYEEINFO".equals(type.toUpperCase())) {// payeeInfo收款方信息PAYEEINFO
		    String name = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payeeInfo[0].name");
		    String id = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].id");
		    String merCatCode = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].merCatCode");
		    String termId = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].termId");
		    String subName = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].accNo");
		    String  accNo= (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].subName");
		    String subId = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].subId");
		    qrBook.setPayee_info_name(name);
		    qrBook.setPayee_info_id(id);
		    qrBook.setPayee_info_mercat_code(merCatCode);
		    qrBook.setPayee_info_term_id(termId);
		    qrBook.setPayee_info_sub_name(subName);
		    qrBook.setPayee_info_acc_no(accNo);
		    qrBook.setPayee_info_sub_id(subId);

		}

	}
    /**
     * @param tpID
     * @param qrBook
     * @param platDate
     * @param seqNo
     * @return
     * @throws Exception 
     * @throws NumberFormatException 
     */
    public static int qrBookData(String tpID, QrBook qrBook, String platDate, int seqNo) throws NumberFormatException, Exception {
    	//对于qrBook中String的属性，循环取OBJ_QRUP_ALL中对应值 对其 赋值
    	String[] elements = {"acqCode","addnOpUrl","backUrl","certId","comInfo","couponInfo","currencyCode","encryptCertId",
    			"invoiceSt","issCode","merCatCode","merId","merName","orderDetails","orderNo","orderTime",
    			"orderType","origOrderNo","origOrderTime","origReqType","origRespCode","origRespMsg","payeeComments",
    			"payerComments","qrType","reqReserved","riskInfo","reqType","respCode","respMsg","specFeeInfo","settleDate",
    			"settleKey","termId","txnNo","upReserved","version","voucherNum","signCheckFlag"};
    	for (int i = 0; i < elements.length; i++) {
    		String elementName = elementFormate(elements[i]);
    		String value =(String) EPOper.get(tpID, "OBJ_QRUP_ALL[0]."+elements[i]);
    		if(value != null && !value.trim().isEmpty()) {//不为空时进行赋值
    			Class clazz = Class.forName("com.adtec.ncps.busi.qrps.bean.QrBook");
//    			QrBook demo = (QrBook)clazz.newInstance();
    			Field f = clazz.getDeclaredField(elementName);
    			f.setAccessible(true);
    			f.set(qrBook,value);
    		}
		}
    	
		qrBook.setPlat_date(platDate);
		qrBook.setSeq_no(seqNo);
		String limitCount = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].limitCount");
		if (!StringUtils.isEmpty(limitCount)) {
			qrBook.setLimit_count(Long.valueOf(limitCount));
		}
		String origTxnAmt = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].origTxnAmt");
		if (!StringUtils.isEmpty(origTxnAmt)) {
		    qrBook.setOrig_txn_amt(Double.valueOf(origTxnAmt));
		}
		String paymentValidTime = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].paymentValidTime");
		if (!StringUtils.isEmpty(paymentValidTime)) {
		    qrBook.setPayment_valid_time(Long.valueOf(paymentValidTime));
		}
		String qrCode = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].qrCode");
		if (StringUtils.isEmpty(qrCode)) {
		    qrBook.setQr_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].qrNo"));
		} else {
		    qrBook.setQr_code(qrCode);
		}
		String qrValidTime = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].qrValidTime");
		if (!StringUtils.isEmpty(qrValidTime)) {
		    qrBook.setQr_valid_time(Long.valueOf(qrValidTime));
		}
		String txnAmt = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].txnAmt");
		if (!StringUtils.isEmpty(txnAmt)) {
		    qrBook.setTxn_amt(Double.valueOf(AmountUtils.changeF2Y(txnAmt)));
		}
//	qrBook.setAcq_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].acqCode"));
//	qrBook.setAddn_opurl((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].addnOpUrl"));
//	qrBook.setBack_url((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].backUrl"));
//	qrBook.setCert_id((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].certId"));
//	qrBook.setCom_info((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].comInfo"));
//	qrBook.setCoupon_info((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].couponInfo"));
//	qrBook.setCurrency_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].currencyCode"));
//	qrBook.setEncrypt_cert_id((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].encryptCertId"));
//	qrBook.setInvoice_info_id((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].invoiceInfo"));
//	qrBook.setInvoice_st((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].invoiceSt"));
//	qrBook.setIss_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].issCode"));
//	qrBook.setMercat_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].merCatCode"));
//	qrBook.setMer_id((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].merId"));
//	qrBook.setMer_name((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].merName"));
//	qrBook.setOrder_details((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].orderDetails"));
//	qrBook.setOrder_no((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].orderNo"));
//	qrBook.setOrder_time((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].orderTime"));
//	qrBook.setOrder_type((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].orderType"));
//	qrBook.setOrig_order_no((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].origOrderNo"));
//	qrBook.setOrig_order_time((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].origOrderTime"));
//	qrBook.setOrig_req_type((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].origReqType"));
//	qrBook.setOrig_resp_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].origRespCode"));
//	qrBook.setOrig_resp_msg((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].origRespMsg"));
//	qrBook.setPayee_comments((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].payeeComments"));
////	qrBook.setPayee_info_mercat_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].payeeInfo"));
//	qrBook.setPayer_comments((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].payerComments"));
////	qrBook.setPayer_info_acc_no((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].payerInfo"));
//	qrBook.setQr_type((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].qrType"));
//	qrBook.setReq_reserved((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].reqReserved"));
//	qrBook.setRisk_info((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].riskInfo"));
//	qrBook.setReq_type((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].reqType"));
//	qrBook.setResp_code((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respCode"));
//	qrBook.setResp_msg((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respMsg"));
//	qrBook.setSpec_fee_info((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].specFeeInfo"));
//	qrBook.setSettle_date((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].settleDate"));
//	qrBook.setSettle_key((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].settleKey"));
//	qrBook.setSign_chk_flag((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].signature"));
//	qrBook.setTerm_id((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].termId"));
//	qrBook.setTxn_no((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].txnNo"));
//	qrBook.setUp_reserved((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].upReserved"));
//	qrBook.setVersion((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].version"));
//	qrBook.setVoucher_num((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].voucherNum"));
//	qrBook.setSign_chk_flag((String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].signCheckFlag"));

	return 0;
    }

    /**
     * 将element格式转换为QrBook中对应的属性对应
     * @param string
     * @return
     */
    private static String elementFormate(String element) {
    	String flag = "";
    	
    	//数据库设计时，没有完全和8583报文匹配，针对特殊情况，可在goal 中配置
    	String[] goal = {"sign_chk_flag:signCheckFlag","mercat_code:merCatCode" };
    	for(int j= 0 ; j< goal.length ; j++) {
    		String[] split = goal[j].split(":");
    		if(element.equals(split[1])) {
    			return split[0];
    		}
    	} 
    	
    	for (int i = 0; i < element.length(); i++) {
    		if(element.charAt(i) >= 65 && element.charAt(i) <= 90) {//是否为大写字母
    			flag =flag + "_"+element.charAt(i);
    		}else {
    			flag =flag +element.charAt(i);
    		}
    	}
    	
		return flag.toLowerCase();
	}
	public static int initResFmt() throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String szTpID = dtaInfo.getTpId();

//	String svcName = (String) EPOper.get(szTpID, "__GDTA_FORMAT[0].__GDTA_SVCNAME[0]");
//	SysPub.appLog("INFO", "服务逻辑名称:%s", svcName);
//
//	String reqFmtObjNm = null;
//	String resFmtObjNm = null;
//	if (!"BS0210000903".equals(svcName)) {
//	    reqFmtObjNm = "OBJ_QRUP_ALL[0].";
//	    resFmtObjNm = "OBJ_QRUP_ALL[0].";
//	} else {
//	    reqFmtObjNm = "OBJ_" + svcName + "_REQ[0].";
//	    resFmtObjNm = "OBJ_" + svcName + "_RES[0].";
//	}
//
//	EPOper.put(szTpID, resFmtObjNm + "version", SDKConstants.VERSION_1_0_0);
//	EPOper.put(szTpID, resFmtObjNm + "signature", "0");
//	EPOper.copy(szTpID, szTpID, reqFmtObjNm + "certId", resFmtObjNm + "certId");
//	EPOper.copy(szTpID, szTpID, reqFmtObjNm + "reqType", resFmtObjNm + "reqType");
//	EPOper.copy(szTpID, szTpID, reqFmtObjNm + "issCode", resFmtObjNm + "issCode");
//	EPOper.put(szTpID, resFmtObjNm + "respCode", "99");
//	EPOper.put(szTpID, resFmtObjNm + "respMsg", "初始化错误信息");

	return 0;
    }

    /**
     * @Description: 将报文数据元素中的Base64编码的数据转换到对象中
     * @author Q
     * @param origData
     * @param type
     *            COUPONINFO优惠信息等
     * @return
     * @throws Exception
     * @date 2018年1月4日下午5:44:45
     */
    public static int parsBase64Json2Ele(String origData, String type, String svrReq) throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	if (origData == null || StringUtils.isBlank(origData) || type == null || StringUtils.isBlank(type)) {
	    SysPub.appLog("ERROR", "要转换的数据或类型为空");
	    return -1;
	}

	// 将base64字符串转换为原字符串
	String resultData = SignService.base64Decode(origData, SDKConstants.UTF_8_ENCODING);
	SysPub.appLog("INFO", "将要解析的字符串：%s", resultData);

	// payerInfo付款方信息PAYERINFO
	if ("COUPONINFO".equals(type.toUpperCase())) {// couponInfo优惠信息
	    // 将字符串解析到json对象中
	    JSONArray jsonArray = JSON.parseArray(resultData);
	    for (Object jsonObject : jsonArray) {
		CouponInfo couponInfo = JSONObject.parseObject(jsonObject.toString(), CouponInfo.class);

		String cType = couponInfo.getType();
		String spnsrId = couponInfo.getSpnsrId();
		String offstAmt = couponInfo.getOffstAmt();
		String id = couponInfo.getId();
		String desc = couponInfo.getDesc();
		String addnInfo = couponInfo.getAddnInfo();

		SysPub.appLog("INFO", "type[%s]spnsrId [%s]offstAmt[%s] id[%s] desc[%s] addnInfo[%s]", type, spnsrId,
			offstAmt, id, desc, addnInfo);

		if (cType != null && cType.length() > 0) {
		    EPOper.put(tpID, "COUPONINFO[0].type", cType);
		}
		if (spnsrId != null && spnsrId.length() > 0) {
		    EPOper.put(tpID, "COUPONINFO[0].spnsrId", spnsrId);
		}
		if (offstAmt != null && offstAmt.length() > 0) {
		    EPOper.put(tpID, "COUPONINFO[0].offstAmt", offstAmt);
		}
		if (id != null && id.length() > 0) {
		    EPOper.put(tpID, "COUPONINFO[0].id", id);
		}
		if (desc != null && desc.length() > 0) {
		    EPOper.put(tpID, "COUPONINFO[0].desc", desc);
		}
		if (addnInfo != null && addnInfo.length() > 0) {
		    EPOper.put(tpID, "COUPONINFO[0].addnInfo", addnInfo);
		}
	    }
	}

	return 0;
    }

    /**
     * @Description: 将报文数据元素中的Base64编码的payerInfo转换到对象PAYERINFO中
     * @author Q
     * @param eleName
     *            存放base64编码的信息
     * @param type
     *            取值有payerInfo、riskInfo、addnCond
     * @return
     * @throws Exception
     * @date 2017年12月16日下午10:05:52
     */
    public static int parsBase64Info2Ele(String origData, String type, String svrReq) throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	if (origData == null || StringUtils.isBlank(origData) || type == null || StringUtils.isBlank(type)) {
	    SysPub.appLog("ERROR", "要转换的数据或类型为空");
	    return -1;
	}

	// 将base64字符串转换为原字符串
	String resultData = SignService.base64Decode(origData, SDKConstants.UTF_8_ENCODING);
	SysPub.appLog("INFO", "将要解析的字符串：%s", resultData);
	// 将字符串解析到map中
	Map<String, String> resultMap = SDKUtil.convertResultStringToMap(resultData);
	// payerInfo付款方信息PAYERINFO
	if ("PAYERINFO".equals(type.toUpperCase())) {
	    String accNo = resultMap.get("accNo");
	    String name = resultMap.get("name");
	    String payerBankInfo = resultMap.get("payerBankInfo");
	    String issCode = resultMap.get("issCode");
	    String acctClass = resultMap.get("acctClass");
	    String certifTp = resultMap.get("certifTp");
	    String certifId = resultMap.get("certifId");
	    String cvn2 = resultMap.get("cvn2");
	    String expired = resultMap.get("expired");
	    String cardAttr = resultMap.get("cardAttr");
	    String mobile = resultMap.get("mobile");

	    if (accNo != null && accNo.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].accNo", resultMap.get("accNo"));
	    }
	    if (name != null && name.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].name", resultMap.get("name"));
	    }
	    if (payerBankInfo != null && payerBankInfo.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].payerBankInfo", resultMap.get("payerBankInfo"));
	    }
	    if (issCode != null && issCode.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].issCode", resultMap.get("issCode"));
	    }
	    if (acctClass != null && acctClass.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].acctClass", resultMap.get("acctClass"));
	    }
	    if (certifTp != null && certifTp.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].certifId", resultMap.get("certifId"));
	    }
	    if (certifId != null && certifId.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].certifTp", resultMap.get("certifTp"));
	    }
	    if (cvn2 != null && cvn2.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].cvn2", resultMap.get("cvn2"));
	    }
	    if (expired != null && expired.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].expired", resultMap.get("expired"));
	    }
	    if (cardAttr != null && cardAttr.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].cardAttr", resultMap.get("cardAttr"));
	    }
	    if (mobile != null && mobile.length() > 0) {
		EPOper.put(tpID, "PAYERINFO[0].mobile", resultMap.get("mobile"));
	    }
	} else if ("RISKINFO".equals(type.toUpperCase())) {// riskInfo风控信息RISKINFO
	    String deviceID = resultMap.get("deviceID");
	    String deviceType = resultMap.get("deviceType");
	    String mobile = resultMap.get("mobile");
	    String accountIdHash = resultMap.get("accountIdHash");
	    String sourceIP = resultMap.get("sourceIP");
	    String DeviceLocation = resultMap.get("DeviceLocation");
	    String fullDeviceNumber = resultMap.get("fullDeviceNumber");
	    String captureMethod = resultMap.get("captureMethod");
	    String deviceSimNumber = resultMap.get("deviceSimNumber");
	    String deviceLanguage = resultMap.get("deviceLanguage");
	    String deviceName = resultMap.get("deviceName");
	    String usrRgstrDt = resultMap.get("usrRgstrDt");
	    String accountEmailLife = resultMap.get("accountEmailLife");
	    String cardHolderName = resultMap.get("cardHolderName");
	    String billingAddress = resultMap.get("billingAddress");
	    String billingZip = resultMap.get("billingZip");
	    String riskScore = resultMap.get("riskScore");
	    String riskStandardVersion = resultMap.get("riskStandardVersion");
	    String deviceScore = resultMap.get("deviceScore");
	    String accountScore = resultMap.get("accountScore");
	    String phoneNumberScore = resultMap.get("phoneNumberScore");
	    String riskReasonCode = resultMap.get("riskReasonCode");
	    String applyChannel = resultMap.get("applyChannel");
	    
	    SysPub.appLog("INFO", "deviceID：%s", deviceID);
	    SysPub.appLog("INFO", "deviceType：%s", deviceType);
	    SysPub.appLog("INFO", "mobile：%s", mobile);
	    SysPub.appLog("INFO", "accountIdHash：%s", accountIdHash);
	    SysPub.appLog("INFO", "sourceIP：%s", sourceIP);
	    SysPub.appLog("INFO", "DeviceLocation：%s", DeviceLocation);
	    SysPub.appLog("INFO", "fullDeviceNumber：%s", fullDeviceNumber);
	    SysPub.appLog("INFO", "captureMethod：%s", captureMethod);
	    SysPub.appLog("INFO", "deviceSimNumber：%s", deviceSimNumber);
	    SysPub.appLog("INFO", "deviceLanguage：%s", deviceLanguage);
	    SysPub.appLog("INFO", "deviceName：%s", deviceName);
	    SysPub.appLog("INFO", "usrRgstrDt：%s", usrRgstrDt);
	    SysPub.appLog("INFO", "accountEmailLife：%s", accountEmailLife);
	    SysPub.appLog("INFO", "cardHolderName：%s", cardHolderName);
	    SysPub.appLog("INFO", "billingAddress：%s", billingAddress);
	    SysPub.appLog("INFO", "billingZip：%s", billingZip);
	    SysPub.appLog("INFO", "riskScore：%s", riskScore);
	    SysPub.appLog("INFO", "riskStandardVersion：%s", riskStandardVersion);
	    SysPub.appLog("INFO", "deviceScore：%s", deviceScore);
	    SysPub.appLog("INFO", "accountScore：%s", accountScore);
	    SysPub.appLog("INFO", "phoneNumberScore：%s", phoneNumberScore);
	    SysPub.appLog("INFO", "riskReasonCode：%s", riskReasonCode);
	    SysPub.appLog("INFO", "applyChannel：%s", applyChannel);

	    if (deviceID != null && deviceID.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].deviceID", deviceID);
	    }
	    if (deviceType != null && deviceType.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].deviceType", deviceType);
	    }
	    if (mobile != null && mobile.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].mobile", mobile);
	    }
	    if (accountIdHash != null && accountIdHash.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].accountIdHash", accountIdHash);
	    }
	    if (sourceIP != null && sourceIP.length() > 4) {
		EPOper.put(tpID, "RISKINFO[0].sourceIP", sourceIP);
	    }
	    if (DeviceLocation != null && DeviceLocation.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].DeviceLocation", DeviceLocation);
	    }
	    if (fullDeviceNumber != null && fullDeviceNumber.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].fullDeviceNumber", fullDeviceNumber);
	    }
	    if (captureMethod != null && captureMethod.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].captureMethod", captureMethod);
	    }
	    if (deviceSimNumber != null && deviceSimNumber.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].deviceSimNumber", deviceSimNumber);
	    }
	    if (deviceLanguage != null && deviceLanguage.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].deviceLanguage", deviceLanguage);
	    }
	    if (deviceName != null && deviceName.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].deviceName", deviceName);
	    }
	    if (usrRgstrDt != null && usrRgstrDt.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].usrRgstrDt", usrRgstrDt);
	    }
	    if (accountEmailLife != null && accountEmailLife.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].accountEmailLife", accountEmailLife);
	    }
	    if (cardHolderName != null && cardHolderName.length() > 0) {
		EPOper.put(tpID,"RISKINFO[0].cardHolderName", cardHolderName);
	    }
	    if (billingAddress != null && billingAddress.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].billingAddress", billingAddress);
	    }
	    if (billingZip != null && billingZip.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].billingZip", billingZip);
	    }
	    if (riskScore != null && riskScore.length() > 0) {
		EPOper.put(tpID,"RISKINFO[0].riskScore", riskScore);
	    }
	    if (riskStandardVersion != null && riskStandardVersion.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].riskStandardVersion", riskStandardVersion);
	    }
	    if (deviceScore != null && deviceScore.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].deviceScore", deviceScore);
	    }
	    if (accountScore != null && accountScore.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].accountScore", accountScore);
	    }
	    if (phoneNumberScore != null && phoneNumberScore.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].phoneNumberScore", phoneNumberScore);
	    }
	    if (riskReasonCode != null && riskReasonCode.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0].riskReasonCode", riskReasonCode);
	    }
	    if (applyChannel != null && applyChannel.length() > 0) {
		EPOper.put(tpID, "RISKINFO[0][0].applyChannel", applyChannel);
	    }

	} else if ("ADDNCOND".equals(type.toUpperCase())) {// addnCond附加处理条件ADDNCOND
	    String currency = resultMap.get("currency");
	    String pinFreeStr = resultMap.get("pinFree");
	    int pinFree = Integer.parseInt(pinFreeStr);
	    String maxAmontStr = resultMap.get("maxAmont");
	    int maxAmont = Integer.parseInt(maxAmontStr);
	    SysPub.appLog("INFO", "%s, %s, %s", currency, pinFreeStr, maxAmontStr);
	    if (currency != null && currency.length() > 0) {
		EPOper.put(tpID, svrReq+"ADDNCOND[0].currency", currency);
	    }
	    if (pinFree > 0) {
		SysPub.appLog("INFO", "pinFree");
		EPOper.put(tpID, svrReq+"ADDNCOND[0].pinFree", pinFreeStr);
	    }
	    if (maxAmont > 0) {
		SysPub.appLog("INFO", "maxAmontStr");
		EPOper.put(tpID, svrReq+"ADDNCOND.maxAmont", maxAmontStr);
	    }
	    SysPub.appLog("INFO", "%s, %d, %d", currency, pinFree, maxAmont);

	} else if ("TRANSADDNINFO".equals(type.toUpperCase())) {// TRANSADDNINFO附加交易信息
	    String merId = resultMap.get("merId");
	    String merName = resultMap.get("merName");
	    String acqCode = resultMap.get("acqCode");
	    if (merId != null && merId.length() > 0) {
		EPOper.put(tpID, "TRANSADDNINFO[0].merId", merId);
	    }
	    if (acqCode != null && acqCode.length() > 0) {
		EPOper.put(tpID, "TRANSADDNINFO[0].acqCode", acqCode);
	    }
	    if (merName != null && merName.length() > 0) {
		EPOper.put(tpID, "TRANSADDNINFO[0].merName", merName);
	    }
	    SysPub.appLog("INFO", "%s, %s, %s", merId, merName, acqCode);
	} else if ("PAYEEINFO".equals(type.toUpperCase())) {// PAYEEINFO收款方信息
	    String merCatCode = resultMap.get("merCatCode");
	    String id = resultMap.get("id");
	    String name = resultMap.get("name");
	    String termId = resultMap.get("termId");
	    String accNo = resultMap.get("accNo");
	    String subId = resultMap.get("subId");
	    String subName = resultMap.get("subName");
	    if (merCatCode != null && merCatCode.length() > 0) {
		EPOper.put(tpID, "PAYEEINFO[0].merCatCode", merCatCode);
	    }
	    if (id != null && id.length() > 0) {
		EPOper.put(tpID, "PAYEEINFO[0].id", id);
	    }
	    if (name != null && name.length() > 0) {
		EPOper.put(tpID, "PAYEEINFO[0].name", name);
	    }
	    if (termId != null && termId.length() > 0) {
		EPOper.put(tpID, "PAYEEINFO[0].termId", termId);
	    }
	    if (accNo != null && accNo.length() > 0) {
		EPOper.put(tpID, "PAYEEINFO[0].accNo", accNo);
	    }
	    if (subId != null && subId.length() > 0) {
		EPOper.put(tpID, "PAYEEINFO[0].subId", subId);
	    }
	    if (subName != null && subName.length() > 0) {
		EPOper.put(tpID, "PAYEEINFO[0].subName", subName);
	    }
	} else if ("COUPONINFO".equals(type.toUpperCase())) {// couponInfo优惠信息
	    String cType = resultMap.get("type");
	    String spnsrId = resultMap.get("spnsrId");
	    String offstAmt = resultMap.get("offstAmt");
	    String id = resultMap.get("id");
	    String desc = resultMap.get("desc");
	    String addnInfo = resultMap.get("addnInfo");

	    if (cType != null && cType.length() > 0) {
		EPOper.put(tpID, "COUPONINFO[0].type", cType);
	    }
	    if (spnsrId != null && spnsrId.length() > 0) {
		EPOper.put(tpID, "COUPONINFO[0].spnsrId", spnsrId);
	    }
	    if (offstAmt != null && offstAmt.length() > 0) {
		EPOper.put(tpID, "COUPONINFO[0].offstAmt", offstAmt);
	    }
	    if (id != null && id.length() > 0) {
		EPOper.put(tpID, "COUPONINFO[0].id", id);
	    }
	    if (desc != null && desc.length() > 0) {
		EPOper.put(tpID, "COUPONINFO[0].desc", desc);
	    }
	    if (addnInfo != null && addnInfo.length() > 0) {
		EPOper.put(tpID, "COUPONINFO[0].addnInfo", addnInfo);
	   }
	 }else if ("INVOICEINFO".equals(type.toUpperCase())) {// invoiceinfo发票信息
		    String id = resultMap.get("id");
		    String amount = resultMap.get("amount");
		    

		    if (id  != null && id .length() > 0) {
			EPOper.put(tpID, "INVOICEINFO[0].id", id);
		    }
		    if (amount != null && amount.length() > 0) {
			EPOper.put(tpID, "INVOICEINFO[0].amount", amount);
			
		    }
	 }
		    
	return 0;
		    
    }

    /**
     * @Description: 数据对象的内容转换到key1=value1&key2=value2...base64字符串
     * @author Q
     * @param type
     * @param urlEncode
     *            是否要url编码
     * @return
     * @throws Exception
     * @date 2017年12月17日下午8:57:07
     */
    public static String parsEle2Base64Info(String type, boolean urlEncode, String svrReq) throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	if (type == null || StringUtils.isBlank(type)) {
	    SysPub.appLog("ERROR", "要转换的数据或类型为空");
	    return null;
	}

	Map<String, String> dataMap = new HashMap<String, String>();
	// 将字符串解析到map中
	// Map<String, String> resultMap =
	// SDKUtil.convertResultStringToMap(resultData);
	// payerInfo付款方信息PAYERINFO
	if ("PAYERINFO".equals(type.toUpperCase())) {
	    String accNo = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].accNo");
	    String name = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].name");
	    String payerBankInfo = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].payerBankInfo");
	    String issCode = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].issCode");
	    String acctClass = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].acctClass");
	    String certifTp = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].certifTp");
	    String certifId = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].certifId");
	    String cvn2 = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].cvn2");
	    String expired = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].expired");
	    String cardAttr = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].cardAttr");
	    String mobile = (String) EPOper.get(tpID, svrReq + "[0].cd[0].payerInfo[0].mobile");

	    if (accNo == null || accNo.trim().length() == 0) {
		SysPub.appLog("ERROR", "付款人账号不能为空");
	    }
	    dataMap.put("accNo", accNo);

	    if (name != null && name.length() > 0) {
		dataMap.put("name", name);
	    }
	    if (payerBankInfo != null && payerBankInfo.length() > 0) {
		dataMap.put("payerBankInfo", payerBankInfo);
	    }
	    if (issCode != null && issCode.length() > 0) {
		dataMap.put("issCode", issCode);
	    }
	    if (acctClass != null && acctClass.length() > 0) {
		dataMap.put("acctClass", acctClass);
	    }
	    if (certifTp != null && certifTp.length() > 0) {
		dataMap.put("certifTp", certifTp);
	    }
	    if (certifId != null && certifId.length() > 0) {
		dataMap.put("certifId", certifId);
	    }
	    if (cvn2 != null && cvn2.length() > 0) {
		dataMap.put("cvn2", cvn2);
	    }
	    if (expired != null && expired.length() > 0) {
		dataMap.put("expired", expired);
	    }
	    if (cardAttr != null && cardAttr.length() > 0) {
		dataMap.put("cardAttr", cardAttr);
	    }
	    if (mobile != null && mobile.length() > 0) {
		dataMap.put("mobile", mobile);
	    }
	} else if ("COUPONINFO".equals(type.toUpperCase())) {//  优惠信息couponInfo
//		CompSDO.getCompSDO( (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo")).toJSON();
		String type1 = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].type");
		String spnsrId = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].spnsrId");
		String offstAmt = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].offstAmt");
		String id = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].id");
		String desc = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].desc");
		String addnInfo = (String) EPOper.get(tpID, svrReq+"[0].cd[0].couponInfo[0].addnInfo");
		
		if (type1 != null && type1.length() > 0) {
			dataMap.put("type", type1);
		}
		if (spnsrId != null && spnsrId.length() > 0) {
			dataMap.put("spnsrId", spnsrId);
		}
		if (offstAmt != null && offstAmt.length() > 0) {
			dataMap.put("offstAmt", AmountUtils.changeY2F(offstAmt));
		}
		if (id != null && id.length() > 0) {
			dataMap.put("id", id);
		}
		if (desc != null && desc.length() > 0) {
			dataMap.put("desc", desc);
		}
		if (addnInfo != null && addnInfo.length() > 0) {
			dataMap.put("addnInfo", addnInfo);
		}
	} else if ("RISKINFO".equals(type.toUpperCase())) {// riskInfo风控信息RISKINFO
	    String deviceID = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceID");
	    String deviceType = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceType");
	    String mobile = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].mobile");
	    String accountIdHash = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].accountIdHash");
	    String sourceIP = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].sourceIP");
	    String DeviceLocation = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].DeviceLocation");
	    String fullDeviceNumber = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].fullDeviceNumber");
	    String captureMethod = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].captureMethod");
	    String deviceSimNumber = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceSimNumber");
	    String deviceLanguage = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceLanguage");
	    String deviceName = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceName");
	    String usrRgstrDt = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].usrRgstrDt");
	    String accountEmailLife = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].accountEmailLife");
	    String cardHolderName = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].cardHolderName");
	    String billingAddress = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].billingAddress");
	    String billingZip = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].billingZip");
	    String riskScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].riskScore");
	    String riskStandardVersion = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].riskStandardVersion");
	    String deviceScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceScore");
	    String accountScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].accountScore");
	    String phoneNumberScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].phoneNumberScore");
	    String riskReasonCode = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].riskReasonCode");
	    String applyChannel = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].applyChannel");

	    if (deviceID != null && deviceID.length() > 0) {
		dataMap.put("deviceID", deviceID);
	    }
	    if (deviceType != null && deviceType.length() > 0) {
		dataMap.put("deviceType", deviceType);
	    }
	    if (mobile != null && mobile.length() > 0) {
		dataMap.put("mobile", mobile);
	    }
	    if (accountIdHash != null && accountIdHash.length() > 0) {
		dataMap.put("accountIdHash", accountIdHash);
	    }
	    if (sourceIP != null && sourceIP.length() > 4) {
		dataMap.put("sourceIP", sourceIP);
	    }
	    if (DeviceLocation != null && DeviceLocation.length() > 0) {
		dataMap.put("DeviceLocation", DeviceLocation);
	    }
	    if (fullDeviceNumber != null && fullDeviceNumber.length() > 0) {
		dataMap.put("fullDeviceNumber", fullDeviceNumber);
	    }
	    if (captureMethod != null && captureMethod.length() > 0) {
		dataMap.put("captureMethod", captureMethod);
	    }
	    if (deviceSimNumber != null && deviceSimNumber.length() > 0) {
		dataMap.put("deviceSimNumber", deviceSimNumber);
	    }
	    if (deviceLanguage != null && deviceLanguage.length() > 0) {
		dataMap.put("deviceLanguage", deviceLanguage);
	    }
	    if (deviceName != null && deviceName.length() > 0) {
		dataMap.put("deviceName", deviceName);
	    }
	    if (usrRgstrDt != null && usrRgstrDt.length() > 0) {
		dataMap.put("usrRgstrDt", usrRgstrDt);
	    }
	    if (accountEmailLife != null && accountEmailLife.length() > 0) {
		dataMap.put("accountEmailLife", accountEmailLife);
	    }
	    if (cardHolderName != null && cardHolderName.length() > 0) {
		dataMap.put("cardHolderName", cardHolderName);
	    }
	    if (billingAddress != null && billingAddress.length() > 0) {
		dataMap.put("billingAddress", billingAddress);
	    }
	    if (billingZip != null && billingZip.length() > 0) {
		dataMap.put("billingZip", billingZip);
	    }
	    if (riskScore != null && riskScore.length() > 0) {
		dataMap.put("riskScore", riskScore);
	    }
	    if (riskStandardVersion != null && riskStandardVersion.length() > 0) {
		dataMap.put("riskStandardVersion", riskStandardVersion);
	    }
	    if (deviceScore != null && deviceScore.length() > 0) {
		dataMap.put("deviceScore", deviceScore);
	    }
	    if (accountScore != null && accountScore.length() > 0) {
		dataMap.put("accountScore", accountScore);
	    }
	    if (phoneNumberScore != null && phoneNumberScore.length() > 0) {
		dataMap.put("phoneNumberScore", phoneNumberScore);
	    }
	    if (riskReasonCode != null && riskReasonCode.length() > 0) {
		dataMap.put("riskReasonCode", riskReasonCode);
	    }
	    if (applyChannel != null && applyChannel.length() > 0) {
		dataMap.put("riskReasonCode", riskReasonCode);
	    }

	} else if ("ADDNCOND".equals(type.toUpperCase())) {// addnCond附加处理条件ADDNCOND
	    String currency = (String) EPOper.get(tpID, svrReq+"[0].cd[0].addnCond[0].currency");
	    String pinFreeStr = (String) EPOper.get(tpID, svrReq+"[0].cd[0].addnCond[0].pinFree");
	    int pinFree = Integer.parseInt(pinFreeStr);
	    String maxAmontStr = (String) EPOper.get(tpID, svrReq+"[0].cd[0].addnCond[0].maxAmont");
	    int maxAmont = Integer.parseInt(maxAmontStr);

	    if (currency != null && currency.length() > 0) {
		dataMap.put("currency", currency);
	    }
	    if (pinFree > 0) {
		dataMap.put("pinFree", pinFreeStr);
	    }
	    if (maxAmont > 0) {
		dataMap.put("maxAmont", maxAmontStr);
	    }
	} else if ("PAYEEINFO".equals(type.toUpperCase())) {// payeeInfo收款方信息PAYEEINFO
	    String name = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payeeInfo[0].name");
	    String id = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].id");
	    String merCatCode = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].merCatCode");
	    String termId = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].termId");
	    String  accNo = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].accNo");
	    String  subName = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].subName");
	    String subId = (String) EPOper.get(tpID,  svrReq+"[0].cd[0].payeeInfo[0].subId");

	    if (name != null && name.length() > 0) {
		dataMap.put("name", name);
	    }
	    if (id != null && id.length() > 0) {
		dataMap.put("id", id);
	    }
	    if (merCatCode != null && merCatCode.length() > 0) {
		dataMap.put("merCatCode", merCatCode);
	    }
	    if (termId != null && termId.length() > 0) {
		dataMap.put("termId", termId);
	    }
	    if (subName != null && subName.length() > 0) {
		dataMap.put("subName", subName);
	    }
	    if (subId != null && subId.length() > 0) {
		dataMap.put("subId", subId);
	    }
	    if (accNo != null && accNo.length() > 0) {
		dataMap.put("accNo", accNo);
	    }
	}

	// 将map数据转换为base64字符串
	if (dataMap.isEmpty()) {
	    return null;
	}
	String resultData = SDKUtil.coverMap2String(dataMap);
	
	SysPub.appLog("INFO", resultData);
//	if("COUPONINFO".equals(type.toUpperCase())) {
//	base64Data = SignService.base64Encode(resultData, SDKConstants.UTF_8_ENCODING);
//} else {
	// 将字符串增加{}
	resultData = SDKConstants.LEFT_BRACE + resultData + SDKConstants.RIGHT_BRACE;
	SysPub.appLog("INFO", resultData);
	String base64Data = SignService.base64Encode(resultData, SDKConstants.UTF_8_ENCODING);
	SysPub.appLog("INFO", base64Data);
	if (urlEncode) {
	    base64Data = URLEncoder.encode(base64Data, SDKConstants.UTF_8_ENCODING);
//	    SysPub.appLog("INFO", base64Data);
	}

	return base64Data;
    }

    /**
     * @Description: 组织银联通讯确认报文
     * @author Q
     * @return
     * @date 2018年1月2日上午11:01:51
     */
    public static int putPubRet() {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	EPOper.put(tpID, "OBJ_QRUP_ALL[0].respCode", "00");
	EPOper.put(tpID, "OBJ_QRUP_ALL[0].respMsg", "交易成功");

	return 0;
    }

    /**
     * @Description: 组织银联通讯确认报文
     * @author Q
     * @return
     * @date 2018年1月2日上午11:01:51
     */
    public static int putPubRet(String respCode, String respMsg,String svrRes) {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	if (StringUtils.isEmpty(respCode)) {
	    respCode = "01";
	    respMsg = "交易失败";
	}

	EPOper.put(tpID, svrRes+"[0].cd[0].respCode", respCode);
	EPOper.put(tpID, svrRes+"[0].cd[0].respMsg", respMsg);
	EPOper.copy(tpID, tpID,svrRes, "OBJ_ALA_abstarct_RES[0].res");

	return 0;
    }

    /*
     * @author
     * 
     * @createAt 2017年8月8日
     * 
     * @version
     */
    public static int getPlatSeq() {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();
	int iSeqNo = PubTool.sys_get_seq();
	EPOper.put(tpID, "INIT[0].SeqNo", iSeqNo);
	return 0;
    }


    /**
     * @Description: 根据交易类型获取ala服务逻辑的编码
     * @author Q
     * @param _reqType
     * @return
     * @throws Exception
     * @date 2017年12月19日下午4:55:43
     */
    public static String getAlaTxCode(String _reqType) throws Exception {

	if (_reqType == null || "".equals(_reqType)) {
	    SysPub.appLog("ERROR", "银联交易类型不能为空");
	    return null;
	}
	String szTxCOde = "";

	if ("01".equals(_reqType.substring(0, 2))) {// 主扫类atm和c2c转账都归结为主扫类
	    szTxCOde = "ZS" + _reqType;
	} else if ("02".equals(_reqType.substring(0, 1))) {// 被扫类
	    szTxCOde = "BS" + _reqType;
	} else {
	    SysPub.appLog("ERROR", "不支持的交易类型：%s", _reqType);
	    throw new Exception("交易类型[" + _reqType + "]错误，请查看原因");
	}
	SysPub.appLog("DEBUG", "银联交易类型[%s]行内交易码[%s]", _reqType, szTxCOde);
	return szTxCOde;
    }

    /**
     * @Description: 获取路由表达式
     * @author Q
     * @return
     * @throws Exception
     * @date 2017年12月19日下午4:54:37
     */
    public static String getLogicSvcName() throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String svcName = dtaInfo.getSvcName();
	String router = getAlaTxCode(svcName);
	SysPub.appLog("DEBUG", "svcName:%s--router:%s", svcName, router);
	return router;
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
	// TODO Auto-generated method stub

	return;
    }


    /**
     * 获取国密用户标识
     */
    public static String getURL() {
    	DtaInfo dtaInfo = DtaInfo.getInstance();
    	String tpID = dtaInfo.getTpId();
    	String svcName = dtaInfo.getSvcName();
    	String szURL = "";
    	
    	if("AT0190".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "BS0210".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "BS0240".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "ZS0120".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "ZS0130".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "ZS0140".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "ZS0160".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "ZS0180".equals(svcName))
    		szURL = "/qrc/api/issBackTransReq.do";
    	else if( "ZS0510".equals(svcName))
    		szURL = "/qrc/api/merBackTransReq.do";
    	else if( "ZS0540".equals(svcName))
    		szURL = "/qrc/api/merBackTransReq.do";
    	else if( "ZS0570".equals(svcName))
    		szURL = "/qrc/api/merBackTransReq.do";
    	else if( "ZS0150".equals(svcName))
    		szURL = "/qrc/api/merBackTransReq.do";
    	else if( "ZS0170".equals(svcName))
    		szURL = "/qrc/api/merBackTransReq.do";
	
	return szURL;
    }





    /****** new ************/
    /**
     * @Description: 按照接口要求组装付款方信息,并转换成base64格式
     * @author Q
     * @return 用{}连接并base64后的付款方信息
     * @throws Exception
     * @date 2017年12月14日下午3:32:58
     */
    public static String getPayerInfo(String svrReq) throws Exception {

	Map<String, String> payerInfoMap = new HashMap<String, String>();

	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	String accNo = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].accNo");
	String name = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].name");
	String payerBankInfo = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].payerBankInfo");
	String issCode = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].issCode");
	String acctClass = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].acctClass");
	String certifTp = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].certifTp");
	String certifId = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].certifId");
	String cvn2 = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].cvn2");
	String expired = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].expired");
	String cardAttr = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].cardAttr");
	String mobile = (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerInfo[0].mobile");

	if (accNo == null || accNo.trim().isEmpty()) {
	    SysPub.appLog("ERROR", "付款账号必须上送");
	    return null;
	}
	payerInfoMap.put("accNo", accNo);

	if (name != null && !name.trim().isEmpty()) {
	    payerInfoMap.put("name", name);
	}
	if (payerBankInfo != null && !payerBankInfo.trim().isEmpty()) {
	    payerInfoMap.put("payerBankInfo", payerBankInfo);
	}
	if (issCode != null && !issCode.trim().isEmpty()) {
	    payerInfoMap.put("issCode", issCode);
	}
	if (acctClass != null && !acctClass.trim().isEmpty()) {
	    payerInfoMap.put("acctClass", acctClass);
	}
	if (certifTp != null && !certifTp.trim().isEmpty()) {
	    payerInfoMap.put("certifTp", certifTp);
	}
	if (certifId != null && !certifId.trim().isEmpty()) {
	    payerInfoMap.put("certifId", certifId);
	}
	if (cvn2 != null && !cvn2.trim().isEmpty()) {
	    payerInfoMap.put("cvn2", cvn2);
	}
	if (expired != null && !expired.trim().isEmpty()) {
	    payerInfoMap.put("expired", expired);
	}
	if (cardAttr != null && !cardAttr.trim().isEmpty()) {
	    payerInfoMap.put("cardAttr", cardAttr);
	}
	if (mobile != null && !mobile.trim().isEmpty()) {
	    payerInfoMap.put("mobile", mobile);
	}
	return SignService.formInfoBase64(payerInfoMap, SDKConstants.UTF_8_ENCODING);
    }

    /**
     * @Description: 按照接口要求组装风控信息,并转换成base64格式
     * @author Q
     * @param encoding
     *            编码方式 默认utf-8
     * @return 用{}连接并base64后的付款方信息
     * @throws Exception
     * @date 2017年12月14日下午3:37:12
     */
    public static String getRiskInfo(String encoding, String svrReq) throws Exception {

	Map<String, String> riskInfoMap = new HashMap<String, String>();

	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	int suffixNo = EPOper.getSuffixNo(tpID, "RISKINFO");
	if (suffixNo < 1) {
	    return null;
	}

	String deviceID = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceID");
	String deviceType = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceType");
	String mobile = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].mobile");
	String accountIdHash = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].accountIdHash");
	String sourceIP = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].sourceIP");
	String DeviceLocation = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].DeviceLocation");
	String fullDeviceNumber = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].fullDeviceNumber");
	String captureMethod = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].captureMethod");
	String deviceSimNumber = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceSimNumber");
	String deviceLanguage = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceLanguage");
	String deviceName = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceName");
	String usrRgstrDt = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].usrRgstrDt");
	String accountEmailLife = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].accountEmailLife");
	String cardHolderName = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].cardHolderName");
	String billingAddress = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].billingAddress");
	String billingZip = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].billingZip");
	String riskScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].riskScore");
	String riskStandardVersion = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].riskStandardVersion");
	String deviceScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].deviceScore");
	String accountScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].accountScore");
	String phoneNumberScore = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].phoneNumberScore");
	String riskReasonCode = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].riskReasonCode");
	String applyChannel = (String) EPOper.get(tpID, svrReq+"[0].cd[0].riskInfo[0].applyChannel");

	if (deviceID == null || deviceID.trim().isEmpty()) {
	    SysPub.appLog("ERROR", "设备标识必须上送");
	    return null;
	}
	riskInfoMap.put("deviceID", deviceID);

	if (deviceType == null || deviceType.trim().isEmpty()) {
	    SysPub.appLog("ERROR", "设备类型必须上送");
	    return null;
	}
	riskInfoMap.put("deviceType", deviceType);

	if (mobile == null || mobile.trim().isEmpty()) {
	    SysPub.appLog("ERROR", "银行预留手机号必须上送");
	    return null;
	}
	riskInfoMap.put("mobile", mobile);

	if (accountIdHash == null || accountIdHash.trim().isEmpty()) {
	    SysPub.appLog("ERROR", "应用提供方账户ID必须上送");
	    return null;
	}
	riskInfoMap.put("accountIdHash", accountIdHash);

	if ((sourceIP == null || sourceIP.trim().isEmpty())
		&& (DeviceLocation == null || DeviceLocation.trim().isEmpty())
		&& (fullDeviceNumber == null || fullDeviceNumber.trim().isEmpty())) {
	    SysPub.appLog("ERROR", "IP、设备GPS 位置、设备 SIM 卡号码，这三要素至少上送一个");
	    return null;
	}
	if (sourceIP != null && sourceIP.trim().isEmpty()) {
	    riskInfoMap.put("sourceIP", sourceIP);
	}
	if (DeviceLocation != null && DeviceLocation.trim().isEmpty()) {
	    riskInfoMap.put("DeviceLocation", DeviceLocation);
	}
	if (fullDeviceNumber != null && fullDeviceNumber.trim().isEmpty()) {
	    riskInfoMap.put("fullDeviceNumber", fullDeviceNumber);
	}

	/* 以下都是可选要素 */
	if (captureMethod != null && captureMethod.trim().isEmpty()) {
	    riskInfoMap.put("captureMethod", captureMethod);
	}
	if (deviceSimNumber != null && deviceSimNumber.trim().isEmpty()) {
	    riskInfoMap.put("deviceSimNumber", deviceSimNumber);
	}
	if (deviceLanguage != null && deviceLanguage.trim().isEmpty()) {
	    riskInfoMap.put("deviceLanguage", deviceLanguage);
	}
	if (deviceName != null && deviceName.trim().isEmpty()) {
	    riskInfoMap.put("deviceName", deviceName);
	}
	if (usrRgstrDt != null && usrRgstrDt.trim().isEmpty()) {
	    riskInfoMap.put("usrRgstrDt", usrRgstrDt);
	}
	if (accountEmailLife != null && accountEmailLife.trim().isEmpty()) {
	    riskInfoMap.put("accountEmailLife", accountEmailLife);
	}
	if (cardHolderName != null && cardHolderName.trim().isEmpty()) {
	    riskInfoMap.put("cardHolderName", cardHolderName);
	}
	if (billingAddress != null && billingAddress.trim().isEmpty()) {
	    riskInfoMap.put("billingAddress", billingAddress);
	}
	if (billingZip != null && billingZip.trim().isEmpty()) {
	    riskInfoMap.put("billingZip", billingZip);
	}
	if (riskScore != null && riskScore.trim().isEmpty()) {
	    riskInfoMap.put("riskScore", riskScore);
	}
	if (riskStandardVersion != null && riskStandardVersion.trim().isEmpty()) {
	    riskInfoMap.put("riskStandardVersion", riskStandardVersion);
	}
	if (deviceScore != null && deviceScore.trim().isEmpty()) {
	    riskInfoMap.put("deviceScore", deviceScore);
	}
	if (accountScore != null && accountScore.trim().isEmpty()) {
	    riskInfoMap.put("accountScore", accountScore);
	}
	if (phoneNumberScore != null && phoneNumberScore.trim().isEmpty()) {
	    riskInfoMap.put("phoneNumberScore", phoneNumberScore);
	}
	if (riskReasonCode != null && riskReasonCode.trim().isEmpty()) {
	    riskInfoMap.put("riskReasonCode", riskReasonCode);
	}
	if (applyChannel != null && applyChannel.trim().isEmpty()) {
	    riskInfoMap.put("applyChannel", applyChannel);
	}

	return SignService.formInfoBase64(riskInfoMap, SDKConstants.UTF_8_ENCODING);
    }

    /**
     * @Description: 修改t_qrps_book的状态
     * @author Q
     * @param stat
     *            状态
     * @return
     * @throws Exception
     * @date 2018年1月2日上午11:31:57
     */
    public static int uptQrpsBookStat(String platDate, int seqNo, String stat) throws Exception {

	if (StringUtils.isEmpty(platDate) || seqNo <= 0 || StringUtils.isEmpty(stat)) {
	    SysPub.appLog("ERROR", "平台日期[%s]平台流水[%d]状态[%s]都不能为空", platDate, seqNo, stat);
	    return 0;
	}

	int ret = 0;

	try {
	    String szSql = "update t_qrp_book set stat = ? where plat_date = ? and seq_no = ? ";
	    SysPub.appLog("INFO", "平台日期[%s]平台流水[%d]状态[%s]", platDate, seqNo, stat);
	    if (StringUtils.isEmpty(stat)) {
		stat = "200";
	    }
	    Object[] value = { stat, platDate, seqNo };
	    ret = DataBaseUtils.execute(szSql, value);
	    if (ret == 0) {
		SysPub.appLog("ERROR", "更新原纪录失败");
	    }
	} catch (Exception e) {
	    throw e;
	}
	return ret;

    }
    
	public static int uptBookErrRet(String retCode, String retMsg) throws Exception {

		// 获取数据源
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String szTpID = dtaInfo.getTpId();

		int ret = 0;

		try {
			String szSql = "update t_qrp_book set resp_code = ?, resp_msg= ?  where plat_date = ? and seq_no = ? ";
			String platDate = (String) EPOper.get(szTpID, "T_PLAT_PARA[0].PLAT_DATE");
			int seqNo = (Integer) EPOper.get(szTpID, "INIT[0].SeqNo");
			                                          
			SysPub.appLog("INFO", "平台日期[%s]平台流水[%d]", platDate, seqNo);
			Object[] value = { retCode, retMsg, platDate, seqNo };
			ret = DataBaseUtils.execute(szSql, value);
			if (ret == 0) {
				SysPub.appLog("ERROR", "更新原纪录失败");
			}
		} catch (Exception e) {
			SysPub.appLog("INFO", "平台日期[%s]平台流水[%s]"+e.getMessage(), retCode, retMsg);
			throw e;
		}
		return ret;

	}

    /**
     * @Description: 根据日期二维码和请求类型查询信息
     * @author Q
     * @param platDate
     * @param qrCode
     * @param reqType
     * @return
     * @throws Exception
     * @date 2018年1月3日下午5:17:40
     */
    public static int queryQrpsBook(String platDate, String qrCode, String reqType) throws Exception {

	int ret = 0;

	if (StringUtils.isEmpty(platDate) || StringUtils.isEmpty(qrCode) || StringUtils.isEmpty(reqType)) {
	    SysPub.appLog("ERROR", "平台日期[%s]二维码[%s]报文类型[%s]都不能为空", platDate, qrCode, reqType);
	    return 0;
	}

	try {
	    String szSql = "select * from t_qrp_book where plat_date = ? and qr_code = ? and req_type = ? ";
	    SysPub.appLog("INFO", "平台日期[%s]二维码[%s]报文类型[%s]", platDate, qrCode, reqType);
	    Object[] value = { platDate, qrCode, reqType };
	    ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
	    if (ret == 0) {
		SysPub.appLog("INFO", "没有找到记录");
	    }
	} catch (Exception e) {
	    throw e;
	}
	return ret;

    }
    /**
     * @Description: 根据日期二维码和请求类型查询信息
     * @author Q
     * @param platDate
     * @param qrCode
     * @param reqType
     * @return
     * @throws Exception
     * @date 2018年1月3日下午5:17:40
     */
    public static int queryQrpsBookByQrCode( String qrCode, String reqType) throws Exception {
    	
    	int ret = 0;
    	
    	if ( StringUtils.isEmpty(qrCode) || StringUtils.isEmpty(reqType)) {
    		SysPub.appLog("ERROR", "二维码[%s]报文类型[%s]都不能为空", qrCode, reqType);
    		return 0;
    	}
    	
    	try {
    		String szSql = "select * from t_qrp_book where  qr_code = ? and req_type = ? ";
    		SysPub.appLog("INFO", "二维码[%s]报文类型[%s]", qrCode, reqType);
    		Object[] value = {  qrCode, reqType };
    		ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
    		if (ret == 0) {
    			SysPub.appLog("INFO", "没有找到记录");
    		}
    	} catch (Exception e) {
    		throw e;
    	}
    	return ret;
    	
    }

    /**
     * @Description: 交易序列号查询
     * @author Q
     * @param txnNo
     * @param reqType
     * @return
     * @throws Exception
     * @date 2018年1月4日上午9:13:23
     */
    public static int queryQrpsBook(String txnNo, String reqType) throws Exception {

	int ret = 0;

	if (StringUtils.isEmpty(txnNo) || StringUtils.isEmpty(reqType)) {
	    SysPub.appLog("ERROR", "交易序列号[%s]报文类型[%s]都不能为空", txnNo, reqType);
	    return 0;
	}

	try {
	    String szSql = "select * from t_qrp_book where txn_no = ? and req_type = ? ";
	    SysPub.appLog("INFO", "交易序列号[%s]报文类型[%s]", txnNo, reqType);
	    Object[] value = { txnNo, reqType };
	    ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
	    if (ret == 0) {
		SysPub.appLog("INFO", "没有找到记录");
	    }
	} catch (Exception e) {
	    throw e;
	}
	return ret;

    }

    /**
     * 根据订单编号和交易类型查询流水
     * 
     * **/
    public static int queryBookbyOrderno(String  orderNo,String orderTime,String reqType) throws Exception {
    	int ret = 0;
    	if (StringUtils.isEmpty(orderNo)|| StringUtils.isEmpty(orderTime) || StringUtils.isEmpty(reqType)) {
    	    SysPub.appLog("ERROR", "订单编号[%s]订单时间[%s]报文类型[%s]都不能为空", orderNo,orderTime, reqType);
    	    return 0;
    	}
    	try {
    	    String szSql = "select * from t_qrp_book where order_no = ? and order_time = ? and req_type = ? ";
    	    SysPub.appLog("INFO", "订单编号[%s]订单时间[%s]报文类型[%s]", orderNo,orderTime, reqType);
    	    Object[] value = { orderNo,orderTime, reqType };
    	    ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
    	    if (ret == 0) {
    		SysPub.appLog("INFO", "没有找到记录");
    	    }
    	} catch (Exception e) {
    	    throw e;
    	}
    	
		return ret;
    	
    }
    
    /**
     * 根据凭证号查询流水
     * 
     * **/
    public static int queryBookbyvoucherNum(String  voucherNum,String reqType) throws Exception {
    	int ret = 0;
    	if (StringUtils.isEmpty(voucherNum)|| StringUtils.isEmpty(reqType)) {
    	    SysPub.appLog("ERROR", "凭证号[%s]报文类型[%s]都不能为空", voucherNum, reqType);
    	    return 0;
    	}
    	try {
    	    String szSql = "select * from t_qrp_book where VOUCHER_NUM = ?  and req_type = ? ";
    	    SysPub.appLog("INFO", "凭证号[%s]报文类型[%s]", voucherNum, reqType);
    	    Object[] value = { voucherNum, reqType };
    	    ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
    	    if (ret == 0) {
    		SysPub.appLog("INFO", "没有找到记录");
    	    }
    	} catch (Exception e) {
    	    throw e;
    	}
    	
		return ret;
    	
    }

    /**
     * @Description: 通过付款凭证号查询
     * @author Q
     * @param txnNo
     * @param reqType
     * @return
     * @throws Exception
     * @date 2018年1月4日上午9:13:23
     */
    public static int queryQrpsBookByVoucherNum(String voucherNum, String reqType) throws Exception {
    	
    	int ret = 0;
    	
    	if (StringUtils.isEmpty(voucherNum) || StringUtils.isEmpty(reqType)) {
    		SysPub.appLog("ERROR", "付款凭证号[%s]报文类型[%s]都不能为空", voucherNum, reqType);
    		return 0;
    	}
    	
    	try {
    		String szSql = "select * from t_qrp_book where voucher_num = ? and req_type = ? ";
    		SysPub.appLog("INFO", "付款凭证号[%s]报文类型[%s]", voucherNum, reqType);
    		Object[] value = { voucherNum, reqType };
    		ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
    		if (ret == 0) {
    			SysPub.appLog("INFO", "没有找到记录");
    		}
    	} catch (Exception e) {
    		throw e;
    	}
    	return ret;
    	
    }

    public static int queryQrpsBookByOrderInfo(String origOrderNo,String origOrderTime, String reqType) throws Exception {
    	
    	int ret = 0;
    	
    	if (StringUtils.isEmpty(origOrderTime) ||StringUtils.isEmpty(origOrderNo) || StringUtils.isEmpty(reqType)) {
    		SysPub.appLog("ERROR", "订单号[%s]订单时间[%s]报文类型[%s]都不能为空", origOrderNo,origOrderTime, reqType);
    		return 0;
    	}
    	
    	try {
    		String szSql = "select * from t_qrp_book where orig_order_no = ? and orig_order_time = ? and req_type = ? ";
    		SysPub.appLog("INFO", "订单号[%s]订单时间[%s]报文类型[%s]都不能为空", origOrderNo,origOrderTime, reqType);
    		Object[] value = { origOrderNo,origOrderTime, reqType };
    		ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
    		if (ret == 0) {
    			SysPub.appLog("INFO", "没有找到记录");
    		}
    	} catch (Exception e) {
    		throw e;
    	}
    	return ret;
    	
    }

    /**
     * @Description: 将app请求的信息转换到数据对象中
     * @author Q
     * @return
     * @throws Exception
     * @date 2017年12月16日下午9:57:52
     */
    public static int parsDataToEle(String svrReq) throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String epID = dtaInfo.getTpId();

	try {

	    // payerInfo付款方信息PAYERINFO
	    String payerInfo = (String) EPOper.get(epID, "OBJ_QRUP_ALL[0].payerInfo");
	    SysPub.appLog("INFO", "payerInfo:%s", payerInfo);
	    if (payerInfo != null && payerInfo.length() > 0) {
		QrBusiPub.parsBase64Info2Ele(payerInfo, "payerInfo", svrReq);
	    }
	    // riskInfo风控信息RISKINFO
	    String riskInfo = (String) EPOper.get(epID, "OBJ_QRUP_ALL[0].riskInfo");
	    if (riskInfo != null && riskInfo.length() > 0) {
		QrBusiPub.parsBase64Info2Ele(riskInfo, "riskInfo", svrReq);
	    }
	    // addnCond附加处理条件ADDNCOND
	    String addnCond = (String) EPOper.get(epID, "OBJ_QRUP_ALL[0].addnCond");
	    if (addnCond != null && addnCond.length() > 0) {
		QrBusiPub.parsBase64Info2Ele(addnCond, "addnCond", svrReq);
	    }
	    // PAYEEINFO收款方信息PAYEEINFO
	    String payeeInfo = (String) EPOper.get(epID, "OBJ_QRUP_ALL[0].payeeInfo");
	    if (payeeInfo != null && payeeInfo.length() > 0) {
		QrBusiPub.parsBase64Info2Ele(payeeInfo, "payeeInfo", svrReq);
	    }
	    // couponInfo优惠信息COUPONINFO
	    // 优惠信息是json格式的字符串，要特殊处理
	    String couponInfo = (String) EPOper.get(epID, "OBJ_QRUP_ALL[0].couponInfo");
	    if (couponInfo != null && couponInfo.length() > 0) {
		QrBusiPub.parsBase64Json2Ele(couponInfo, "couponInfo", svrReq);
	    }
	} catch (Exception e) {
	    EPOper.put(epID, "INIT._FUNC_RETURN", 0, "-1");
	    SysPub.appLog("ERROR", "解析base64字符串到数据对象失败！-%s", e.getMessage());
	    throw e;
	}

	EPOper.put(epID, "INIT._FUNC_RETURN", 0, "0");

	return 0;
    }

    /**
     * 提前结束时，为 respCode respMsg 赋值
     * @param svrRes
     * @param respCode
     * @param respMsg
     * @param qrBook
     * @throws Exception
     */
    public static void evaluateCodeAMsg(String svrRes, String respCode, String respMsg, QrBook qrBook) throws Exception {
	    	DtaInfo dtaInfo = DtaInfo.getInstance();
	    	String tpID = dtaInfo.getTpId();
			SysPub.appLog("ERROR", respMsg);
		    EPOper.put(tpID, svrRes+"[0].cd[0].respCode",respCode);
			EPOper.put(tpID, svrRes+"[0].cd[0].respMsg",respMsg);
			qrBook.setResp_code(respCode);
			qrBook.setResp_msg(respMsg);
	}
    
    /**
  	 *	更新t_qrp_book reqType=0120000903	 交易对应记录信息
  	 * @param stat
  	 * @param respCodeEle
  	 * @param respMsgEle
  	 * @return
  	 * @throws Exception
  	 */
  	public static int uptOldQrNoInfo(String stat,String respCodeEle, String respMsgEle) throws Exception {
  		SysPub.appLog("INFO", "STEP==>>	uptOldQrNoInfo()：[%s]","begin");
  	// 获取数据源
  	DtaInfo dtaInfo = DtaInfo.getInstance();
  	String szTpID = dtaInfo.getTpId();

  	int ret = 0;

  	try {
  	    String szSql = "update t_qrp_book set stat = ? where plat_date = ? and seq_no = ? ";

  	    String platDate = (String) EPOper.get(szTpID, "T_QRP_BOOK[0].PLAT_DATE");
  	    int seqNo = Integer.valueOf(String.valueOf( EPOper.get(szTpID, "T_QRP_BOOK[0].SEQ_NO")));
  	    SysPub.appLog("INFO", "状态[%s]平台日期[%s]平台流水[%d]", stat,platDate, seqNo);
  	    if( stat == null || stat.trim().isEmpty()) {
  	    		return 0;
  	    }
  	    Object[] value = { stat,  platDate, seqNo };
  	    ret = DataBaseUtils.execute(szSql, value);
  	    if (ret == 0) {
	  	    	EPOper.put(szTpID, respCodeEle,"01");
	  	    	EPOper.put(szTpID, respMsgEle,"更新原纪录失败");
	  	    	SysPub.appLog("ERROR", "更新原纪录失败");
	  	    	return -1;
  	    }
  	  SysPub.appLog("INFO", "更新数据，返回:%d", ret);
  	} catch (Exception e) {
  		EPOper.put(szTpID, respCodeEle,"01");
  		SysPub.appLog("ERROR", "更新t_qrp_book表失败:%s", e.getMessage());
  		e.printStackTrace();
      	EPOper.put(szTpID, respMsgEle,"更新数据失败");
      	throw e;
//  	    return -1;
  	}
  	return ret;

      }
  	 /**
     * 组织数据登记到book表
     * @param qrBook
     * @param platDate
     * @param iseq_no
     * @return
     * @throws Exception
     */ 
    public static int insQrBook(QrBook qrBook, String platDate, int iseq_no ,String respCodeEle, String respMsgEle) throws Exception {
	SysPub.appLog("INFO", "开始登记簿数据");
	int iResult = 0;
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	try {
	    // 非对象类型赋值
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, iseq_no);

	    // 登记交易序列号
	    String txnNo = (String) EPOper.get(tpID, "T_QRP_BOOK[0].TXN_NO");
	    qrBook.setTxn_no(txnNo);
	    if( queryBookByUI_T_QRP_BOOK1(qrBook, respCodeEle, respCodeEle) == -1) {
	    	return -1;
	    } else if( queryBookByUI_T_QRP_BOOK1(qrBook, respCodeEle, respCodeEle) == 0) {
	    	iResult = QrBookDao.insert(qrBook);
	    } else {
			iResult = uptBookByUI_T_QRP_BOOK1(qrBook, respCodeEle, respMsgEle);
		}
	    if (iResult <= 0) {
	    	EPOper.put(tpID, respCodeEle,"01");
	    	EPOper.put(tpID, respMsgEle,"数据记录失败");
	    	SysPub.appLog("ERROR", "插入t_qrp_book表失败");
	    }
	} catch (Exception e) {
			EPOper.put(tpID, respCodeEle,"01");
	    	EPOper.put(tpID, respMsgEle,"录入数据失败");
		    SysPub.appLog("ERROR", "插入t_qrp_book表失败");
		    e.printStackTrace();
		    SysPub.appLog("ERROR", "插入t_qrp_book表失败:%s", e.getMessage());
//	    return -1;
	    throw e;
	}

	SysPub.appLog("INFO", "插入数据，返回:%d", iResult);
	return iResult;
    }
    
	/**
	 * 通过唯一索引 UI_T_QRP_BOOK1 查询
	 * @param qrBook
	 * @param respCodeEle 
	 * @param respMsgEle 
	 * @return
	 * @throws Exception 
	 */
	private static int queryBookByUI_T_QRP_BOOK1(QrBook qrBook, String respCodeEle, String respMsgEle) throws Exception {

		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();
		
		int ret = -1;

		try {
			String szSql1 = "select * from t_qrp_book where  ";
			String szSql = "";
			String order_no = qrBook.getOrder_no();
			String order_time = qrBook.getOrder_time();
			String txn_no = qrBook.getTxn_no();
			String voucher_num = qrBook.getVoucher_num();
			String req_type = qrBook.getReq_type();
			String value = "";
			
			if(order_no != null && !order_no.trim().isEmpty()) {
				szSql += "and  order_no = ? ";
				value += (order_no+",");
			}
			if(order_time != null && !order_time.trim().isEmpty()) {
				szSql += "and   order_time = ? ";
				value += (order_time+",");
			}
			if(txn_no != null && !txn_no.trim().isEmpty()) {
				szSql += "and   txn_no = ? ";
				value += (txn_no+",");
			}
			if(voucher_num != null && !voucher_num.trim().isEmpty()) {
				szSql += "and   voucher_num = ? ";
				value += (voucher_num+",");
			}
			if(req_type != null && !req_type.trim().isEmpty()) {
				szSql += "and   req_type = ? ";
				value += (req_type+",");
			}
			if (!szSql.trim().isEmpty()) {
				value = value.substring(0, value.length()-1);
				szSql = szSql.substring(4,szSql.length());
			}
			szSql1 = szSql1 + szSql ;
			SysPub.appLog("INFO", "order_no[%s] order_time[%s] txn_no[%s] voucher_num[%s] req_type[%s]",
					order_no, order_time ,txn_no ,voucher_num ,req_type);
			if (value.equals("") ) {
				EPOper.put(tpID, respCodeEle,"01");
				EPOper.put(tpID, respMsgEle,"查询条件不能为空");
		    	SysPub.appLog("ERROR", "查询条件不能为空");
				return -1;
			}
			SysPub.appLog("INFO", "szSql1:[%s]",szSql1);
			SysPub.appLog("INFO", "value:[%s]",value);
			
		    Object[] value1 = value.split(",");
		    ret = DataBaseUtils.queryToCount(szSql1,  value1);
		    if (ret == 0) {
		    	SysPub.appLog("INFO", "没有找到记录");
		    }
		} catch (Exception e) {
				e.printStackTrace();
				throw e;
		}
		return ret;

		
	}
	
	/**
	 * 通过 唯一索引UI_T_QRP_BOOK1 更新 数据
	 * @param qrBook
	 * @param respCodeEle
	 * @param respMsgEle
	 * @return
	 * @throws Exception 
	 */
	private static int uptBookByUI_T_QRP_BOOK1(QrBook qrBook, String respCodeEle, String respMsgEle) throws Exception {
		SysPub.appLog("INFO", "uptBookByUI_T_QRP_BOOK1[begin]");
		// 获取数据源
	  	DtaInfo dtaInfo = DtaInfo.getInstance();
	  	String szTpID = dtaInfo.getTpId();

	  	int ret = -1;

	  	try {
			String order_no = qrBook.getOrder_no();
			String order_time = qrBook.getOrder_time();
			String txn_no = qrBook.getTxn_no();
			String voucher_num = qrBook.getVoucher_num();
			String req_type = qrBook.getReq_type();
			String value = "";
			if(order_no != null && !order_no.trim().isEmpty()) {
				value += ("order_no"+",");
			}
			if(order_time != null && !order_time.trim().isEmpty()) {
				value += ("order_time"+",");
			}
			if(txn_no != null && !txn_no.trim().isEmpty()) {
				value += ("txn_no"+",");
			}
			if(voucher_num != null && !voucher_num.trim().isEmpty()) {
				value += ("voucher_num"+",");
			}
			if(req_type != null && !req_type.trim().isEmpty()) {
				value += ("req_type"+",");
			}
			SysPub.appLog("INFO", "order_no[%s] order_time[%s] txn_no[%s] voucher_num[%s] req_type[%s]",
					order_no, order_time ,txn_no ,voucher_num ,req_type);
			if (!value.trim().isEmpty()) {
				value = value.substring(0, value.length()-1);
			}
			SysPub.appLog("INFO", "value:[%s]", value);
			
			ret = BaseDao.updateObject(qrBook, "t_qrp_book", value);
	  	  SysPub.appLog("INFO", "更新数据，返回:%d", ret);
	  	} catch (Exception e) {
	  		EPOper.put(szTpID, respCodeEle,"01");
	  		e.printStackTrace();
	      	EPOper.put(szTpID, respMsgEle,"更新数据失败");
	      	throw e;
//	  	    return -1;
	  	}
	  	return ret;
	}

}
