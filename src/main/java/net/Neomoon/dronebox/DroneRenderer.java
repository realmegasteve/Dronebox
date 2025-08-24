package net.Neomoon.dronebox;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Quaterniond;

@Environment(EnvType.CLIENT)
public class DroneRenderer extends MobEntityRenderer<
	Drone,
	DroneRenderer.DroneRenderState,
	DroneEntityModel
	> {

	private static final Identifier TEXTURE =
		Identifier.of("dronebox", "textures/entity/drone");

	public static class DroneRenderState extends DroneEntityRenderState {
		public Drone entity;

		public double prevRoll;
		public double prevPitch;
		public double prevYaw;

		public double roll;
		public double pitch;
		public double yaw;
	}

	public DroneRenderer(EntityRendererFactory.Context context) {
		super(context, new DroneEntityModel(context.getPart(DroneModelLayers.DRONE)), 0.75F);
	}


	@Override
	public Identifier getTexture(DroneRenderState state) {
		if (state != null && state.entity != null) {
			return EntityTextureRegistry.getTexture(state.entity, TEXTURE);
		}
		return TEXTURE;
	}



	@Override
	public DroneRenderState createRenderState() {
		return new DroneRenderState();
	}

	@Override
	public void updateRenderState(Drone drone, DroneRenderState state, float tickDelta) {
		state.entity = drone;

		state.prevYaw = drone.prevYaw;
		state.yaw     = drone.getYaw();

		state.prevPitch = drone.prevPitch;
		state.pitch     = drone.getPitch();

		state.prevRoll = drone.prevRoll;
		state.roll     = drone.getRoll();
	}

	private double lerpAngle(float delta, double start, double end) {
		double diff = (end - start) % 360.0;
		if (diff < -180.0) diff += 360.0;
		if (diff >= 180.0) diff -= 360.0;
		return start + delta * diff;
	}

	@Override
	protected void setupTransforms(DroneRenderState state, MatrixStack matrices, float yaw, float tickDelta) {
		double interpYaw   = -state.entity.getHeadYaw();
		double interpPitch = lerpAngle(tickDelta, state.prevPitch, state.pitch);
		double interpRoll  = lerpAngle(tickDelta, state.prevRoll, state.roll);

		super.setupTransforms(state, matrices, (float) 0, tickDelta);

		Quaterniond quatD = new Quaterniond()
			.rotateX(Math.toRadians(interpPitch))
			.rotateZ(Math.toRadians(interpRoll))
			.rotateY(Math.toRadians(interpYaw));

		//System.out.println(interpYaw + ", " + interpPitch  + ", " + interpRoll);

		matrices.multiply(new Quaternionf(quatD));

	}

	@Override
	protected void scale(DroneRenderState state, MatrixStack matrices) {
		float s = 1.0F + 0.15F * state.size;
		matrices.scale(s, s, s);
		matrices.translate(0.0F, 1.3125F, 0.1875F);
	}
}
