package com.adtec.ncps.busi.chnl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.AmountUtils;
import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.PubTool;
import com.adtec.ncps.busi.ncp.SftpClientUtils;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.global.SysDef;
import com.adtec.starring.respool.ResPool;
import com.adtec.starring.struct.dta.DtaInfo;

/**
 * 代发工资数据导入
 * @author Administrator
 *
 */
public class GW3001 {

	public static final int CLUM = 7;
	
	public static int cmp(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(String.valueOf(v1));
		BigDecimal b2 = new BigDecimal(String.valueOf(v2));
		return b1.compareTo(b2);
	}

	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(String.valueOf(v1));
		BigDecimal b2 = new BigDecimal(String.valueOf(v2));
		return b1.add(b2).doubleValue();
	}

	public static int clear_bat(String batNO) throws Exception {
		try {
			String szSql1 = "delete from t_bat_detail where bat_no = ?";
			Object[] value1 = { batNO };

			DataBaseUtils.executenotr(szSql1, value1);

			szSql1 = "delete from t_bat_ctl where bat_no = ?";
			DataBaseUtils.executenotr(szSql1, value1);

		} catch (Exception e) {
			throw e;
		}
		return 0;
	}

	public static int chk_trans(String tpID, String svrReq, String svrRes, Integer totalNumber, Double totalAmount,
			String contractNo, String filePathName) {

		if (null == totalNumber || totalNumber.intValue() == 0) {
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", "输入总笔数不能为空");
			return -1;
		}
		if (totalNumber.intValue() > 20000) {
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", "输入总笔数大于20000");
			return -1;
		}
		if (null == totalAmount || totalAmount.compareTo((double) 0.00) <= 0) {
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", "输入总金额不能为空");
			return -1;
		}
		if (null == contractNo || contractNo.length() == 0) {
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", "输入放款账号不能为空");
			return -1;
		}
		if (null == filePathName || filePathName.length() == 0) {
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", "输入文件名不能为空");
			return -1;
		}
		/* 当天代发记录有两笔代发总笔数与总金额都相等! */

		return 0;

	}

	public static int anlyse_recvfile(String batNO, String svrReq, String svrRes, String filePathName)
			throws Exception {
		try {

			DtaInfo dtaInfo = DtaInfo.getInstance();
			String tpID = dtaInfo.getTpId();
			Integer totalNumber = Integer.parseInt( (String) EPOper.get(tpID, svrReq + "[0].cd[0].totalNumber"));
			Double totalAmount = Double.parseDouble( (String) EPOper.get(tpID, svrReq + "[0].cd[0].totalAmount"));
			/* 登记表信息 */
			String filePath = SysDef.WORK_DIR + ResPool.configMap.get("FilePath")+"/netbank/";
			String szFileName = filePath + "/" + filePathName;
			SysPub.appLog("INFO", "STEP2 解析文件[%s]", szFileName);
			/****************************************************************************
			 * 文件格式： 序号|转入账号|转入户名|金额|证件类型(可空)|证件号(可空)|代理行(可空)|
			 ****************************************************************************/

			File file = new File(szFileName);
			if (!file.exists()) {
				SysPub.appLog("INFO", "[%s]不存在", szFileName);
				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", "文件不存在");
				return -1;
			}
			FileInputStream fs = new FileInputStream(file);
			InputStreamReader read = new InputStreamReader(fs, "GBK");
			BufferedReader bufferedReader = new BufferedReader(read);

			String szOneLine = "";
			int seq = 0;
			
			double totAmt = 0.00;
			while ((szOneLine = bufferedReader.readLine()) != null) {

				String[] destString = szOneLine.split("\\|");
				SysPub.appLog("INFO", "lenght=" + destString.length);
				if (CLUM != destString.length) {

					clear_bat(batNO);

					String msg = String.format("[%s]第 [%d]分隔符错", szFileName, seq + 1);
					EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
					EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
					bufferedReader.close();
					read.close();
					fs.close();
					return -1;
				}
				
				Double damt = Double.valueOf(destString[3]);
				totAmt = add(totAmt, damt);
				String reqDate = PubTool.getDate8();
				String szSql1 = "insert into t_bat_detail values (?,?,?,'','',?,?,?,?,'','','',?,?,?,'','','','','','','0','','','','','','','','','',0.00,0.00,0.00,0.00,0.00)";

				Object[] value1 = { batNO, seq, destString[0], reqDate, destString[1], destString[2], damt,
						destString[4], destString[5], destString[6] };
				DataBaseUtils.execute(szSql1, value1);
				seq++;

			}

			if (totalNumber != seq) {

				clear_bat(batNO);

				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
				String msg = String.format("文件明细笔数[%d]与总笔数[%d]不符", seq, totalNumber);

				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
				bufferedReader.close();
				read.close();
				fs.close();
				return -1;
			}
			if (cmp(totalAmount, totAmt) != 0) {

				clear_bat(batNO);
				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
				String msg = String.format("文件明细总金额[%f]与总金额[%f]不符", totAmt, totalAmount);
				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
				bufferedReader.close();
				read.close();
				fs.close();
				return -1;
			}

			SysPub.appLog("INFO", "文件笔数[%d]金额[%s]", seq, String.valueOf(totAmt));
			bufferedReader.close();
			read.close();
			fs.close();
			return 0;
		} catch (Exception e) {
			SysPub.appLog("ERROR", "执行 deal_jrnl 方法失败");
			throw e;
		}
	}

	/**
	 *	交易流程：
	 *		请求报文中的文件 下载到sftp文件服务器
	 *		调用核心服务S654400 检查付款账号
	 *		登记批次信息
	 *		根据下载文件 记录明细t_bat_detail
	 *		调用核心服务654500对公账户下账到内部户
	 * @return
	 * @throws Exception
	 * @author dengp_w
	 * @date 2018年3月29日 下午4:21:29
	 */
	public static int deal_trans() throws Exception {
		try {
			DtaInfo dtaInfo = DtaInfo.getInstance();
			String tpID = dtaInfo.getTpId();
			
			String svcName = (String) EPOper.get(tpID, "OBJ_ALA_abstarct_REQ[0].svcName");
			svcName = svcName.toUpperCase();
			SysPub.appLog("INFO", "svcName2=" + svcName);
		
			String svrReq = "OBJ_EBANK_SVR_" + svcName + "_REQ";
			String svrRes = "OBJ_EBANK_SVR_" + svcName + "_RES";

			

			SysPub.appLog("INFO", "svcName=" + svcName);
			EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_REQ[0].req", svrReq);

			/* 登记流水 */
			String platDate = PubTool.getDate8();
			// String strTime = PubTool.getTime();
			int platSeq = PubTool.sys_get_seq10();
			String brchNo = "50001";//网银机构号 
			String txDate = PubTool.getDate8();//平台日期 
			String txTime =  PubTool.getTime();//平台时间
			String tellerNo = "900015";// 	
			String txCode = "GW3001";//交易码
			String chNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].channelNo");
			String reqNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].requestSeqNo");
			String termNO = "EBANK";
			String estwSeq = (String) EPOper.get(tpID, "__PLAT_FLOW.__FLOW_SEQ");

			Integer totalNumber = Integer.parseInt( (String) EPOper.get(tpID, svrReq + "[0].cd[0].totalNumber"));
			Double totalAmount = Double.parseDouble((String) EPOper.get(tpID, svrReq + "[0].cd[0].totalAmount"));
			String contractNo = (String) EPOper.get(tpID, svrReq + "[0].cd[0].contractNo");
			String filePathName = (String) EPOper.get(tpID, svrReq + "[0].cd[0].filePathName");
			SysPub.appLog("INFO", "totalAmount=%f", totalAmount);
			String batNo = String.format("%s%d", platDate, platSeq);

			String szSql1 = "insert into t_jrnl values (?,?,?,?,?,?,'','','',?,?,'',?,?,?,'','','','','','','','','',0,0,'','','','','','','','','','',0,'','','','','','','','',0,'','','','','',0,0,0,0)";
			Object[] value1 = { platDate, platSeq, brchNo, tellerNo, termNO, chNo, estwSeq, txCode, txDate, txTime,
					reqNo };
			DataBaseUtils.execute(szSql1, value1);

			SysPub.appLog("INFO", "预计流水完成！");

			/* 交易报文检查 */

			if (chk_trans(tpID, svrReq, svrRes, totalNumber, totalAmount, contractNo, filePathName) < 0) {

				/* 报文头 */
				EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

				/* 报文体 */

				EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
				/* 更新返回码 */
				String retCode = (String) EPOper.get(tpID, svrRes + "[0].hostReturnCode");
				String retMsg = (String) EPOper.get(tpID, svrRes + "[0].hostErrorMessage");
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] value = { retCode, retMsg, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, value);

				return 0;

			}

			//sftp下载文件
			String[] fileAtt = getFileNameAndPath(filePathName);
			SftpClientUtils.netbankFile(fileAtt[0], fileAtt[1], "1");
			
			/* 上核心查询 */
			SysPub.appLog("INFO", "STEP1 去核心[%s]检查付款账户", "6544");
			
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_002",brchNo);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_003",brchNo);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_005",txDate);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_007",tellerNo);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_010","NCPS");//前台终端号
			
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_016","6544");//交易代码
			EPOper.put(tpID,  "ISO_8583" + "[0].iso_8583_040",Double.parseDouble(AmountUtils.changeY2F(totalAmount.toString())));//发往核心记账需要*100
			EPOper.put(tpID,  "ISO_8583" + "[0].iso_8583_037",contractNo);//
			EPOper.copy(tpID, tpID,  "ISO_8583", "OBJ_ALA_abstarct_REQ[0].req");
			try {
				DtaTool.call("HOST_CLI", "S654400");
			} catch (Exception e) {
				SysPub.appLog("ERROR", "调用核心服务失败");
			}
			EPOper.delete(tpID, "ISO_8583");
			EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res",  "ISO_8583");
			String inAcctNo = (String) EPOper.get(tpID,   "ISO_8583" + "[0].iso_8583_038");//内部账号
			String inAcctName = (String) EPOper.get(tpID,  "ISO_8583" + "[0].iso_8583_026");//客户姓名
			String retCode1 = (String) EPOper.get(tpID,   "ISO_8583[0].iso_8583_012");//响应代码
			String retMsg1 = (String) EPOper.get(tpID,   "ISO_8583" + "[0].iso_8583_013");//响应信息
			if(!"0000".equals(retCode1)){
				/* 报文头 */
				EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

				/* 报文体 */
				EPOper.put(tpID, svrRes + "[0].hostReturnCode", retCode1);
				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", retMsg1);
				
				EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
				/* 更新返回码 */
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] value = { retCode1, retMsg1, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, value);

				return 0;
			}
			/* 更新返回码 */
			String szSqlStr1 = "UPDATE t_jrnl  SET cust_no = ?   WHERE plat_date = ? and seq_no = ? 	";
			Object[] value4 = { contractNo , platDate, platSeq };
			DataBaseUtils.execute(szSqlStr1, value4);
			SysPub.appLog("INFO", "STEP1 去核心[%s]检查完成", "6544");

			/* 登记批次信息表 */
			String szSql2 = "select * from  t_bat_ctl where plat_date = ? and acct_no = ? and tot_num = ? and tot_amt = ?  ";
			Object[] value2 = { platDate, contractNo, totalNumber, totalAmount };
			int ret = DataBaseUtils.queryToCount(szSql2, value2);
			if (ret > 0) {

				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
				String msg = String.format("当天代发记录有两笔代发总笔数与总金额都相等!");
				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
				/* 报文头 */
				EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

				/* 报文体 */
				EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
				String retCode = (String) EPOper.get(tpID, svrRes + "[0].hostReturnCode");
				String retMsg = (String) EPOper.get(tpID, svrRes + "[0].hostErrorMessage");
				
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] value = { retCode, retMsg, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, value);

				return 0;
			}

			SysPub.appLog("INFO", "登记批次信息！[%d]", ret);
			/* 登记批次信息表 */
			String szSql3 = "insert into t_bat_ctl values (?,?,'','X','','','','',?,?,?,'','','','','','',?,?,0,0,0,0,0,?,'','','','','','','',0,'','','','','','')";
			Object[] value3 = { batNo, platDate, contractNo,inAcctNo,inAcctName,totalNumber, totalAmount, filePathName };
			DataBaseUtils.execute(szSql3, value3);

			String fileName = filePathName.substring(filePathName.lastIndexOf("/"));
			if (anlyse_recvfile(batNo, svrReq, svrRes, fileName) < 0) {
				/* 报文头 */
				EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

				/* 报文体 */

				EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
				/* 更新返回码 */
				String retCode = (String) EPOper.get(tpID, svrRes + "[0].hostReturnCode");
				String retMsg = (String) EPOper.get(tpID, svrRes + "[0].hostErrorMessage");
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] value = { retCode, retMsg, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, value);

				return 0;
			}
			
			/* 上核心查询 */
			SysPub.appLog("INFO", "STEP2 去核心[%s] 对公账户下账到内部户", "6545");
			
			EPOper.delete(tpID, "ISO_8583");
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_002",brchNo);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_003",brchNo);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_005",txDate);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_007",tellerNo);
			EPOper.put(tpID,   "ISO_8583[0].iso_8583_010","NCPS");//前台终端号
			
			EPOper.put(tpID,   "ISO_8583" + "[0].iso_8583_016","6545");
			EPOper.put(tpID,   "ISO_8583" + "[0].iso_8583_037",contractNo);
			EPOper.put(tpID,   "ISO_8583" + "[0].iso_8583_040",Double.parseDouble(AmountUtils.changeY2F(totalAmount.toString())));
			EPOper.copy(tpID, tpID,  "ISO_8583", "OBJ_ALA_abstarct_REQ[0].req");
			try {
				DtaTool.call("HOST_CLI", "S654500");
			} catch (Exception e) {
				SysPub.appLog("ERROR", "调用核心服务失败");
			}
			EPOper.delete(tpID, "ISO_8583");
			EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res",  "ISO_8583");
			String retCode2 = (String) EPOper.get(tpID,   "ISO_8583" + "[0].iso_8583_012");//响应代码
			String retMsg2 = (String) EPOper.get(tpID,   "ISO_8583" + "[0].iso_8583_013");//响应信息
			if(!"0000".equals(retCode2)){
				/* 报文头 */
				EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

				/* 报文体 */
				EPOper.put(tpID, svrRes + "[0].hostReturnCode", retCode2);
				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", retMsg2);
				
				EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
				/* 更新返回码 */
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] value = { retCode2, retMsg2, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, value);

				return 0;
			}
			
			/* 报文头 */
			EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "0000");
			String msg = String.format("交易成功");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
			/* 报文体 */
			EPOper.put(tpID, svrRes + "[0].cd[0].batchNo", batNo);

			EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
			/* 更新返回码 */
			String retCode = (String) EPOper.get(tpID, svrRes + "[0].hostReturnCode");
			String retMsg = (String) EPOper.get(tpID, svrRes + "[0].hostErrorMessage");
			String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
			Object[] value = { retCode, retMsg, platDate, platSeq };
			DataBaseUtils.execute(szSqlStr, value);

			SysPub.appLog("INFO", "更新业务状态完成");

			return 0;
		} catch (Exception e) {
			SysPub.appLog("ERROR", "执行 deal_jrnl 方法失败");
			throw e;
		}
	}
	
	/**
	 * 通过绝对路径获取文件路径，文件名
	 * @param fileNamePath
	 * @return
	 * 		fileAtt[0]:文件路径
	 * 		fileAtt[0]:文件名
	 */
	public static String[] getFileNameAndPath(String filePathName) {
		String[] fileAtt = new String[2] ;
		fileAtt[0] = filePathName.substring(0,filePathName.lastIndexOf("/"));
		fileAtt[1] = filePathName.substring(filePathName.lastIndexOf("/")+1);
		return fileAtt ;
	}


}
