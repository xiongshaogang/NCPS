package com.adtec.ncps.busi.qrps.qr;

import java.net.URLEncoder;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.AmountUtils;
import com.adtec.ncps.busi.ncp.BusiPub;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.qrps.QrBusiPub;
import com.adtec.ncps.busi.qrps.bean.QrBook;
import com.adtec.ncps.busi.qrps.dao.QrBookDao;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.struct.dta.DtaInfo;
import com.union.sdk.SDKConstants;

/**
 * @ClassName: ZS0510000903
 * @Description: 人到人生成二维码
 * @author Q
 * @date 2018年3月15日下午8:27:52
 */
public class ZS0510000903 {

	private static QrBook pubBook = new QrBook();

	public static int chk() throws Exception {
		SysPub.appLog("INFO", "二维码码申请开始");
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String szTpID = dtaInfo.getTpId();

		try {
			// 初始化返回报文
			initResFmt();
		} catch (Exception e) {
			EPOper.put(szTpID, "INIT._FUNC_RETURN", 0, "-1");
			SysPub.appLog("ERROR", "ZS0510000903chk失败");
			e.printStackTrace();
			throw e;
		}
		EPOper.put(szTpID, "INIT._FUNC_RETURN", 0, "0");
		SysPub.appLog("INFO", "二维码申请chk完成");
		return 0;
	}

	public static void initResFmt() {
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String szTpID = dtaInfo.getTpId();

		// EPOper.put(szTpID, "OBJ_QRUP_ALL[0].version",
		// SDKConstants.VERSION_1_0_0);
		// EPOper.put(szTpID, "OBJ_QRUP_ALL[0].signature", "0");
		// EPOper.copy(szTpID, szTpID, "OBJ_QRUP_ALL[0].certId",
		// "OBJ_QRUP_ALL[0].certId");
		// EPOper.copy(szTpID, szTpID, "OBJ_QRUP_ALL[0].reqType",
		// "OBJ_QRUP_ALL[0].reqType");
		// EPOper.copy(szTpID, szTpID, "OBJ_QRUP_ALL[0].issCode",
		// "OBJ_QRUP_ALL[0].issCode");
		// EPOper.put(szTpID, "OBJ_QRUP_ALL[0].qrValidTime", 100);
		// EPOper.put(szTpID, "OBJ_QRUP_ALL[0].qrNo", "0");
		// EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respCode", "99");
		// EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respMsg", "初始化错误");
	}

