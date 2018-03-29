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
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.union.sdk.SDKConstants;

/**
 * @author dengp_m  
 * @date 2018年3月24日 下午3:04:34 
 */
public class ZS0130000903 {
	
	private static QrBook qrBook ;
	private static String tpID ;
	private static String svcName ;
	private static String svrReq ;
	private static String cltReq ;
	private static String svrRes ;
	private static String cltRes ;
	private static int seqNo ;
	private static String platDate ;
	
	/**
	 * 表明 出错的步骤，用于控制交易不管任何步骤出错，都会执行后面的流水记录、更新及抽象对象赋值
	 */
	private static int errorStep = 0;
	
	/**
	 *     初始化公共参数
	 * @author dengp_m   
	 * @throws Exception 
	 * @date 2018年3月24日 下午3:38:15 
	 */
	public static void iniPubParameter() throws Exception{
			SysPub.appLog("INFO", "[STEP] 初始化全局变量：[%s]","begin");
			
			qrBook = new QrBook();
		    	BusiPub.getPlatSeq();// 先产生流水号
		    	DtaInfo dtaInfo = DtaInfo.getInstance();
		    	tpID = dtaInfo.getTpId();
		    	errorStep = 0;
		    	
		    	svcName = (String) EPOper.get(tpID, "OBJ_ALA_abstarct_REQ[0].svcName");
		    	svcName = svcName.toUpperCase();
		    	
		    	svrReq = "OBJ_EBANK_SVR_" + svcName + "_REQ";
		    	cltReq = "OBJ_QRUP_ALL";
		    	svrRes = "OBJ_EBANK_SVR_" + svcName + "_RES";
		    	cltRes = "OBJ_QRUP_ALL";
		    	
		    	seqNo = (Integer) EPOper.get(tpID, "INIT[0].SeqNo");
		    	platDate = (String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE");
		    	
		    	SysPub.appLog("INFO", "[INFO] qrBook：[%s]",qrBook);
		    	SysPub.appLog("INFO", "[INFO] tpID：[%s]",tpID);
		    	SysPub.appLog("INFO", "[INFO] svcName：[%s]",svcName);
		    	SysPub.appLog("INFO", "[INFO] svrReq：[%s]",svrReq);
		    	SysPub.appLog("INFO", "[INFO] cltReq：[%s]",cltReq);
		    	SysPub.appLog("INFO", "[INFO] svrRes：[%s]",svrRes);
		    	SysPub.appLog("INFO", "[INFO] cltRes：[%s]",cltRes);
		    	SysPub.appLog("INFO", "[INFO] seqNo：[%d]",seqNo);
		    	SysPub.appLog("INFO", "[INFO] platDate：[%s]",platDate);
		    	SysPub.appLog("INFO", "[STEP] 初始化全局变量：[%s]","end");
			 	
	}
   
    /**
     * 交易逻辑处理
     * @return
     * 	0 ：正常返回
     * 	其他 ：异常返回
     * @throws Exception    
     * @author dengp_m   
     * @date 2018年3月24日 下午3:30:05 
     */
    public static int deal() throws Exception {
    	SysPub.appLog("INFO", "[STEP] [%s]业务处理：[%s]",svcName,"begin");
    	
    	iniPubParameter();
    	
    	EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);
    	SysPub.appLog("INFO", "[STEP] 复制svrReq[%s]：[%s]",svrReq,"success");
	    	
    	int flag = 0;
	    if ( errorStep == 0 && qryOrderInfo() != 0) {
			flag = 1;
		} 
		
//	    	为1时，为了保证请求报文的数据保存到qrBook中，最终记录到流水
		if(errorStep == 0 && sendUP(flag ) != 0) {
			errorStep = 2;
		}
		
		if(errorStep == 0 && recvUp( ) != 0) {
			errorStep = 3;
		}			
		
		SysPub.appLog("INFO", "[INFO] errorStep：[%d]",errorStep);
		
	    QrBusiPub.insQrBook( qrBook,  platDate,  seqNo,svrRes+"[0].cd[0].respCode", svrRes+"[0].cd[0].respMsg");
		 	
	    SysPub.appLog("INFO", "SEQ_NO：[%s]",Integer.valueOf(String.valueOf(EPOper.get(tpID, "T_QRP_BOOK[0].SEQ_NO"))));
		QrBusiPub.uptOldQrNoInfo(qrBook.getStat(),svrRes+"[0].cd[0].respCode", svrRes+"[0].cd[0].respMsg");
		 	
		EPOper.copy(tpID, tpID,svrRes, "OBJ_ALA_abstarct_RES[0].res");
		SysPub.appLog("INFO", "[STEP] [%s]业务处理：[%s]",svcName,"end");
		return 0;
    }
    
