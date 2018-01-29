package mapper;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bus.chelaile.service.StartService;

public class MapperTest {

	
	public static void main(String[] args) {
	
	ApplicationContext context = new ClassPathXmlApplicationContext(
			"classpath:servicebiz/locator-sharecar.xml");
	
	StartService st = context.getBean(StartService.class);
	}
}
