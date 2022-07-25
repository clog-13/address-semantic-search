package com.rrs.rd.address.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.rrs.rd.address.persist.AddressEntity;
import com.rrs.rd.address.persist.AddressPersister;
import com.rrs.rd.address.persist.RegionEntity;
import com.rrs.rd.address.similarity.SimilarityComputer;

public class BuildFileCache {
	private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

	public static void main(String[] args) {
		//启动spring容器
		AddressPersister persist;
		SimilarityComputer computer;
		ClassPathXmlApplicationContext context;
		try{
			context = new ClassPathXmlApplicationContext(new String[] { "spring-config.xml" });
			computer = context.getBean(SimilarityComputer.class);
			persist = context.getBean(AddressPersister.class);
			if(computer == null || persist == null){
				System.out.println("> [错误] 应用初始化失败，无法初始化spring context或者AddressPersister、SimilarityComputer对象");
				return;
			}
		}catch(Exception ex){
			System.out.println("> [错误] spring-config.xml文件配置错误：" + ex.getMessage());
			ex.printStackTrace(System.out);
			return;
		}
		context.start(); 
		
		RegionEntity root = persist.rootRegion();
		for(RegionEntity province : root.getChildren()){
			//if(province.getId()!=110000) continue; // 仅测试北京的地址
			for(RegionEntity city : province.getChildren()){
				if(city.getChildren()==null){
					long start = System.currentTimeMillis();
					Date startDate = new Date();
					try{
						List<AddressEntity> addresses = persist.loadAddresses(province.getId(), city.getId(), 0);
						if(addresses==null || addresses.isEmpty()) continue;
						
						computer.buildDocumentFileCache(computer.buildCacheKey(addresses.get(0)), addresses);
						System.out.println("> [" + format.format(startDate) + " -> " + format.format(new Date()) + "] "
							+ province.getName() + "-" + city.getName() + ", " + addresses.size() + " addresses, " 
							+ "elapsed: " + (System.currentTimeMillis()-start)/1000.0 + "s.");
					}catch(Exception ex){
						System.out.println("> [" + format.format(startDate) + " -> " + format.format(new Date()) + "] "
							+ province.getName() + "-" + city.getName() + " error: " + ex.getMessage());
						ex.printStackTrace(System.out);
					}
				}else{
					for(RegionEntity county : city.getChildren()){
						long start = System.currentTimeMillis();
						Date startDate = new Date();
						try{
							List<AddressEntity> addresses = persist.loadAddresses(province.getId(), city.getId(), county.getId());
							if(addresses==null || addresses.isEmpty()) continue;
							computer.buildDocumentFileCache(computer.buildCacheKey(addresses.get(0)), addresses);
							System.out.println("> [" + format.format(startDate) + " -> " + format.format(new Date()) + "] "
								+ province.getName() + "-" + city.getName() + "-" + county.getName() + ", " + addresses.size() + " addresses, " 
								+ "elapsed: " + (System.currentTimeMillis()-start)/1000.0 + "s.");
						}catch(Exception ex){
							System.out.println("> [" + format.format(startDate) + " -> " + format.format(new Date()) + "] "
								+ province.getName() + "-" + city.getName() + " error: " + ex.getMessage());
							ex.printStackTrace(System.out);
						}
					}
				}
			}
		}
	}
}