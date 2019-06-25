package model.services;

import java.util.List;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentService {
	
	private DepartmentDao dao = DaoFactory.createDepartmentDao();

	public List<Department> findAll() {	
		return dao.findAll();
	}
	
	public Department findByName(String deparmentName) {
		return dao.findByName(deparmentName);
	}
	
	public void saveOrUpdate(Department obj) {
		if(obj.getId() == null ) {
			dao.insert(obj);
		} 
		else {
			dao.update(obj);
		}
		
	}
	
	public void remove(Department obj) {
		
		dao.deleteById(obj.getId());
		
	}

}
