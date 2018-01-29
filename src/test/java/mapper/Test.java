package mapper;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.util.New;

public class Test {

	public static void main(String[] args) {
		
		List<String> options = New.arrayList();
		options.add("苹果");
		options.add("芒果");
		options.add("猫咪");
		
		System.out.println(JSONObject.toJSONString(options));
	}
}
