package com.adtec.ncps.busi.chnl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.PubTool;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.global.SysDef;
import com.adtec.starring.respool.ResPool;
import com.adtec.starring.struct.dta.DtaInfo;

public class GW3003 {

	/**
	 *	交易流程：
	 *		根据T_BAT_DETAIL 写明细文件
	 *		返回文件名
	 * @return
	 * @throws Exception
	 * @author dengp_w
	 * @date 2018年3月29日 下午4:51:49
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
			String txCode = "GW3003";//交易码
			String chNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].channelNo");
			String reqNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].requestSeqNo");
			String termNO = "EBANK";
			String estwSeq = (String) EPOper.get(tpID, "__PLAT_FLOW.__FLOW_SEQ");

			
			String batNo = (String) EPOper.get(tpID, svrReq + "[0].cd[0].batchNo");
		

			String szSql = "insert into t_jrnl values (?,?,?,?,?,?,'','','',?,?,'',?,?,?,'','','','','','','','','',0,0,'','','','','','','','','','',0,'','','','','','','','',0,'','','','','',0,0,0,0)";
			Object[] value = { platDate, platSeq, brchNo, tellerNo, termNO, chNo, estwSeq, txCode, txDate, txTime,
					reqNo };
			DataBaseUtils.execute(szSql, value);

			SysPub.appLog("INFO", "预计流水完成！");

			String szSql2 = "select * from  t_bat_ctl where bat_no=? and stat >1  ";
			Object[] value2 = { batNo };
			int ret = DataBaseUtils.queryToCount(szSql2, value2);
			if (ret != 1) {

				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
				String msg = String.format("当前批次不能取结果");
				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
				/* 报文头 */
				EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

				/* 报文体 */

				EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
				String retCode = (String) EPOper.get(tpID, svrRes + "[0].hostReturnCode");
				String retMsg = (String) EPOper.get(tpID, svrRes + "[0].hostErrorMessage");
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] valuestr = { retCode, retMsg, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, valuestr);

				return 0;
			}
			/* 登记批次信息表 */
			String szSql3 = "select * from  t_bat_detail where bat_no = ?   ";
			Object[] value3 = { batNo };
			int iret = DataBaseUtils.queryToElem(szSql3,"T_BAT_DETAIL", value3 );
			if (iret <= 0 ) {

				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
				String msg = String.format("该批次存在");
				EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
				/* 报文头 */
				EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");

				/* 报文体 */

				EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
				String retCode = (String) EPOper.get(tpID, svrRes + "[0].hostReturnCode");
				String retMsg = (String) EPOper.get(tpID, svrRes + "[0].hostErrorMessage");
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] valuestr = { retCode, retMsg, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, valuestr);

				return 0;
			}
			
			/****************************************************************************
			文件格式：
			批次号|序列号|收款账号|收款户名|代发金额|状态|记账描述|错误信息|
			****************************************************************************/
			String filePath = SysDef.WORK_DIR + ResPool.configMap.get("FilePath")+"/netbank/";
			String szFileName =  batNo+"dzres_t.txt";
			SysPub.appLog("INFO", "STEP2 解析文件[%s]", szFileName);
			/********************************************t********************************
			 * 文件格式： 序号|转入账号|转入户名|金额|证件类型(可空)|证件号(可空)|代理行(可空)|
			 ****************************************************************************/

			File file = new File(filePath+"/"+szFileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			
			String sline ="";
			FileOutputStream in = new FileOutputStream(file);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(in));

			for(int i =0 ;i<= ret ;i++)
			{	
				Integer seqNo = Integer.parseInt(EPOper.get(tpID, "T_BAT_DETAIL["+i+"].SEQ_NO").toString());
				String acctNo = (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].ACCT_NO");
				String acctName= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].ACCT_NAME");
				Double txAmt= Double.parseDouble( EPOper.get(tpID, "T_BAT_DETAIL["+i+"].TX_AMT").toString());
				String stat= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].STAT");
				String retMsg= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].RET_MSG");
				String retCode= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].RET_CODE");
				sline = String.format("%s|%d|%s|%s|%f|%s|%s|%s\r\n", batNo,seqNo,acctNo,acctName,txAmt,stat,retMsg,retCode);
				SysPub.appLog("INFO", "字符串：[%s]", sline);
			    writer.write(sline);
			}
			writer.close();

			/* 报文头 */
			EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "0000");
			String msg = String.format("交易成功");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
			/* 报文体 */
			EPOper.put(tpID, svrRes + "[0].cd[0].filePathName", szFileName);

			EPOper.copy(tpID, tpID, svrRes, "OBJ_ALA_abstarct_RES[0].res");
			/* 更新返回码 */
			String retCode = (String) EPOper.get(tpID, svrRes + "[0].hostReturnCode");
			String retMsg = (String) EPOper.get(tpID, svrRes + "[0].hostErrorMessage");
			String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
			Object[] valuestr = { retCode, retMsg, platDate, platSeq };
			DataBaseUtils.execute(szSqlStr, valuestr);

			SysPub.appLog("INFO", "更新业务状态完成");

			return 0;
		} catch (Exception e) {
			SysPub.appLog("ERROR", "执行 deal_jrnl 方法失败");
			throw e;
		}
	}

}
