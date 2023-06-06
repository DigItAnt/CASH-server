package it.cnr.ilc.lari.itant.cash.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.cnr.ilc.lari.itant.cash.DBManager;
import it.cnr.ilc.lari.itant.cash.exc.NodeNotFoundException;
import it.cnr.ilc.lari.itant.cash.om.AddFolderRequest;
import it.cnr.ilc.lari.itant.cash.om.AddFolderResponse;
import it.cnr.ilc.lari.itant.cash.om.CopyFileToRequest;
import it.cnr.ilc.lari.itant.cash.om.CopyFileToResponse;
import it.cnr.ilc.lari.itant.cash.om.CreateFileRequest;
import it.cnr.ilc.lari.itant.cash.om.DeleteMetadataRequest;
import it.cnr.ilc.lari.itant.cash.om.DeleteMetadataResponse;
import it.cnr.ilc.lari.itant.cash.om.DocumentSystemNode;
import it.cnr.ilc.lari.itant.cash.om.DownloadFileRequest;
import it.cnr.ilc.lari.itant.cash.om.DownloadFileResponse;
import it.cnr.ilc.lari.itant.cash.om.FileInfo;
import it.cnr.ilc.lari.itant.cash.om.MoveFileToRequest;
import it.cnr.ilc.lari.itant.cash.om.MoveFileToResponse;
import it.cnr.ilc.lari.itant.cash.om.MoveFolderRequest;
import it.cnr.ilc.lari.itant.cash.om.MoveFolderResponse;
import it.cnr.ilc.lari.itant.cash.om.RemoveFileRequest;
import it.cnr.ilc.lari.itant.cash.om.RemoveFileResponse;
import it.cnr.ilc.lari.itant.cash.om.RemoveFolderRequest;
import it.cnr.ilc.lari.itant.cash.om.RemoveFolderResponse;
import it.cnr.ilc.lari.itant.cash.om.RenameFileRequest;
import it.cnr.ilc.lari.itant.cash.om.RenameFileResponse;
import it.cnr.ilc.lari.itant.cash.om.RenameFolderRequest;
import it.cnr.ilc.lari.itant.cash.om.RenameFolderResponse;
import it.cnr.ilc.lari.itant.cash.om.UpdateMetadataRequest;
import it.cnr.ilc.lari.itant.cash.om.UpdateMetadataResponse;
import it.cnr.ilc.lari.itant.cash.om.UploadFileResponse;
import it.cnr.ilc.lari.itant.cash.utils.LogUtils;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@CrossOrigin
@RestController
public class CRUDController {
	private static final Logger log = LoggerFactory.getLogger(CRUDController.class);

	@PostMapping("/api/crud/addFolder")
	public AddFolderResponse addFolder(@RequestBody AddFolderRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		AddFolderResponse toret = new AddFolderResponse();
		toret.setRequestUUID(request.getRequestUUID());
		long fid = DBManager.addFolder(request.getElementId());
		toret.setNode(DocumentSystemNode.populateNode(fid));
		return toret;
	}

	@PostMapping("/api/crud/renameFolder")
	public RenameFolderResponse renameFolder(@RequestBody RenameFolderRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		RenameFolderResponse toret = new RenameFolderResponse();
		toret.setRequestUUID(request.getRequestUUID());
		DBManager.renameNode(request.getElementId(), request.getRenameString());
		return toret;
	}

	@PostMapping("/api/crud/removeFolder")
	public RemoveFolderResponse removeFolder(@RequestBody RemoveFolderRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		RemoveFolderResponse toret = new RemoveFolderResponse();
		toret.setRequestUUID(request.getRequestUUID());
		DBManager.removeNode(request.getElementId());
		return toret;
	}