    /**
     * 	查询原订单记录信息
     * @return
     * @throws Exception    
     * @author dengp_m   
     * @date 2018年3月24日 下午3:05:14 
     */
    public static int qryOrderInfo() throws Exception {
    	SysPub.appLog("INFO", "STEP==>>	qryOrderInfo()：[%s]","begin");
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();
	
		String txnNo = (String) EPOper.get(tpID, svrReq+".cd[0].txnNo");
		int ret = QrBusiPub.queryQrpsBook(txnNo, "0120000903");
		SysPub.appLog("INFO", "SEQ_NO：[%s]",Integer.valueOf(String.valueOf(EPOper.get(tpID, "T_QRP_BOOK[0].SEQ_NO"))));
		if (ret != 1) {
			String respCode = "01";
			String respMsg = "原订单信息没有找到";
			SysPub.appLog("ERROR", respMsg);
		    EPOper.put(tpID, svrRes+"[0].cd[0].respCode",respCode);
			EPOper.put(tpID, svrRes+"[0].cd[0].respMsg",respMsg);
			qrBook.setResp_code(respCode);
			qrBook.setResp_msg(respMsg);
//		    QrBusiPub.putPubRet("01", "原订单信息没有找到",svrRes);
		    return -1;
		} else {
		    // 判断状态
		    String stat1 = (String) EPOper.get(tpID, "T_QRP_BOOK[0].STAT");
		    if (!"000".equals(stat1) && !"401".equals(stat1)) {
				SysPub.appLog("ERROR", "订单状态为【%s】", stat1);
				String retMsg;
				if ("400".equals(stat1)) {
				    retMsg = "该订单状态为已支付，不能重复支付。";
				} else {
				    retMsg = "该订单状态为[" + stat1 + "]，不能支付。";
				}
				EPOper.put(tpID, svrRes+"[0].cd[0].respCode","01");
				EPOper.put(tpID, svrRes+"[0].cd[0].respMsg",retMsg);
				qrBook.setResp_code("01");
				qrBook.setResp_msg(retMsg);
				SysPub.appLog("ERROR", retMsg);
				return -1;
		    }
		}
		return 0;
    }

