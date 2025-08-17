package net.Neomoon.dronebox;

import net.Neomoon.dronebox.python.CustomRegexMarkersPython;
import net.Neomoon.dronebox.python.MinecraftPythonInterpreter;
import net.Neomoon.dronebox.python.PythonObjects.PYController;
import net.Neomoon.dronebox.python.PythonObjects.PYDrone;
import net.Neomoon.dronebox.python.PythonObjects.PYRadio;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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
	public double yawRate;
	public double pitchRate;
	public double rollRate;
	private double roll;
	private double getterYaw;
	private double pitch;
	private boolean pythonLoaded = false;

	public double prevRoll;
	public double prevYaw;
	public double prevPitch;
	public double strafeInput = 0;
	public double forwardInput = 0;
	public double yawInput = 0;
	public double upInput = 0;

	public static final TrackedData<Integer> TEXTURE_ID =
		DataTracker.registerData(Drone.class, TrackedDataHandlerRegistry.INTEGER);


	final MinecraftPythonInterpreter py = new MinecraftPythonInterpreter();


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
		py.set(new PYController(this), "controller");
		py.set(PYRadio.class, "radio");
	}

	public void controllerMovementInput(double forward, double strafe, double up, double yaw){
		strafeInput = strafe;
		forwardInput = forward;
		yawInput = yaw;
		upInput = up;
	}

	public void loadPythonScript(String code){
		NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		root.put("code", NbtString.of(code));
		this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));

		try {
			py.run(code.replaceAll(Pattern.quote(CustomRegexMarkersPython.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersPython.returnMarker), "\n"));
			py.runSetup();
			pythonLoaded = true;
		} catch (ExecutionException | InterruptedException e) {
			root.put("code", NbtString.of(""));
			this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
			e.printStackTrace();
			pythonLoaded = false;
		}
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(TEXTURE_ID, 0);
	}


	private double smoothedNx = 0.0;
	private double smoothedNz = 0.0;

	@Override
	public void tick() {
		double yaw = this.getHeadYaw();
		if (pythonLoaded) {

			Vec3d velocity = this.getVelocity();
			super.tick();
			this.setVelocity(velocity);
			runPython();
			controllerMovementInput(0,0, 0,0);
			physics();

		} else {
			//Controller logic
			double yawRad = Math.toRadians(yaw);
			double moveSpeed = 0.35;
			double controllerVx = (-Math.sin(yawRad) * forwardInput + Math.cos(yawRad) * strafeInput) * moveSpeed;
			double controllerVz = (Math.cos(yawRad) * forwardInput + Math.sin(yawRad) * strafeInput) * moveSpeed;

			double controllerVy = upInput * moveSpeed;
			this.setManualVelocity(controllerVx, controllerVy, controllerVz);

			double yawRate = yawInput * 3.5;
			this.setRotationVelocity(yawRate, 0.0, 0.0);

			controllerMovementInput(0,0, 0,0);


			//physics
			super.tick();

			Vec3d velocity = this.getVelocity();

			this.prevYaw = yaw;
			this.prevPitch = this.pitch;
			this.prevRoll = this.roll;

			this.setHeadYaw((float) (yaw + this.yawRate));

			this.move(MovementType.SELF, this.getVelocity());

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
				targetRoll = smoothedNx * 15.0;
			} else {
				double uprightSmooth = 0.1;
				smoothedNx += (0 - smoothedNx) * uprightSmooth;
				smoothedNz += (0 - smoothedNz) * uprightSmooth;
				targetPitch = 0;
				targetRoll = 0;
			}

			float tiltSmooth = 0.2f;
			this.pitch += (targetPitch - this.pitch) * tiltSmooth;
			this.roll += (targetRoll - this.roll) * tiltSmooth;
		}
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

		if (!loadedCode.isEmpty()){
			try {
				py.run(loadedCode);
				py.runTick();
			} catch (ExecutionException | InterruptedException e) {

				root.put("code", NbtString.of(""));
				this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
				e.printStackTrace();
			}
		}
	}

	private void physics(){
		Vec3d velocity = this.getVelocity();
		double speed = velocity.length();

		double dragCoefficient = 0.03;
		double dragMagnitude = dragCoefficient * speed * speed;

		Vec3d dragForce = velocity.normalize().multiply(-dragMagnitude);

		velocity = velocity.add(dragForce.multiply(0.002));

		velocity = velocity.add(new Vec3d(0, -0.02, 0));

		this.setVelocity(velocity);

		this.move(MovementType.SELF, velocity);

		this.prevYaw = this.getHeadYaw();
		this.prevPitch = this.pitch;
		this.prevRoll = this.roll;

		this.setHeadYaw((float) (this.getHeadYaw() + this.yawRate));
		this.pitch += this.pitchRate;
		this.roll  += this.rollRate;


	}

	public double getRoll() {
		return roll;
	}

	@Override
	public float getYaw() {
		return (float) this.getHeadYaw();
	}

	public float getPitch() {
		return (float) pitch;
	}
}
