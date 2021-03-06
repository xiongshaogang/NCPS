package com.adtec.ncps.busi.qrps.qr;

import org.apache.commons.lang3.StringUtils;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.AmountUtils;
import com.adtec.ncps.busi.ncp.BusiPub;
import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.qrps.QrBusiPub;
import com.adtec.ncps.busi.qrps.bean.QrBook;
import com.adtec.ncps.busi.qrps.dao.QrBookDao;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.struct.dta.DtaInfo;
import com.union.sdk.SDKConstants;

/**
 * @ClassName: ZS0140000903
 * @Description: 退款
 * @author Q
 * @date 2018年1月4日上午11:26:41
 *
 */
public class ZS0150000903 {

	/**
	 * 用于判读流程是否中断
	 * 	查询
	 * 	发送报文
	 * 	接受报文
	 * 以上任何流程中断，isGoing 则为false，不在进行后面的流程，直接进入 流水插入 流水更新
	 */
	public static boolean isGoing = true;
	
	/**
	 * svr端发起
	 *  ZS0151交易
	 * @return
	 * @throws Exception 
	 */
	public static int deal_svr() throws Exception {
		SysPub.appLog("INFO", "开始ZS0150000903业务处理【ZS0151交易】");
		
		QrBook qrBook = new QrBook();
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();
		
		int ret = 0;

		// 先产生流水号
	    BusiPub.getPlatSeq();
	    int iseq_no = (Integer) EPOper.get(tpID, "INIT[0].SeqNo");
	    String platDate = (String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE");
	    
//		String svcName = (String) EPOper.get(tpID, "OBJ_ALA_abstarct_REQ[0].svcName");
//		svcName = svcName.toUpperCase();
		
		String  svrReq = "OBJ_QRUP_ALL";
		String  svrRes = "OBJ_QRUP_ALL";
		
		EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);
		
		// 查询付款交易
//		String voucherNum = (String) EPOper.get(tpID, svrReq+".voucherNum");
//		ret = QrBusiPub.queryQrpsBookByVoucherNum(voucherNum, "0130000903");
//		if (ret != 1) {
//			    if (0 == ret) {
//				// 没有查到原交易记录， 保存交易序列号
//				EPOper.put(tpID, "T_QRP_BOOK[0].VOUCHER_NUM", voucherNum);
//				SysPub.appLog("INFO", "没有查到原付款交易");
//			    } else {
//				SysPub.appLog("ERROR", "查询原付款信息失败");
//			    }	
//		    return -1;
//		}
		
		// 非对象类型赋值
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, iseq_no);
	    
    	
    	EPOper.delete(tpID, svrRes);
    	
    	EPOper.put(tpID, svrRes+"[0].version", SDKConstants.VERSION_1_0_0);
    	EPOper.put(tpID, svrRes+"[0].signature", "0");
    	EPOper.put(tpID, svrRes+"[0].certId", "68759529225");
    	EPOper.put(tpID, svrRes+"[0].reqType", "0150000903");
    	EPOper.put(tpID, svrRes+"[0].respCode", "00");
    	EPOper.put(tpID, svrRes+"[0].respMsg", "成功[0000000]");
    	
//    	插入登记簿
    	qrBook.setRmrk("trans_code:ZS0151");//标明 此为银联返回报文
    	QrBusiPub.insQrBook( qrBook,  platDate,  iseq_no,svrRes+"[0].respCode", svrRes+"[0].respMsg");
		
    	EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
		return 0;
	}
	
	/**
	 * client端发起
	 *  ZS0150交易
	 * @return
	 */
    public static int deal() throws Exception {
	SysPub.appLog("INFO", "开始ZS0150000903业务处理");
	
	QrBook qrBook = new QrBook();
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();
	isGoing = true;
	
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
	
	SysPub.appLog("INFO", "复制svrReq成功[%s]",svrReq);
	
	int flag = 0;
	if(isGoing && qryOrderInfo(svrReq, svrRes, qrBook) != 0) {
		flag = -1;
	}
	
	if(isGoing && sendUP( tpID,  cltReq,  svrReq, flag ,svrRes ,qrBook) != 0) {
		isGoing = false ;
	}
	
	if(isGoing &&  recvUP( tpID,  qrBook,  platDate,  iseq_no,  cltRes,  svrRes,svrReq) != 0) {
		isGoing = false ;
	}

	QrBusiPub.insQrBook( qrBook,  platDate,  iseq_no,svrRes+"[0].cd[0].respCode", svrRes+"[0].cd[0].respMsg");
	
	QrBusiPub.uptOldQrNoInfo(qrBook.getStat(),svrRes+"[0].cd[0].respCode", svrRes+"[0].cd[0].respMsg");
	
	EPOper.copy(tpID, tpID,svrRes, "OBJ_ALA_abstarct_RES[0].res");
//	// 根据银联返回结果处理数据
//	ret = uptOrigQrBook();
//	if (ret != 0) {
//	    SysPub.appLog("ERROR", "修改数据库表失败");
//	    return -1;
//	} else {
//	    SysPub.appLog("INFO", "修改原付款数据成功");
//	}
//
//	// 修改原订单的信息
//	ret = uptOldQrNoInfo();
//	if (ret != 1) {
//	    SysPub.appLog("ERROR", "修改数据库表失败");
//	    return -1;
//	} else {
//	    SysPub.appLog("INFO", "修改原付款数据成功");
//	}

	return 0;
    }

    private static int recvUP(String tpID, QrBook qrBook, String platDate, int iseq_no, String cltRes, String svrRes, String svrReq) throws Exception {
    	// 非对象类型赋值
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, iseq_no);
	    
    	
    	EPOper.delete(tpID, cltRes);
    	EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res", cltRes);
    	SysPub.appLog("INFO", "svrRes[%s]",svrRes);
    	SysPub.appLog("INFO", "cltRes[%s]",cltRes);
    	
    	EPOper.put(tpID, svrRes+"[0].cd[0].reqType", (String) EPOper.get(tpID, cltRes+"[0].reqType"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].acqCode", (String) EPOper.get(tpID, cltRes+"[0].acqCode"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].settleKey", (String) EPOper.get(tpID, cltRes+"[0].settleKey"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].settleDate", (String) EPOper.get(tpID, cltRes+"[0].settleDate"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].comInfo", (String) EPOper.get(tpID, cltRes+"[0].comInfo"));
    	EPOper.put(tpID, svrRes+"[0].cd[0].respMsg", (String) EPOper.get(tpID, cltRes+"[0].respMsg"));
    	EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");
    	
    	String respCode = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respCode");
    	if("00".equals(respCode)) {
 	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode","0000");
 	    }else {
 	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode", (String) EPOper.get(tpID, cltRes+"[0].respCode"));
 	    }
