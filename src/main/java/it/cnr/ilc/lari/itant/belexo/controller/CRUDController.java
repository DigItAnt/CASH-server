package it.cnr.ilc.lari.itant.belexo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.cnr.ilc.lari.itant.belexo.JcrManager;
import it.cnr.ilc.lari.itant.belexo.om.AddFolderRequest;
import it.cnr.ilc.lari.itant.belexo.om.AddFolderResponse;
import it.cnr.ilc.lari.itant.belexo.om.CopyFileToRequest;
import it.cnr.ilc.lari.itant.belexo.om.CopyFileToResponse;
import it.cnr.ilc.lari.itant.belexo.om.DeleteMetadataRequest;
import it.cnr.ilc.lari.itant.belexo.om.DeleteMetadataResponse;
import it.cnr.ilc.lari.itant.belexo.om.DownloadFileRequest;
import it.cnr.ilc.lari.itant.belexo.om.DownloadFileResponse;
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
import it.cnr.ilc.lari.itant.belexo.om.UploadFileRequest;
import it.cnr.ilc.lari.itant.belexo.om.UploadFileResponse;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@CrossOrigin
@RestController
public class CRUDController {
	@PostMapping("/api/crud/addFolder")
	public AddFolderResponse addFolder(@RequestBody AddFolderRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		AddFolderResponse toret = factory.manufacturePojo(AddFolderResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		JcrManager.addFolder(request.getElementId());
		return toret;
	}

	@PostMapping("/api/crud/renameFolder")
	public RenameFolderResponse renameFolder(@RequestBody RenameFolderRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		RenameFolderResponse toret = factory.manufacturePojo(RenameFolderResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/removeFolder")
	public RemoveFolderResponse removeFolder(@RequestBody RemoveFolderRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		RemoveFolderResponse toret = factory.manufacturePojo(RemoveFolderResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		JcrManager.removeFolder(request.getElementId());
		return toret;
	}

	@PostMapping("/api/crud/moveFolder")
	public MoveFolderResponse moveFolder(@RequestBody MoveFolderRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		MoveFolderResponse toret = factory.manufacturePojo(MoveFolderResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/removeFile")
	public RemoveFileResponse removeFile(@RequestBody RemoveFileRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		RemoveFileResponse toret = factory.manufacturePojo(RemoveFileResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/renameFile")
	public RenameFileResponse renameFile(@RequestBody RenameFileRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		RenameFileResponse toret = factory.manufacturePojo(RenameFileResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/copyFileTo")
	public CopyFileToResponse copyFileTo(@RequestBody CopyFileToRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		CopyFileToResponse toret = factory.manufacturePojo(CopyFileToResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/moveFileTo")
	public MoveFileToResponse moveFileTo(@RequestBody MoveFileToRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		MoveFileToResponse toret = factory.manufacturePojo(MoveFileToResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/updateMetadata")
	public UpdateMetadataResponse updateMetadata(@RequestBody UpdateMetadataRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		UpdateMetadataResponse toret = factory.manufacturePojo(UpdateMetadataResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/deleteMetadata")
	public DeleteMetadataResponse deleteMetadata(@RequestBody DeleteMetadataRequest request) {
		PodamFactory factory = new PodamFactoryImpl();
		DeleteMetadataResponse toret = factory.manufacturePojo(DeleteMetadataResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/uploadFile")
	public UploadFileResponse uploadFile(@RequestBody UploadFileRequest request) {
		// TODO: Missing the file StreamBuffer!
		PodamFactory factory = new PodamFactoryImpl();
		UploadFileResponse toret = factory.manufacturePojo(UploadFileResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}

	@PostMapping("/api/crud/downloadFile")
	public DownloadFileResponse downloadFile(@RequestBody DownloadFileRequest request) {
		// TODO: Return File!!
		PodamFactory factory = new PodamFactoryImpl();
		DownloadFileResponse toret = factory.manufacturePojo(DownloadFileResponse.class);
		toret.setRequestUUID(request.getRequestUUID());
		return toret;
	}
}