    /**
     * 接收并处理银联返回报文，发往ebank
     * @return
     * @throws Exception    
     * @author dengp_m   
     * @date 2018年3月24日 下午3:05:50 
     */
    private static int recvUp() throws Exception {
    	SysPub.appLog("INFO", "STEP==>>	recvUp()：[%s]","begin");
    	// 非对象类型赋值,便于后面的数据删除
	    QrBusiPub.qrBookData(tpID, qrBook, platDate, seqNo);
	    
	   	EPOper.delete(tpID, cltRes);
	   	EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res", cltRes);
	   	
	   	String respCode = (String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respCode");		
	   	
	   	EPOper.put(tpID, svrRes+"[0].cd[0].reqType", (String) EPOper.get(tpID, cltRes+"[0].reqType"));
	   	EPOper.put(tpID, svrRes+"[0].cd[0].issCode", (String) EPOper.get(tpID, cltRes+"[0].issCode"));
	   	String txnAmt = String.valueOf(EPOper.get(tpID, cltRes+"[0].txnAmt"));
	   	if(txnAmt != null && "".endsWith(txnAmt)) {
	   		EPOper.put(tpID, svrRes+"[0].cd[0].txnAmt", AmountUtils.changeF2Y(txnAmt));
	   	}else {
	   		EPOper.put(tpID, svrRes+"[0].cd[0].txnAmt", "");
	   	}
	   	EPOper.put(tpID, svrRes+"[0].cd[0].voucherNum", (String) EPOper.get(tpID, cltRes+"[0].voucherNum"));
	   	EPOper.put(tpID, svrRes+"[0].cd[0].settleKey", (String) EPOper.get(tpID, cltRes+"[0].settleKey"));
	   	EPOper.put(tpID, svrRes+"[0].cd[0].settleDate", (String) EPOper.get(tpID, cltRes+"[0].settleDate"));
	   	EPOper.put(tpID, svrRes+"[0].cd[0].comInfo", (String) EPOper.get(tpID, cltRes+"[0].comInfo"));
	   	EPOper.put(tpID, svrRes+"[0].cd[0].respMsg", (String) EPOper.get(tpID, cltRes+"[0].respMsg"));
	   	EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");
	   	
	    if("00".equals(respCode)) {
	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode","0000");
	    	qrBook.setStat("400") ;
	    }else {
	    	EPOper.put(tpID, svrRes+"[0].cd[0].respCode", (String) EPOper.get(tpID, cltRes+"[0].respCode"));
	    	SysPub.appLog("ERROR", "银联服务0130000903返回失败：%s-%s", respCode,
	 				(String) EPOper.get(tpID, "OBJ_QRUP_ALL[0].respMsg"));
	 		 qrBook.setStat( "401");
	    }
	    SysPub.appLog("INFO", "STEP==>>	recvUp()：[%s]","end");
		return 0;
	}							

    /**
     * 组织发送报文，上送银联
     * @return
     * @throws Exception    
     * @author dengp_m   
     * @param flag 
     * @date 2018年3月24日 下午3:06:36 
     */
    public static int sendUP(int flag) throws Exception {
    	SysPub.appLog("INFO", "[STEP] sendUP()：[%s]","begin");

	String txnAmtStr;
	try {
	    /* 报文头赋值 */
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].version", SDKConstants.VERSION_1_0_0);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].signature", "0");
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].certId", "68759529225");
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].reqType", (String) EPOper.get(tpID, svrReq+"[0].cd[0].reqType"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].issCode", (String) EPOper.get(tpID, svrReq+"[0].cd[0].issCode"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].txnNo", (String) EPOper.get(tpID, svrReq+"[0].cd[0].txnNo"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].currencyCode", (String) EPOper.get(tpID, svrReq+"[0].cd[0].currencyCode"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].encryptCertId", (String) EPOper.get(tpID, svrReq+"[0].cd[0].encryptCertId"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].payerComments", (String) EPOper.get(tpID, svrReq+"[0].cd[0].payerComments"));
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].backUrl", (String) EPOper.get(tpID, svrReq+"[0].cd[0].backUrl"));
	    String origTxnAmt =(String) EPOper.get(tpID, svrReq+"[0].cd[0].origTxnAmt");
	    if(origTxnAmt != null && !origTxnAmt.trim().isEmpty()) {
	    	EPOper.put(tpID, "OBJ_QRUP_ALL[0].origTxnAmt", AmountUtils.changeY2F(origTxnAmt));
	    }

	    txnAmtStr = (String) EPOper.get(tpID, svrReq+"[0].cd[0].txnAmt");
	    if (flag == 0 && StringUtils.isEmpty(txnAmtStr)) {
	    	SysPub.appLog("ERROR", "请求金额没有输入");
	    	String respCode = "01";
			String respMsg = "请求金额没有输入";
		    EPOper.put(tpID, svrRes+"[0].cd[0].respCode",respCode);
			EPOper.put(tpID, svrRes+"[0].cd[0].respMsg",respMsg);
			qrBook.setResp_code(respCode);
			qrBook.setResp_msg(respMsg);
	    	return -1;
	    } 
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].txnAmt", AmountUtils.changeY2F(String.valueOf(txnAmtStr)));

	    // 付款方信息
	    String payerInfo = QrBusiPub.parsEle2Base64Info("payerInfo", true,svrReq);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].payerInfo", payerInfo);
	    QrBusiPub.parsInfo2Obj("payerInfo", qrBook, svrReq);

	    // 风控信息
	    String riskInfo = QrBusiPub.parsEle2Base64Info("riskInfo", true,svrReq);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].riskInfo", riskInfo);

	    // 优惠信息couponInfo
	    String couponInfo = QrBusiPub.parsEle2Base64Info("couponInfo", true,svrReq);
	    QrBusiPub.parsInfo2Obj("couponInfo", qrBook, svrReq);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].couponInfo",couponInfo);
	    
	    
	    if(flag != 0 ) {
	    	 SysPub.appLog("INFO", "查记录失败，sendUP中结束");
	    	 return -1;
	    }
	    EPOper.copy(tpID, tpID,cltReq , "OBJ_ALA_abstarct_REQ[0].req" );
	    
	    // 调度银联0130000903服务
	    SysPub.appLog("INFO", "调用银联0130000903服务开始");
	    DtaTool.call("QRUP_CLI", "ZS0130");

		} catch (Exception e) {
			String respCode = "01";
			String respMsg = "调用银联服务0130000903失败";
		    EPOper.put(tpID, svrRes+"[0].cd[0].respCode",respCode);
			EPOper.put(tpID, svrRes+"[0].cd[0].respMsg",respMsg);
			qrBook.setResp_code(respCode);
			qrBook.setResp_msg(respMsg);
		    SysPub.appLog("ERROR", "调用银联服务0130000903失败：%s", e.getMessage());
//		    QrBusiPub.putPubRet("01", "调用银联服务失败",svrRes);
		    return -1;
		}
	
		return 0;
    }

}
