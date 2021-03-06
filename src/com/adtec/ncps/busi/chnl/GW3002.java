package com.adtec.ncps.busi.chnl;

import com.adtec.ncps.DtaTool;
import com.adtec.ncps.busi.ncp.AmountUtils;
import com.adtec.ncps.busi.ncp.DataBaseUtils;
import com.adtec.ncps.busi.ncp.PubTool;
import com.adtec.ncps.busi.ncp.SysPub;
import com.adtec.starring.datapool.EPOper;
import com.adtec.starring.log.TrcLog;
import com.adtec.starring.struct.dta.DtaInfo;

/**
 * GW3002代发工资数据上传清算
 * @author Administrator
 *
 */
public class GW3002 {

	/*
	 * 循环处理初始状态为0的批次，一次处理一个批次
	 * 
	 * 
	 */

	public static int auto_bat() throws Exception {
		SysPub.appLog("INFO", "方法：auto_bat	开始");
		try {
			DtaInfo dtaInfo = DtaInfo.getInstance();
			String tpID = dtaInfo.getTpId();
			
			
			/* 登记流水 */
			String platDate = PubTool.getDate8();
			// String strTime = PubTool.getTime();
			int platSeq = PubTool.sys_get_seq10();
			String strSeq = String.valueOf(platSeq);
			String brchNo = "50001";//网银机构号 
			String txDate = PubTool.getDate8();//平台日期 
			String txTime =  PubTool.getTime();//平台时间
			String tellerNo = "900015";// 	
			String txCode = "GW3002a";//交易码自动任务
			String chNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].channelNo");
			String reqNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].requestSeqNo");
			String termNO = "EBANK";
			String[] pubData = {strSeq,brchNo,txDate,txTime,tellerNo,txCode,chNo,reqNo,termNO};
			String estwSeq = (String) EPOper.get(tpID, "__PLAT_FLOW.__FLOW_SEQ");
			
			String szSql0 = "insert into t_jrnl values (?,?,?,?,?,?,'','','',?,?,'',?,?,?,'','','','','','','','','',0,0,'','','','','','','','','','',0,'','','','','','','','',0,'','','','','',0,0,0,0)";
			Object[] value0 = { platDate, platSeq, brchNo, tellerNo, termNO, chNo, estwSeq, txCode, txDate, txTime,
					reqNo };
			DataBaseUtils.execute(szSql0, value0);
			SysPub.appLog("INFO", "预计流水完成！");
			
			String szSql = "select * from  t_bat_ctl where  stat ='0'  ";

			int ret = DataBaseUtils.queryToElem(szSql, "T_BAT_CTL", null);
			if (ret <= 0) {
				return 0;
			}
			String batNo =  (String) EPOper.get(tpID, "T_BAT_CTL[0].BAT_NO");
			String inAcctNo= (String) EPOper.get(tpID, "T_BAT_CTL[0].IN_ACCT_NO");
			/*修改状态为正在处理*/
			/* 代发工资状态 stat  0:未代发 1:正在代发 2:成功 3:失败 */
			String szSql1 = "update  t_bat_ctl set stat= '1' where  stat ='0' and bat_no =? ";
			Object[] value1 = { batNo };
			DataBaseUtils.execute(szSql1, value1);
			/*循环单笔处理明细记录*/
			String szSql3 = "select * from  t_bat_detail where bat_no = ? and stat <> '2' order by seq_no  ";
			Object[] value3 = { batNo };
			int iret = DataBaseUtils.queryToElem(szSql3,"T_BAT_DETAIL", value3 );
			if (iret <= 0 ) {
				SysPub.appLog("ERROR", "批次[%s]T_BAT_DETAIL无记录",batNo);
				return 0;
			}
			
			String ISO_8583 = "ISO_8583";
			
