package com.rrs.rd.address.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.rrs.rd.address.Division;
import com.rrs.rd.address.index.TermIndexBuilder;
import com.rrs.rd.address.interpret.AddressInterpreter;
import com.rrs.rd.address.interpret.RegionInterpreterVisitor;
import com.rrs.rd.address.persist.AddressEntity;
import com.rrs.rd.address.persist.AddressPersister;
import com.rrs.rd.address.utils.StringUtil;

public class AddressInterpretTest extends TestBase {
	@Test
	public void testExtractTown(){
		AddressPersister pt = context.getBean(AddressPersister.class);
		AddressInterpreter inter = context.getBean(AddressInterpreter.class);
		RegionInterpreterVisitor v = new RegionInterpreterVisitor(pt);
		
		Map<Long, List<String>> towns = new HashMap<Long, List<String>>();
		
		doExtractTownVillageTest(inter, v, towns, "山东青岛平度市中庄镇西中庄村青岛平度中庄镇西中庄村", "", 370283, "中庄镇", "中庄村");
		//万子湖村不能将万子湖匹配城万子湖乡，留下一个村字，被匹配城【村新四村】
		doExtractTownVillageTest(inter, v, towns, "湖南益阳沅江市万子湖乡万子湖乡万子湖村新四村民组", "新四村民组", 430981, null, "万子湖村");
		//肥城市、肥城县都应当匹配成区县，不能匹配出【县桃源镇】来
		doExtractTownVillageTest(inter, v, towns, "山东泰安肥城市桃园镇桃园镇山东省泰安市肥城县桃园镇东伏村", "", 370983, null, "东伏村");
		//西乡县，不能匹配出【西乡】来
		doExtractTownVillageTest(inter, v, towns, "陕西汉中汉台区汉白公路汉台区陕西汉中市西乡县城东三岔路口", "城东三岔路口", 610702, null, null);
		//目前匹配不上【临湖镇】，因为是在删除冗余时才匹配上
		doExtractTownVillageTest(inter, v, towns, "江苏苏州吴中区渡村镇农行渡村分理处江苏省苏州市吴中区临湖镇渡村前塘村村前", "前塘村村前", 320506, null, "渡村");
		//不能匹配出【中关村】来
		doExtractTownVillageTest(inter, v, towns, "北京北京海淀区中关村南大街九龙商务中心", "中关村南大街九龙商务中心", 110108, null, null);
		//正确匹配【永镇村】，不能匹配成【永镇】
		//清水镇使用的清水街道匹配，匹配出的Region并不属于镇
		doExtractTownVillageTest(inter, v, towns, "安徽芜湖鸠江区清水镇永镇村芜湖鸠江经济开发区万春中路永镇路", "芜湖鸠江经济开发区万春中路永镇路", 340207, null, "永镇村");
		doExtractTownVillageTest(inter, v, towns, "上海上海浦东新区川沙镇川沙镇川沙镇城南路", "城南路", 310115, "川沙镇", null);
		doExtractTownVillageTest(inter, v, towns, "北京北京海淀区温泉温泉镇温泉镇温泉服装厂对面", "服装厂对面", 110108, "温泉镇", null);
		doExtractTownVillageTest(inter, v, towns, "广东广州白云区均和街新市镇广州市白云区均禾街长红村", "均禾街长红村", 440111, null, null);
		doExtractTownVillageTest(inter, v, towns, "黑龙江哈尔滨道里区顾乡大街顾乡公园", "顾乡大街顾乡公园", 230102, null, null);
		doExtractTownVillageTest(inter, v, towns, "北京北京昌平区龙乡小区", "龙乡小区", 110114, null, null);
		doExtractTownVillageTest(inter, v, towns, "浙江省金华市婺城区中村社区", "中村社区", 330702, null, null);
		doExtractTownVillageTest(inter, v, towns, "河南洛阳偃师市李村镇上庄村3组", "3组", 410381, "李村镇", "上庄村");
		doExtractTownVillageTest(inter, v, towns, "河南省焦作市孟州市城关镇移民新村寺村三区", "寺村三区", 410883, "城关镇", "移民新村");
		doExtractTownVillageTest(inter, v, towns, "湖北省黄冈市红安县红安县八里湾镇前进路205号", "湾镇前进路205号", 421122, null, null);
		doExtractTownVillageTest(inter, v, towns, "河南鹤壁浚县新镇镇孟庄村48号", "48号", 410621, "新镇镇", "孟庄村");
		doExtractTownVillageTest(inter, v, towns, "安徽滁州天长市新街镇李坡村郑兴队14号", "郑兴队14号", 341181, "新街镇", "李坡村");
		doExtractTownVillageTest(inter, v, towns, "江苏南通海门市万年镇镇兴村18组8号", "18组8号", 320684, "万年镇", "镇兴村");
	}
	private void doExtractTownVillageTest(AddressInterpreter interpreter, RegionInterpreterVisitor visitor
			, Map<Long, List<String>> towns, String addrText, String leftText, long did, String town, String village){
		towns.clear();
		AddressEntity addr = new AddressEntity(addrText);
		interpreter.extractRegion(addr, visitor);
		assertTrue(addr.hasDistrict());
		assertEquals(did, addr.getDistrict().getId());
		interpreter.removeRedundancy(addr, visitor);
		interpreter.extractTownVillage(addr, towns);
		
		LOG.info(addrText + " >> " + addr +
				(towns.containsKey(did) ? " + " + towns.get(did).toString() : "") 
				);
		
		assertEquals(leftText, addr.getText());
		
		String actual = null;
		if(town!=null){
			if(towns.containsKey(did)) {
				List<String> strs = towns.get(did);
				for(String s : strs) {
					if(s.equals(town)) {
						actual = s;
						break;
					}
				}
			}
			assertTrue( (addr.hasTown() && addr.getTown().orderedNameAndAlias().contains(town)) || town.equals(actual) );
		}
		if(village!=null){
			if(towns.containsKey(did)) {
				List<String> strs = towns.get(did);
				for(String s : strs) {
					if(s.equals(village)) {
						actual = s;
						break;
					}
				}
			}
			assertTrue( (addr.hasVillage() && addr.getVillage().orderedNameAndAlias().contains(village)) || village.equals(actual) );
		}
		
		if(town==null && village==null) 
			assertFalse(towns.containsKey(did));
		else if(town==null || village==null) {
			assertTrue(!towns.containsKey(did) || (towns.containsKey(did) && towns.get(did).size()==1));
		}
	}
	
