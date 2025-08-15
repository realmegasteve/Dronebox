package net.Neomoon.dronebox;

import net.Neomoon.dronebox.items.DroneControllerItem;
import net.Neomoon.dronebox.items.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.registry.Registries;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class CentralDroneInit implements ModInitializer {
	public static final Identifier DRONE_ID = Identifier.of(DroneboxMain.MOD_ID, "drone");
	public static EntityType<Drone> DRONE_ENTITY_TYPE;
	public static final RegistryKey<EntityType<?>> DRONE_KEY =
		RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(DroneboxMain.MOD_ID, "drone"));

	public static Item DRONE_CONTROLLER;

	@Override
	public void onInitialize() {

		DRONE_ENTITY_TYPE = FabricEntityTypeBuilder.<Drone>create(SpawnGroup.MISC, Drone::new)
			.dimensions(EntityDimensions.fixed(0.75f, 0.75f))
			.trackRangeBlocks(80)
			.trackedUpdateRate(3)
			.build(DRONE_KEY);
		Registry.register(Registries.ENTITY_TYPE, DRONE_KEY.getValue(), DRONE_ENTITY_TYPE);
		FabricDefaultAttributeRegistry.register(DRONE_ENTITY_TYPE, Drone.createDroneAttributes());
		DroneNetworking.register();

		DRONE_CONTROLLER = ModItems.DRONE_CONTROLLER;


	}

	public static class Drone extends MobEntity {
		private double yawRate, pitchRate, rollRate;
		private double roll;
		private double yaw;
		private double pitch;

		public double prevRoll;
		public double prevYaw;
		public double prevPitch;

		public static DefaultAttributeContainer.Builder createDroneAttributes() {
			return MobEntity.createMobAttributes()
				.add(EntityAttributes.MAX_HEALTH, 20.0)
				.add(EntityAttributes.MOVEMENT_SPEED, 0.25)
				.add(EntityAttributes.FLYING_SPEED, 0.6)
				.add(EntityAttributes.GRAVITY, 0);
		}

		public Drone(EntityType<? extends Drone> type, World world) {
			super(type, world);
			this.setNoGravity(true);
		}

		@Override
		protected void initDataTracker(DataTracker.Builder builder) {
			super.initDataTracker(builder);
		}

		private double smoothedNx = 0.0;
		private double smoothedNz = 0.0;

		@Override
		public void tick() {
			super.tick();

			this.prevYaw = this.yaw;
			this.prevPitch = this.pitch;
			this.prevRoll = this.roll;


			this.yaw += (float) this.yawRate;


			this.move(MovementType.SELF, this.getVelocity());


			Vec3d velocity = this.getVelocity();
			double vx = velocity.x;
			double vz = velocity.z;
			double horizontalSpeed = Math.sqrt(vx * vx + vz * vz);

			double targetPitch = 0.0;
			double targetRoll = 0.0;

			if (horizontalSpeed > 1e-4) {
				double nx = vx / horizontalSpeed;
				double nz = vz / horizontalSpeed;

				double dirSmooth = 0.3;
				smoothedNx += (nx - smoothedNx) * dirSmooth;
				smoothedNz += (nz - smoothedNz) * dirSmooth;

				targetPitch = -smoothedNz * 15.0;
				targetRoll  =  smoothedNx * 15.0;
			} else {

				double uprightSmooth = 0.1;
				smoothedNx += (0 - smoothedNx) * uprightSmooth;
				smoothedNz += (0 - smoothedNz) * uprightSmooth;
				targetPitch = 0;
				targetRoll = 0;
			}


			float tiltSmooth = 0.2f;
			this.pitch += (targetPitch - this.pitch) * tiltSmooth;
			this.roll  += (targetRoll - this.roll) * tiltSmooth;



			


		}

		public byte[] renderCameraToBytes() {
			try {
				int width = 128;
				int height = 128;


				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			// REMEMBER TO ACTUALLY IMPLEMENT THIS FUTURE ME!
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						image.setRGB(x, y, 0xFF00FF00);
					}
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "PNG", baos);
				return baos.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
				return new byte[0];
			}
		}

		@Override
		public boolean damage(ServerWorld world, DamageSource source, float amount) {
			return false;
		}

		@Override
		protected void readCustomData(ReadView view) {
			super.readCustomData(view);
		}

		@Override
		protected void writeCustomData(WriteView view) {
			super.writeCustomData(view);
		}

		public void setManualVelocity(double vx, double vy, double vz) {
			this.setVelocity(vx, vy, vz);
		}

		public void setRotationVelocity(double yawRate, double pitchRate, double rollRate) {
			this.yawRate = yawRate;
			this.pitchRate = pitchRate;
			this.rollRate = rollRate;
		}

		public double getRoll() {
			return roll;
		}
	}
}
