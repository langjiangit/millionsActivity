package mapper;

import java.io.IOException;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import com.bus.chelaile.util.New;

public class Test {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		
//		List<String> options = New.arrayList();
//		options.add("苹果");
//		options.add("芒果");
//		options.add("猫咪");
//		
//		System.out.println(JSONObject.toJSONString(options));
//		
//		
//		String url = "http://web.chelaile.net.cn/outman/social/getInvalidPeople";
//		for(int i = 0;i < 50 ; i ++) {
//			
//			String response = HttpUtils.get(url, "utf-8");
//			JSONObject j = JSONObject.parseObject(response);
//			System.out.println(j.getString("nickname"));
//			
//		}
		
		Set<String> a = New.hashSet();
		a.add("1");a.add("2");a.add("3");
		Set<String> b = New.hashSet();
		b.add("1");b.add("2");
		a.removeAll(b);
		System.out.println(a);
	}
}
