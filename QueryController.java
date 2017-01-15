package com.xiao4r.web.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jfinal.core.Controller;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Commons;
import com.xiao4r.consts.Const;
import com.xiao4r.util.biz.BasePage;
import com.xiao4r.util.domain.User;
import com.xiao4r.util.tools.HttpHelper;
import com.xiao4r.utils.CommonUtils;
import com.xiao4r.utils.MyCaptchaRender;
import com.xiao4r.web.cache.TrafficCache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QueryController extends BasePage
{
	private static Logger log = LogManager.getLogger(QueryController.class);
	
	private final static String service_name="publicQueryService2/";
	
	private final static String FORM_KEY_FUND = "fund";
	private final static String FORM_KEY_TRAFFIC = "traffic";
   /**
	* 
	* @Title: breakRule 
	* @Description: 交通违章查询 
	* @param     
	* @return void 
	* @throwspages 
	*/
	/*public void breakRule(){
    	boolean temp = CommonUtils.isMobileDevice(getRequest());
    	if(StringUtils.isEmpty(getPara("carType"))){
		    setAttr("cars",Const.cars);
		    if(temp){//wap端
		    	setAttr("mobile", getPara(1));
				render("/wap/query_vio_traffic_rule.html");
		    }else{
		        render("/pc/query/breakRule.html");
		    }
    	}else{
			Map<Object, Object> params=new HashMap<Object, Object>();
			try {
				User usr=this.getCurrentUser();
				params.put("carType",URLEncoder.encode(getPara("carType"), "UTF-8"));
				params.put("carLicenseNumber",URLEncoder.encode(getPara("carLicenseNumber"), "UTF-8"));
		        params.put("carID", URLEncoder.encode(getPara("carID"), "UTF-8"));
				params.put("mobileForToken", URLEncoder.encode(getPara("mobileForToken"), "UTF-8"));
		        params.put("smsToken", URLEncoder.encode(getPara("smsToken"), "UTF-8"));
		        params.put("userid", usr.getUserId());
			} catch (Exception e) {
				// TODO: handle exception
			}
	        String param=CommonUtils.Splicing("getTrafficViolationOfJSON",params);
			String info=CommonUtils.doGet(Const.HTTP_URL+service_name+param);
			setAttr("title","查询结果" );
			//setAttr("backUrl", "breakRule");
			if(StringUtils.isEmpty(info)||info.contains("empty")){
				setAttr("message", "您目前没有违章记录或信息输入错误!<br/>向遵纪守法的交通参与者致敬");
				if(temp){//wap端
					render("/wap/query_result.html");
				}else{
				    render("/pc/query/result.html");
				}
			}else{
				List<Map<String, String>> list=CommonUtils.toMapMany2(info);
				if(info.contains("error")){
					setAttr("message", "对不起，程序报错了！");
					if (list != null && list.size() > 0 && StringUtils.isNotEmpty(list.get(0).get("errMsg"))) {
						setAttr("message", list.get(0).get("errMsg"));
					}
					
					if(temp){//wap端
						render("/wap/query_result.html");
					}else{
					    render("/pc/query/result.html");
					}
				}else if("NO".equals(list.get(0).get("flag"))){
					setAttr("message", list.get(0).get("message"));
					if(temp){//wap端
						render("/wap/query_result.html");
					}else{
					    render("/pc/query/result.html");
					}
				}else{
					setAttr("list",list);
					if(temp){//wap端
						render("/wap/query_vio_traffic_rule_result.html");
				    }else{
				        render("/pc/query/breakRule.html");
				    }
				}
			}
			
    	}
        
	}*/
	
	public void breakRule(){
		boolean temp = CommonUtils.isMobileDevice(getRequest());
		if( !temp ){
			renderText("对不起，该服务暂时关闭！");
	    }else{ //wap端
			//String cityInfo=CommonUtils.doGet(Const.HTTP_URL+service_name+"getSupportCityInfo");
	    	String cityInfo = TrafficCache.getInstance().getCityInfo();
			Map<String, Object> map = JSON.parseObject(cityInfo);
			if ( "error".equals(map.get("flag")) ){
				render("/error/500.html");
				return;
			}
			if ( !"200".equals(map.get("resultcode")) ){
				setAttr("title", "交通违章查询");
				setAttr("message", map.get("reason")); 
				render("/error/messages.html");  
				return;
			}
			Map<String, Object> result = (Map<String, Object>) map.get("result");
			setAttr("result", result);
			setAttr("cars",Const.cars);
			setAttr("provinceCodes", Const.provinceCodes);
			setAttr("abbrs", Const.abbrs);
			setAttr("mobile", getPara(1));
			
			if ( this.getCurrentUser()!=null ){
    			int flag = 0;
    			Map<Object, Object> params=new HashMap<Object, Object>();
    			params=new HashMap<Object, Object>();
    			params.put("userId", this.getCurrentUser().getUserId());
    			params.put("formKey", FORM_KEY_TRAFFIC);
    			params.put("flag", flag);
    			String param=CommonUtils.Splicing("formHistory",params);
    	    	String formHistory = CommonUtils.doGet(Const.HTTP_URL+service_name+param);
    	    	List<Object> l = JSON.parseArray(formHistory);
    	    	setAttr("userNos", l);
    		}
			
			render("/wap/query_vio_traffic_rule_new.html");  
	    }
	}
	
	public void queryTrafficVio(){
		String safecode = getPara("safecode");
		if( ! MyCaptchaRender.validate(this, safecode) ){
			setAttr("message", "验证码输入错误！");
		    render("/wap/query_result.html");
		    return;
		}
	    Map<Object, Object> params=new HashMap<Object, Object>();
		try {
			params.put("userid", this.getCurrentUser().getUserId());
			params.put("city", getPara("city"));
			params.put("hphm", URLEncoder.encode(getPara("licenseNumber"), "UTF-8"));
			params.put("hpzl", getPara("carType"));
	        params.put("engineno", getPara("engineno"));
	        params.put("classno",  getPara("classno"));
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			render("/error/500.html");
			return;
		}
		String trafficVioInfo=CommonUtils.doGet(Const.HTTP_URL + service_name 
				+ CommonUtils.Splicing("getTrafficVioInfo", params) );
		Map<String, Object> map = JSON.parseObject(trafficVioInfo);
		if ( "error".equals(map.get("flag")) ){
			render("/error/500.html");
			return;
		}
		if ( !"200".equals(map.get("resultcode")) ){
			setAttr("title", "交通违章查询");
			setAttr("message", map.get("reason")); 
			render("/error/messages.html");  
			return;
		}
		
		int flag = 1;
		params=new HashMap<Object, Object>();
		params.put("userId", this.getCurrentUser().getUserId());
		params.put("formKey", FORM_KEY_TRAFFIC);
		params.put("inputValueMaster", getPara("licenseNumber").substring(2));
		params.put("inputValueSlaves", getPara("engineno") + "%7C%7C" + getPara("classno"));
		params.put("flag", flag);
		String param=CommonUtils.Splicing("formHistory",params);
    	CommonUtils.doGet(Const.HTTP_URL+service_name+param);
		
		Map<String, Object> result = (Map<String, Object>) map.get("result");
		List<Map<String, String>> lists = (List<Map<String, String>>) result.get("lists");
		if ( lists.size() == 0 ){
			setAttr("title", "交通违章查询");
			setAttr("message", "您目前没有违章记录"); 
			render("/error/messages.html");  
			return;
		}
		setAttr("list",lists);
		render("/wap/query_vio_traffic_rule_result_new.html");
	}
	
   /**
    * 
    * @Title: exam 
    * @Description: 人事考试查询
    * @param     
    * @return void 
    * @throws
    */
    public void exam(){
    	//640102198312190012
    	boolean temp = CommonUtils.isMobileDevice(getRequest());
    	if(StringUtils.isNotEmpty(getPara("year"))){
    		Map<Object, Object> params=new HashMap<Object, Object>();
    		try {
				params.put("year",URLEncoder.encode(getPara("year"), "UTF-8"));
				params.put("examType", URLEncoder.encode(getPara("examType"), "UTF-8"));
		        params.put("name", URLEncoder.encode(getPara("name"), "UTF-8"));
		        params.put("IDType", URLEncoder.encode(getPara("IDType"), "UTF-8"));
		        params.put("IDNumber", URLEncoder.encode(getPara("IDNumber"), "UTF-8"));
	    	
			} catch (Exception e) {
				// TODO: handle exception
			}
	        String param=CommonUtils.Splicing("getExamResultOfJSON",params);
			String info=CommonUtils.doGet(Const.HTTP_URL+service_name+param);
			setAttr("title","查询结果" );
			//setAttr("backUrl", "exam");
			if(StringUtils.isEmpty(info)){
				setAttr("message", "对不起，您的考试信息不存在！");
				if(temp){//wap端
					render("/wap/query_result.html");
				}else{
				    render("/pc/query/result.html");
				}
			}else{
				if(info.contains("error")){
					setAttr("message", "对不起，程序报错了！");
					if(temp){//wap端
						render("/wap/query_result.html");
					}else{
					    render("/pc/query/result.html");
					}
				}else{
					setAttr("message",CommonUtils.toMapMany2(info));
					if(temp){//wap端
						render("/wap/query_exam_result.html");
					}else{
					    render("/pc/query/exam.html");
					}
				}
			}
    	}else{
    		String info=CommonUtils.doGet(Const.HTTP_URL+service_name+"getExamListOfJSON?year=2016");
    		if(info.indexOf("[")==-1){
        		int count = info.length();
        		info=info.substring(7, count-1);
        		info= "["+info+"]";
        	}
    		setAttr("types",CommonUtils.toMapMany2(info));
    		if(temp){//wap端
    			setAttr("mobile", getPara(1));
				render("/wap/query_exam.html");
		    }else{
		        render("/pc/query/exam.html");
		    }
    	}
    	
	}
    /**
     * 
     * @Title: gaokao 
     * @Description: 高考成绩查询
     * @param     
     * @return void 
     * @throws
     */
     public void gaokao(){
     	if(StringUtils.isNotEmpty(getPara("cardCode"))){
     		setAttr("title","查询结果" );
 			//setAttr("backUrl", "gaokao");
     		Date now=new Date();
     		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
     		try {
				Date begin_date=sdf.parse("2015-6-20 03:00");
				Date end_date=sdf.parse("2015-09-28 18:00");
				if(begin_date.after(now)||end_date.before(now)){
					setAttr("message", "对不起，还不是查询的时候！");
					render("/pc/query/result.html");
					return;
				}
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				log.error(e1);
			}
     		
     		Map<Object, Object> params=new HashMap<Object, Object>();
     		try {
 		        params.put("cardCode", URLEncoder.encode(getPara("cardCode"), "UTF-8"));
 		        params.put("IDNumber", URLEncoder.encode(getPara("IDNumber"), "UTF-8"));
 	    	} catch (Exception e) {
 				// TODO: handle exception
 			}
 	        String param=CommonUtils.Splicing("getNCEEResult",params);
 			String info=CommonUtils.doGet(Const.HTTP_URL+service_name+param);
 			/*if(StringUtils.isEmpty(info) || info.contains("error")){
				setAttr("message", "对不起，没有查询到您的信息，请确认您的准考证号，身份证号！");
				render("/pc/query/result.html");
			}else{
				setAttr("message",CommonUtils.toMapMany2(info));
				render("/pc/query/gaokao.html");
			}*/
 			setAttr("message", info);
			render("/pc/query/result.html");
 		}else{
     		render("/pc/query/gaokao.html");
     	}
     	
 	}
     
     public void expr()
     {
    	 this.render("/wap/kuaidi.html");
     }
     public void express(){

    	 String num = this.getPara("num");
    	 String info = null;
    	 if(StringUtils.isNotEmpty(num))
    	 {
	    	 try
	 		 {
		 		String url = "http://m.kuaidi100.com/autonumber/auto?num="+num;
		 		info = HttpHelper.sendGet(url, 10);
		 		log.info(info);
		 		JSONArray arr = JSONArray.fromObject(info);
		 		String comCode = arr.getJSONObject(0).getString("comCode");
		 		url = "http://m.kuaidi100.com/query?type="+comCode+"&postid="+num+"&id=1&valicode=&temp=0.0016236775270264125";
		 		info = HttpHelper.sendGet(url, 10);
		 		log.info(info);
	 		}
	 		catch(Exception ex)
	 		{
	 			log.error(ex);
	 		}
    	 }
    	 this.renderText(info);
 	}
    /**
     * 
    * @Title: ExamList 
    * @Description: 根据年份查询对应的考试专业 
    * @param     
    * @return void 
    * @throws
     */
    public void ExamList(){
    	String info=CommonUtils.doGet(Const.HTTP_URL+service_name+"getExamListOfJSON?year="+getPara("year"));
    	if(info.indexOf("[")!=-1){
    		renderJson(info);
    	}else {
    		int count = info.length();
    		info=info.substring(7, count-1);
    		info= "["+info+"]";
    		renderJson(info);
    	}
    }
   /**
    * 
    * @Title: pages 
    * @Description: 办证查询 
    * @param     
    * @return void 
    * @throws
    */
    public void pages(){
    	boolean temp = CommonUtils.isMobileDevice(getRequest());
    	if(StringUtils.isNotEmpty(getPara("userNo"))){
    	   String info=CommonUtils.doGet(Const.HTTP_URL+service_name+"getApplicationResultOfJSON?applicationID="+getPara("userNo"));
    	   setAttr("title","查询结果" );
		   //setAttr("backUrl", "pages");
			
		if(info.contains("error")){
			setAttr("message", "对不起，程序报错了！");
			if(temp){//wap端
				render("/wap/query_result.html");
			}else{
			    render("/pc/query/result.html");
			}
		}else{
			Map<String, String> list=CommonUtils.toMapOne2(info);
			//{"dsdealdate":"","disfinishdt":"","finishstate":"","proname":"","factfinishdate":"","dsno":"","deptname":""}
			if(StringUtils.isEmpty(list.get("deptname"))&&StringUtils.isEmpty(list.get("dsno"))&&StringUtils.isEmpty(list.get("proname"))){
				setAttr("message", "对不起，没有查询到您的办证记录！<br/>请核实一下您的查询条件");
				if(temp){//wap端
					render("/wap/query_result.html");
				}else{
				    render("/pc/query/result.html");
				}
				return;
			}
			setAttr("list",list);
			setAttr("flag","flag");
			if(temp){//wap端
				render("/wap/query_submission_result.html");
		    }else{
		        render("/pc/query/pages.html");
		    }
		}
			
    	}else{
    		if(temp){//wap端
    			setAttr("mobile", getPara(1));
				render("/wap/query_submission.html");
		    }else{
		        render("/pc/query/pages.html");
		    }
    	}
	}
   /**
    * 
    * @Title: fund 
    * @Description: 公积金查询 
    * @param     
    * @return void 
    * @throws
    */
    public void fund(){
    	boolean temp = CommonUtils.isMobileDevice(getRequest());
    	String idCard=getPara("idCard");
    	String phoneNumber=getPara("phoneNumber");
    	if(CommonUtils.isNotEmpty(idCard,phoneNumber)){
    		Map<Object, Object> params=new HashMap<Object, Object>();
    		params.put("idCard", idCard);
    		params.put("phoneNumber", phoneNumber);
	        String param=CommonUtils.Splicing("newAccumulationFundQuery",params);
	    	String info=CommonUtils.doGet(Const.HTTP_URL+service_name+param);
	    	setAttr("title","查询结果" );
			if(StringUtils.isEmpty(info)
					||info.contains("error")
					||info.contains("empty")){
				setAttr("message", "对不起，没有查询到您的公积金账户！<br/>请核实一下您的查询条件");
				if(temp){//wap端
					render("/wap/query_result.html");
				}else{
				    render("/pc/query/result.html");
				}
			}else{
				if(!info.contains(idCard)){
					setAttr("message","没有"+idCard+"的公积金账户信息,请核实一下查询信息." );
					if(temp){//wap端
						render("/wap/query_result.html");
					}else{
					    render("/pc/query/result.html");
					}
					return;
				}
				List<Map<String, String>> list=CommonUtils.toMapMany2(info);
				int flag = 1;
				params=new HashMap<Object, Object>();
				params.put("userId", this.getCurrentUser().getUserId());
				params.put("formKey", FORM_KEY_FUND);
				params.put("inputValueMaster", getPara("idCard"));
				params.put("flag", flag);
				param=CommonUtils.Splicing("formHistory",params);
		    	CommonUtils.doGet(Const.HTTP_URL+service_name+param);
				setAttr("list",list);
				if(temp){//wap端
					render("/wap/query_collective_reserve_fund_detail.html");
			    }else{
			        render("/pc/query/fund.html");
			    }
			}
    	}else{
    		if (this.getCurrentUser()!=null ){
    			int flag = 0;
    			Map<Object, Object> params=new HashMap<Object, Object>();
    			params=new HashMap<Object, Object>();
    			params.put("userId", this.getCurrentUser().getUserId());
    			params.put("formKey", FORM_KEY_FUND);
    			params.put("flag", flag);
    			String param=CommonUtils.Splicing("formHistory",params);
    	    	String formHistory = CommonUtils.doGet(Const.HTTP_URL+service_name+param);
    	    	List<Object> l = JSON.parseArray(formHistory);
    	    	setAttr("userNos", l);
    		}
    		
//    		String info=CommonUtils.doGet(Const.HTTP_URL+service_name+"fundCityList");
//    		setAttr("citys", CommonUtils.toMapMany2(info));
    		if(temp){//wap端
    			setAttr("mobile", getPara(1));
				render("/wap/query_collective_reserve_fund.html");
		    }else{
		        render("/pc/query/fund.html");
		    }
    	}
	}
    /**
     * 
    * @Title: nxTravel 
    * @Description:宁
    * @param     
    * @return void 
    * @throws
     */
    public void nxTravel(){
    	 render("/wap/lvyou.html");
    }
}