	@Test
	public void testInterpretAddress(){
		//AddressInterpreter interpreter = context.getBean(AddressInterpreter.class);
		
//		//测试正常解析
//		AddressEntity addr = interpreter.interpret("青海海西格尔木市河西街道郭镇盐桥村");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertNotNull("未解析出省份", addr.getProvince());
//		assertEquals("省份错误", 630000, addr.getProvince().getId());
//		assertNotNull("未解析出地级市", addr.getCity());
//		assertEquals("地级市错误", 632800, addr.getCity().getId());
//		assertNotNull("未解析出区县", addr.getDistrict());
//		assertEquals("区县错误", 632801, addr.getDistrict().getId());
//		assertNotNull("未解析出街道乡镇", addr.getStreet());
//		assertEquals("街道乡镇错误", 632801004, addr.getStreet().getId());
//		assertNotNull("未解析出乡镇", addr.getTowns());
//		assertEquals("乡镇错误", 1, addr.getTowns().size());
//		assertEquals("乡镇错误", "郭镇", addr.getTowns().get(0));
//		assertEquals("村庄错误", "盐桥村", addr.getVillage());
//		
//		addr = interpreter.interpret("山东青岛即墨市龙山镇官庄村即墨市龙山街道办事处管庄村");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertTrue(addr.hasDistrict());
//		assertTrue(addr.hasCity());
//		assertEquals("区县错误", 130185, addr.getDistrict().getId());
//		assertEquals("详细地址错误", "贺庄回迁楼", addr.getText());
//		assertEquals("道路错误", "镇宁路", addr.getRoad());
//		assertEquals("房间号错误", "1号楼1单元602室", addr.getBuildingNum());
//		
//		//测试bug加入用例
//		addr = interpreter.interpret("河北省石家庄市鹿泉市镇宁路贺庄回迁楼1号楼1单元602室");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertTrue(addr.hasDistrict());
//		assertTrue(addr.hasCity());
//		assertEquals("区县错误", 130185, addr.getDistrict().getId());
//		assertEquals("详细地址错误", "贺庄回迁楼", addr.getText());
//		assertEquals("道路错误", "镇宁路", addr.getRoad());
//		assertEquals("房间号错误", "1号楼1单元602室", addr.getBuildingNum());
//		
//		//测试bug加入用例
//		addr = interpreter.interpret("北京北京海淀区北京市海淀区万寿路翠微西里13号楼1403室");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "翠微西里", addr.getText());
//		assertEquals("道路错误", "万寿路", addr.getRoad());
//		assertEquals("房间号错误", "13号楼1403室", addr.getBuildingNum());
//		
//		//测试bug加入用例
//		addr = interpreter.interpret("海南海南省直辖市县定安县见龙大道财政局宿舍楼702");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "财政局宿舍楼702", addr.getText());
//		assertEquals("道路错误", "见龙大道", addr.getRoad());
//		
//		//测试bug加入用例
//		addr = interpreter.interpret("甘肃临夏临夏县先锋乡张梁村史上社17号");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "史上社17号", addr.getText());
//		assertNotNull("未解析出乡镇", addr.getTowns());
//		assertEquals("乡镇错误", 1, addr.getTowns().size());
//		assertEquals("乡镇错误", "先锋乡", addr.getTowns().get(0));
//		assertEquals("村庄错误", "张梁村", addr.getVillage());
//		
//		//bug fix: 解析出来的镇为：市毛田乡，查bug用
//		addr = interpreter.interpret("湖南湘潭湘乡市湖南省湘乡市毛田乡崇山村洪家组");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "洪家组", addr.getText());
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 430381113, addr.getStreet().getId());
//		assertEquals("区县错误", 430381, addr.getDistrict().getId());
//		assertEquals("村庄错误", "崇山村", addr.getVillage());
//		
//		//辽宁锦州北镇市高山子镇辽宁省北镇市高山子镇南民村545号
//		
//		//镇名字中出现【镇】字
//		addr = interpreter.interpret("浙江丽水缙云县壶镇镇缙云县壶镇镇 下潜村257号");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "257号", addr.getText());
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 331122101, addr.getStreet().getId());
//		assertEquals("区县错误", 331122, addr.getDistrict().getId());
//		
//		//两个乡，解析出最后出现的（目前逻辑）
//		addr = interpreter.interpret("云南文山壮族苗族自治州砚山县盘龙彝族乡盘龙乡白泥井村");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "", addr.getText());
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 532622203, addr.getStreet().getId());
//		assertEquals("区县错误", 532622, addr.getDistrict().getId());
//		assertEquals("村庄错误", "白泥井村", addr.getVillage());
//		
//		//两个镇，解析出最后出现的（目前逻辑）
//		addr = interpreter.interpret("福建宁德福安市上白石镇潭头镇潭头村");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "", addr.getText());
//		assertNotNull("未解析出乡镇", addr.getTowns());
//		assertEquals("乡镇错误", 1, addr.getTowns().size());
//		assertEquals("乡镇错误", "潭头镇", addr.getTowns().get(0));
//		assertEquals("村庄错误", "潭头村", addr.getVillage());
//		
//		//能够正确解析出：曹镇乡、焦庄村。因为镇、乡都是关键字，容易发生错误解析情况
//		addr = interpreter.interpret("河南平顶山湛河区平顶山市湛河区曹镇乡焦庄村苗桥");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "苗桥", addr.getText());
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 410411200, addr.getStreet().getId());
//		assertEquals("区县错误", 410411, addr.getDistrict().getId());
//		assertEquals("道路错误", "焦庄村", addr.getVillage());
//		
//		//能够正常解析出：南村镇，强镇街。因为强镇街中包含关键字【镇】，容易发生错误解析情况
//		addr = interpreter.interpret("河北石家庄长安区南村镇强镇街51号南村工商管理局");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "南村工商管理局", addr.getText());
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 130102101, addr.getStreet().getId());
//		assertEquals("区县错误", 130102, addr.getDistrict().getId());
//		assertEquals("道路错误", "强镇街", addr.getRoad());
//		
//		//测试去冗余，保留完整的村委、村委会
//		addr = interpreter.interpret("浙江杭州萧山区浙江省杭州市萧山区益农镇兴裕村委东150米");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 330109115, addr.getStreet().getId());
//		assertEquals("区县错误", 330109, addr.getDistrict().getId());
//		assertEquals("详细地址错误", "村委东", addr.getText());
//		assertEquals("村庄错误", "兴裕村", addr.getVillage());
//		
//		//测试正确提取村庄，村的名称必须是【三居洋村】，不能是【三居洋村村】
//		addr = interpreter.interpret("福建三明明溪县夏阳乡三居洋村村口");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 350421202, addr.getStreet().getId());
//		assertEquals("区县错误", 350421, addr.getDistrict().getId());
//		assertEquals("详细地址错误", "村口", addr.getText());
//		assertEquals("村庄错误", "三居洋村", addr.getVillage());
//		
//		//重复出现的乡镇
//		addr = interpreter.interpret("广东湛江廉江市石岭镇，石岭镇， 外村乡凉伞树下村〈村尾钟其德家〉");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "村尾钟其德家", addr.getText());
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 440881113, addr.getStreet().getId());
//		assertEquals("区县错误", 440881, addr.getDistrict().getId());
//		assertNotNull("未解析出乡镇", addr.getTowns());
//		assertEquals("乡镇错误", 1, addr.getTowns().size());
//		assertEquals("乡镇错误", "外村乡", addr.getTowns().get(0));
//		assertEquals("村庄错误", "凉伞树下村", addr.getVillage());
//		
//		//长春下面有绿园区、汽车产业开发区，所以移除冗余时会把长春汽车产业开发区去掉
//		addr = interpreter.interpret("吉林长春绿园区长春汽车产业开发区（省级）（特殊乡镇）长沈路1000号力旺格林春天");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "力旺格林春天", addr.getText());
//		assertNotNull("未解析出省份", addr.getProvince());
//		assertEquals("省份错误", 220000, addr.getProvince().getId());
//		assertNotNull("未解析出地级市", addr.getCity());
//		assertEquals("地级市错误", 220100, addr.getCity().getId());
//		assertNotNull("未解析出区县", addr.getDistrict());
//		assertEquals("区县错误", 220106, addr.getDistrict().getId());
//		assertEquals("道路错误", "长沈路", addr.getRoad());
//		
//		//去冗余时，秦皇岛市昌黎镇马铁庄村，不能将【昌黎】匹配成区县。
//		addr = interpreter.interpret("河北秦皇岛昌黎县昌黎镇秦皇岛市昌黎镇马铁庄村");
//		assertNotNull("解析失败", addr);
//		LOG.info("> " + addr.getRawText() + " --> " + addr);
//		assertEquals("详细地址错误", "", addr.getText());
//		assertTrue(addr.hasStreet());
//		assertEquals("街道乡镇错误", 130322100, addr.getStreet().getId());
//		assertEquals("区县错误", 130322, addr.getDistrict().getId());
//		assertEquals("村庄错误", "马铁庄村", addr.getVillage());
	}
	
	@Test
	public void testExtractRegionPerf(){
		AddressPersister persister = context.getBean(AddressPersister.class);
		TermIndexBuilder builder = context.getBean(TermIndexBuilder.class);
		RegionInterpreterVisitor visitor = new RegionInterpreterVisitor(persister);
		
		//预热
		indexSearchRegionPerf("山东青岛市市南区宁德路金梦花园", builder, visitor);
		indexSearchRegionPerf("广东广州从化区温泉镇新田村", builder, visitor);
		indexSearchRegionPerf("湖南湘潭市湘潭县易俗河镇中南建材市场", builder, visitor);
		
		//性能测试
		int loop = 3000000;
		long start = System.nanoTime();
		
		for(int i=0; i<loop; i++) {
			indexSearchRegionPerf("山东青岛市市南区宁德路金梦花园", builder, visitor);
			indexSearchRegionPerf("广东广州从化区温泉镇新田村", builder, visitor);
			indexSearchRegionPerf("湖南湘潭市湘潭县易俗河镇中南建材市场", builder, visitor);
			indexSearchRegionPerf("浙江省绍兴市绍兴县孙端镇村西村", builder, visitor);
		}
		long time2 = System.nanoTime() - start;
		
		LOG.info("倒排索引方式耗时: " + (time2/1000000/1000.0) + "s");
	}
	private void indexSearchRegionPerf(String text, TermIndexBuilder builder, RegionInterpreterVisitor visitor){
		builder.deepMostQuery(text, visitor);
		visitor.reset();
	}
	
	@Test
	public void testExtractRegion(){
		AddressPersister persister = context.getBean(AddressPersister.class);
		TermIndexBuilder builder = context.getBean(TermIndexBuilder.class);

		RegionInterpreterVisitor visitor = new RegionInterpreterVisitor(persister);
		
//		doExtractRegionTest(builder, visitor, 610000, 610800, 610826, 610826106
//				, "陕西榆林绥德县吉镇镇柳湾村136号", "柳湾村", "容错：多余关键字");

		//测试 正常的地址解析
		doExtractRegionTest(builder, visitor, 440000, 440100, 440184, 440184103
				, "广东广州从化区温泉镇新田村", "新田村", "正常解析");
		//测试 地址中缺失省份的情况
		doExtractRegionTest(builder, visitor, 440000, 440100, 440184, 440184103
				, "广州从化区温泉镇新田村", "新田村", "容错-缺省份");
		//测试 地址中缺失地级市，且乡镇名称以特殊字符【镇】开头的情况
		doExtractRegionTest(builder, visitor, 430000, 430100, 430181, 430181115
				, "湖南浏阳镇头镇回龙村", "回龙村", "特殊名-镇头镇");
		
		//测试：容错性，都匀市属于【黔南】，而不是【黔东南】
		doExtractRegionTest(builder, visitor, 520000, 522700, 522701, 0
				, "贵州黔东南都匀市大西门州中医院食堂4楼", "大西门州中医院食堂4楼", "容错-城市错误");
		
		doExtractRegionTest(builder, visitor, 650000, 652100, 652122, 0
				, "新疆维吾尔自治区吐鲁番地区鄯善县经济贸易委员会", "经济贸易委员会", "容错");
		
		//测试 直辖市3级表示的情况
		doExtractRegionTest(builder, visitor, 310000, 310100, 310230, 310230203
				, "上海上海崇明县横沙乡", "", "直辖市-3级");
		//测试 直辖市2级表示的情况
		doExtractRegionTest(builder, visitor, 310000, 310100, 310230, 310230203
				, "上海崇明县横沙乡", "", "直辖市-2级");
		
		//特殊区县名称：以【市】字开头的区县，例如：山东青岛的市南区、市北区。
		//测试完整表示法：山东青岛市市南区
		doExtractRegionTest(builder, visitor, 370000, 370200, 370202, 0
				, "山东青岛市市南区宁德路金梦花园", "宁德路金梦花园", "特殊名-市南区-完整");
		//特殊区县名称：以【市】字开头的区县，例如：山东青岛的市南区、市北区。
		//测试简写表示法：山东青岛市南区
		//错误匹配方式：山东 青岛市 南区，会导致区县无法匹配
		//正确匹配方式：山东 青岛 市南区
		doExtractRegionTest(builder, visitor, 370000, 370200, 370202, 0
				, "山东青岛市南区宁德路金梦花园", "宁德路金梦花园", "特殊名-市南区-简写");
		
		//地级市下面存在与地级市名称相同的县级行政区划，例如：湖南湘潭市湘潭县易俗河镇中南建材市场
		//测试 正常表示法（省市区完整）：湖南湘潭市湘潭县易俗河镇中南建材市场
		doExtractRegionTest(builder, visitor, 430000, 430300, 430321, 430321100
				, "湖南湘潭市湘潭县易俗河镇中南建材市场", "中南建材市场", "区市同名-完整");
		//地级市下面存在与地级市名称相同的县级行政区划，例如：湖南湘潭市湘潭县易俗河镇中南建材市场
		//测试 地级市缺失情况：湖南湘潭县易俗河镇中南建材市场
		doExtractRegionTest(builder, visitor, 430000, 430300, 430321, 430321100
				, "湖南湘潭县易俗河镇中南建材市场", "中南建材市场", "区市同名-缺城市");
		
		//地级市下面存在与地级市名称相同的县级行政区划，但后来改名了，例如：浙江省绍兴市绍兴县，后改名为：浙江省绍兴市柯桥区
		//在标准行政区域数据中，将绍兴县放在了柯桥区的别名中
		//测试 地址完整的情况：湖南湘潭县易俗河镇中南建材市场
		doExtractRegionTest(builder, visitor, 330000, 330600, 330621, 330621102
				, "浙江省绍兴市绍兴县孙端镇村西村", "村西村", "区市同名-县改区-完整");
		//地级市下面存在与地级市名称相同的县级行政区划，但后来改名了，例如：浙江省绍兴市绍兴县，后改名为：浙江省绍兴市柯桥区
		//在标准行政区域数据中，将绍兴县放在了柯桥区的别名中
		//测试 地址完整的情况：湖南湘潭县易俗河镇中南建材市场
		doExtractRegionTest(builder, visitor, 330000, 330600, 330621, 330621102
				, "浙江省绍兴县孙端镇村西村", "村西村", "区市同名-县改区-缺城市");
		
		//省直辖县级行政区划，采用特殊的3级地址表示法（国家统计局官网公布的数据，采用的这种形式）
		//海南海南省直辖市县昌江黎族自治县
		//正确匹配方式：海南 海南省直辖市县 昌江黎族自治县，忽略掉中间的【海南省直辖市县】部分，最后解析为：海南 昌江黎族自治县
		doExtractRegionTest(builder, visitor, 460000, 469031, 469031, 469026100
				, "海南海南省直辖市县昌江黎族自治县石碌镇", "", "省直辖县市-3级");
		//省直辖县级行政区划，采用较常用的3级地址表示法
		doExtractRegionTest(builder, visitor, 460000, 469005, 469005, 0
				, "海南省文昌文昌市文建东路13号", "文建东路13号", "省直辖县市-3级");
		//省直辖县级行政区划，采用2级地址表示法
		doExtractRegionTest(builder, visitor, 460000, 469005, 469005, 0
				, "海南省文昌市文建东路13号", "文建东路13号", "省直辖县市-2级");
		
		//新疆阿克苏地区阿拉尔市
		//到目前为止，新疆下面仍然有地级市【阿克苏地区】
		//【阿拉尔市】是县级市，以前属于地级市【阿克苏地区】，目前已变成新疆的省直辖县级行政区划
		//即，老的行政区划关系为：新疆->阿克苏地区->阿拉尔市
		//新的行政区划关系为（当前项目采用的标准行政区划数据关系）：
		//新疆->阿克苏地区
		//新疆->阿拉尔市
		//错误匹配方式：新疆 阿克苏地区 阿拉尔市，会导致在【阿克苏地区】下面无法匹配到【阿拉尔市】
		//正确匹配结果：新疆 阿拉尔市
		doExtractRegionTest(builder, visitor, 650000, 659002, 659002, 0
				, "新疆阿克苏地区阿拉尔市新苑祥和小区", "新苑祥和小区", "省直辖县市-后升级");
	}
	private void doExtractRegionTest(TermIndexBuilder index, RegionInterpreterVisitor visitor
			, long province, long city, long district, int town
			, String addrText, String expectedLeft, String title){
		visitor.reset();
		index.deepMostQuery(addrText, visitor);
		Division division = visitor.resultDivision();
		assertNotNull(title + ": 省份未解析", division.getProvince());
		assertNotNull(title + ": 地级市未解析", division.getCity());
		assertNotNull(title + ": 区县未解析", division.getDistrict());
		if(town>0) assertNotNull(title + ": 街道乡镇未解析", division.getStreet());
		String left = StringUtil.substring(addrText, visitor.resultEndPosition()+1);
		LOG.info("> " + title + ": " + addrText + " --> " + division.toString() + " " + left);
		assertEquals(title + ": 省份错误", province, division.getProvince().getId());
		assertEquals(title + ": 地级市错误", city, division.getCity().getId());
		assertEquals(title + ": 区县错误", district, division.getDistrict().getId());
		if(town>0) assertEquals(title + ": 区县错误", town, division.getStreet().getId());
		assertEquals(title + ": 解析后的地址错误", expectedLeft, left);
	}
	
	@Test
	public void testRemoveRedundancy(){
		AddressInterpreter interpreter = context.getBean(AddressInterpreter.class);
		AddressPersister persister = context.getBean(AddressPersister.class);
		
		//测试正常删除冗余
		removeRedundancy(interpreter, persister, "湖南长沙望城区湖南省长沙市望城县长沙市望城区金星北路尚公馆", "金星北路尚公馆"
				, 430000, 430100, 430122, "测试-删除冗余");
		removeRedundancy(interpreter, persister, "山东青岛市南区山东省青岛市市南区宁德路金梦花园东门", "宁德路金梦花园东门"
				, 370000, 370200, 370202, "测试-删除冗余");
		removeRedundancy(interpreter, persister, "泾渭街道陕西省西安市高陵县泾河工业园泾欣园", "泾河工业园泾欣园"
				, 610000, 610100, 610126, "测试-删除冗余");
		removeRedundancy(interpreter, persister, "六安经济开发区安徽省六安市经济开发区经三路与寿春路交叉口", "经三路与寿春路交叉口"
				, 340000, 341500, 341502, "测试-删除冗余");
		
		//存在省直辖县级市【东方市】，在不进行限制的情况下，使用后序数组匹配省市区过程中能够得到省份（能够处理省份缺失情况）、
		//地级市、区县（省直辖县级市情况下无法匹配区县时会直接将区县设置为地级市的值）。
		removeRedundancy(interpreter, persister, "浏阳大道创意东方新天地小区7栋", "浏阳大道创意东方新天地小区7栋"
				, 430000, 430100, 430181, "测试-删除冗余");
		//同上，存在中山市
		removeRedundancy(interpreter, persister, "岳阳街道中山二路125弄75号102室", "岳阳街道中山二路125弄75号102室"
				, 430000, 430600, 430621, "测试-删除冗余");
		removeRedundancy(interpreter, persister, "嘉峪关路集散中心祥林货运部", "嘉峪关路集散中心祥林货运部"
				, 620000, 620200, 430621, "测试-删除冗余");
		
		removeRedundancy(interpreter, persister, "九峰镇东街52号", "九峰镇东街52号"
				, 620000, 620200, 430621, "测试-删除冗余");
		
		//删除冗余时，省份+区县完整，缺失地级市的情况
		removeRedundancy(interpreter, persister, "安徽省临泉县白庙镇白庙行政村刘庄37号", "白庙行政村刘庄37号"
				, 340000, 341200, 341221, "测试-删除冗余");
	}
	
	@Test
	public void testRemoveSpecialChar(){
		AddressInterpreter interpreter = context.getBean(AddressInterpreter.class);
		AddressEntity addr = new AddressEntity();
		
		addr.setText("四川成都武侯区武侯大道铁佛段千盛百货\\/ \r\n\t对面200米金履三路288号绿地610015圣路易名邸");
		interpreter.removeSpecialChars(addr);
		assertEquals("四川成都武侯区武侯大道铁佛段千盛百货对面200米金履三路288号绿地圣路易名邸", addr.getText());
	}
	
	
	@Test
	public void testExtractBracket(){
		AddressInterpreter interpreter = context.getBean(AddressInterpreter.class);
		AddressEntity addr = new AddressEntity();
		
		//测试正常抽取括号内容
		addr.setText("()四{}川{aa}(bb)成（）都（cc）武[]侯[dd]区【】武【ee】侯<>大<ff>道〈〉铁〈gg〉佛「」段「hh」千盛百货对面200米金履三路288号绿地圣路易名邸[]");
		String brackets = interpreter.extractBrackets(addr);
		assertEquals("aabbccddeeffgghh", brackets);
		assertEquals("四川成都武侯区武侯大道铁佛段千盛百货对面200米金履三路288号绿地圣路易名邸", addr.getText());
		
		//测试存在异常的情况
//		addr.setText("四川成都(武[]侯区武侯大道铁佛{aa}段千】盛百货对面200米金履三【bb】路288号绿地圣路易名邸");
//		brackets = service.extractBrackets(addr);
//		assertEquals("aabb", brackets);
//		assertEquals("四川成都(武侯区武侯大道铁佛段千】盛百货对面200米金履三路288号绿地圣路易名邸", addr.getText());
	}

	
	private void removeRedundancy(AddressInterpreter interpreter, AddressPersister persister
			, String text, String expected, int pid, int cid, int did, String title){
		RegionInterpreterVisitor visitor = new RegionInterpreterVisitor(persister);
		AddressEntity addr = new AddressEntity(text);
		addr.setProvince(persister.getRegion(pid));
		addr.setCity(persister.getRegion(cid));
		addr.setDistrict(persister.getRegion(did));
		interpreter.removeRedundancy(addr, visitor);
		LOG.info("> " + addr.getRawText() + " -> " + addr.getText());
		assertEquals(title + ": 删冗余后的结果错误", expected, addr.getText());
	}
	
	/**
	 * 从一批地址中删除冗余部分，根据日志记录的删除情况找出一些特殊格式，用作测试用例，以保证删除冗余的逻辑正确性。
	 */
	//@Ignore
	@Test
	public void testRemoveRedundancyFromAddressFile(){
		AddressInterpreter interpreter = context.getBean(AddressInterpreter.class);
		RegionInterpreterVisitor visitor = new RegionInterpreterVisitor(context.getBean(AddressPersister.class));
		
		File file = new File(AddressInterpretTest.class.getClassLoader().getResource("test-addresses.txt").getPath());
		InputStreamReader sr = null;
		BufferedReader br = null;
		try {
			sr = new InputStreamReader(new FileInputStream(file), "utf8");
			br = new BufferedReader(sr);
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
			return;
		}
		
		String line = null;
		try{
            while((line = br.readLine()) != null){
            	AddressEntity addr = new AddressEntity(line);
            	if(!interpreter.extractRegion(addr, visitor)) 
            		continue;
            	interpreter.extractBrackets(addr);
            	interpreter.removeSpecialChars(addr);
            	
            	AddressEntity removed = new AddressEntity(addr.getText());
            	removed.setProvince(addr.getProvince());
            	removed.setCity(addr.getCity());
            	removed.setDistrict(addr.getDistrict());
            	if(interpreter.removeRedundancy(removed, visitor))
            		LOG.info("> " + addr.getText() + " --> " + removed.getText());
            }
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		} finally{
			try {
				br.close();
			} catch (IOException e) { } 
			try {
				sr.close();
			} catch (IOException e) { }
		}
	}
}