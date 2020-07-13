package lt.vtmc.docTypes.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import lt.vtmc.docTypes.model.DocType;

/**
 * 
 * DocType data access object.
 *
 */
public interface DocTypeRepository extends JpaRepository<DocType, String> {

	DocType findDocTypeByName(String name);

	/**
	 * Finds doc types with pageable parameter and custom search phrase.
	 * 
	 * @param searchValueString search phrase
	 * @param firstPageable     to set amount of items per page, search phrase,
	 *                          sorting order
	 * @return page of document types requested in parameters
	 */
	@Query("SELECT d FROM DocType d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', ?1,'%'))")
	Page<DocType> findLike(String searchValueString, Pageable firstPageable);

	/**
	 * Finds list of document type names with pageable parameter.
	 * 
	 * @param searchValuString search phrase
	 * @param pageable         to set amount of items per page, search phrase,
	 *                         sorting order
	 * @return list of document type names
	 */
	@Query("select dt.name from DocType dt where LOWER(dt.name) LIKE LOWER(CONCAT('%', ?1,'%'))")
	Page<String> findLikeDocTypeNames(String searchValuString, Pageable pageable);

	/**
	 * Finds groups that approve specific document type by name.
	 * 
	 * @param name of document type
	 * @return list of groups approving
	 */
	@Query("select ga.name from DocType d join d.groupsApproving ga where d.name = ?1")
	List<String> findGroupsApprovingByDocTypeName(String name);

	/**
	 * Finds groups that create specific document type by name.
	 * 
	 * @param name of document type
	 * @return list of groups creating
	 */
	@Query("select gc.name from DocType d join d.groupsCreating gc where d.name = ?1")
	List<String> findGroupsCreatingByDocTypeName(String name);

}
