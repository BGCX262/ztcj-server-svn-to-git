package com.wm927.action.data;

import java.util.List;
import java.util.Map;

import com.wm927.commons.Contants;
import com.wm927.service.impl.MiddlewareActionService;
/**
 * 显示N条动态接口
 * 2013/11/5 更新
 */
public class ShowDynamicAction extends MiddlewareActionService {
	private String lastid;//最后一条动态ID
	private String uid;
	private String condition = "1,2,3,4,5,6,7";
	public void execute() {
		if(!checkNull(new Object[]{"最新一条动态ID不能为空","用户ID不能为空"},new Object[]{lastid,uid}))
			return ; 
		//加载的自己关注的人的所有动态
		String friendSql = "SELECT FID FROM wm_user_friends WHERE UID = ?";
		StringBuilder friend = new StringBuilder();
		StringBuilder sql = new StringBuilder();
		List<Map<String,Object>> friend_list = middlewareService.find(friendSql,uid);
		friend.append(Contants.DEFAULT_OID);//公司系统ID
		for( Map<String,Object> map : friend_list){
			friend.append(", " );
			friend.append(map.get("fid"));
		}
		//TYPE=8为问答的，不显示在首页动态里面
		sql.append("SELECT COUNT(*) AS COUNT FROM wm_user_dynamic WHERE ID > "+lastid+"  AND ");
		
		//拼接好友UID
		if(condition.indexOf("7") != -1){
			//分类为获利的
			sql.append(" ( UID IN (" + friend.toString() + ")") ;
			sql.append(" OR OUID IN (" + friend.toString() + ") )") ;
		}else{
			sql.append(" UID IN (" + friend.toString() + ")") ;
		}
		
		
		//分类条件
		//此处仅为了防止他人传入错误的参数，导致程序出错
		try{
			condition.split(",");
		}catch(Exception e){
			condition = "1,2,3,4,5,6,7";
		}
		sql.append(" AND TYPE IN (" + condition +")");
		
		List<Map<String,Object>> list_value = middlewareService.find(sql.toString());
		responseInfo("1","返回成功",list_value);
		
	}

	public String getLastid() {
		return lastid;
	}
	public void setLastid(String lastid) {
		this.lastid = lastid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

}
