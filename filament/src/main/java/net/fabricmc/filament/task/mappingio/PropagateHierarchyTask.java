package net.fabricmc.filament.task.mappingio;

import java.io.IOException;
import java.util.Map;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;

import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.fabricmc.mappingio.tree.TinyRemapperHierarchyProvider;

import net.fabricmc.mappingio.tree.VisitOrder;
import net.fabricmc.tinyremapper.TinyRemapper;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;

public abstract class PropagateHierarchyTask extends MappingOutputTask {
	@InputFile
	public abstract RegularFileProperty getIntermediaryJarFile();

	@InputDirectory
	public abstract DirectoryProperty getMappingsDirectory();

	@Override
	void run(MappingWriter writer) throws IOException {
		var mappingTree = new MemoryMappingTree();
		TinyRemapper remapper = TinyRemapper.newRemapper()
				.propagateBridges(TinyRemapper.LinkedMethodPropagation.ENABLED)
				.build();
		remapper.readInputs(getIntermediaryJarFile().getAsFile().get().toPath());

		mappingTree.setHierarchyInfoProvider(new TinyRemapperHierarchyProvider(remapper.getEnvironment(), "intermediary"));

		var nsRenamer = new MappingNsRenamer(mappingTree, Map.of(
				"source", "intermediary",
				"target", "named"
		));
		MappingReader.read(
				getMappingsDirectory().get().getAsFile().toPath(),
				MappingFormat.ENIGMA_DIR,
				nsRenamer
		);

		var nsCompleter = new MappingNsCompleter(writer, Map.of("named", "intermediary"), true);
		mappingTree.accept(nsCompleter, VisitOrder.createByName());

		remapper.finish();
	}
}
