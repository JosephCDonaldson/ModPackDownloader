package com.nincraft.modpackdownloader.processor;

import com.google.common.collect.Lists;
import com.nincraft.modpackdownloader.container.Manifest;
import com.nincraft.modpackdownloader.container.Mod;
import com.nincraft.modpackdownloader.handler.ForgeHandler;
import com.nincraft.modpackdownloader.util.DownloadHelper;
import com.nincraft.modpackdownloader.util.Reference;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

@Log4j2
public class DownloadModsProcessor extends AbstractProcessor {
	private static final List<Mod> MOD_LIST = Lists.newArrayList();

	public DownloadModsProcessor(final List<File> manifestFiles) {
		super(manifestFiles);
	}

	public static final void downloadMods(final Manifest manifest) {
		setExecutorService(Executors.newFixedThreadPool(MOD_LIST.size() + 1));
		Runnable forgeThread = new Thread(() -> {
			ForgeHandler.downloadForge(manifest.getMinecraftVersion(), manifest.getMinecraft().getModLoaders());
		});

		getExecutorService().execute(forgeThread);

		log.trace(String.format("Downloading %s mods...", MOD_LIST.size()));
		int downloadCount = 1;
		for (val mod : MOD_LIST) {
			log.info(String.format(Reference.DOWNLOADING_MOD_X_OF_Y, mod.getName(), downloadCount++,
					Reference.downloadTotal));

			Runnable modDownload = new Thread(() -> {
				MOD_HANDLERS.get(mod.getClass()).downloadMod(mod);
				Reference.downloadCount++;
				log.trace(String.format("Finished downloading %s", mod.getName()));
			});
			getExecutorService().execute(modDownload);
		}
		getExecutorService().shutdown();
		log.trace(String.format("Finished downloading %s mods.", MOD_LIST.size()));
	}

	@Override
	protected void init(final Map<File, Manifest> manifestMap) {
		for (val manifestEntry : manifestMap.entrySet()) {
			MOD_LIST.addAll(buildModList(manifestEntry.getKey(), manifestEntry.getValue()));
		}

		Reference.downloadTotal = MOD_LIST.size();
		log.debug("A total of %s mods will be downloaded.");
	}

	@Override
	protected void preprocess(final Entry<File, Manifest> manifestEntry) {
		// no-op
	}

	@Override
	protected void process(final Entry<File, Manifest> manifestEntry) {
		downloadMods(manifestEntry.getValue());
	}

	@Override
	protected void postProcess(final Entry<File, Manifest> manifestEntry) {
		DownloadHelper.getDownloadSummarizer().summarize();
	}
}
