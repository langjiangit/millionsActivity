package com.bus.chelaile.mvc;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.account.AccountInfo;
import com.bus.chelaile.service.PublishDataService;
import com.bus.chelaile.service.ServiceManager;
import com.bus.chelaile.service.StartService;
import com.bus.chelaile.service.StaticService;

@Controller
@RequestMapping("v1")
public class QueryAction extends AbstractController {

	@Resource
	private ServiceManager serviceManager;
	@Resource
	private PublishDataService publishDataService;
	@Resource
	private StartService startService;
	private static final Logger logger = LoggerFactory.getLogger(QueryAction.class);

	/*
	 * 查询活动信息
	 */
	@ResponseBody
	@RequestMapping(value = "homepage.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String homepage(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);

		return serviceManager.queryHomeInfo(param);
	}

	
	
	/*
	 * 房间初始化、查询房间信息
	 */
	@ResponseBody
	@RequestMapping(value = "romeInit.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String romeInit(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);

		return serviceManager.queryRoomInfo(param);
	}

	/*
	 * 查询答题结果，用户的生死
	 */
	@ResponseBody
	@RequestMapping(value = "queryAnswerResult.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String queryAnswerResult(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);
		logger.info("用户查询生死状态, accountId={}, udid={}", param.getAccountId(), param.getUdid());

		return serviceManager.queryAccountInfo(param);
	}