	@PostMapping("/api/crud/moveFolder")
	public MoveFolderResponse moveFolder(@RequestBody MoveFolderRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		MoveFolderResponse toret = new MoveFolderResponse();
		DBManager.moveNode(request.getElementId(), request.getTargetId());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/removeFile")
	public RemoveFileResponse removeFile(@RequestBody RemoveFileRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		RemoveFileResponse toret = new RemoveFileResponse();
		DBManager.removeNode(request.getElementId());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/renameFile")
	public RenameFileResponse renameFile(@RequestBody RenameFileRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		RenameFileResponse toret = new RenameFileResponse();
		DBManager.renameNode(request.getElementId(), request.getRenameString());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/copyFileTo")
	public CopyFileToResponse copyFileTo(@RequestBody CopyFileToRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		CopyFileToResponse toret = new CopyFileToResponse();
		FileInfo finfo = DBManager.copyNode(request.getElementId(), request.getTargetId());
		toret.setRequestUUID(request.getRequestUUID());
		toret.setNode(new DocumentSystemNode(finfo, true));
		return toret;
	}

	@PostMapping("/api/crud/moveFileTo")
	public MoveFileToResponse moveFileTo(@RequestBody MoveFileToRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		MoveFileToResponse toret = new MoveFileToResponse();
		DBManager.moveNode(request.getElementId(), request.getTargetId());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/updateMetadata")
	public UpdateMetadataResponse updateMetadata(@RequestBody UpdateMetadataRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		UpdateMetadataResponse toret = new UpdateMetadataResponse();
		DBManager.updateNodeMetadata(request.getElementId(), request.getMetadata());
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/deleteMetadata")
	public DeleteMetadataResponse deleteMetadata(@RequestBody DeleteMetadataRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

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
										 @RequestParam("file") MultipartFile file, Principal principal) throws Exception {
		if ( principal != null ) log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		UploadFileResponse toret = new UploadFileResponse();
		InputStream fis = file.getInputStream();
		long fid = DBManager.addFile(elementID, file.getOriginalFilename(), fis, file.getContentType());
		log.info("File created as node with id: " + fid);
		toret.setNode(DocumentSystemNode.populateNode(fid));
		toret.setRequestUUID(requestUUID);
		return toret;
	}

	@RequestMapping(
    path = "/api/crud/updateFileMetadata", 
    method = RequestMethod.POST, 
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)	
	public UploadFileResponse updateFileMetadata(@RequestParam("requestUUID") String requestUUID, 
										         @RequestParam("element-id") Integer elementID,
										         @RequestParam("file") MultipartFile file, Principal principal) throws Exception {
		if ( principal != null ) log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		UploadFileResponse toret = new UploadFileResponse();
		InputStream fis = file.getInputStream();
		long fid = DBManager.updateFileMetadata(elementID, file.getOriginalFilename(), fis, file.getContentType());
		log.info("File id: " + fid + " medatada updated");
		toret.setNode(DocumentSystemNode.populateNode(fid));
		toret.setRequestUUID(requestUUID);
		return toret;
	}

	@PostMapping("/api/crud/createFile")
	public UploadFileResponse createFile(@RequestBody CreateFileRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		UploadFileResponse toret = new UploadFileResponse();
		InputStream fis = new ByteArrayInputStream("".getBytes("UtF-8"));
		long fid = DBManager.addFile(request.getElementId(), request.getFilename(), fis, null);
		log.info("File created as node with id: " + fid);
		toret.setNode(DocumentSystemNode.populateNode(fid));
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/public/crud/downloadFile")
	public DownloadFileResponse downloadFile(@RequestBody DownloadFileRequest request, Principal principal) throws Exception {
		log.info(LogUtils.CASH_INVOCATION_LOG_MSG, LogUtils.getPrincipalName(principal));

		// TODO: Return File!!
		FileInfo node = DBManager.getNodeById(request.getElementId()); // also raises exception if needed
		if ( node == null ) {
            log.error("Cannot download non-existent node " + request.getElementId());
            throw new NodeNotFoundException();
        }
		if ( node.getType() != DocumentSystemNode.FileDirectory.file ) {
			log.error("Cannot download non-file node " + request.getElementId());
			throw new it.cnr.ilc.lari.itant.cash.exc.InvalidParamException();
		}
		String content = DBManager.getRawContent(node.getElementId(), null);
		
        // Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", node.getName());

		DownloadFileResponse toret = new DownloadFileResponse(content, headers);

		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}
}
