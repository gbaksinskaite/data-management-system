package lt.vtmc.files.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lt.vtmc.documents.dao.DocumentRepository;
import lt.vtmc.documents.model.Document;
import lt.vtmc.documents.service.DocumentService;
import lt.vtmc.files.dao.FilesRepository;
import lt.vtmc.files.dto.FileDetailsDTO;
import lt.vtmc.files.model.File4DB;
import lt.vtmc.user.controller.UserController;

/**
 * Service layer for uploading and downloading files. Note that files that are
 * being uploaded are saved on database as BLOBs.
 * 
 * @author pra-va
 *
 */
@Service
public class FileService {

	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private FilesRepository filesRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private DocumentService docService;

	/**
	 * This method saves single files to a database.
	 * 
	 * @param file to be saved to database
	 * @param doc  document owner of file
	 * @throws Exception if saving file fails
	 */
	@Transactional
	public void saveFile(MultipartFile file, Document doc) throws Exception {
		LOG.info("saving file to db");
		byte[] bytes = file.getBytes();
		String fileName = file.getOriginalFilename();
		String fileType = file.getContentType();
		long fileSize = file.getSize();
		File4DB file4db = new File4DB(fileName, fileType, bytes, docService.generateUID(Instant.now().toString()),
				fileSize);
		file4db.setDocument(doc);
		List<File4DB> tmplist = doc.getFileList();
		tmplist.add(file4db);
		doc.setFileList(tmplist);
		filesRepository.save(file4db);
		documentRepository.save(doc);
	}

	/**
	 * This method handles single file download from database. It looks for specific
	 * file name and returns this file as response entity.
	 * 
	 * @param fileUID that is requested to download
	 * @return response entity with file
	 */
	@Transactional
	public ResponseEntity<Resource> downloadFileByUID(String fileUID) {
		File4DB file4db = filesRepository.findFile4dbByUID(fileUID);
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(file4db.getFileType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file4db.getFileName() + "\"")
				.body(new ByteArrayResource(file4db.getData()));
	}

	/**
	 * Will turn requested file in bytes array to response entity of type resource.
	 * 
	 * @param fileByteData to convert to response entity
	 * @param fileName     for new file
	 * @return ResponseEntity with file
	 */
	@Transactional
	public ResponseEntity<Resource> downloadFileByUID(byte[] fileByteData, String fileName) {
		return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
				.body(new ByteArrayResource(fileByteData));
	}

	/**
	 * This method returns all file details created by username
	 * 
	 * @param username of owner of files
	 * @return file details transfer object
	 */

	public List<FileDetailsDTO> findAllFileDetailsByUsername(String username) {
		List<Document> tmpList = docService.findAllDocumentsByUsername(username);
		Set<File4DB> listToReturn = new HashSet<File4DB>();
		for (Document document : tmpList) {
			List<File4DB> tmp = document.getFileList();
			listToReturn.addAll(tmp);
		}
		List<FileDetailsDTO> returnList = new ArrayList<FileDetailsDTO>();
		for (File4DB file4db : listToReturn) {
			returnList.add(new FileDetailsDTO(file4db));
		}
		return returnList;
	}

	/**
	 * This method returns all file details attached to document
	 * 
	 * @param UID of document
	 * @return list of file details
	 */

	public List<FileDetailsDTO> findAllFileDetailsByDocument(String UID) {
		Document tmpDoc = docService.findDocumentByUID(UID);
		Set<File4DB> listToReturn = new HashSet<File4DB>();
		listToReturn.addAll(tmpDoc.getFileList());
		List<FileDetailsDTO> returnList = new ArrayList<FileDetailsDTO>();
		for (File4DB file4db : listToReturn) {
			returnList.add(new FileDetailsDTO(file4db));
		}
		return returnList;
	}

	/**
	 * Returns Resource type ResponseEntity for users uploaded files and documents.
	 * 
	 * @param username of csv file owner
	 * @return response entity with csv file
	 */

	public ResponseEntity<Resource> generateCSV(String username) {
		String csv = null;
		try {
			csv = getCsv(username);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return downloadFileByUID(csv.getBytes(), "file.csv");
	}

	/**
	 * Returns string representation of csv file.
	 * 
	 * @param username of generated csv file owner
	 * @return String representation of csv file.
	 */
	public String getCsv(String username) {
		List<FileDetailsDTO> usersFilesDetails = findAllFileDetailsByUsername(username);
		StringBuilder builder = new StringBuilder();
		builder.append(
				"\"File ID\", \"File Name\", \"Document ID\", \"Document Name\", \"Document Type\", \"Created\"\n");
		for (FileDetailsDTO fileDetailsDTO : usersFilesDetails) {
			builder.append(fileDetailsDTO.getCsvDetails());
		}
		return builder.toString();
	}

	/**
	 * This method will find all files by username provided as parameter.
	 * 
	 * @param username of files owner
	 * @return Map of files that belong to a user
	 */
	public Map<String, ByteArrayResource> findAllFilesByUsername(String username) {
		List<FileDetailsDTO> usersFilesDetails = findAllFileDetailsByUsername(username);
		Map<String, ByteArrayResource> filesAsBytes = new HashMap<>();
		for (FileDetailsDTO file : usersFilesDetails) {
			filesAsBytes.put(file.getFileName(),
					new ByteArrayResource(filesRepository.findFile4dbByUID(file.getUID()).getData()));
		}
		return filesAsBytes;
	}

	/**
	 * Deletes file from the system by UID
	 * 
	 * @param uID of file that should be deleted
	 */

	public void deleteFileByUID(String uID) {
		File4DB tmpFile = filesRepository.findFile4dbByUID(uID);
		tmpFile.getDocument().getFileList().remove(tmpFile);
		filesRepository.delete(tmpFile);
	}

	/**
	 * Deletes file from the system
	 * 
	 * @param tmpFile representation of file to be deleted
	 */

	public void deleteFile(File4DB tmpFile) {
		filesRepository.delete(tmpFile);
	}
}
