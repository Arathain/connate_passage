package arathain.miku_machines.logic.worldshell;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormats;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.*;

/**
 * @author snakefangox
 * */
public class WorldshellRenderCache {
	private boolean rendered = false;
	private static final List<RenderLayer> blockRenderLayers = RenderLayer.getBlockLayers();

	private final Map<RenderLayer, VertexBuffer> bufferStorage = new HashMap<>();
	private final Map<RenderLayer, BufferBuilder> buffers = new HashMap<>();

	private final Set<RenderLayer> bufferFilled = new HashSet<>();

	public WorldshellRenderCache() {
		fillBuffers();
	}

	private void fillBuffers() {
		blockRenderLayers.forEach(renderLayer -> {
			BufferBuilder bufferBuilder = new BufferBuilder(renderLayer.getExpectedBufferSize());
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
			buffers.put(renderLayer, bufferBuilder);
			bufferStorage.put(renderLayer, new VertexBuffer(VertexBuffer.Usage.STATIC));
		});
	}

	public BufferBuilder get(RenderLayer renderLayer) {
		if (buffers.containsKey(renderLayer)) {
			bufferFilled.add(renderLayer);
			return buffers.get(renderLayer);
		} else {
			bufferFilled.add(getDefault());
			return buffers.get(getDefault());
		}
	}

	private RenderLayer getDefault() {
		return RenderLayer.getSolid();
	}

	public void upload() {
		for(RenderLayer layer : bufferFilled) {
			BufferBuilder builder = buffers.get(layer);
			VertexBuffer buffer = bufferStorage.get(layer);
			buffer.bind();
			buffer.upload(builder.end());
		}
		rendered = true;
	}

	public boolean isRendered() {
		return rendered;
	}

	public void draw(MatrixStack matrices) {
		Matrix4f proj = RenderSystem.getProjectionMatrix();
		for(RenderLayer renderLayer : bufferFilled) {
			VertexBuffer entry = bufferStorage.get(renderLayer);
			renderLayer.startDrawing();
			entry.bind();
			entry.draw(matrices.peek().getModel(), proj, RenderSystem.getShader());
			renderLayer.endDrawing();
			VertexBuffer.unbind();
		}
	}

	public void reset() {
		buffers.forEach((key, entry) -> entry.reset());
		bufferStorage.forEach((key, entry) -> entry.close());
		buffers.clear();
		bufferStorage.clear();
		bufferFilled.clear();
		fillBuffers();
	}
}
