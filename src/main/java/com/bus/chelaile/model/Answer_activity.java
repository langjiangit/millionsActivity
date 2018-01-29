package com.bus.chelaile.model;


/**
 * 活动库
 * @author quekunkun
 *
 */
public class Answer_activity {
    private Integer activityId;

    private String activityName;

    private String startTime; // 活动开始时间 

    private Integer totalBonus;	// 总奖金

    private Integer robotMultiple;	// 机器人倍数

    private String createTime;

    private String updateTime;

//  private String operate;

    private Integer status;

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Integer getTotalBonus() {
        return totalBonus;
    }

    public void setTotalBonus(Integer totalBonus) {
        this.totalBonus = totalBonus;
    }

    public Integer getRobotMultiple() {
        return robotMultiple;
    }

    public void setRobotMultiple(Integer robotMultiple) {
        this.robotMultiple = robotMultiple;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}