package com.rrs.rd.address.demo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Demo服务，使用Dubbox的REST方式，仅用于分析测试目的。
 * @author Richie 刘志斌 yudi@sina.com
 * 2016年9月25日
 */
@Path("demo")
public interface HttpDemoService {
	
	@GET
	@Path("find/{addr: .+}")
	@Produces({"text/html;charset=UTF-8"})
	String find(@PathParam("addr") String addrText, @QueryParam("top") int topN);
	
}