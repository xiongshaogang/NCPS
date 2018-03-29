package com.adtec.ncps.busi.qrps.qr;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.qrps.QrBusiPub;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.struct.dta.DtaInfo;
import com.union.sdk.SDKConstants;

/**
 * @ClassName: BS0240000903
 * @Description: 发送C2B附加处理结果通知
 * @author Q
 * @date 2018年1月2日上午11:10:00
 *
 */
public class BS0240000903 {

    /**
     * @Description: 调用银联接口 发送银联
     * @author Q
     * @return
     * @throws Exception
     * @date 2017年12月17日下午5:04:44
     */
    public static int sendUP(String svrReq) throws Exception {
	SysPub.appLog("INFO", "发往银联开始");
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String szTpID = dtaInfo.getTpId();



	try {
	    String version = null;
	    String signature = null;
	    String certId = null;
	    String reqType = null;
	    String issCode = null;
	    String qrType = null;
	    String payerInfo = null;
	    String qrValidTime = null;
	    String riskInfo = null;
	    String qrNo = null;
	    String addnCond = null;
	    String addnOpUrl = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].addnOpUrl");
	    String encryptCertId = null;
	    String backUrl = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].backUrl");
	    String reqReserved = null;

	    /* 报文头赋值 */
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].version", SDKConstants.VERSION_1_0_0);
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].signature", "0");
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].certId", "68759529225");
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].reqType", "0240000903");
	    // EPOper.put(tpID, "OBJ_QRUP_ALL[0].issCode", issCode);
	    // EPOper.put(tpID, "OBJ_QRUP_ALL[0].qrType", qrType);

	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respCode", "00");
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respMsg", "交易成功");
	    
	    
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].issCode", "OBJ_QRUP_ALL[0].issCode");
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].reqType", "OBJ_QRUP_ALL[0].reqType");
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].qrType", "OBJ_QRUP_ALL[0].qrType");
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].qrValidTime", "OBJ_QRUP_ALL[0].qrValidTime");
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].addnOpUrl", "OBJ_QRUP_ALL[0].addnOpUrl");
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].backUrl", "OBJ_QRUP_ALL[0].backUrl");
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].reqReserved", "OBJ_QRUP_ALL[0].reqReserved");
	    EPOper.copy(szTpID, szTpID, svrReq+"[0].cd[0].qrNo", "OBJ_QRUP_ALL[0].qrNo");


	    // EPOper.put(tpID, "OBJ_QRUP_ALL[0].qrValidTime", );

	    riskInfo = QrBusiPub.parsEle2Base64Info("riskInfo", true, svrReq);
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].riskInfo", riskInfo);

	    // EPOper.put(tpID, "OBJ_QRUP_ALL[0].encryptCertId",
	    // encryptCertId);

	    EPOper.copy(szTpID, szTpID, "OBJ_QRUP_ALL", "OBJ_ALA_abstarct_REQ[0].req");
	    // 调度银联CUP0303服务
	    SysPub.appLog("INFO", "调用银联0240000903服务开始");
	    DtaTool.call("QRUP_CLI", "BS0240");

	} catch (Exception e) {
	    SysPub.appLog("ERROR", "调用银联服务0240000903失败：%s", e.getMessage());
	    throw e;
	}

	return 0;
    }

    public static int deal() throws Exception {
	SysPub.appLog("INFO", "0240000903服务开始");

	// 获取数据源
	DtaInfo dtaInfo = DtaInfo.getInstance();
	String szTpID = dtaInfo.getTpId();
	int ret = 0;

	String svcName1 = (String) EPOper.get(szTpID, "__GDTA_FORMAT.__GDTA_SVCNAME");
	SysPub.appLog("INFO", "svcName1=" + svcName1);
	String svcName = (String) EPOper.get(szTpID, "OBJ_ALA_abstarct_REQ[0].svcName");
	SysPub.appLog("INFO", "svcName2=" + svcName);

	// String svcName = (String) EPOper.get(tpID, svrReq +
	// "[0].cd[0].serviceCode");
	String svrReq = "OBJ_EBANK_SVR_QR0240_REQ";
	String cltReq = "OBJ_QRUP_ALL";
	String svrRes = "OBJ_EBANK_SVR_QR0240_RES";
	String cltRes = "OBJ_QRUP_ALL";
	
	String c2bPlatDate = null;
	int c2bSeqNo = 0;
	String c2bStat = null;

	try {
	    // 将base64信息转换到数据对象中
	    // QrBusiPub.parsDataToEle();

		EPOper.copy(szTpID, szTpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);
	    // 查询原c2b码申请记录
		String platDate = (String) EPOper.get(szTpID, svrReq+"[0].cd[0].orgPlatDate");
		String qrNo = (String) EPOper.get(szTpID, svrReq+"[0].cd[0].qrNo");
		//String reqType = (String) EPOper.get(szTpID, "[0].cd[0].reqType");
	    ret = BS0230000903.queryOldQrNoInfo(platDate, qrNo, "0210000903");
	    if (0 == ret) {
		EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respCode", "01");
		EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respMsg", "没有找到原c2b码申请记录");
		return -1;
	    }
	    c2bStat = (String) EPOper.get(szTpID, "T_QRP_BOOK[0].STAT");
	    if (!"200".equals(c2bStat) && !"201".equals(c2bStat)) {
		QrBusiPub.putPubRet("01", "原交易状态不支持发送c2b码确认应答",svrRes);
		return -1;
	    }
	    c2bPlatDate = (String) EPOper.get(szTpID, "T_QRP_BOOK[0].PLAT_DATE");
	    c2bSeqNo = (Integer) EPOper.get(szTpID, "T_QRP_BOOK[0].SEQ_NO");

	    // 组织到银联报文
	    //String qrNo = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].qrNo");
	    ret = QrBusiPub.queryQrpsBook(c2bPlatDate, qrNo, "0240000903");
	    if (ret > 0) {
		// 付款凭证号 voucherNum
		EPOper.copy(szTpID, szTpID, "T_QRP_BOOK[0].VOUCHER_NUM", "OBJ_QRUP_ALL[0].voucherNum");
		// 银联保留域 upReserved
		EPOper.copy(szTpID, szTpID, "T_QRP_BOOK[0].UP_RESERVED", "OBJ_QRUP_ALL[0].upReserved");
	    }
	    
	    // 付款方信息 payerInfo  118.122.185.158 12028
	    // 卡号与账户类型必填，姓名、证件号、cvn2、有效期均可不填；若上送了部分上述可选要素，银联会将这些要素填入 8583
	    // 消费报文中送给发卡行系统验证
	    String payerInfo = QrBusiPub.parsEle2Base64Info("payerInfo", true, svrReq);
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].payerInfo", payerInfo);
	    
	    
	    String riskInfo = QrBusiPub.parsEle2Base64Info("riskInfo", true, svrReq);
	    EPOper.put(szTpID, "OBJ_QRUP_ALL[0].riskInfo", riskInfo);
	    
	    // 发送到银联
	    ret = sendUP(svrReq);
	    // 修改原c2b申请交易状态
	    if (0 == ret) {
		ret = QrBusiPub.uptQrpsBookStat(c2bPlatDate, c2bSeqNo, "300");
	    }
	    
	  //返回前端赋值
  		EPOper.copy(szTpID, szTpID, "OBJ_ALA_abstarct_RES[0].res[0].respMsg", svrRes+"[0].hostErrorMessage");

  		
  		EPOper.copy(szTpID, szTpID, svrReq+"[0].tx_code", svrRes+"[0].tx_code");
  		
  		String  respCode= (String) EPOper.get(szTpID, "OBJ_ALA_abstarct_RES[0].res[0].respCode");
  		
  		if("00".equals(respCode))
  			EPOper.put(szTpID, svrRes+"[0].hostReturnCode", "0000");
  		else
  			EPOper.copy(szTpID, szTpID, "OBJ_ALA_abstarct_RES[0].res[0].respCode", svrRes+"[0].hostReturnCode");
  		
  		EPOper.put(szTpID, svrRes+"[0].msgReturnFrom", "交易成功");
  		
  		EPOper.copy(szTpID, szTpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
	} catch (Exception e) {
	    throw e;
	}
	return 0;
    }

}
