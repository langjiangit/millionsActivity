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
    
    private Integer isonLive; // 是否正在直播 OR 开播前5分钟内

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

	public Integer getIsonLive() {
		return isonLive;
	}

	public void setIsonLive(Integer isonLive) {
		this.isonLive = isonLive;
	}
	
	 @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
			result = prime * result + ((activityName == null) ? 0 : activityName.hashCode());
			result = prime * result + ((createTime == null) ? 0 : createTime.hashCode());
			result = prime * result + ((isonLive == null) ? 0 : isonLive.hashCode());
			result = prime * result + ((robotMultiple == null) ? 0 : robotMultiple.hashCode());
			result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
			result = prime * result + ((status == null) ? 0 : status.hashCode());
			result = prime * result + ((totalBonus == null) ? 0 : totalBonus.hashCode());
			result = prime * result + ((updateTime == null) ? 0 : updateTime.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Answer_activity other = (Answer_activity) obj;
			if (activityId == null) {
				if (other.activityId != null)
					return false;
			} else if (!activityId.equals(other.activityId))
				return false;
			if (activityName == null) {
				if (other.activityName != null)
					return false;
			} else if (!activityName.equals(other.activityName))
				return false;
			if (createTime == null) {
				if (other.createTime != null)
					return false;
			} else if (!createTime.equals(other.createTime))
				return false;
			if (isonLive == null) {
				if (other.isonLive != null)
					return false;
			} else if (!isonLive.equals(other.isonLive))
				return false;
			if (robotMultiple == null) {
				if (other.robotMultiple != null)
					return false;
			} else if (!robotMultiple.equals(other.robotMultiple))
				return false;
			if (startTime == null) {
				if (other.startTime != null)
					return false;
			} else if (!startTime.equals(other.startTime))
				return false;
			if (status == null) {
				if (other.status != null)
					return false;
			} else if (!status.equals(other.status))
				return false;
			if (totalBonus == null) {
				if (other.totalBonus != null)
					return false;
			} else if (!totalBonus.equals(other.totalBonus))
				return false;
			if (updateTime == null) {
				if (other.updateTime != null)
					return false;
			} else if (!updateTime.equals(other.updateTime))
				return false;
			return true;
		}
}