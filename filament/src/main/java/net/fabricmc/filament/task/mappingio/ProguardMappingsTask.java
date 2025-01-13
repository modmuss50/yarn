package net.fabricmc.filament.task.mappingio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import net.fabricmc.filament.task.base.WithFileInput;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.extras.TinyRemapperHierarchyProvider;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;

public abstract class ProguardMappingsTask extends MappingOutputTask implements WithFileInput {
	@InputFiles
	abstract ConfigurableFileCollection getHierarchyClasspath();

	public ProguardMappingsTask() {
		getHierarchyClasspath().setFrom(getProject().getTasks().named("mergeMinecraftJars"));
		getOutputFormat().set(MappingFormat.PROGUARD_FILE);
	}

	@Override
	void run(MappingWriter writer) throws IOException {
		MemoryMappingTree tree = new MemoryMappingTree();
		MappingReader.read(getInputPath(), tree);

		TinyRemapper tinyRemapper = TinyRemapper.newRemapper().build();
		tinyRemapper.readClassPath(getHierarchyClasspath().getFiles().stream().map(File::toPath).toArray(Path[]::new));
		tree.setHierarchyInfoProvider(new TinyRemapperHierarchyProvider(tinyRemapper.getEnvironment(), "official"));

		tinyRemapper.finish();

		var dstReorder = new MappingDstNsReorder(writer, List.of("intermediary"));
		var sourceNs = new MappingSourceNsSwitch(dstReorder, "named");
		tree.accept(sourceNs);
	}
}