	public static int transDeal() {
		try {
			DtaInfo dtaInfo = DtaInfo.getInstance();
			String tpID = dtaInfo.getTpId();
			SysPub.appLog("INFO", "执行 signDeal 方法开始");
			// String svcName = dtaInfo.getSvcName();

			// String svcName = (String) EPOper.get(tpID, svrReq +
			// "[0].cd[0].serviceCode");
			String svrReq = "OBJ_EBANK_SVR_QR0510_REQ";
			String cltReq = "OBJ_QRUP_ALL";
			String svrRes = "OBJ_EBANK_SVR_QR0510_RES";
			String cltRes = "OBJ_QRUP_ALL";

			SysPub.appLog("INFO", "svrReq= " + svrReq);
			EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);

			SysPub.appLog("INFO", "复制svrReq成功");



			// 插入失败
			
			int ret=deal(svrReq);
            if(ret !=0){
            	EPOper.put(tpID, svrRes + "[0].msgReturnFrom", "银联二维码");
            	EPOper.put(tpID, "OBJ_QRUP_ALL[0].respCode", "12");
    			EPOper.put(tpID, "OBJ_QRUP_ALL[0].respMsg", "记录重复");
    		  
            	EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
            	return 0;
            }
			
			// 发送失败
			ret=sendUP(svrReq, cltReq, cltRes);
			 if(ret !=0){
	            	EPOper.put(tpID, svrRes + "[0].msgReturnFrom", "银联二维码");
	            	EPOper.put(tpID, "OBJ_QRUP_ALL[0].respCode", "12");
	    			EPOper.put(tpID, "OBJ_QRUP_ALL[0].respMsg", "记录重复");
	    	
	            	EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
	    			String retCode = (String) EPOper.get(tpID, svrRes+"[0].hostReturnCode");
	    			String retMsg = (String) EPOper.get(tpID, svrRes+"[0].hostErrorMessage");
	    			QrBusiPub.uptBookErrRet(retCode, retMsg);
	            	return 0;
	            }

			// 银联返回失败
			ret=recvUP(svrRes);
			 if(ret !=0){
	            	EPOper.put(tpID, svrRes + "[0].msgReturnFrom", "银联二维码");
	            	EPOper.put(tpID, "OBJ_QRUP_ALL[0].respCode", "12");
	    			EPOper.put(tpID, "OBJ_QRUP_ALL[0].respMsg", "记录重复");
	    				
	            	EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
	    			String retCode = (String) EPOper.get(tpID, svrRes+"[0].hostReturnCode");
	    			String retMsg = (String) EPOper.get(tpID, svrRes+"[0].hostErrorMessage");
	    			QrBusiPub.uptBookErrRet(retCode, retMsg);
	            	return 0;
	            }

			// 返回前端赋值
			EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res[0].respMsg", svrRes + "[0].hostErrorMessage");

			EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

			String respCode = (String) EPOper.get(tpID, "OBJ_ALA_abstarct_RES[0].res[0].respCode");

			if ("00".equals(respCode)) {
				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "0000");
				EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res[0].qrCode", svrRes + "[0].cd[0].qrCode");
				EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res[0].qrValidTime", svrRes + "[0].cd[0].qrValidTime");
				EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res[0].limitCount", svrRes + "[0].cd[0].limitCount");
			} else {
				EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res[0].respCode", svrRes + "[0].hostReturnCode");
			}
			EPOper.put(tpID, svrRes + "[0].msgReturnFrom", "银联二维码");

			EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
			String retCode = (String) EPOper.get(tpID, svrRes+"[0].hostReturnCode");
			String retMsg = (String) EPOper.get(tpID, svrRes+"[0].hostErrorMessage");
			QrBusiPub.uptBookErrRet(retCode, retMsg);
		} catch (Exception e) {
			return -1;
		}
		return 0;
	}


	public static int deal(String svrReq) throws Exception {
		SysPub.appLog("INFO", "开始ZS0510000903业务处理");
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();

		// 组织数据登记到book表
		int ret = insQrBook(svrReq);
		if (ret != 1) {
			EPOper.put(tpID, "INIT._FUNC_RETURN", 0, "-1");
			SysPub.appLog("ERROR", "插入数据库表失败");
		} else {
			EPOper.put(tpID, "INIT._FUNC_RETURN", 0, "0");
			SysPub.appLog("INFO", "插入数据库成功");
		}

		return 0;
	}

	public static int insQrBook(String svrReq) throws Exception {
		SysPub.appLog("INFO", "开始ZS0510000903登记簿数据");
		int iResult = 0;
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();

		QrBook qrBook = new QrBook();

		try {// 先产生流水号
			BusiPub.getPlatSeq();
			int iseq_no = (Integer) EPOper.get(tpID, "INIT[0].SeqNo");
			String seq_no = String.valueOf(iseq_no);
			qrBook.setSeq_no(iseq_no);
			qrBook.setQr_code(seq_no);
			// 保存平台流水号和平台日期，更新流水时用
			qrBook.setPlat_date((String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE"));
			qrBook.setVersion(SDKConstants.VERSION_1_0_0);
			qrBook.setCert_id((String) EPOper.get(tpID, svrReq + "[0].cd[0].certId"));
			qrBook.setReq_type((String) EPOper.get(tpID, svrReq + "[0].cd[0].reqType"));
			qrBook.setIss_code((String) EPOper.get(tpID, svrReq + "[0].cd[0].issCode"));
			qrBook.setQr_type((String) EPOper.get(tpID, svrReq + "[0].cd[0].qrType"));
			

			// 付款方信息 payerInfo
			// 卡号与账户类型必填，姓名、证件号、cvn2、有效期均可不填；若上送了部分上述可选要素，银联会将这些要素填入 8583
			// 消费报文中送给发卡行系统验证PAYERINFO

			qrBook.setPayee_info_acc_no((String) EPOper.get(tpID, svrReq + "[0].cd[0].payeeInfo[0].accno"));
			qrBook.setPayee_info_id((String) EPOper.get(tpID, svrReq + "[0].cd[0].payeeInfo[0].id"));
			qrBook.setPayee_info_name((String) EPOper.get(tpID, svrReq + "[0].cd[0].payeeInfo[0].name"));
			qrBook.setOrder_no((String) EPOper.get(tpID, svrReq + "[0].cd[0].orderNo"));
			qrBook.setOrder_time((String) EPOper.get(tpID, svrReq + "[0].cd[0].orderTime"));
			qrBook.setOrder_type((String) EPOper.get(tpID, svrReq + "[0].cd[0].orderType"));

			String qrValidTime = (String) EPOper.get(tpID, svrReq + "[0].cd[0].qrValidTime");
			qrBook.setQr_valid_time(Long.valueOf(qrValidTime));
			String limitCount = (String) EPOper.get(tpID, svrReq + "[0].cd[0].limitCount");
			qrBook.setLimit_count(Long.valueOf(limitCount));
			String txnAmt = (String) EPOper.get(tpID, svrReq + "[0].cd[0].txnAmt");
			Double txamt=Double.valueOf(txnAmt);
			qrBook.setTxn_amt(txamt);
			


			qrBook.setEncrypt_cert_id((String) EPOper.get(tpID, svrReq + "[0].cd[0].encryptCertId"));
			qrBook.setBack_url((String) EPOper.get(tpID, svrReq + "[0].cd[0].backUrl"));
			// qrBook.setReq_reserved((String) EPOper.get(tpID, svrReq +
			// "[0].cd[0].reqReserved"));

			qrBook.setStat("000");

			pubBook = qrBook;

			iResult = QrBookDao.insert(qrBook);
			if (iResult <= 0) {
				SysPub.appLog("ERROR", "插入t_qrp_book表失败");
			}
		} catch (Exception e) {
			SysPub.appLog("ERROR", "插入t_qrp_book表失败");
			e.printStackTrace();
			//throw e;
			return -1;
		}

		SysPub.appLog("INFO", "插入数据，返回:%d", iResult);
		return iResult;
	}

	/**
	 * @Description: 调用银联接口 发送银联
	 * @author Q
	 * @return
	 * @throws Exception
	 * @date 2017年12月17日下午5:04:44
	 */
	public static int sendUP(String svrReq, String cltReq, String cltRes) throws Exception {
		SysPub.appLog("INFO", "发往银联开始");
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();

		try {
			String version = null;
			String signature = null;
			String certId = null;
			String reqType = null;
			String issCode = null;
			String qrType = null;
			String payeeInfo = null;
			String qrValidTime = null;
			String riskInfo = null;
			String qrNo = null;
			String addnCond = null;
			String addnOpUrl = (String) EPOper.get(tpID, svrReq + "[0].cd[0].addnOpUrl");
			String encryptCertId = null;
			String backUrl = (String) EPOper.get(tpID, svrReq + "[0].cd[0].backUrl");
			String reqReserved = null;

			/* 报文头赋值 */
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].version", SDKConstants.VERSION_1_0_0);
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].signature", "0");
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].certId", "68759529225");
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].encryptCertId", "68759529225");

			// 付款方信息 payerInfo 118.122.185.158 12028
			// 卡号与账户类型必填，姓名、证件号、cvn2、有效期均可不填；若上送了部分上述可选要素，银联会将这些要素填入 8583
			// 消费报文中送给发卡行系统验证
			payeeInfo = QrBusiPub.parsEle2Base64Info("payeeInfo", true, svrReq);
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].payeeInfo", payeeInfo);

			// EPOper.put(tpID, "OBJ_QRUP_ALL[0].qrValidTime", );
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].orderTime", "OBJ_QRUP_ALL[0].orderTime");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].reqType", "OBJ_QRUP_ALL[0].reqType");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].qrType", "OBJ_QRUP_ALL[0].qrType");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].qrValidTime", "OBJ_QRUP_ALL[0].qrValidTime");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].orderNo", "OBJ_QRUP_ALL[0].orderNo");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].backUrl", "OBJ_QRUP_ALL[0].backUrl");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].reqReserved", "OBJ_QRUP_ALL[0].reqReserved");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].acqCode", "OBJ_QRUP_ALL[0].acqCode");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].qrCode", "OBJ_QRUP_ALL[0].qrCode");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].orderType", "OBJ_QRUP_ALL[0].orderType");
			EPOper.copy(tpID, tpID, svrReq + "[0].cd[0].areaInfo", "OBJ_QRUP_ALL[0].areaInfo");
			
			String txnAmt = (String) EPOper.get(tpID, svrReq + "[0].cd[0].txnAmt");			
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].txnAmt", AmountUtils.changeY2F(txnAmt));
			
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].backUrl", URLEncoder.encode(backUrl, SDKConstants.UTF_8_ENCODING));


			EPOper.copy(tpID, tpID, "OBJ_QRUP_ALL", "OBJ_ALA_abstarct_REQ[0].req");
			SysPub.appLog("INFO", "复制OBJ_ALA_abstarct_REQ[0].req成功");
			// 调度银联CUP0303服务
			SysPub.appLog("INFO", "调用0510000903服务开始");
			DtaTool.call("QRUP_CLI", "ZS0510");

		} catch (Exception e) {
			EPOper.put(tpID, "INIT._FUNC_RETURN", 0, "-1");
			SysPub.appLog("ERROR", "调用银联服务0510000903失败：%s", e.getMessage());
			throw e;
		}
		EPOper.put(tpID, "INIT._FUNC_RETURN", 0, "0");

		return 0;
	}

	/**
	 * @Description: 银联返回处理
	 * @author Q
	 * @return
	 * @throws Exception
	 * @date 2017年12月17日下午5:07:06
	 */
	public static int recvUP(String svrRes) throws Exception {
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String szTpID = dtaInfo.getTpId();
		int iResult = 0;

		QrBook qrBook = pubBook;

		try {

			EPOper.copy(szTpID, szTpID, "OBJ_ALA_abstarct_RES[0].res", "OBJ_QRUP_ALL");
			String respCode = (String) EPOper.get(szTpID, "OBJ_ALA_abstarct_RES[0].res[0].respCode");
			String respMsg = (String) EPOper.get(szTpID, "OBJ_ALA_abstarct_RES[0].res[0].respMsg");

			// 更新数据库
			qrBook.setResp_code(respCode);
			qrBook.setResp_msg(respMsg);
			if ("00".equals(respCode)) {
				String qrNo = (String) EPOper.get(szTpID, "OBJ_ALA_abstarct_RES[0].res[0].qrCode");
				String signChkFlag = (String) EPOper.get(szTpID, "OBJ_ALA_abstarct_RES[0].res[0].signCheckFlag");
				int limitCount = (int) EPOper.get(szTpID, "OBJ_ALA_abstarct_RES[0].res[0].limitCount");
				String qrValidTime = (String) EPOper.get(szTpID, "OBJ_ALA_abstarct_RES[0].res[0].qrValidTime");
				SysPub.appLog("INFO", "付款码[%s]银联返回错误码：[%s]错误信息[%s]验签状态[%s]", qrNo, respCode, respMsg, signChkFlag);
				qrBook.setQr_code(qrNo);
				qrBook.setSign_chk_flag(signChkFlag);
				Long Qr = Long.valueOf(qrValidTime);
				qrBook.setQr_valid_time(Qr);
				// Long Li=Long.valueOf(limitCount);
				qrBook.setLimit_count(limitCount);
			}

			iResult = QrBookDao.update(qrBook);
			if (iResult <= 0) {
				EPOper.put(szTpID, "INIT._FUNC_RETURN", 0, "-1");
				SysPub.appLog("ERROR", "更新t_qrp_book表失败");
			}
		} catch (Exception e) {
			SysPub.appLog("ERROR", "银联返回信息处理异常：%s", e.getMessage());
			EPOper.put(szTpID, "INIT._FUNC_RETURN", 0, "-1");
			e.printStackTrace();

			throw e;
		}
		EPOper.put(szTpID, "INIT._FUNC_RETURN", 0, "0");

		return 0;
	}

}