	/*
	 * 发布题目 | 公布答案
	 */
	@ResponseBody
	@RequestMapping(value = "sendAnsAndSub.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String sendAnsAndSub(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);
		if (param.getType() == 0) {
			System.out.println("发布题目, subjectId=" + param.getSubjectId() + ", type=" + param.getType());
			logger.info("发布题目, subjectId={}, type={}", param.getSubjectId(), param.getType());
			return serviceManager.SendQuestion(param);
		} else if (param.getType() == 1) {
			System.out.println("发布答案, subjectId=" + param.getSubjectId() + ", type=" + param.getType());
			logger.info("公布答案, subjectId={}, type={}", param.getSubjectId(), param.getType());
			return serviceManager.SendAnswer(param);
		} else if (param.getType() == 2) {
			logger.info("主持人说话, type={}", param.getType());
			return publishDataService.sendEmceeMsg(getInt(request, "msgType"), param.getContent());
		} else if (param.getType() == 3) {
			logger.info("开始计时, type={}", param.getType());
			return publishDataService.sendBeginMsg();
		} else if (param.getType() == 4) {
			logger.info("公布获奖结果, type={}", param.getType());
			return publishDataService.sendResult(param);
		} else if (param.getType() == 5) {
			logger.info("聊天室消息, type={}", param.getType());
			return publishDataService.sendChatMsg(param.getNickName(), param.getContent());
		} else {
			logger.info("未知类型的type， type={}", param.getType());
			return "";
		}
	}
	@ResponseBody
    @RequestMapping(value = "yuanxiangupdate.action", produces = "Content-Type=text/plain;charset=UTF-8")
    public String yuanxiangupdate(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws Exception {
	    String subjectId=request.getParameter("subjectId");
	    StaticService.SENDED_SUBJECT.remove(Integer.parseInt(subjectId));
	    return "{\"success\":\"00\"}";
    }

	/*
	 * 上传答案
	 */
	@ResponseBody
	@RequestMapping(value = "submitAnswer.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String submitAnswer(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);
//		System.out.println("上传答案, accountId=" + param.getAccountId() + ", subjectId=" + param.getSubjectId()
//				+ ", answer=" + param.getAnswer());
		logger.info("上传答案, accountId={}, udid={}, subjectId={}, answer={}", param.getAccountId(), param.getUdid(),
				param.getSubjectId(), param.getAnswer());

		return serviceManager.answerQuestion(param);
	}

	/*
	 * 查询答题数据 , 运营平台使用
	 */
	@ResponseBody
	@RequestMapping(value = "queryQuestionData.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String queryQuestionData(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		return serviceManager.queryQuestionData();
	}

	/*
	 * 调整数据，运营平台使用
	 */
	@ResponseBody
	@RequestMapping(value = "changeNum.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String changeNum(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		int index = getInt(request, "index");
		int activityId = getInt(request, "activityId");
		int watchingRobot = getInt(request, "watchingRobot");
		int option1Robot = getInt(request, "option1Robot");
		int option2Robot = getInt(request, "option2Robot");
		int option3Robot = getInt(request, "option3Robot");
		int notAnsRobot = getInt(request, "notAnsRobot");
		int reLivingRobot = getInt(request, "reLivingRobot");

		logger.info("调整答题总数据, activityId={}", activityId);

		return serviceManager.changeRobotNum(index, activityId, watchingRobot, option1Robot, option2Robot,
				option3Robot, notAnsRobot, reLivingRobot);
	}

	/*
	 * 填写邀请码
	 */
	@ResponseBody
	@RequestMapping(value = "fillInCode.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String fillInCode(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);
		logger.info("填写邀请码, accountId={}, inviteCode={}", param.getAccountId(), param.getInviteCode());

		return serviceManager.fillInCode(param);
	}
	
	/*
	 * reloadData
	 */
	@ResponseBody
	@RequestMapping(value = "reloadData.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String reloadData(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		logger.info("reload *******************************");
		System.out.println("reload *******************************");

		startService.init();
		
		return serviceManager.getClienSucMap("", "00");
	}
	
	
	/*
	 * for TEST 
	 */
	@ResponseBody
	@RequestMapping(value = "setCode.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String setCode(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);
		int codeNum = getInt(request, "codeNum");
		logger.info("设置复活码数目：accountId={}, codeNum={}", param.getAccountId(), codeNum);
		
		AccountInfo accountInfo = QuestionCache.getAccountInfo(param.getAccountId(), true);
		accountInfo.setCardNum(codeNum);
		QuestionCache.updateAccountInfo(param.getAccountId(), accountInfo);
		
		return serviceManager.getClienSucMap(new JSONObject(), "00");
	}
	
	/*
	 * 用户查询信息（主机器，有写操作）
	 */
	@ResponseBody
	@RequestMapping(value = "privateinfo.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String privateinfo(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {
		QuestionParam param = getActionParam(request);
		return serviceManager.getAccountInfo(param);
	}
	
	
	@ResponseBody
	@RequestMapping(value = "qPrivateinfo.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String qPrivateinfo(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {
		QuestionParam param = getActionParam(request);
		return serviceManager.getQAccountInfo(param);
	}
	
	/**
	 * 设置第一题的可答题总人数
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "changeFirstAnswerNum.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String changeFirstAnswerNum(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {
		int totalAnswerNum = getInt(request, "firstAnswerNum");
		return serviceManager.changeFirstAnswerNum(totalAnswerNum);
	}
	
	// 修改在线人数
	@ResponseBody
	@RequestMapping(value = "changeOnlineNum.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String changeOnlineNum(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {
		int totalAnswerNum = getInt(request, "onlineNum");
		return serviceManager.changeOnlineNum(totalAnswerNum);
	}
	
	// 查询真实在线人数
	@ResponseBody
	@RequestMapping(value = "queryOnlineNum.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String queryOnlineNum(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {
		return serviceManager.getOnlineNum();
	}
	
	/*
	 * 车来了宣布活动无效！
	 */
	@ResponseBody
	@RequestMapping(value = "finish.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String finish(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		logger.info("车来了宣布活动无效！！ ");

		return publishDataService.sendFinishMsg();
	}
	
	/*
	 * 车来了宣布活动恢复！
	 */
	@ResponseBody
	@RequestMapping(value = "restart.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String restart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		logger.info("车来了宣布活动归来！！ ");

		return publishDataService.sendRestartMsg();
	}
	

	/*
	 * test
	 */
	@ResponseBody
	@RequestMapping(value = "test.action", produces = "Content-Type=text/plain;charset=UTF-8")
	public String getAllReservation(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws Exception {

		QuestionParam param = getActionParam(request);

		return "Hello ,第一步完成 !" + JSONObject.toJSONString(param);
	}
}
