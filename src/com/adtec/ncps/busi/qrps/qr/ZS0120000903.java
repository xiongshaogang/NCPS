package com.adtec.ncps.busi.qrps.qr;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.BusiPub;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.qrps.QrBusiPub;
import com.adtec.ncps.busi.qrps.bean.QrBook;
import com.adtec.ncps.busi.qrps.dao.QrBookDao;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.log.TransLog;
import com.adtec.starring.struct.dta.DtaInfo;
import com.union.sdk.SDKConstants;

/**
 * @ClassName: ZS0120000903
 * @Description: 查询订单信息
 * @author Q
 * @date 2018年1月3日下午3:07:58
 *
 */
public class ZS0120000903 {

    public static int deal() throws Exception {
	SysPub.appLog("INFO", "STEP==>> 开始ZS0120000903业务处理");
	
	SysPub.appLog("INFO", "STEP==>>	初始化参数：[%s]","","begin");
	
	QrBook qrBook = new QrBook();
	BusiPub.getPlatSeq();// 先产生流水号
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();
	int qryRet = -1;
	int result = 0 ;
	
	String svcName = (String) EPOper.get(tpID, "OBJ_ALA_abstarct_REQ[0].svcName");
	svcName = svcName.toUpperCase();
	
	String svrReq = "OBJ_EBANK_SVR_" + svcName + "_REQ";
	String cltReq = "OBJ_QRUP_ALL";
	String svrRes = "OBJ_EBANK_SVR_" + svcName + "_RES";
	String cltRes = "OBJ_QRUP_ALL";
	
    int iseq_no = (Integer) EPOper.get(tpID, "INIT[0].SeqNo");
    String platDate = (String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE");
	
	EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);
	
	SysPub.appLog("INFO", "STEP==>>	复制svrReq[%s]：[%s]",svrReq,"success");
	
	qryRet = sendUP( svrReq,  cltReq, svrRes,  cltRes,  qrBook,  platDate,  iseq_no);

	 insQrBook( qrBook,  platDate,  iseq_no, svrRes+"[0].cd[0].respCode", svrRes+"[0].cd[0].respMsg");
	    
	 EPOper.copy(tpID, tpID,svrRes, "OBJ_ALA_abstarct_RES[0].res");
	return 0;
    }

    /**
     * 插入记录 到 t_qrp_book表，并提供相应的数据处理
     * @param qrBook
     * @param platDate
     * @param iseq_no
     * @param respCodeEle 
     * @param respMsgEle 
     * @return
     * @throws Exception
     */
    public static int insQrBook(QrBook qrBook, String platDate, int iseq_no, String respCodeEle, String respMsgEle) throws Exception {
	SysPub.appLog("INFO", "开始ZS0120000903登记簿数据");
	int iResult = 0;
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();

	try {
	    
	    // 非对象类型赋值
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, iseq_no);

	    // 补登记二维码
//	    qrBook.setQr_code((String) EPOper.get(tpID, "INIT[0].__ERR_MSG"));
	    // 收款方信息
	    qrBook.setPayee_info_acc_no((String) EPOper.get(tpID, "PAYEEINFO[0].accNo"));
	    qrBook.setPayee_info_id((String) EPOper.get(tpID, "PAYEEINFO[0].id"));
	    qrBook.setPayee_info_mercat_code((String) EPOper.get(tpID, "PAYEEINFO[0].merCatCode"));
	    qrBook.setPayee_info_name((String) EPOper.get(tpID, "PAYEEINFO[0].name"));
	    qrBook.setPayee_info_sub_id((String) EPOper.get(tpID, "PAYEEINFO[0].subId"));
	    qrBook.setPayee_info_sub_name((String) EPOper.get(tpID, "PAYEEINFO[0].subName"));
	    qrBook.setPayee_info_term_id((String) EPOper.get(tpID, "PAYEEINFO[0].termId"));

	    qrBook.setStat("000");// 初始状态

	    iResult = QrBookDao.insert(qrBook);
	    if (iResult <= 0) {
	    	EPOper.put(tpID, respCodeEle,"01");
	    	EPOper.put(tpID, respMsgEle,"录入数据失败");
	    	SysPub.appLog("ERROR", "插入t_qrp_book表失败");
	    }
	} catch (Exception e) {
		EPOper.put(tpID, respCodeEle,"01");
    	EPOper.put(tpID, respMsgEle,"录入数据失败");
	    SysPub.appLog("ERROR", "插入t_qrp_book表失败");
	    e.printStackTrace();
	    return -1;
	}

