package com.nincraft.modpackdownloader.util;

import com.google.common.base.Strings;
import com.nincraft.modpackdownloader.container.CurseFile;
import com.nincraft.modpackdownloader.container.DownloadableFile;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Log4j2
@UtilityClass
public final class FileSystemHelper {

	public static void createFolder(final String folder) {
		if (folder != null) {
			final File dir = new File(folder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
	}

	public static void moveFromLocalRepo(final DownloadableFile downloadableFile, final String fileName, boolean downloadToLocalRepo) {
		val newProjectName = getProjectNameOrDefault(downloadableFile.getName());
		String folder = downloadableFile.getFolder();
		if (Strings.isNullOrEmpty(folder)) {
			folder = Reference.modFolder;
		}
		try {
			File downloadedFile = getDownloadedFile(fileName, folder);
			if (downloadToLocalRepo) {
				FileUtils.copyFileToDirectory(getLocalFile(downloadableFile, newProjectName), new File(folder));
			} else if (!downloadedFile.exists()) {
				FileUtils.moveFileToDirectory(getLocalFile(downloadableFile, newProjectName), new File(folder), true);
			}
			if (!Strings.isNullOrEmpty(downloadableFile.getRename())) {
				downloadedFile.renameTo(new File(downloadedFile.getParent() + File.separator + downloadableFile.getRename()));
			}
		} catch (final IOException e) {
			log.error(String.format("Could not copy %s from local repo.", newProjectName), e);
		}
	}

	public static boolean isInLocalRepo(final DownloadableFile downloadableFile) {
		if (downloadableFile instanceof CurseFile) {
			CurseFile curseFile = (CurseFile) downloadableFile;
			return new File(Reference.userhome + File.separator + getProjectNameOrDefault(downloadableFile.getName()) + File.separator + curseFile.getFileID()).exists();
		}
		return getLocalFile(downloadableFile, getProjectNameOrDefault(downloadableFile.getName())).exists();
	}

	public static File getDownloadedFile(final String fileName) {
		return getDownloadedFile(fileName, null);
	}

	public static String getProjectNameOrDefault(final String projectName) {
		return projectName != null ? projectName : "thirdParty";
	}

	public static File getLocalFile(final DownloadableFile downloadableFile, final String newProjectName) {
		String fileNamePath = Reference.userhome + newProjectName + File.separator;
		if (downloadableFile instanceof CurseFile) {
			CurseFile curseFile = (CurseFile) downloadableFile;
			fileNamePath += curseFile.getFileID() + File.separator;
		}
		if(Strings.isNullOrEmpty(downloadableFile.getFileName())){
			fileNamePath += "*.jar";
		}
		else{
			fileNamePath += downloadableFile.getFileName();
		}
		return new File(fileNamePath);
	}

	public static File getDownloadedFile(String fileName, String folder) {
		if (folder != null) {
			createFolder(folder);
			return new File(folder + File.separator + fileName);
		} else if (Reference.modFolder != null) {
			createFolder(Reference.modFolder);
			return new File(Reference.modFolder + File.separator + fileName);
		} else {
			return new File(fileName);
		}
	}
}
