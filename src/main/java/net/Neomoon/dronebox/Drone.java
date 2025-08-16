package net.Neomoon.dronebox;

import net.Neomoon.dronebox.python.CustomRegexMarkersPython;
import net.Neomoon.dronebox.python.MinecraftPythonInterpreter;
import net.Neomoon.dronebox.python.PythonObjects.PYDrone;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Drone extends MobEntity {
	public double yawRate, pitchRate, rollRate;
	private double roll;
	private double yaw;
	private double pitch;

	public double prevRoll;
	public double prevYaw;
	public double prevPitch;
	private double dragCoefficient = 0.02;

	MinecraftPythonInterpreter py = new MinecraftPythonInterpreter();

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
		py.set(new PYDrone(this), "drone");
	}

	public void loadPythonScript(String code){
		NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		root.put("code", NbtString.of(code));
		this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));

		try {
			py.run(code.replaceAll(Pattern.quote(CustomRegexMarkersPython.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersPython.returnMarker), "\n"));
			py.runSetup();
		} catch (ExecutionException | InterruptedException e) {
			//clear code when crashing
			root.put("code", NbtString.of(""));
			this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
			e.printStackTrace();
		}
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
	}

	private double smoothedNx = 0.0;
	private double smoothedNz = 0.0;

	@Override
	public void tick() {
		//Overwriting default vanilla velocity
		Vec3d velocity = this.getVelocity();
		super.tick();
		this.setVelocity(velocity);

		physics();

		runPython();

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

	private void runPython(){
		NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		String loadedCode = root.getString("code", "").replaceAll(Pattern.quote(CustomRegexMarkersPython.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersPython.returnMarker), "\n");

		if (loadedCode != ""){
			try {
				py.run(loadedCode);
				py.runTick();
			} catch (ExecutionException | InterruptedException e) {
				//clear code when crashing
				root.put("code", NbtString.of(""));
				this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
				e.printStackTrace();
			}
		}
	}

	private void physics(){
		//======================[apply drag and gravity]======================

		//Drag
		Vec3d velocity = this.getVelocity();
		double speed = velocity.length();

		double dragMagnitude = dragCoefficient * speed * speed;

		Vec3d dragForce = velocity.normalize().multiply(-dragMagnitude);

		velocity = velocity.add(dragForce.multiply(0.002));



		//Gravity
		velocity.add(new Vec3d(0, -0.02, 0));



		this.setVelocity(velocity);

		//======================[Applying and closing]======================
		//Apply rotation
		this.prevYaw = this.yaw;
		this.prevPitch = this.pitch;
		this.prevRoll = this.roll;

		this.yaw += this.yawRate;
		this.pitch += this.pitchRate;
		this.roll  += this.rollRate;

		System.out.println(yaw + "/" + pitch + "/" + roll);

		//Apply movement
		this.move(MovementType.SELF, this.getVelocity());
	}

	public double getRoll() {
		return roll;
	}

	@Override
	public float getYaw() {
		return (float) yaw;
	}
}
