package it.cnr.ilc.lari.itant.belexo.controller;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.cnr.ilc.lari.itant.belexo.DBManager;
import it.cnr.ilc.lari.itant.belexo.om.AddFolderRequest;
import it.cnr.ilc.lari.itant.belexo.om.AddFolderResponse;
import it.cnr.ilc.lari.itant.belexo.om.CopyFileToRequest;
import it.cnr.ilc.lari.itant.belexo.om.CopyFileToResponse;
import it.cnr.ilc.lari.itant.belexo.om.DeleteMetadataRequest;
import it.cnr.ilc.lari.itant.belexo.om.DeleteMetadataResponse;
import it.cnr.ilc.lari.itant.belexo.om.DocumentSystemNode;
import it.cnr.ilc.lari.itant.belexo.om.DownloadFileRequest;
import it.cnr.ilc.lari.itant.belexo.om.DownloadFileResponse;
import it.cnr.ilc.lari.itant.belexo.om.FileInfo;
import it.cnr.ilc.lari.itant.belexo.om.MoveFileToRequest;
import it.cnr.ilc.lari.itant.belexo.om.MoveFileToResponse;
import it.cnr.ilc.lari.itant.belexo.om.MoveFolderRequest;
import it.cnr.ilc.lari.itant.belexo.om.MoveFolderResponse;
import it.cnr.ilc.lari.itant.belexo.om.RemoveFileRequest;
import it.cnr.ilc.lari.itant.belexo.om.RemoveFileResponse;
import it.cnr.ilc.lari.itant.belexo.om.RemoveFolderRequest;
import it.cnr.ilc.lari.itant.belexo.om.RemoveFolderResponse;
import it.cnr.ilc.lari.itant.belexo.om.RenameFileRequest;
import it.cnr.ilc.lari.itant.belexo.om.RenameFileResponse;
import it.cnr.ilc.lari.itant.belexo.om.RenameFolderRequest;
import it.cnr.ilc.lari.itant.belexo.om.RenameFolderResponse;
import it.cnr.ilc.lari.itant.belexo.om.UpdateMetadataRequest;
import it.cnr.ilc.lari.itant.belexo.om.UpdateMetadataResponse;
import it.cnr.ilc.lari.itant.belexo.om.UploadFileResponse;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@CrossOrigin
@RestController
public class CRUDController {
	private static final Logger log = LoggerFactory.getLogger(CRUDController.class);

	@PostMapping("/api/crud/addFolder")
	public AddFolderResponse addFolder(@RequestBody AddFolderRequest request) throws Exception {
		AddFolderResponse toret = new AddFolderResponse();
		toret.setRequestUUID(request.getRequestUUID());
		long fid = DBManager.addFolder(request.getElementId());
		toret.setNode(DocumentSystemNode.populateNode(fid));
		return toret;
	}

	@PostMapping("/api/crud/renameFolder")
	public RenameFolderResponse renameFolder(@RequestBody RenameFolderRequest request) throws Exception {
		PodamFactory factory = new PodamFactoryImpl();
		RenameFolderResponse toret = factory.manufacturePojo(RenameFolderResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		DBManager.renameNode(request.getElementId(), request.getRenameString());
		toret.setDocumentSystem(DocumentSystemNode.populateTree(DBManager.getRootNodeId()));
		return toret;
	}

	@PostMapping("/api/crud/removeFolder")
	public RemoveFolderResponse removeFolder(@RequestBody RemoveFolderRequest request) throws Exception {
		PodamFactory factory = new PodamFactoryImpl();
		RemoveFolderResponse toret = factory.manufacturePojo(RemoveFolderResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		DBManager.removeNode(request.getElementId());
		toret.setDocumentSystem(DocumentSystemNode.populateTree(DBManager.getRootNodeId()));
		return toret;
	}

	@PostMapping("/api/crud/moveFolder")
	public MoveFolderResponse moveFolder(@RequestBody MoveFolderRequest request) throws Exception {
		PodamFactory factory = new PodamFactoryImpl();
		MoveFolderResponse toret = factory.manufacturePojo(MoveFolderResponse.class);
		DBManager.moveNode(request.getElementId(), request.getTargetId());
		toret.setDocumentSystem(DocumentSystemNode.populateTree(DBManager.getRootNodeId()));
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/removeFile")
	public RemoveFileResponse removeFile(@RequestBody RemoveFileRequest request) throws Exception {
		RemoveFileResponse toret = new RemoveFileResponse();
		DBManager.removeNode(request.getElementId());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/renameFile")
	public RenameFileResponse renameFile(@RequestBody RenameFileRequest request) throws Exception {
		RenameFileResponse toret = new RenameFileResponse();
		DBManager.renameNode(request.getElementId(), request.getRenameString());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/copyFileTo")
	public CopyFileToResponse copyFileTo(@RequestBody CopyFileToRequest request) throws Exception {
		CopyFileToResponse toret = new CopyFileToResponse();
		DBManager.copyNode(request.getElementId(), request.getTargetId());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/moveFileTo")
	public MoveFileToResponse moveFileTo(@RequestBody MoveFileToRequest request) throws Exception {
		MoveFileToResponse toret = new MoveFileToResponse();
		DBManager.moveNode(request.getElementId(), request.getTargetId());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/updateMetadata")
	public UpdateMetadataResponse updateMetadata(@RequestBody UpdateMetadataRequest request) throws Exception {
		UpdateMetadataResponse toret = new UpdateMetadataResponse();
		DBManager.updateNodeMetadata(request.getElementId(), request.getMetadata());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/deleteMetadata")
	public DeleteMetadataResponse deleteMetadata(@RequestBody DeleteMetadataRequest request) throws Exception {
		DeleteMetadataResponse toret = new DeleteMetadataResponse();
		DBManager.deleteNodeMetadata(request.getElementId());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@RequestMapping(
    path = "/api/crud/uploadFile", 
    method = RequestMethod.POST, 
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)	
	public UploadFileResponse uploadFile(@RequestParam("requestUUID") String requestUUID, 
										 @RequestParam("element-id") Integer elementID, 
										 @RequestParam("file") MultipartFile file) throws Exception {
		UploadFileResponse toret = new UploadFileResponse();
		InputStream fis = file.getInputStream();
		long fid = DBManager.addFile(elementID, file.getOriginalFilename(), fis, file.getContentType());
		log.info("File created as node with id: " + fid);
		toret.setNode(DocumentSystemNode.populateTree(fid));
		//toret.setDocumentSystem(DocumentSystemNode.empty());
		toret.setRequestUUID(requestUUID);
		return toret;
	}

	@PostMapping("/api/crud/downloadFile")
	public DownloadFileResponse downloadFile(@RequestBody DownloadFileRequest request) throws Exception {
		// TODO: Return File!!
		FileInfo node = DBManager.getNodeById(request.getElementId());
		PodamFactory factory = new PodamFactoryImpl();
		DownloadFileResponse toret = factory.manufacturePojo(DownloadFileResponse.class);
		// TODO
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}
}