// 	    EPOper.copy(tpID, tpID,svrRes, "OBJ_ALA_abstarct_RES[0].res");
 	    
 	    if (!"00".equals(respCode)) {
     	    SysPub.appLog("ERROR", "银联服务0150000903返回失败：%s-%s", respCode,
     		    (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respMsg"));
     	}else {
     		qrBook.setStat("403");
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
     * @param flag 
     * @param svrRes 
     * @param qrBook 
     * @return
     * @throws Exception
     * @date 2017年12月17日下午5:04:44
     */
    public static int sendUP(String tpID, String cltReq, String svrReq, int flag, String svrRes, QrBook qrBook) throws Exception {
	SysPub.appLog("INFO", "发往银联开始");
	try {
	    /* 报文头赋值 */
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].version", SDKConstants.VERSION_1_0_0);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].signature", "0");
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].certId", "68759529225");
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].reqType", (String) EPOper.get(tpID, svrReq+"[0].cd[0].reqType"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].voucherNum", (String) EPOper.get(tpID, svrReq+"[0].cd[0].voucherNum"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].origOrderNo", (String) EPOper.get(tpID, svrReq+"[0].cd[0].origOrderNo"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].origOrderTime", (String) EPOper.get(tpID, svrReq+"[0].cd[0].origOrderTime"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].orderNo", (String) EPOper.get(tpID, svrReq+"[0].cd[0].orderNo"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].orderTime", (String) EPOper.get(tpID, svrReq+"[0].cd[0].orderTime"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].currencyCode", (String) EPOper.get(tpID, svrReq+"[0].cd[0].currencyCode"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].txnAmt", AmountUtils.changeY2F((String) EPOper.get(tpID, svrReq+"[0].cd[0].txnAmt")));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].acqCode", (String) EPOper.get(tpID, svrReq+"[0].cd[0].acqCode"));

	    if(flag != 0 ) {
		    	SysPub.appLog("INFO", "查记录失败，sendUP中结束");
		    	return -1;
	    }
	    EPOper.copy(tpID, tpID,cltReq , "OBJ_ALA_abstarct_REQ[0].req" );
	    // 调度银联0150000903服务
	    SysPub.appLog("INFO", "调用银联0150000903服务开始");
	    DtaTool.call("QRUP_CLI", "ZS0150");

	} catch (Exception e) {
	    SysPub.appLog("ERROR", "调用银联服务0150000903失败：%s", e.getMessage());
	    e.printStackTrace();
	    QrBusiPub.evaluateCodeAMsg(svrRes, "01", "调用银联服务0140000903失败", qrBook);
	    return -1;
	}

	return 0;
    }
    
    public static int qryOrderInfo(String svrReq, String svrRes, QrBook qrBook) throws Exception {
    		SysPub.appLog("INFO", "STEP==>>	qryOrderInfo()：[%s]","begin");
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();
	
		// 查询付款交易
		String voucherNum = (String) EPOper.get(tpID, svrReq+"[0].cd[0].voucherNum");
		String origOrderNo = (String) EPOper.get(tpID, svrReq+"[0].cd[0].origOrderNo");
		String origOrderTime = (String) EPOper.get(tpID, svrReq+"[0].cd[0].origOrderTime");
		int ret ;
		if (voucherNum != null && !voucherNum.trim().isEmpty()) {
			ret = QrBusiPub.queryQrpsBookByVoucherNum(voucherNum, "0130000903");
		}else {
			ret = QrBusiPub.queryQrpsBookByOrderInfo(origOrderNo,origOrderTime, "0130000903");
		}
		if (ret == 1) {
			String txnNo = (String) EPOper.get(tpID, "T_QRP_BOOK[0].TXN_NO");
			SysPub.appLog("INFO", "txnNo：[%s]",txnNo);
			EPOper.delete(tpID, "T_QRP_BOOK");
			ret = QrBusiPub.queryQrpsBook(txnNo, "0120000903");
		}
		if (ret != 1) {
//			int ret2 = QrBusiPub.queryQrpsBookByVoucherNum(voucherNum, "0140000903");
				QrBusiPub.evaluateCodeAMsg(svrRes, "01", "无对应订单信息", qrBook);
				return -1;
		}
		
		String stat = (String) EPOper.get(tpID, "T_QRP_BOOK[0].STAT");
		if("400".equals(stat) || "401".equals(stat) || "403".equals(stat) || "404".equals(stat) || "406".equals(stat)){//已付款
			QrBusiPub.evaluateCodeAMsg(svrRes, "01", "订单状态为【"+stat+"】，不能发起退款", qrBook);
			return -1;
		}
	
		return 0;
    }

  
}
