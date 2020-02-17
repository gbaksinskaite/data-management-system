package lt.vtmc.documents.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lt.vtmc.docTypes.dao.DocTypeRepository;
import lt.vtmc.documents.dao.DocumentRepository;
import lt.vtmc.documents.dto.DocumentDetailsDTO;
import lt.vtmc.documents.model.Document;
import lt.vtmc.user.dao.UserRepository;
/**
 * Document service for creating and managing documents.
 * 
 * @author VStoncius
 *
 */
@Service
public class DocumentService {
	
	@Autowired
	private DocumentRepository docRepo;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private DocTypeRepository dTypeRepo;
	/**
	 * 
	 * This method finds a document from group repository by name.
	 * 
	 * @return Document
	 */
	public DocumentDetailsDTO findDocumentByName(String name) {
		return new DocumentDetailsDTO(docRepo.findDocumentByName(name));
	}
	
	/**
	 * Method to create new document.
	 * 
	 * @return Document
	 */
	@Transactional
	public Document createDocument(String name, String authorUsername, String description, String dType) {
		Document newDocument = new Document("Time 123", userRepo.findUserByUsername(authorUsername), dTypeRepo.findDocTypeByName(dType), name, description);
		newDocument.setDateProcessed(null);
		newDocument.setDateSubmit(null);
		newDocument.setHandler(null);
		newDocument.setReasonToReject(null);
		return docRepo.save(newDocument);
	}

	public List<DocumentDetailsDTO> findAll() {
		List<Document> tmpList = docRepo.findAll();
		List<DocumentDetailsDTO>list = new ArrayList<DocumentDetailsDTO>();
		for (int i = 0; i < tmpList.size(); i++) {
			list.add(new DocumentDetailsDTO(tmpList.get(i)));
		}
		return list;
	}
}
