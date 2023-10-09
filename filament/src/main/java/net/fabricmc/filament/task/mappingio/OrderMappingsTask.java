package net.fabricmc.filament.task.mappingio;

import java.io.IOException;

import net.fabricmc.filament.task.base.WithFileInput;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.mappingio.tree.VisitOrder;

// Just used to sort mappings for easy comparison
public abstract class OrderMappingsTask extends MappingOutputTask implements WithFileInput {
	@Override
	void run(MappingWriter writer) throws IOException {
		var mappingTree = new MemoryMappingTree();
		MappingReader.read(
				getInputPath(),
				mappingTree
		);
		mappingTree.accept(writer, VisitOrder.createByName());
	}
}
