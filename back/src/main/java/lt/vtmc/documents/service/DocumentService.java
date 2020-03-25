package lt.vtmc.documents.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lt.vtmc.docTypes.dao.DocTypeRepository;
import lt.vtmc.docTypes.model.DocType;
import lt.vtmc.documents.Status;
import lt.vtmc.documents.dao.DocumentRepository;
import lt.vtmc.documents.dto.DocumentDetailsDTO;
import lt.vtmc.documents.model.Document;
import lt.vtmc.files.model.File4DB;
import lt.vtmc.files.service.FileService;
import lt.vtmc.paging.PagingData;
import lt.vtmc.paging.PagingResponse;
import lt.vtmc.user.dao.UserRepository;
import lt.vtmc.user.model.User;

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

	@Autowired
	private FileService fileService;

	/**
	 * This method finds a document from group repository by name.
	 * 
	 * @param UID document unique identification number
	 * @return Document type object
	 */

	public Document findDocumentByUID(String UID) {
		return docRepo.findDocumentByUID(UID);
	}

	/**
	 * Method to create new document.
	 * 
	 * @param name           of the document
	 * @param authorUsername is the username of user creating the document
	 * @param description    string value describing the document
	 * @param dType          is document type
	 * @param currentTime    local system time
	 * @return Document type object
	 */
	@Transactional
	public Document createDocument(String name, String authorUsername, String description, String dType,
			String currentTime) {
		Document newDocument = new Document(currentTime, userRepo.findUserByUsername(authorUsername),
				dTypeRepo.findDocTypeByName(dType), name, description, String.valueOf(System.currentTimeMillis()));
		List<Document> tmpList = userRepo.findUserByUsername(authorUsername).getCreatedDocuments();
		tmpList.add(newDocument);
		List<Document> tmpListDocuments = dTypeRepo.findDocTypeByName(dType).getDocumentList();
		tmpListDocuments.add(newDocument);
		newDocument.setFileList(new ArrayList<File4DB>());
		return docRepo.save(newDocument);
	}

	/**
	 * Returns all documents
	 * 
	 * @param pagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of items found
	 */
	public Map<String, Object> retrieveAllDocuments(PagingData pagingData) {
		Pageable firstPageable = pagingData.getPageable();
		Page<Document> documentlist = docRepo.findLike(pagingData.getSearchValueString(), firstPageable);
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("pagingData",
				new PagingResponse(documentlist.getNumber(), documentlist.getTotalElements(), documentlist.getSize()));
		responseMap.put("documentList", documentlist.getContent().stream().map(user -> new DocumentDetailsDTO(user))
				.collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Deletes a document from the system
	 * 
	 * @param Document to delete from database
	 */

	@Transactional
	public void deleteDocument(Document document) {
		List<File4DB> tmpList = document.getFileList();
		if (tmpList != null) {
			for (File4DB file4db : tmpList) {
				fileService.deleteFile(file4db);
			}
		}

		User author = document.getAuthor();
		User handler = document.getHandler();
		DocType docType = document.getdType();

		if (author != null) {
			List<Document> tmpListAuth = author.getCreatedDocuments();
			tmpListAuth.remove(document);
			author.setCreatedDocuments(tmpListAuth);
			document.setAuthor(null);
		}

		if (handler != null) {
			List<Document> tmpListHand = handler.getProcessedDocuments();
			tmpListHand.remove(document);
			handler.setProcessedDocuments(tmpListHand);
			document.setHandler(null);
		}
		if (docType != null) {
			List<Document> tmpListDocType = docType.getDocumentList();
			tmpListDocType.remove(document);
			docType.setDocumentList(tmpListDocType);
			document.setdType(null);
		}
		docRepo.delete(document);
	}

	/**
	 * Sets document status to Submitted
	 * 
	 * @param UID document unique identification number
	 */

	@Transactional
	public void setStatusPateiktas(String UID) {
		Document tmp = findDocumentByUID(UID);
		tmp.setDateSubmit(Instant.now().toString());
		tmp.setStatus(Status.SUBMITTED);
		docRepo.save(tmp);
	}

	/**
	 * Sets document status to Accepted
	 * 
	 * @param UID document unique identification number
	 */

	@Transactional
	public void setStatusPriimtas(String UID, String username) {
		Document tmp = findDocumentByUID(UID);
		tmp.setDateProcessed(Instant.now().toString());
		User tmpUser = userRepo.findUserByUsername(username);
		tmp.setHandler(tmpUser);
		List<Document> tmpList = tmpUser.getProcessedDocuments();
		System.out.println(tmpList.toString());
		tmpList.add(tmp);
		tmpUser.setProcessedDocuments(tmpList);
		tmp.setStatus(Status.ACCEPTED);
		userRepo.save(tmpUser);
		docRepo.save(tmp);
	}

	/**
	 * Sets document status to Rejected
	 * 
	 * @param UID            document unique identification number
	 * @param reasonToReject to explain why the document was rejected
	 */

	@Transactional
	public void setStatusAtmestas(String UID, String username, String reasonToReject) {
		Document tmp = findDocumentByUID(UID);
		tmp.setDateProcessed(Instant.now().toString());
		tmp.setReasonToReject(reasonToReject);
		User tmpUser = userRepo.findUserByUsername(username);
		tmp.setHandler(tmpUser);
		List<Document> tmpList = tmpUser.getProcessedDocuments();
		tmpList.add(tmp);
		tmpUser.setProcessedDocuments(tmpList);
		tmp.setStatus(Status.DECLINED);
		userRepo.save(tmpUser);
		docRepo.save(tmp);
	}

	/**
	 * Generates unique UID from time of creation
	 * 
	 * @param time to generate UID for the document from
	 * @return UID document unique identification number
	 */
	public String generateUID(String time) {
		StringBuilder UID = new StringBuilder();
		for (int i = 0; i < time.length(); i++) {
			if (Character.isDigit(time.charAt(i)) == true) {
				UID.append(time.charAt(i));
			}
		}
		return UID.toString();
	}

	/**
	 * Returns all documents by username
	 * 
	 * @param username   users' unique name within the system
	 * @param PagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of items found
	 */

	public Map<String, Object> returnAllDocumentsByUsername(String username, PagingData pagingData) {
		Pageable pageable = pagingData.getPageable();
		Page<Document> documents = userRepo.docsByUsername(username, pagingData.getSearchValueString(), pageable);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("pagingData",
				new PagingResponse(documents.getNumber(), documents.getTotalElements(), documents.getSize()));
		responseMap.put("documents",
				documents.getContent().stream().map(doc -> new DocumentDetailsDTO(doc)).collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Returns all documents by username for internal use
	 * 
	 * @param username users' unique name within the system
	 * @return getCreatedDocuments() users created documents
	 */

	public List<Document> findAllDocumentsByUsername(String username) {
		User tmpUser = userRepo.findUserByUsername(username);
		return tmpUser.getCreatedDocuments();
	}

	/**
	 * Returns all documents the user is permitted to approve/reject
	 * 
	 * @param username   users' unique name within the system
	 * @param PagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of items found
	 */

	public Map<String, Object> findAllDocumentsToSignByUsername(String username, PagingData pagingData) {
		Pageable pageable = pagingData.getPageable();
		Page<Document> documents = userRepo.docsToSignByUsername(username, pagingData.getSearchValueString(), pageable);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("pagingData",
				new PagingResponse(documents.getNumber(), documents.getTotalElements(), documents.getSize()));
		responseMap.put("documents",
				documents.getContent().stream().map(doc -> new DocumentDetailsDTO(doc)).collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Updates document details
	 * 
	 * @param docUID         documents Unique ID
	 * @param newName        new desired name for the document
	 * @param newDescription new desired description for the document
	 * @param newDocType     new desired type
	 * @param filesToRemove  from the document filelist
	 */

	@Transactional
	public void updateDocument(String docUID, String newName, String newDescription, String newDocType,
			String[] filesToRemove) {
		Document documentToUpdate = findDocumentByUID(docUID);
		documentToUpdate.setName(newName);
		documentToUpdate.setDescription(newDescription);
		List<Document> listToRemoveFrom = documentToUpdate.getdType().getDocumentList();
		listToRemoveFrom.remove(documentToUpdate);
		List<Document> listToAddTo = dTypeRepo.findDocTypeByName(newDocType).getDocumentList();
		listToAddTo.add(documentToUpdate);
		documentToUpdate.setdType(dTypeRepo.findDocTypeByName(newDocType));

		for (String file : filesToRemove) {
			fileService.deleteFileByUID(file);
		}

		docRepo.save(documentToUpdate);
	}

	/**
	 * Returns all documents the user submitted
	 * 
	 * @param username   users' unique name within the system
	 * @param PagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of items found
	 */

	public Map<String, Object> returnSubmitted(String username, PagingData pagingData) {
		Pageable pageable = pagingData.getPageable();
		Page<Document> documents = userRepo.docsByUsernameAndStatus(username, pagingData.getSearchValueString(),
				Status.SUBMITTED, pageable);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("pagingData",
				new PagingResponse(documents.getNumber(), documents.getTotalElements(), documents.getSize()));
		responseMap.put("documents",
				documents.getContent().stream().map(doc -> new DocumentDetailsDTO(doc)).collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Returns all documents with status accepted for the user
	 * 
	 * @param username   users' unique name within the system
	 * @param PagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of items found
	 */

	public Map<String, Object> returnAccepted(String username, PagingData pagingData) {
		Pageable pageable = pagingData.getPageable();
		Page<Document> documents = userRepo.docsByUsernameAndStatus(username, pagingData.getSearchValueString(),
				Status.ACCEPTED, pageable);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("pagingData",
				new PagingResponse(documents.getNumber(), documents.getTotalElements(), documents.getSize()));
		responseMap.put("documents",
				documents.getContent().stream().map(doc -> new DocumentDetailsDTO(doc)).collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Returns all documents with status rejected for the user
	 * 
	 * @param username   users' unique name within the system
	 * @param PagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of items found
	 */

	public Map<String, Object> returnRejected(String username, PagingData pagingData) {
		Pageable pageable = pagingData.getPageable();
		Page<Document> documents = userRepo.docsByUsernameAndStatus(username, pagingData.getSearchValueString(),
				Status.DECLINED, pageable);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("pagingData",
				new PagingResponse(documents.getNumber(), documents.getTotalElements(), documents.getSize()));
		responseMap.put("documents",
				documents.getContent().stream().map(doc -> new DocumentDetailsDTO(doc)).collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Returns all documents with status created for the user
	 * 
	 * @param username   users' unique name within the system
	 * @param PagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of items found
	 */

	public Map<String, Object> returnCreated(String username, PagingData pagingData) {
		Pageable pageable = pagingData.getPageable();
		Page<Document> documents = userRepo.docsByUsernameAndStatus(username, pagingData.getSearchValueString(),
				Status.CREATED, pageable);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("pagingData",
				new PagingResponse(documents.getNumber(), documents.getTotalElements(), documents.getSize()));
		responseMap.put("documents",
				documents.getContent().stream().map(doc -> new DocumentDetailsDTO(doc)).collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Deletes the document if the user requesting this is the author of the
	 * document
	 * 
	 * @param UID      unique document number
	 * @param username users' unique name within the system
	 */

	@Transactional
	public boolean deleteDocumentRequestedByUser(String uid, String username) {
		boolean doesUserHaveDoc = docRepo.doesUserHaveDoc(uid, username);

		if (doesUserHaveDoc) {
			deleteDocument(docRepo.findDocumentByUID(uid));
			return true;
		} else {
			return false;
		}
	}
}
