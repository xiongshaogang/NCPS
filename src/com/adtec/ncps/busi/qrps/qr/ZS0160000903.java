package com.adtec.ncps.busi.qrps.qr;

import com.adtec.ncps.busi.ncp.BusiPub;
import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.qrps.QrBusiPub;
import com.adtec.ncps.busi.qrps.bean.QrBook;
import com.adtec.ncps.busi.qrps.dao.QrBookDao;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.struct.dta.DtaInfo;
import com.sun.xml.internal.bind.v2.TODO;
import com.union.sdk.SDKConstants;

/**
 * @ClassName: ZS0160000903
 * @Description: 被通知的交易的类型，包括付款交易0130000903、退款交易0150000903
 * @author Q
 * @date 2018年1月4日上午11:28:10
 *
 */
public class ZS0160000903 {

    public static int deal() throws Exception {
	SysPub.appLog("INFO", "开始ZS0160000903业务处理");

	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();
	QrBook qrBook = new QrBook();
	int ret = 0;

	// 先产生流水号
    BusiPub.getPlatSeq();
    int iseq_no = (Integer) EPOper.get(tpID, "INIT[0].SeqNo");
    String platDate = (String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE");
    
	String  svrReq = "OBJ_QRUP_ALL";
	String  svrRes = "OBJ_QRUP_ALL";
	
	EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);
	String respCode = "00";
	String respMsg = "成功[0000000]"	;
	
	// 查询原交易数据到数据元素T_QRP_BOOK
	String origReqType = (String) EPOper.get(tpID, svrReq+"[0].origReqType");
	String txnNo = (String) EPOper.get(tpID, svrReq+"[0].txnNo");
	SysPub.appLog("INFO", "原交易类型[%s]交易序列号[%s]", origReqType, txnNo);
	ret = QrBusiPub.queryQrpsBook(txnNo, "0120000903");
	if (ret != 1) {
		if(ret == 0) {
			respCode = "01";
			respMsg = "原订单信息未找到";
		}else {
			respCode = "01";
			respMsg = "原订单信息查询失败";
		}
	}
	
	String origReqType1 = (String) EPOper.get(tpID, svrReq+"[0].origReqType");
	String origRespCode = (String) EPOper.get(tpID, svrReq+"[0].origRespCode");
	// 非对象类型赋值
    QrBusiPub.qrBookData(tpID, qrBook, platDate, iseq_no);
    
	EPOper.delete(tpID, svrRes);
	EPOper.put(tpID, svrRes+"[0].version", SDKConstants.VERSION_1_0_0);
	EPOper.put(tpID, svrRes+"[0].signature", "0");
	EPOper.put(tpID, svrRes+"[0].certId", "68759529225");
	EPOper.put(tpID, svrRes+"[0].reqType", "0160000903");
	EPOper.put(tpID, svrRes+"[0].respCode", respCode);
	EPOper.put(tpID, svrRes+"[0].respMsg", respMsg);
	
	if ( "0130000903".equals(origReqType1) ){//付款
		if("00".equals(origRespCode)) {
			qrBook.setStat("402");//付款成功
		}else {
			qrBook.setStat("401");//付款失败
		}
	} else if ( "0150000903".equals(origReqType1) ){//退款
		if("00".equals(origRespCode)) {
			qrBook.setStat("404");//成功
		}else {
			qrBook.setStat("405");//失败
		}
	}
	
	QrBusiPub.insQrBook( qrBook,  platDate,  iseq_no,svrRes+"[0].respCode", svrRes+"[0].respMsg");
	
	if("00".equals((String)EPOper.get(tpID, svrRes+"[0].respCode"))) {
		
		QrBusiPub.uptOldQrNoInfo(qrBook.getStat(),svrRes+"[0].respCode", svrRes+"[0].respMsg");
	}
	
	
	EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
	
	
//	// 组织数据登记到book表
//	ret = insQrBook( qrBook,  tpID,  platDate,  iseq_no);
//	if (ret != 1) {
//	    SysPub.appLog("ERROR", "插入数据库表失败");
//	} else {
//	    SysPub.appLog("INFO", "插入数据库成功");
//	}

//	uptOldQrNoInfo();

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
	    String txnNo = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].txnNo");

	    // 付款成功信息
	    String voucherNum = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].voucherNum");
	    String settleKey = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].settleKey");
	    String settleDate = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].settleDate");
	    String comInfo = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].comInfo");

	    String respCode = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].origRespCode");
	    String respMsg = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].origRespMsg");

	    String origReqType = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].origReqType");

	    // 原交易要素判断
	    String stat = null;
	    if ("0130000903".equals(origReqType))// 付款交易0130000903
		stat = "402";
	    else if ("0150000903".equals(origReqType))// 退款交易0150000903
		stat = "403";

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

    public static int origBookDeal() throws Exception {
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	int ret = 0;
	String platDate = (String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE");
	String origReqType = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].origReqType");
	SysPub.appLog("INFO", "原交易类型[%s]", origReqType);

	// 原交易要素判断 TODO

	String stat = null;
	if ("0130000903".equals(origReqType))// 付款交易0130000903
	    stat = "402";
	else if ("0150000903".equals(origReqType))// 退款交易0150000903
	    stat = "403";

	int seqNo = (Integer) EPOper.get(tpID, "T_QRP_BOOK[0].SEQ_NO");
	ret = QrBusiPub.uptQrpsBookStat(platDate, seqNo, stat);
	if (ret != 1) {
	    SysPub.appLog("ERROR", "修改原交易状态失败");
	}
	return 0;
    }

    public static int insQrBook(QrBook qrBook, String tpID, String platDate, int seqNo) throws Exception {
	SysPub.appLog("INFO", "开始ZS0160000903登记簿数据");
	int iResult = 0;

	try {
	    // 非对象类型赋值
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, seqNo);

	    iResult = QrBookDao.insert(qrBook);
	    if (iResult <= 0) {
		SysPub.appLog("ERROR", "插入t_qrp_book表失败");
	    }
	} catch (Exception e) {
	    SysPub.appLog("ERROR", "插入t_qrp_book表失败");
	    e.printStackTrace();
	    throw e;
	}

	SysPub.appLog("INFO", "插入数据，返回:%d", iResult);
	return iResult;
    }

}
