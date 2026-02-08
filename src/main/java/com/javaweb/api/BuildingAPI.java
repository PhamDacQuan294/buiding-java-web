package com.javaweb.api;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javaweb.model.BuildingDTO;
import com.javaweb.model.BuildingRequestDTO;
import com.javaweb.repository.BuildingRepository;
import com.javaweb.repository.entity.BuildingEntity;
import com.javaweb.repository.entity.DistrictEntity;
import com.javaweb.service.BuildingService;

import customexception.FieldRequiredException;

@RestController
@PropertySource("classpath:application.properties")
@Transactional
public class BuildingAPI {
	@Autowired
	private BuildingService buildingService;
	
	@Autowired
	private BuildingRepository buildingRepository;
	
	@Value("${dev.nguyen}")
	private String data;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@GetMapping(value = "/api/building/")
	public List<BuildingDTO> getBuilding(@RequestParam Map<String, Object> params,
										@RequestParam(name="typeCode", required = false) List<String> typeCode) {
		List<BuildingDTO> result = buildingService.findAll(params, typeCode);
		return result;
	}
	
	@GetMapping(value = "/api/building/{id}")
	public BuildingDTO getBuildingById(@PathVariable Long id) {
		BuildingDTO result = new BuildingDTO();
		BuildingEntity building = buildingRepository.findById(id).get();
		return result;
	}

	@GetMapping(value = "/api/building/{name}/{street}")
	public BuildingDTO getBuildingByName(@PathVariable String name, @PathVariable String street) {
		BuildingDTO result = new BuildingDTO();
		List<BuildingEntity> building = buildingRepository.findByNameContainingAndStreet(name, street);
		return result;
	}

	
	@PostMapping(value="/api/building/")
	public void creatBuilding(@RequestBody BuildingRequestDTO buildingRequestDTO) {
	    BuildingEntity builEntity = new BuildingEntity();
	    builEntity.setName(buildingRequestDTO.getName());
	    builEntity.setStreet(buildingRequestDTO.getStreet());
	    builEntity.setWard(buildingRequestDTO.getWard());
	    DistrictEntity districtEntity = new DistrictEntity();
	    districtEntity.setId(buildingRequestDTO.getDistrictId());
	    builEntity.setDistrict(districtEntity);
	    entityManager.persist(builEntity);
	    System.out.print("ok");
	    // Đoạn code này làm ở dưới tầng Repo làm ở đây chỉ để test cho nhanh dễ nhìn
	}

	@PutMapping(value="/api/building/")
	public void updateBuilding(@RequestBody BuildingRequestDTO buildingRequestDTO) {
//	    BuildingEntity builEntity = buildingRepository.findById(buildingRequestDTO.getId()).get();
		BuildingEntity builEntity = new BuildingEntity();
	    builEntity.setId(1L);
	    builEntity.setName(buildingRequestDTO.getName());
	    builEntity.setStreet(buildingRequestDTO.getStreet());
	    builEntity.setWard(buildingRequestDTO.getWard());
	    DistrictEntity districtEntity = new DistrictEntity();
	    districtEntity.setId(buildingRequestDTO.getDistrictId());
	    builEntity.setDistrict(districtEntity);
	    buildingRepository.save(builEntity);
	    System.out.print("ok");
	}

	@DeleteMapping(value="/api/building/{ids}")
	public void deleteBuilding(@PathVariable Long[] ids) {
		buildingRepository.deleteByIdIn(ids);
	}
	
	public void valiDate(BuildingDTO buildingDTO) {
		if (buildingDTO.getName() == null || buildingDTO.getName().equals("")) {
			throw new FieldRequiredException("name is null");
		}
	}
}