			SysPub.appLog("INFO", "方法：auto_bat	循环查询T_BAT_DETAIL表开始");
			SysPub.appLog("INFO", "方法：auto_bat	iret"+iret);
			for(int i =0 ;i< iret ;i++)
			{
				Integer seqNo = Integer.parseInt(String.valueOf( EPOper.get(tpID, "T_BAT_DETAIL["+i+"].SEQ_NO")));
				String acctNo = (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].ACCT_NO");
				String acctName= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].ACCT_NAME");
				Double txAmt= Double.parseDouble( EPOper.get(tpID, "T_BAT_DETAIL["+i+"].TX_AMT").toString());
				String platDate1= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].PLAT_DATE");
				String reserve2= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].RESERVE2");
				String listNo= (String) EPOper.get(tpID, "T_BAT_DETAIL["+i+"].LIST_NO");
				
				
				SysPub.appLog("INFO", "方法：auto_bat	调[6546]服务");
				/* 上核心查询 */
				SysPub.appLog("INFO", "STEP1 去核心[%s]网银从内部账户转入个人户", "6546");
				
				EPOper.delete(tpID, ISO_8583);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_002",brchNo);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_003",brchNo);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_005",txDate);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_007",tellerNo);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_010","NCPS");//前台终端号
				
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_016","6546");//交易代码
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_037",inAcctNo);//付款账号（内部账号）
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_038",acctNo);//收款人账号
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_026",acctName);//收款人名字
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_052",strSeq);//平台流水
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_044",platDate1);//平台日期
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_028",seqNo);//明细序列号
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_040",Double.parseDouble(AmountUtils.changeY2F(txAmt.toString())));//金额
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_081",reserve2);//短信内容
				EPOper.copy(tpID, tpID, ISO_8583, "OBJ_ALA_abstarct_REQ[0].req");
				
				try {
					DtaTool.call("HOST_CLI", "S654600");
				} catch (Exception e) {
					SysPub.appLog("ERROR", "调用核心服务失败");
				}
				EPOper.delete(tpID, ISO_8583);
				EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res", ISO_8583);
				String hostReqNo = (String) EPOper.get(tpID,  ISO_8583 + "[0].iso_8583_004");//核心流水
				String retCode = (String) EPOper.get(tpID,  ISO_8583 + "[0].iso_8583_012");//响应代码
				String retMsg = (String) EPOper.get(tpID,  ISO_8583 + "[0].iso_8583_013");//响应信息
				
				SysPub.appLog("INFO", "方法：auto_bat	调[6546]服务完成");
				/***
				 * 1记流水，更新返回T_BAT_DETAIL表，
				 * 2。登记核心序列号
				 */
				String stat = "2";//代发工资状态 stat  0:未代发 1:正在代发 2:成功 3:失败 
				if(!"0000".equals(retCode)){
					stat = "3";
				}
				String szSqlStr2 = "UPDATE T_BAT_DETAIL  SET host_seq_no = ?,ret_code=?,ret_msg = ? ,stat = ?where bat_no = ? and stat <> '2' and seq_no = ?	";
				Object[] value2 = { hostReqNo, retCode, retMsg,stat, batNo ,seqNo};
				DataBaseUtils.execute(szSqlStr2, value2);
			}
			SysPub.appLog("INFO", "方法：auto_bat	循环查询T_BAT_DETAIL表结束");
			
			/*统计成功失败笔数*/
			
			/*更新批次状态*/
			String szSql4 = "select count(*) as fail_num,sum(tx_amt) as fail_amt from t_bat_detail where bat_no = ? and stat ='3'";
			Object[] value4 = { batNo };
			DataBaseUtils.queryToElem(szSql4,"T_BAT_CTL", value4 );
			
			Integer failNum = Integer.parseInt(String.valueOf( EPOper.get(tpID, "T_BAT_CTL[0].FAIL_NUM")));
			Double failAmt= failNum == 0 ? 0 :Double.parseDouble(String.valueOf(  EPOper.get(tpID, "T_BAT_CTL[0].FAIL_AMT")));
			String stat= "2";//状态
				
			String acctNo_CTL =  (String) EPOper.get(tpID, "T_BAT_CTL[0].ACCT_NO");
			String acctName_CTL =  (String) EPOper.get(tpID, "T_BAT_CTL[0].IN_ACCT_NAME");
			String inAcctNo_CTL =  (String) EPOper.get(tpID, "T_BAT_CTL[0].IN_ACCT_NO");
			
			/* 上核心查询 */
				SysPub.appLog("INFO", "STEP1 去核心[%s]退款", "6546");
				
				EPOper.delete(tpID, ISO_8583);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_002",brchNo);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_003",brchNo);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_005",txDate);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_007",tellerNo);
				EPOper.put(tpID,   "ISO_8583[0].iso_8583_010","NCPS");//前台终端号
			
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_016","6546");//交易代码 EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_037",inAcctNo_CTL);//付款人账号(内部账号)
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_038",acctNo_CTL);//收款人账号（对公账号）
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_037",inAcctNo_CTL);//收款人账号（对公内部账号）
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_026",acctName_CTL);//收款人名字
				EPOper.put(tpID,  ISO_8583 + "[0].iso_8583_040",Double.parseDouble(AmountUtils.changeY2F(failAmt.toString())));//金额
				EPOper.copy(tpID, tpID, ISO_8583, "OBJ_ALA_abstarct_REQ[0].req");
	
				try {
					DtaTool.call("HOST_CLI", "S654600");
				} catch (Exception e) {
					SysPub.appLog("ERROR", "调用核心服务失败");
				}
				
				EPOper.delete(tpID, ISO_8583);
				EPOper.copy(tpID, tpID, "OBJ_ALA_abstarct_RES[0].res", ISO_8583);
				String hostReqNo = (String) EPOper.get(tpID,  ISO_8583 + "[0].iso_8583_004");//核心流水
				String retCode = (String) EPOper.get(tpID,  ISO_8583 + "[0].iso_8583_012");//响应代码
				String retMsg = (String) EPOper.get(tpID,  ISO_8583 + "[0].iso_8583_013");//响应信息
			if(!"0000".equals(retCode)){
				SysPub.appLog("ERROR", "核心退款失败[%s][%s]",retCode,retMsg);
				
				/* 更新返回码 */
				String szSqlStr = "UPDATE t_jrnl  SET ret_code=?, ret_msg =?  WHERE plat_date = ? and seq_no = ? 	";
				Object[] value = { retCode, retMsg, platDate, platSeq };
				DataBaseUtils.execute(szSqlStr, value);
				return -1;
			}
			
			
			/*
			 * 1记流水，更新返回T_BAT_CTL表TK相关字段
			 */
			String szSql5 = "select count(*) as succ_num,sum(tx_amt) as succ_amt from t_bat_detail where bat_no = ? and stat ='2'";
			Object[] value5 = { batNo };
			DataBaseUtils.queryToElem(szSql5,"T_BAT_CTL", value5 );
			
			Integer succNum = Integer.parseInt(String.valueOf(EPOper.get(tpID, "T_BAT_CTL[0].SUCC_NUM")));
			Double succAmt= Double.parseDouble(String.valueOf(EPOper.get(tpID, "T_BAT_CTL[0].SUCC_AMT")));
			
			/*更新批次状态*/
			String szSql2 = "update  t_bat_ctl set fail_num = ? ,fail_amt =? ,succ_num =? ,succ_amt=?,stat =? where  stat ='1' and bat_no =? ";
			Object[] value2 = { failNum,failAmt,succNum,succAmt ,stat , batNo};
			DataBaseUtils.execute(szSql2, value2);

		} catch (Exception e) {
			SysPub.appLog("ERROR", "执行 auto_bat 方法失败");
			throw e;
		}

		return 0;
	}

	/**
	 *	交易流程：
	 *		
	 * @return
	 * @throws Exception
	 * @author dengp_w
	 * @date 2018年3月29日 下午4:46:36
	 */
	public static int deal_trans() throws Exception {
		try {
			//test
//			int auto_bat = auto_bat();
//			if(auto_bat == 0 || auto_bat == -1){
//				SysPub.appLog("INFO", "执行 auto_bat 方法完成");
//				return 0;
//			}
			
			
			
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
			String txCode = "GW3002";//交易码
			String chNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].channelNo");
			String reqNo = "";//(String) EPOper.get(tpID, svrReq + "[0].cd[0].requestSeqNo");
			String termNO = "EBANK";
			String estwSeq = (String) EPOper.get(tpID, "__PLAT_FLOW.__FLOW_SEQ");
			

			String batNo = (String) EPOper.get(tpID, svrReq + "[0].cd[0].batchNo");

			String szSql1 = "insert into t_jrnl values (?,?,?,?,?,?,'','','',?,?,'',?,?,?,'','','','','','','','','',0,0,'','','','','','','','','','',0,'','','','','','','','',0,'','','','','',0,0,0,0)";
			Object[] value1 = { platDate, platSeq, brchNo, tellerNo, termNO, chNo, estwSeq, txCode, txDate, txTime,
					reqNo };
			DataBaseUtils.execute(szSql1, value1);

			SysPub.appLog("INFO", "预计流水完成！");

			/* 登记批次信息表 */
			String szSql2 = "select * from  t_bat_ctl where bat_no = ? and stat ='X' ";
			Object[] value2 = { batNo };
			int ret = DataBaseUtils.queryToCount(szSql2, value2);
			if (ret != 1) {

				EPOper.put(tpID, svrRes + "[0].hostReturnCode", "9999");
				String msg = String.format("该批次不能触发，请查看批次状态");
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
			String szSql3 = "update t_bat_ctl set stat ='0' where bat_no =? ";
			Object[] value3 = { batNo };
			DataBaseUtils.execute(szSql3, value3);

			/* 报文头 */
			EPOper.copy(tpID, tpID, svrReq + "[0].tx_code", svrRes + "[0].tx_code");
			EPOper.put(tpID, svrRes + "[0].hostReturnCode", "0000");
			String msg = String.format("交易成功");
			EPOper.put(tpID, svrRes + "[0].hostErrorMessage", msg);
			/* 报文体 */
//			EPOper.put(tpID, svrRes + "[0].cd[0].batchNo", batNo);

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

}
