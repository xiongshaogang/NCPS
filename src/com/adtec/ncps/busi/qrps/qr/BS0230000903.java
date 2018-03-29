package com.adtec.ncps.busi.qrps.qr;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.adtec.ncps.busi.ncp.BusiPub;
import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.ncps.busi.qrps.QrBusiPub;
import com.adtec.ncps.busi.qrps.bean.QrBook;
import com.adtec.ncps.busi.qrps.dao.QrBookDao;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.struct.dta.DtaInfo;
import com.union.sdk.AcpService;
import com.union.sdk.DemoBase;
import com.union.sdk.SDKConstants;
import com.union.sdk.SDKUtil;
import com.union.sdk.SignService;

/**
 * @ClassName: BS0230000903
 * @Description:接收 C2B 附加处理
 * @author Q
 * @date 2017年12月22日下午1:55:16
 *
 */
public class BS0230000903 {

	/**
	 * @Description: 将请求的信息转换到数据对象中
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
			// transAddnInfo交易附加信息TRANSADDNINFO
			String transAddnInfo = (String) EPOper.get(epID, "OBJ_QRUP_ALL[0].transAddnInfo");
			if (transAddnInfo != null && transAddnInfo.length() > 0) {
				QrBusiPub.parsBase64Info2Ele(transAddnInfo, "transAddnInfo", svrReq);
			}
		} catch (Exception e) {
			SysPub.appLog("ERROR", "解析base64字符串到数据对象失败！-%s", e.getMessage());
		}

		return 0;
	}

	public static int deal() throws Exception {
		SysPub.appLog("INFO", "开始BS0230000903业务处理");
		int ret = 0;

		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();

		String svrReq = "OBJ_QRUP_ALL";

		String svrRes = "OBJ_QRUP_ALL";

		SysPub.appLog("INFO", "svrReq= " + svrReq);
		EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);

		SysPub.appLog("INFO", "复制svrReq成功");
		// 解析组合数据到数据对象
		parsDataToEle(svrReq);

		// 查询原交易数据到数据元素T_QRP_BOOK
		ret = queryOldQrNoInfo();
		if (ret > 0) {
			// 修改原申请c2b码交易状态
			uptOldQrNoInfo();
		}

		// 组织数据登记到book表
		ret = insQrBook();
		if (ret != 1) {
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].respCode", "99");
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].respMsg", "登记book表失败");
			SysPub.appLog("ERROR", "插入数据库表失败");
		} else {
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].respCode", "00");
			EPOper.put(tpID, "OBJ_QRUP_ALL[0].respMsg", "附加处理接收成功");
			SysPub.appLog("INFO", "插入数据库成功");
		}
		
	
	    /* 报文头赋值 */
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].version", SDKConstants.VERSION_1_0_0);
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].signature", "0");
	    EPOper.put(tpID, "OBJ_QRUP_ALL[0].certId", "68759529225");
	    
		EPOper.copy(tpID, tpID, "OBJ_QRUP_ALL","OBJ_ALA_abstarct_RES[0].res");

		return 0;
	}

	public static int queryOldQrNoInfo() throws Exception {

		// 获取数据源
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String szTpID = dtaInfo.getTpId();

		int ret = 0;

		try {
			String szSql = "select * from t_qrp_book where plat_date = ? and qr_code = ? and req_type = ? ";
			String platDate = (String) EPOper.get(szTpID, "T_PLAT_PARA[0].PLAT_DATE");
			String qrNo = (String) EPOper.get(szTpID, "OBJ_QRUP_ALL[0].qrNo");
			SysPub.appLog("INFO", "平台日期[%s]c2b码[%s]", platDate, qrNo);
			Object[] value = { platDate, qrNo, "0210000903" };
			ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
			if (ret == 0) {
				SysPub.appLog("ERROR", "没有找到c2b码的申请记录");
				EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respMsg", "没有找到c2b码的申请记录");
			}
		} catch (Exception e) {
			throw e;
		}
		return ret;

	}

	public static int queryOldQrNoInfo(String platDate, String qrNo, String req) throws Exception {

		// 获取数据源
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String szTpID = dtaInfo.getTpId();

		int ret = 0;

		try {
			String szSql = "select * from t_qrp_book where plat_date = ? and qr_code = ? and req_type = ? ";

			SysPub.appLog("INFO", "平台日期[%s]c2b码[%s]", platDate, qrNo);
			Object[] value = { platDate, qrNo, "0210000903" };
			ret = DataBaseUtils.queryToElem(szSql, "T_QRP_BOOK", value);
			if (ret == 0) {
				SysPub.appLog("ERROR", "没有找到c2b码的申请记录");
				EPOper.put(szTpID, "OBJ_QRUP_ALL[0].respMsg", "没有找到c2b码的申请记录");
			}
		} catch (Exception e) {
			throw e;
		}
		return ret;

	}
	public static int uptOldQrNoInfo() throws Exception {

		// 获取数据源
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String szTpID = dtaInfo.getTpId();

		int ret = 0;

		try {
			String szSql = "update t_qrp_book set stat = ? where plat_date = ? and seq_no = ? ";
			String platDate = (String) EPOper.get(szTpID, "T_PLAT_PARA[0].PLAT_DATE");
			int seqNo = (Integer) EPOper.get(szTpID, "T_QRP_BOOK[0].SEQ_NO");
			SysPub.appLog("INFO", "平台日期[%s]平台流水[%d]", platDate, seqNo);
			Object[] value = { "200", platDate, seqNo };
			ret = DataBaseUtils.execute(szSql, value);
			if (ret == 0) {
				SysPub.appLog("ERROR", "更新原纪录失败");
			}
		} catch (Exception e) {
			throw e;
		}
		return ret;

	}

	public static int insQrBook() throws Exception {
		SysPub.appLog("INFO", "开始BS0230000903登记簿数据");
		int iResult = 0;
		DtaInfo dtaInfo = DtaInfo.getInstance();
		String tpID = dtaInfo.getTpId();

		QrBook qrBook = new QrBook();

		try {// 先产生流水号
			BusiPub.getPlatSeq();
			int seqNo = (Integer) EPOper.get(tpID, "INIT[0].SeqNo");
			String platDate = (String) EPOper.get(tpID, "T_PLAT_PARA[0].PLAT_DATE");
			// 非对象类型赋值
			QrBusiPub.qrBookData(tpID, qrBook, platDate, seqNo);

			// 付款方信息 payerInfo
			// 卡号与账户类型必填，姓名、证件号、cvn2、有效期均可不填；若上送了部分上述可选要素，银联会将这些要素填入 8583
			// 消费报文中送给发卡行系统验证PAYERINFO
			qrBook.setPayer_info_acc_no((String) EPOper.get(tpID, "PAYERINFO[0].accNo"));

			// TRANSADDNINFO
			qrBook.setTrans_addn_info_acq_code((String) EPOper.get(tpID, "TRANSADDNINFO[0].acqCode"));
			qrBook.setTrans_addn_info_mer_id((String) EPOper.get(tpID, "TRANSADDNINFO[0].merId"));
			qrBook.setTrans_addn_info_mer_name((String) EPOper.get(tpID, "TRANSADDNINFO[0].merName"));

			qrBook.setResp_code("00");
			qrBook.setResp_msg("接收c2b附加信息成功");

			iResult = QrBookDao.insert(qrBook);
			if (iResult <= 0) {
				SysPub.appLog("ERROR", "插入t_qrp_book表失败");
			}
		} catch (Exception e) {
			SysPub.appLog("ERROR", "插入t_qrp_book表失败");
			e.printStackTrace();
			//throw e;
		}

		SysPub.appLog("INFO", "插入数据，返回:%d", iResult);
		return iResult;
	}
	
	public static void main(String[] args) throws Exception {
		String acqCode="12345678";
		String merId="111111111111";
		String merName="faang";
		Map<String, String> contentData = new HashMap<String, String>();
		contentData.put("accNo", "6216261000000002485");
		contentData.put("name", "全渠道");
		//contentData.put("id", null);
		//contentData.put("merName", merName);
		
		String transAddnInfo = DemoBase.getPayeeInfo(contentData, "UTF-8");
		
		
//		String resultData = SDKUtil.coverMap2String(contentData);
//		System.out.println("transAddnInfo="+resultData);
//		// 将字符串增加{}
//		resultData = SDKConstants.LEFT_BRACE + resultData + SDKConstants.RIGHT_BRACE;
//		System.out.println("transAddnInfo="+resultData);
//		String base64Data = SignService.base64Encode(resultData, SDKConstants.UTF_8_ENCODING);
//		System.out.println("transAddnInfo="+base64Data);
//		
//		    base64Data = URLEncoder.encode(base64Data, SDKConstants.UTF_8_ENCODING);
//		
//
//		System.out.println("transAddnInfo="+base64Data);
//		String transAddnInfo = "transAddnInfo="+trans;
		//String resTrans=AcpService.base64Decode(trans, "UTF-8");
		System.out.println(transAddnInfo);
		
		
		String accNo="12345678";
		String name="111111111111";
		String acctClass="01";
		contentData = new HashMap<String, String>();
		contentData.put("accNo", accNo);
		contentData.put("name", name);
		contentData.put("acctClass", acctClass);
		
		String payerInfo = DemoBase.getAddnCond(contentData, "UTF-8");
		
		
		//String payerInfo="payerInfo="+trans;
		//resTrans=AcpService.base64Decode(trans, "UTF-8");
		//System.out.println(payerInfo);
		
		
		contentData = new HashMap<String, String>();

		contentData.put("desc", "维码测试随机立减");
		contentData.put("id", "1201201704170002");
		contentData.put("spnsrId", "00010000");  //这里使用光大银行机构号
		
		contentData.put("type", "DD01");
		
		//contentData.put("payerInfo", payerInfo);
		//contentData.put("transAddnInfo", transAddnInfo);
		
		
		String reqData=SDKUtil.coverMap2String(contentData);
		reqData="{" + reqData + "}";
		reqData=SignService.formInfoBase64(contentData, SDKConstants.UTF_8_ENCODING);
		String base64Data = URLEncoder.encode(reqData, SDKConstants.UTF_8_ENCODING);
		System.out.println(base64Data);
		
	
	}

}