	SysPub.appLog("INFO", "插入数据，返回:%d", iResult);
	return iResult;
    }

    /**
     * 组织报文发送到银联
     * @author Q
     * @param qrBook 
     * @param platDate 
     * @param iseq_no 
     * @return
     * @throws Exception
     * @date 2017年12月17日下午5:04:44
     */
    public static int sendUP(String svrReq, String cltReq,String svrRes, String cltRes, QrBook qrBook, String platDate, int iseq_no) throws Exception {
	SysPub.appLog("INFO", "发往银联开始");
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String tpID = dtaInfo.getTpId();
	
	String reqType = (String) EPOper.get(tpID, svrReq+"[0].cd[0].reqType");
	String issCode = (String) EPOper.get(tpID, svrReq+"[0].cd[0].issCode");
	String qrCode = (String) EPOper.get(tpID, svrReq+"[0].cd[0].qrCode");
	try {
		
	    /* 报文头赋值 */
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].version", SDKConstants.VERSION_1_0_0);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].signature", "0");
	     EPOper.put(tpID, "OBJ_QRUP_ALL[0].certId", "68759529225");//证书id
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].reqType", reqType);
	     EPOper.put(tpID, "OBJ_QRUP_ALL[0].issCode", issCode);
	     EPOper.put(tpID, "OBJ_QRUP_ALL[0].qrCode", qrCode);

	    // 将二维码的值保存下来后面登记表用
//	    EPOper.copy(tpID, tpID, "OBJ_QRUP_ALL[0].qrCode", "INIT[0].__ERR_MSG");
	    
	    EPOper.copy(tpID, tpID, cltReq, "OBJ_ALA_abstarct_REQ[0].req");
	    SysPub.appLog("INFO", "复制OBJ_ALA_abstarct_REQ[0].req成功");
	    // 调度银联0120000903服务
	    SysPub.appLog("INFO", "调用银联0120000903服务开始");
	    DtaTool.call("QRUP_CLI", "ZS0120");
	    
	 // 非对象类型赋值,便于后面删除数据
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, iseq_no);
	    
	    EPOper.delete(tpID, cltRes);
	    EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res", cltRes);
	    EPOper.put(tpID, svrRes+"[0].cd[0].reqType", (String) EPOper.get(tpID, cltRes+"[0].reqType"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].issCode", (String) EPOper.get(tpID, cltRes+"[0].issCode"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].txnNo", (String) EPOper.get(tpID, cltRes+"[0].txnNo"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].orderType", (String) EPOper.get(tpID, cltRes+"[0].orderType"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].paymentValidTime", (String) EPOper.get(tpID, cltRes+"[0].paymentValidTime"));
	    
	    QrBusiPub.parsBase64Info2Ele((String) EPOper.get(tpID, cltRes+"[0].payeeInfo"), "payeeInfo", svrRes);
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeInfo[0].merCatCode", (String) EPOper.get(tpID, "PAYEEINFO[0].merCatCode"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeInfo[0].id", (String) EPOper.get(tpID, "PAYEEINFO[0].id"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeInfo[0].name", (String) EPOper.get(tpID, "PAYEEINFO[0].name"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeInfo[0].termId", (String) EPOper.get(tpID, "PAYEEINFO[0].termId"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeInfo[0].accNo", (String) EPOper.get(tpID, "PAYEEINFO[0].accNo"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeInfo[0].subId", (String) EPOper.get(tpID, "PAYEEINFO[0].subId"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeInfo[0].subName", (String) EPOper.get(tpID, "PAYEEINFO[0].subName"));
	    QrBusiPub.parsInfo2Obj("payeeInfo", qrBook, svrRes);
	    
	    EPOper.put(tpID, svrRes+"[0].cd[0].acqCode", (String) EPOper.get(tpID, cltRes+"[0].acqCode"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].encryptCertId", (String) EPOper.get(tpID, cltRes+"[0].encryptCertId"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].txnAmt", (String) EPOper.get(tpID, cltRes+"[0].txnAmt"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].currencyCode", (String) EPOper.get(tpID, cltRes+"[0].currencyCode"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].payeeComments", (String) EPOper.get(tpID, cltRes+"[0].payeeComments"));
	    EPOper.put(tpID, svrRes+"[0].cd[0].invoiceSt", (String) EPOper.get(tpID, cltRes+"[0].invoiceSt"));
	    EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");
	    if("00".equals((String) EPOper.get(tpID, cltRes+"[0].respCode"))) {
	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode","0000");
	    }else {
	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode", (String) EPOper.get(tpID, cltRes+"[0].respCode"));
	    }
	    EPOper.put(tpID, svrRes+"[0].cd[0].respMsg", (String) EPOper.get(tpID, cltRes+"[0].respMsg"));
	    
	} 
	catch (Exception e) {
		EPOper.put(tpID, svrRes+"[0].cd[0].respCode","01");
		EPOper.put(tpID, svrRes+"[0].cd[0].respMsg","调用银联服务0120000903失败");
		qrBook.setResp_code("01");
		qrBook.setResp_msg("调用银联服务0120000903失败");
	    SysPub.appLog("ERROR", "调用银联服务0120000903失败：%s", e.getMessage());
    	return -1;
	}

	return 0;
    }

}
