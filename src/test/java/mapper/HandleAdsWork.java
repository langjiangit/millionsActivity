package mapper;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.util.New;

public class HandleAdsWork {
	public static  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	
	public static void main(String args[]) throws IOException, InterruptedException, ParseException{
		
		CacheUtil.initClient();
		Set<String> shops = New.hashSet();
//		String adFileIn = "D://后台/广告/广告收益计算/2017年广告 - 副本.txt";
//		String financeFile = "D://后台/广告/广告收益计算/OKR-广告与商务合作部 Q4 20161028 - 副本.csv";
//		String adFileOut = "D://后台/广告/广告收益计算/2017年广告输出 - 副本.txt";
//		String adFileOutEx = "D://后台/广告/广告收益计算/2017年广告输出(未处理) - 副本.txt";
		
//		String weichuli = "D://后台/广告/广告收益计算/2017年广告输出(未处理).txt";
//		String chuli = "D://后台/广告/广告收益计算/2017年广告输出(未处理)1.txt";
//		
//		BufferedReader weichuliR = new BufferedReader(new InputStreamReader(new FileInputStream(weichuli)));
//		BufferedWriter chuliW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(chuli)));
//		
//		String line2 = null;
//		while((line2 = weichuliR.readLine()) != null) {
//			String[] buf = line2.split("###");
//			if(buf.length > 3) {
//				chuliW.write(line2);
//				chuliW.newLine();
//				chuliW.flush();
//			}
//		}
//		chuliW.close();
//		weichuliR.close();
//		if(1 == 1)
//			return;
		
		
		
		String adFileIn = "D://后台/广告/广告收益计算/2017年广告.txt";
		String financeFile = "D://后台/广告/广告收益计算/OKR-广告与商务合作部 Q4 20161028.csv";
		String adFileOut = "D://后台/广告/广告收益计算/2017年广告输出 .txt";
		String adFileOutEx = "D://后台/广告/广告收益计算/2017年广告输出(未处理).txt";
		
		File fileAdIn = new File(adFileIn);
		File fileFinance = new File(financeFile);
		File fileAdOut = new File(adFileOut);
		File fileAdOutEx = new File(adFileOutEx);
		
		InputStreamReader readerAdStream = new InputStreamReader(new FileInputStream(fileAdIn));
		BufferedReader readerAd = new BufferedReader(readerAdStream);
		BufferedReader readerFinance = new BufferedReader(new InputStreamReader(new FileInputStream(fileFinance)));
		BufferedWriter writerAd = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileAdOut)));
		BufferedWriter writerAdEx = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileAdOutEx)));
		
		String line1 = null;
		while((line1 = readerFinance.readLine()) != null) {
			String[] buf = line1.split(",");
			String key = buf[1];
			String field = "";
			if(buf.length > 3) {
				field = buf[2] + "," + buf[3];
			} else {
				System.out.println("长度小于3的, line=" + line1);
				continue;
			}
			String value = buf[0];
			
			shops.add(key);
			CacheUtil.setHashSetValue(key, field, value);
		}
		
		Thread.sleep(2000);
		
		
		//  开始处理广告
		String line = null;
		while((line = readerAd.readLine()) != null) {
			if(! line.contains("广告") || line.contains("测试"))
				continue;
			String[] buf = line.split("###");
			if(buf.length < 5) {
				System.out.println("长度小于5," + line);
				continue;
			}
			
			// 找到对应的key
			boolean hasShop = false;
			for(String s : shops) {
				if(buf[1].contains(s)) {
					hasShop = true;
					Map<String, String> dateValue = CacheUtil.getHsetAll(s);
					// 时间符合区间要求的
					Calendar cal=Calendar.getInstance();
					Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(buf[3].split(" ")[0]);
					cal.setTime(dateBegin);
					
					Calendar calEnd=Calendar.getInstance();
					Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(buf[4].split(" ")[0]);
					calEnd.setTime(dateEnd);
					
					while(true) {
						if ( !cal.after(calEnd)) {
							String value = inarea(cal, dateValue);
							if(value != null) {
//								System.out.println("匹配上了 , line=" + buf[0] + "###" + buf[1] + "###" + df.format(cal.getTime()) + "###" + value);
								writerAd.write(buf[0] + "###" + buf[1] + "###" + df.format(cal.getTime()) + "###" + value);
								writerAd.newLine();
								writerAd.flush();
							} else {
//								System.out.println("匹配不上， line=" + buf[0] + "###" + buf[1] + "###" + df.format(cal.getTime()));
								writerAdEx.write(buf[0] + "###" + buf[1] + "###" + df.format(cal.getTime()));
								writerAdEx.newLine();
								writerAdEx.flush();
							}
						} else {
							break;
						}
						// 从 广告投放的开始日期到结束日期，每天分开
						cal.add(Calendar.DATE, 1);
					}
				} 
			}
			if (!hasShop) {
				System.out.println("标题没有匹配到商户名: line=" + line);
				writerAdEx.write(line);
				writerAdEx.newLine();
				writerAdEx.flush();
			}
		}
		readerAd.close();
		readerFinance.close();
		writerAd.close();
		writerAdEx.close();
	}

	
	// 日期cal是否包含在dateValue map的keys中
	private static String inarea(Calendar cal, Map<String, String> dateValue) {
		for (Entry<String, String> entry : dateValue.entrySet()) {
			if(dateValue.size() == 1) { // 说明该商家只有一个项目编号
				return entry.getValue();
			}

			String field = entry.getKey();
			String[] buf = field.split(",");
			if (df.format(cal.getTime()).compareTo(buf[0]) >= 0 && df.format(cal.getTime()).compareTo(buf[1]) <= 0) {
				return entry.getValue();
			}
			
		}
		return null;
	}

	
	private static String dateCompare(Map<String, String> dateValue, String beginDate, String endDate) {
		for(Entry<String, String> entry : dateValue.entrySet()) {
			String field = entry.getKey();
			if(field.contains(",")) {
				String[] buf = field.split(",");
				// TODO 这个地方有待测试
				if(buf[0].compareTo(beginDate.split(" ")[0]) <= 0 && buf[1].compareTo(endDate.split(" ")[0]) >= 0){
					return entry.getValue();
				}
			}
		}
		return null;
	}
}

