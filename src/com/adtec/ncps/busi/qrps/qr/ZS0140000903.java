package com.adtec.ncps.busi.qrps.qr;

import org.apache.commons.lang3.StringUtils;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.BusiPub;
import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.qrps.QrBusiPub;
import com.adtec.ncps.busi.qrps.bean.QrBook;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.struct.dta.DtaInfo;
import com.union.sdk.SDKConstants;

/**
 * @ClassName: ZS0140000903
 * @Description: 查询付款状态
 * @author Q
 * @date 2018年1月4日上午11:26:41
 *
 */
public class ZS0140000903 {

    public static int deal() throws Exception {
	SysPub.appLog("INFO", "开始ZS0140000903业务处理");
	
	QrBook qrBook = new QrBook();
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();
	
	int ret = 0;

	// 先产生流水号
    BusiPub.getPlatSeq();
    int iseq_no = (Integer) EPOper.get(tpID, "INIT[0].SeqNo");
    String platDate = (String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE");
    
	String svcName = (String) EPOper.get(tpID, "OBJ_ALA_abstarct_REQ[0].svcName");
	svcName = svcName.toUpperCase();
	
	String svrReq = "OBJ_EBANK_SVR_" + svcName + "_REQ";
	String cltReq = "OBJ_QRUP_ALL";
	String svrRes = "OBJ_EBANK_SVR_" + svcName + "_RES";
	String cltRes = "OBJ_QRUP_ALL";
	
	EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);
	
	SysPub.appLog("INFO", "复制svrReq成功");
	
	// 查询付款交易
	String txnNo = (String) EPOper.get(tpID, svrReq+"[0].cd[0].txnNo");
	ret = QrBusiPub.queryQrpsBook(txnNo, "0130000903");
	if (ret >= 1) {
	    if (1 == ret) {
//		// 没有查到原交易记录， 保存交易序列号
//		EPOPER.PUT(TPID, "T_QRP_BOOK[0].TXN_NO", TXNNO);
//		SYSPUB.APPLOG("INFO", "没有查到原付款交易");
	    	
	    	
	    	EPOper.put(tpID, svrRes+"[0].cd[0].reqType", (String) EPOper.get(tpID, "T_QRP_BOOK[0].REQ_TYPE"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].issCode", (String) EPOper.get(tpID, "T_QRP_BOOK[0].ISS_CODE"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].voucherNum", (String) EPOper.get(tpID, "T_QRP_BOOK[0].VOUCHER_NUM"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].settleKey", (String) EPOper.get(tpID, "T_QRP_BOOK[0].SETTLE_KEY"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].settleDate", (String) EPOper.get(tpID, "T_QRP_BOOK[0].SETTLE_DATE"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].comInfo", (String) EPOper.get(tpID, "T_QRP_BOOK[0].COM_INFO"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].origRespCode", (String) EPOper.get(tpID, "T_QRP_BOOK[0].RESP_CODE"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].origRespMsg", (String) EPOper.get(tpID, "T_QRP_BOOK[0].RESP_MSG"));
	    	EPOper.put(tpID, svrRes+"[0].cd[0].respMsg", "成功");
	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode","0000");
	 	    EPOper.copy(tpID, tpID,svrRes, "OBJ_ALA_abstarct_RES[0].res");
	 	    
	    } else {
	    	SysPub.appLog("ERROR", "查询原付款信息失败");
	    	return -1;
	    }
	}else {
	// 组织报文发送到银联
	sendUP( tpID,  cltReq,  svrReq);
	
	ret = recvUP( tpID,  qrBook,  platDate,  iseq_no,  cltRes,  svrRes,svrReq);
	if (ret != 0) {
	    return -1;
	}

	// 根据银联返回结果处理数据
	ret = uptOrigQrBook();
	if (ret != 0) {
	    SysPub.appLog("ERROR", "修改数据库表失败");
	    return -1;
	} else {
	    SysPub.appLog("INFO", "修改原付款数据成功");
	}
	}

	// 修改原订单的信息
	ret = uptOldQrNoInfo();
	if (ret != 1) {
	    SysPub.appLog("ERROR", "修改数据库表失败");
	    return -1;
	} else {
	    SysPub.appLog("INFO", "修改原付款数据成功");
	}

	return 0;
    }

    private static int recvUP(String tpID, QrBook qrBook, String platDate, int iseq_no, String cltRes, String svrRes, String svrReq) throws Exception {
    	// 非对象类型赋值
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, iseq_no);
	    
    	
    	EPOper.delete(tpID, cltRes);
    	EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res", cltRes);
    	
    	EPOper.put(tpID, svrRes+"[0].cd[0].reqType", (String) EPOper.get(tpID, cltRes+"[0].reqType"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].issCode", (String) EPOper.get(tpID, cltRes+"[0].issCode"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].voucherNum", (String) EPOper.get(tpID, cltRes+"[0].voucherNum"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].settleKey", (String) EPOper.get(tpID, cltRes+"[0].settleKey"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].settleDate", (String) EPOper.get(tpID, cltRes+"[0].settleDate"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].comInfo", (String) EPOper.get(tpID, cltRes+"[0].comInfo"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].origRespCode", (String) EPOper.get(tpID, cltRes+"[0].origRespCode"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].origRespMsg", (String) EPOper.get(tpID, cltRes+"[0].origRespMsg"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].respMsg", (String) EPOper.get(tpID, cltRes+"[0].respMsg"));
    	EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");
    	
    	String respCode = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respCode");
    	if("00".equals(respCode)) {
 	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode","0000");
 	    }else {
 	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode", (String) EPOper.get(tpID, cltRes+"[0].respCode"));
 	    }
 	    EPOper.copy(tpID, tpID,svrRes, "OBJ_ALA_abstarct_RES[0].res");
 	    
 	    if (!"00".equals(respCode)) {
     	    SysPub.appLog("ERROR", "银联服务0140000903返回失败：%s-%s", respCode,
     		    (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respMsg"));
     	    return -1;
     	}
		return 0;
	}

	public static int uptOldQrNoInfo() throws Exception {

	// 获取数据源
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String szTpID = dtaInfo.getTpId();

	int ret = 0;

	try {
	    String szSql = "update t_qrp_book set stat = ?,voucher_num = ?, settle_key = ?, settle_date = ?, "
		    + "com_info = ?,resp_code = ?, resp_msg = ? " + "where txn_no = ? and req_type = ? ";

	    String reqType = "0120000903";
	    String txnNo = (String) EPOper.get(szTpID, "T_QRP_BOOK[0].TXN_NO");

	    // 付款成功信息
	    String voucherNum = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].voucherNum");
	    String settleKey = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].settleKey");
	    String settleDate = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].settleDate");
	    String comInfo = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].comInfo");

