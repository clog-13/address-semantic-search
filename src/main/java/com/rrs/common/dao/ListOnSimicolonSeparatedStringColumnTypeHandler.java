package com.rrs.common.dao;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

@MappedJdbcTypes(value = { JdbcType.VARCHAR, JdbcType.NVARCHAR, JdbcType.CHAR, JdbcType.NCHAR })
@MappedTypes(value = {List.class, ArrayList.class})
public class ListOnSimicolonSeparatedStringColumnTypeHandler extends BaseTypeHandler<List<String>> {
	@Override
	public void setParameter(PreparedStatement ps, int i, java.util.List<String> parameter, JdbcType jdbcType) throws SQLException {
		if(parameter==null) {
			ps.setString(i, "");
			return;
		}
		if(parameter.size()==1){
			ps.setString(i, parameter.get(0));
			return;
		}
		StringBuilder sb = new StringBuilder();
		for(int index = 0; index < parameter.size(); index++){
			if(index > 0) sb.append(';');
			sb.append(parameter.get(index));
		}
		ps.setString(i, sb.toString());
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
		this.setParameter(ps, i, parameter, jdbcType);
	}

	@Override
	public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return getColumnString(rs.getString(columnName));
	}

	@Override
	public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return getColumnString(rs.getString(columnIndex));
	}

	@Override
	public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return getColumnString(cs.getString(columnIndex));
	}

	private List<String> getColumnString(String string) {
		if(string ==null || string.length()<=0) return null;
		String[] tokens = string.split(";");
		List<String> result = new ArrayList<>(tokens.length);
		Collections.addAll(result, tokens);
		return result;
	}
}