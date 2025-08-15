package net.Neomoon.dronebox;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PhantomEntityModel;
import net.minecraft.client.render.entity.state.PhantomEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Quaterniond;

@Environment(EnvType.CLIENT)
public class DroneRenderer extends MobEntityRenderer<
	Drone,
	DroneRenderer.DroneRenderState,
	PhantomEntityModel
	> {

	private static final Identifier TEXTURE =
		Identifier.of("minecraft", "textures/entity/phantom.png");

	public static class DroneRenderState extends PhantomEntityRenderState {
		public double roll;
		public double Pitch;
		public double yaw;
	}

	public DroneRenderer(EntityRendererFactory.Context context) {
		super(context, new PhantomEntityModel(context.getPart(EntityModelLayers.PHANTOM)), 0.75F);
	}

	@Override
	public Identifier getTexture(DroneRenderState state) {
		return TEXTURE;
	}

	@Override
	public DroneRenderState createRenderState() {
		return new DroneRenderState();
	}

	@Override
	public void updateRenderState(Drone drone, DroneRenderState state, float tickDelta) {
		state.yaw   = drone.getYaw();
		state.Pitch = drone.prevPitch;
		state.roll  = drone.getRoll();
	}

	private double lerpAngle(float delta, double start, double end) {
		double diff = (end - start) % 360.0;
		if (diff < -180.0) diff += 360.0;
		if (diff >= 180.0) diff -= 360.0;
		return start + delta * diff;
	}

	@Override
	protected void setupTransforms(DroneRenderState state, MatrixStack matrices, float yaw, float tickDelta) {
		super.setupTransforms(state, matrices, (float) state.yaw, tickDelta);

		Quaterniond quatD = new Quaterniond()
			.rotateX(Math.toRadians(state.Pitch))
			.rotateZ(Math.toRadians(state.roll));

		matrices.multiply(new Quaternionf(quatD));
	}

	@Override
	protected void scale(DroneRenderState state, MatrixStack matrices) {
		float s = 1.0F + 0.15F * state.size;
		matrices.scale(s, s, s);
		matrices.translate(0.0F, 1.3125F, 0.1875F);
	}
}
