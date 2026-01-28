package com.javaweb.repository.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import com.javaweb.builder.BuildingSearchBuilder;
import com.javaweb.repository.BuildingRepository;
import com.javaweb.repository.entity.BuildingEntity;
import com.javaweb.utils.ConnectionJDBCUtil;
import com.javaweb.utils.NumberUntil;
import com.javaweb.utils.StringUtil;

@Repository
@PropertySource("classpath:application.properties")
public class JDBCBuildingRepositoryImpl implements BuildingRepository {
	@Value("${spring.datasource.url}")
	private String DB_URL;
	
	@Value("${spring.datasource.username}")
	private String USER;
	
	@Value("${spring.datasource.password}")
	private String PASS = "123456";
	
	public static void joinTable(BuildingSearchBuilder buildingSearchBuilder, StringBuilder sql) {
		Long staffId = buildingSearchBuilder.getStaffId();
		if (staffId != null) {
			sql.append(" INNER JOIN assignmentbuilding ON b.id = assignmentbuilding.buildingid ");
		}
		List<String> typeCode = buildingSearchBuilder.getTypeCode();
		if (typeCode != null && typeCode.size() != 0) {
			sql.append(" INNER JOIN buildingrenttype ON b.id = buildingrenttype.buildingid ");
			sql.append(" INNER JOIN renttype ON renttype.id = buildingrenttype.renttypeid ");
		}
	}
	
	public static void queryNomal(BuildingSearchBuilder buildingSearchBuilder, StringBuilder where) {
		try {
			Field[] fields = BuildingSearchBuilder.class.getDeclaredFields();
			for (Field item : fields) {
				item.setAccessible(true);
				String fieldName = item.getName();
				if (!fieldName.equals("staffId") && !fieldName.equals("typeCode") && !fieldName.startsWith("area")
						&& !fieldName.startsWith("rentPrice")) {
					Object value = item.get(buildingSearchBuilder);
					if (value != null) {
						if (item.getType().getName().equals("java.lang.Long") || item.getType().getName().equals("java.lang.Integer")) {
							where.append(" AND b." + fieldName + " = " + value);
						} else if(item.getType().getName().equals("java.lang.String")) {
							where.append(" AND b." + fieldName + " LIKE '%" + value + "%' ");
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void querySpecial(BuildingSearchBuilder buildingSearchBuilder, StringBuilder where) {
		Long staffId = buildingSearchBuilder.getStaffId();
	    if (staffId != null) {
	        where.append(" AND assignmentbuilding.staffid = " + staffId);
	    }

	    Long rentAreaTo = buildingSearchBuilder.getAreaTo();
	    Long rentAreaFrom = buildingSearchBuilder.getAreaFrom();

	    if (rentAreaFrom != null || rentAreaTo != null) {
	    		where.append(" AND EXISTS (SELECT * FROM rentarea r WHERE b.id = r.buildingid ");
	        if (rentAreaFrom != null) {
	            where.append(" AND r.value >= " + rentAreaFrom);
	        }

	        if (rentAreaTo != null) {
	            where.append(" AND r.value <= " + rentAreaTo);
	        }
	        where.append(") ");
	    }
	    
	    String rentPriceTo = buildingSearchBuilder.getRentPriceTo().toString();
	    String rentPriceFrom = buildingSearchBuilder.getRentPriceFrom().toString();

	    if (rentPriceTo != null || rentPriceFrom != null) {
	        if (rentAreaFrom != null) {
	            where.append(" AND b.rentprice >= " + rentPriceFrom);
	        }

	        if (rentAreaTo != null) {
	            where.append(" AND b.rentprice <= " + rentPriceTo);
	        }
	    }
	    // Java 8
	    List<String> typeCode = buildingSearchBuilder.getTypeCode();
	    if (typeCode != null && typeCode.size() != 0) {
	    		where.append(" AND(");
	    		String sql = typeCode.stream().map(it-> "renttype.code Like" + "'%" + it + "%' ").collect(Collectors.joining(" OR "));
	    		where.append(sql);
	    		where.append(" ) ");
	    }
	}

	
	@Override
	public List<BuildingEntity> findAll(BuildingSearchBuilder buildingSearchBuilder) {
		StringBuilder sql = new StringBuilder(
			    "SELECT b.id, b.name, b.districtid, b.street, b.ward, " +
			    "b.numberofbasement, b.floorarea, b.rentprice, " +
			    "b.managername, b.managerphonenumber, b.servicefee, b.brokeragefee " +
			    "\nFROM building b "
			);
		joinTable (buildingSearchBuilder, sql);
		StringBuilder where = new StringBuilder(" WHERE 1=1 ");
		queryNomal(buildingSearchBuilder, where);
		querySpecial(buildingSearchBuilder, where);
		where.append(" GROUP BY b.id;");
		sql.append(where);
		List<BuildingEntity> result = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql.toString());) {

			while (rs.next()) {
				BuildingEntity buildingEntity = new BuildingEntity();
				
				buildingEntity.setId(rs.getLong("b.id"));
				buildingEntity.setName(rs.getString("b.name"));
				buildingEntity.setWard(rs.getString("b.ward"));
				buildingEntity.setStreet(rs.getString("b.street"));
				buildingEntity.setFloorArea(rs.getLong("b.floorarea"));
				buildingEntity.setRentPrice(rs.getLong("b.rentprice"));
				buildingEntity.setServiceFee(rs.getString("b.servicefee"));
				buildingEntity.setBrokerageFee(rs.getLong("b.brokeragefee"));
				buildingEntity.setManagerName(rs.getString("b.managername"));
				buildingEntity.setManagerPhoneNumber(rs.getString("b.managerphonenumber"));
				result.add(buildingEntity);
			}
			System.out.println("Connected database successfully...");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Connected database failed...");
		}
		return result;
	}

	@Override
	public void DeleteById(Long id) {
		// TODO Auto-generated method stub

	}

}
