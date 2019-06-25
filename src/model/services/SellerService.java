package model.services;

import java.util.List;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Seller;

public class SellerService {

	// create Seller dao to connection a data base
	private SellerDao dao = DaoFactory.createSellerDao();

	//return all seller from the database
	public List<Seller> findAll() {
		return dao.findAll();
	}

	// this method save if id is null or update if id isn't null
	public void saveOrUpdate(Seller obj) {
		if (obj.getId() == null) {
			dao.insert(obj);
		} else {
			dao.update(obj);
		}
	}

	//the method remove a seller
	public void remove(Seller obj) {
		dao.deleteById(obj.getId());
	}
}