	    String respCode = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].origRespCode");
	    String respMsg = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].origRespMsg");

	    String stat = "000";
	    if ("00".equals(respCode)) {
		stat = "400";
	    } else {
		stat = "401";
	    }

	    Object[] value = { stat, voucherNum, settleKey, settleDate, comInfo, respCode, respMsg, txnNo, reqType };
	    ret = DataBaseUtils.execute(szSql, value);
	    if (ret == 0) {
		SysPub.appLog("ERROR", "更新原纪录失败");
	    }
	} catch (Exception e) {
	    throw e;
	}
	return ret;

    }

    public static int uptOrigQrBook() throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String szTpID = dtaInfo.getTpId();
	int iResult = 0;
	String szSql;
	Object[] value = null;
	int seqNo = 0;
	try {
	    // 更新数据库
	    String platDate = (String) EPOper.get(szTpID, "T_QRP_BOOK[0].PLAT_DATE");
	    SysPub.appLog("INFO", "platDate-%s", platDate);

	    // 返回信息
	    String voucherNum = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].voucherNum");
	    String settleDate = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].settleDate");
	    String settleKey = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].settleKey");
	    String comInfo = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].comInfo");
	    String respCode = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].origRespCode");
	    String respMsg = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].origRespMsg");

	    if (StringUtils.isEmpty(platDate)) {// 没有查到原付款记录
		szSql = "insert into t_qrp_book(plat_date, seq_no, voucher_num, settle_date , "
			+ "settle_key,com_info, resp_code, resp_msg, rmrk) values( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		// 生成新的流水
		BusiPub.getPlatSeq();
		seqNo = (Integer) EPOper.get(szTpID, "INIT[0].SeqNo");
		platDate = (String) EPOper.get(szTpID, "T_PLAT_PARA[0].PLAT_DATE");
		value = new Object[] { platDate, seqNo, voucherNum, settleDate, settleKey, comInfo, respCode, respMsg,
			"付款查询后补登记" };
	    } else {
		seqNo = (Integer) EPOper.get(szTpID, "T_QRP_BOOK[0].SEQ_NO");

		szSql = "update t_qrp_book set voucher_num =?, settle_date =?, "
			+ "settle_key=?,com_info=?, resp_code=?, resp_msg =? where plat_date = ? and seq_no = ? ";
		value = new Object[] { voucherNum, settleDate, settleKey, comInfo, respCode, respMsg, platDate, seqNo };
	    }

	    iResult = DataBaseUtils.execute(szSql, value);
	    if (iResult <= 0) {
		SysPub.appLog("ERROR", "更新t_qrp_book表失败");
	    }
	} catch (Exception e) {
	    throw e;
	}

	return 0;
    }

    /**
     * @Description: 调用银联接口 发送银联
     * @author Q
     * @param cltReq 
     * @param svrReq 
     * @return
     * @throws Exception
     * @date 2017年12月17日下午5:04:44
     */
    public static int sendUP(String tpID, String cltReq, String svrReq) throws Exception {
	SysPub.appLog("INFO", "发往银联开始");
	try {
	    /* 报文头赋值 */
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].version", SDKConstants.VERSION_1_0_0);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].signature", "0");
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].certId", "68759529225");
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].reqType", (String) EPOper.get(tpID, svrReq+"[0].cd[0].reqType"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].issCode", (String) EPOper.get(tpID, svrReq+"[0].cd[0].issCode"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].txnNo", (String) EPOper.get(tpID, svrReq+"[0].cd[0].txnNo"));

	    EPOper.copy(tpID, tpID,cltReq , "OBJ_ALA_abstarct_REQ[0].req" );
	    // 调度银联0140000903服务
	    SysPub.appLog("INFO", "调用银联0140000903服务开始");
	    DtaTool.call("QRUP_CLI", "ZS0140");

	} catch (Exception e) {
	    SysPub.appLog("ERROR", "调用银联服务0140000903失败：%s", e.getMessage());
	    throw e;
	}

	return 0;
    }

}
