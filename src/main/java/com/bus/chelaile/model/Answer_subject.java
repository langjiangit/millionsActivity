package com.bus.chelaile.model;

/**
 * 题库
 * @author quekunkun
 *
 */
public class Answer_subject {
    private Integer id;

    private Integer activityId;

    private Integer orderNo;

    private String subject;

    private String option;

    private Integer answer;	 // 正确答案

    private Integer level;

    private Integer status;

    private String updataTime;
    
    
    // 程序生成的
    private long effectStartTime;
    
    private long effectEndTime;
    
    private Integer realOrder; // 题序

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public Integer getAnswer() {
        return answer;
    }

    public void setAnswer(Integer answer) {
        this.answer = answer;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUpdataTime() {
        return updataTime;
    }

    public void setUpdataTime(String updataTime) {
        this.updataTime = updataTime;
    }

	public long getEffectStartTime() {
		return effectStartTime;
	}

	public void setEffectStartTime(long effectStartTime) {
		this.effectStartTime = effectStartTime;
	}

	public long getEffectEndTime() {
		return effectEndTime;
	}

	public void setEffectEndTime(long effectEndTime) {
		this.effectEndTime = effectEndTime;
	}

	public Integer getRealOrder() {
		return realOrder;
	}

	public void setRealOrder(Integer realOrder) {
		this.realOrder = realOrder;
	}
}