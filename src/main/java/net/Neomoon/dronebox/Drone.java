package net.Neomoon.dronebox;

import net.Neomoon.dronebox.LUA.CustomRegexMarkersLUA;
import net.Neomoon.dronebox.LUA.LUAObjects.*;
import net.Neomoon.dronebox.LUA.MinecraftLuaInterpreter;
import net.Neomoon.dronebox.items.ModItems;
import net.Neomoon.dronebox.network.DroneStateC2SPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Drone extends MobEntity {
	public double yawRate;
	public PlayerEntity pendriveOwner;
	public double pitchRate;
	public double rollRate;
	private double roll;
	private double pitch;
	private boolean pythonLoaded = false;

	public double prevRoll;
	public boolean owner = false;
	public double prevYaw;
	public double prevPitch;
	public double strafeInput = 0;
	public double forwardInput = 0;
	public double yawInput = 0;
	public double upInput = 0;
	public DroneStateC2SPayload payload;

	public boolean accessoryState;

	public Vec3d remoteTarget;
	public int remoteTime = 9999;

	final MinecraftLuaInterpreter Lua = new MinecraftLuaInterpreter();

	public static final TrackedData<Integer> TEXTURE_ID =
		DataTracker.registerData(Drone.class, TrackedDataHandlerRegistry.INTEGER);

	private static final ChunkTicketType DRONE_TICKET =
		ChunkTicketType.PLAYER_SIMULATION;

	public void setAccessory(boolean state) {
		accessoryState = state;
	}

	// === Accessory System ===
	public interface AccessoryApply {
		void apply(World world, UUID droneId, Drone drone);
	}
	public interface AccessoryTick {
		void tick(World world, UUID droneId, Drone drone);

		AccessoryTick EMPTY = (world, droneId, drone) -> {};
	}
	public interface AccessoryRemove {
		void remove(World world, UUID droneId, Drone drone);
	}

	private static final Map<Item, List<AccessoryApply>> APPLY_HANDLERS = new HashMap<>();
	private static final Map<Item, List<AccessoryTick>> TICK_HANDLERS = new HashMap<>();
	private static final Map<Item, List<AccessoryRemove>> REMOVE_HANDLERS = new HashMap<>();

	private final Map<Item, Integer> equippedAccessories = new HashMap<>();

	public static void registerAccessory(Item item, AccessoryApply onApply, AccessoryTick onTick, AccessoryRemove onRemove) {
		if (onApply != null) APPLY_HANDLERS.computeIfAbsent(item, k -> new ArrayList<>()).add(onApply);
		if (onTick != null) TICK_HANDLERS.computeIfAbsent(item, k -> new ArrayList<>()).add(onTick);
		if (onRemove != null) REMOVE_HANDLERS.computeIfAbsent(item, k -> new ArrayList<>()).add(onRemove);
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack held = player.getStackInHand(hand);

		// Shift + right-click with empty hand -> retrieve and remove one accessory (first one found)
		if (player.isSneaking() && held.isEmpty()) {
			Iterator<Item> it = equippedAccessories.keySet().iterator();
			if (it.hasNext()) {
				Item acc = it.next();
				// remove from drone and run remove handlers
				removeAccessory(acc);
				ItemStack returnStack = new ItemStack(acc);
				if (!player.getInventory().insertStack(returnStack)) {
					player.dropItem(returnStack, false);
				}
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		}

		// Right-click with an accessory in hand -> attach / swap
		if (!held.isEmpty()) {
			Item heldItem = held.getItem();
			boolean isAccessory = APPLY_HANDLERS.containsKey(heldItem) || TICK_HANDLERS.containsKey(heldItem) || REMOVE_HANDLERS.containsKey(heldItem);
			if (isAccessory) {
				// If the drone already has the same accessory, remove it back to player (toggle off)
				if (equippedAccessories.containsKey(heldItem)) {
					removeAccessory(heldItem);
					// give one back to the player (unless creative)
					if (!player.isCreative()) {
						ItemStack give = new ItemStack(heldItem);
						if (!player.getInventory().insertStack(give)) player.dropItem(give, false);
					}
					return ActionResult.SUCCESS;
				}

				// Swap: if drone has any accessory, remove the first and give to player
				Item prev = null;
				if (!equippedAccessories.isEmpty()) {
					Iterator<Item> it = equippedAccessories.keySet().iterator();
					if (it.hasNext()) {
						prev = it.next();
						removeAccessory(prev);
						ItemStack prevStack = new ItemStack(prev);
						if (!player.getInventory().insertStack(prevStack)) player.dropItem(prevStack, false);
					}
				}

				// Attach held accessory
				equippedAccessories.put(heldItem, 1);
				if (!player.isCreative()) held.decrement(1);
				if (APPLY_HANDLERS.containsKey(heldItem)) {
					for (AccessoryApply fn : APPLY_HANDLERS.get(heldItem)) fn.apply(this.getWorld(), this.getUuid(), this);
				}
				return ActionResult.SUCCESS;
			}
		}

		return super.interactMob(player, hand);
	}

	private void removeAccessory(Item item) {
		if (equippedAccessories.remove(item) != null) {
			if (REMOVE_HANDLERS.containsKey(item)) {
				for (AccessoryRemove fn : REMOVE_HANDLERS.get(item)) {
					fn.remove(getWorld(), this.getUuid(), this);
				}
			}
			onAccessoryReset();
		}
	}



	protected void onAccessoryReset() {
		if (this.getWorld() instanceof ServerWorld serverWorld) {
			EntityTextureRegistry.setTexture(serverWorld, uuid, CentralDroneInit.DRONE_ENTITY_TYPE, 0);
		}
	}

	// === Normal Drone ===
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
		Lua.init();
		Lua.set(new LUADrone(this), "drone");
		Lua.set(new LUAController(this), "controller");
		Lua.set(new LUARadio(), "radio");
		Lua.set(new LUAMath(), "math");
		Lua.set(new LUADrone(this), "Drone");
		Lua.set(new LUAController(this), "Controller");
		Lua.set(new LUARadio(), "Radio");
		Lua.set(new LUAMath(), "Math");
	}

	public void controllerMovementInput(double forward, double strafe, double up, double yaw){
		strafeInput = strafe;
		forwardInput = forward;
		yawInput = yaw;
		upInput = up;
	}

	public void loadPythonScript(String code, PlayerEntity owner){
		pendriveOwner = owner;
		Lua.set(new LUALogger(pendriveOwner), "logger");
		Lua.set(new LUALogger(pendriveOwner), "Logger");


		NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		root.put("code", NbtString.of(code));
		this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
		if (getWorld().isClient) {
			try {
				Lua.run(code.replaceAll(Pattern.quote(CustomRegexMarkersLUA.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersLUA.returnMarker), "\n"));
				Lua.runSetup();
				pythonLoaded = true;
			} catch (ExecutionException | InterruptedException e) {
				root.put("code", NbtString.of(""));
				this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
				e.printStackTrace();
				pythonLoaded = false;
			}
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
		// Tick accessories
		for (Item acc : equippedAccessories.keySet()) {
			if (TICK_HANDLERS.containsKey(acc)) {
				for (AccessoryTick fn : TICK_HANDLERS.get(acc)) {
					fn.tick(getWorld(), this.getUuid(), this);
				}
			}
			if (accessoryState) {
				if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
					if (acc == ModItems.TOPLIGHT_ACCESSORY) {
						ServerWorld serverWorld = (ServerWorld) getWorld();
						serverWorld.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.1, this.getZ(), 1, 0.03, 0.03, 0.03, 0);
					}

					if (acc == ModItems.SPOTLIGHT_ACCESSORY) {
						ServerWorld serverWorld = (ServerWorld) getWorld();
						serverWorld.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.1, this.getZ(), 1, 0.03, 0.03, 0.03, 0);
					}
				}
			}

		}

		if (!this.getWorld().isClient() && this.getWorld() instanceof ServerWorld serverWorld) {
			ChunkPos center = new ChunkPos(this.getBlockPos());

			int radius = 2; // same as vanilla player's simulation distance radius
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					ChunkPos pos = new ChunkPos(center.x + dx, center.z + dz);
					serverWorld.getChunkManager().addTicket(
						DRONE_TICKET,
						pos,
						2    // 2 = fully ticking like player
					);
				}
			}
		}

		double yaw = this.getHeadYaw();
		if (pythonLoaded) {
			if (getWorld().isClient) { // modified by mixin
				super.tick();
			}
		} else {
			//Controller logic
			double yawRad = Math.toRadians(yaw);
			double moveSpeed = 0.1;
			double controllerVx = (-Math.sin(yawRad) * forwardInput + Math.cos(yawRad) * strafeInput) * moveSpeed;
			double controllerVz = (Math.cos(yawRad) * forwardInput + Math.sin(yawRad) * strafeInput) * moveSpeed;

			double controllerVy = upInput * moveSpeed;
			double d = 0.8;
			this.setManualVelocity(controllerVx + (d * getVelocity().x), controllerVy + (d * getVelocity().y), controllerVz + (d * getVelocity().z));

			yawRate = yawInput * 4 + (0.6 * yawRate);
			this.setRotationVelocity(yawRate, 0.0, 0.0);

			controllerMovementInput(0,0, 0,0);

			if (remoteTime < 60){
				double dist = remoteTarget.distanceTo(this.getPos());
				if (dist > 0.5) {
					remoteTime++;
					Vec3d dir = remoteTarget.subtract(this.getPos()).normalize();
					double speed = (0.02 * dist * dist) + 0.2;

					Vec3d acell = dir.multiply(speed).multiply(0.2);

					this.setVelocity(this.getVelocity().add(acell).multiply(0.9));
				} else {
					remoteTime += 3;
					this.setVelocity(this.getVelocity().multiply(0.5));
				}

			}


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
		if (payload != null){
			setPos(payload.X(), payload.Y(), payload.Z());
			setVelocity(payload.XS(), payload.YS(), payload.ZS());
			accessoryState = payload.accessoryState();
			payload = null;
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!this.getWorld().isClient() && this.getWorld() instanceof ServerWorld serverWorld) {
			ChunkPos pos = new ChunkPos(this.getBlockPos());
			serverWorld.getChunkManager().removeTicket(DRONE_TICKET, pos, 2);
		}
		super.remove(reason);
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
		return super.damage(world, source, amount);
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
		if (getWorld().isClient) {
			NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
			NbtCompound root = comp.copyNbt();
			String loadedCode = root.getString("code", "").replaceAll(Pattern.quote(CustomRegexMarkersLUA.tabMarker), "\t").replaceAll(Pattern.quote(CustomRegexMarkersLUA.returnMarker), "\n");

			if (!loadedCode.isEmpty()) {
				try {
					Lua.runTick();
				} catch (ExecutionException | InterruptedException e) {

					root.put("code", NbtString.of(""));
					this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
					e.printStackTrace();
				}
			}
		}
	}

	private void physics(){
		Vec3d velocity = this.getVelocity();
		double speed = velocity.length();

		double dragCoefficient = 0.07;
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
		return this.getHeadYaw();
	}

	public float getPitch() {
		return (float) pitch;
	}
}
