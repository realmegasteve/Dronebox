package net.Neomoon.dronebox;

import net.Neomoon.dronebox.python.CustomRegexMarkersPython;
import net.Neomoon.dronebox.python.MinecraftPythonInterpreter;
import net.Neomoon.dronebox.python.PythonObjects.Controller;
import net.Neomoon.dronebox.python.PythonObjects.PYDrone;
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
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.registry.Registries;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
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

	public static final TrackedData<Integer> ACCESSORY_RAW_ID =
		DataTracker.registerData(Drone.class, TrackedDataHandlerRegistry.INTEGER);

	public static final Map<Item, BiConsumer<World, UUID>> ITEM_BEHAVIORS = new HashMap<>();

	public static void registerAccessoryBehavior(Item item, BiConsumer<World, UUID> behavior){
		ITEM_BEHAVIORS.put(item, behavior);
	}

	final MinecraftPythonInterpreter py = new MinecraftPythonInterpreter();

	private ItemStack accessoryStack = ItemStack.EMPTY;

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
		py.set(new Controller(this), "controller");
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
		builder.add(ACCESSORY_RAW_ID, -1);
	}


	private double smoothedNx = 0.0;
	private double smoothedNz = 0.0;

	@Override
	public void tick() {
		double yaw = this.getHeadYaw();

		System.out.println("Nyaaa!: " + yaw + ", " + this.getId());

		if (pythonLoaded) {
			Vec3d velocity = this.getVelocity();
			super.tick();
			this.setVelocity(velocity);
			runPython();
			controllerMovementInput(0,0, 0,0);
			physics();
		} else {
			double yawRad = Math.toRadians(yaw);
			double moveSpeed = 0.35;
			double controllerVx = (-Math.sin(yawRad) * forwardInput + Math.cos(yawRad) * strafeInput) * moveSpeed;
			double controllerVz = (Math.cos(yawRad) * forwardInput + Math.sin(yawRad) * strafeInput) * moveSpeed;

			double controllerVy = upInput * moveSpeed;
			this.setManualVelocity(controllerVx, controllerVy, controllerVz);

			double yawRate = yawInput * 3.5;
			this.setRotationVelocity(yawRate, 0.0, 0.0);

			controllerMovementInput(0,0, 0,0);

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

		NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		if (root != null && root.contains("Accessory")) {
			Optional<NbtCompound> accNbt = root.getCompound("Accessory");
			if (accNbt != null) {
				String itemId = accNbt.get().getString("Item", "Item");
				Optional<Integer> count = accNbt.get().getInt("Count");
				if (itemId != null && !itemId.isEmpty()) {
					try {
						Identifier id = Identifier.of(itemId);
						Item item = Registries.ITEM.get(id);
						if (item != null) {
							this.accessoryStack = new ItemStack(item, Math.max(1, count.get()));
							int rawId = Registries.ITEM.getRawId(item);
							this.dataTracker.set(ACCESSORY_RAW_ID, rawId);
						} else {
							this.accessoryStack = ItemStack.EMPTY;
							this.dataTracker.set(ACCESSORY_RAW_ID, -1);
						}
					} catch (Exception e) {
						e.printStackTrace();
						this.accessoryStack = ItemStack.EMPTY;
						this.dataTracker.set(ACCESSORY_RAW_ID, -1);
					}
				} else {
					this.accessoryStack = ItemStack.EMPTY;
					this.dataTracker.set(ACCESSORY_RAW_ID, -1);
				}
			} else {
				this.accessoryStack = ItemStack.EMPTY;
				this.dataTracker.set(ACCESSORY_RAW_ID, -1);
			}
		} else {
			this.accessoryStack = ItemStack.EMPTY;
			this.dataTracker.set(ACCESSORY_RAW_ID, -1);
		}
	}

	@Override
	protected void writeCustomData(WriteView view) {
		super.writeCustomData(view);

		NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();

		if (this.accessoryStack != null && !this.accessoryStack.isEmpty()) {
			NbtCompound accNbt = new NbtCompound();
			Identifier id = Registries.ITEM.getId(this.accessoryStack.getItem());
			accNbt.putString("Item", (id != null) ? id.toString() : "");
			accNbt.putInt("Count", this.accessoryStack.getCount());
			root.put("Accessory", accNbt);
		} else {
			root.remove("Accessory");
		}

		this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}

	public boolean equipAccessoryFromStack(ItemStack stack) {
		if (stack == null) return false;
		if (stack.isEmpty()) return false;
		if (!ITEM_BEHAVIORS.containsKey(stack.getItem())) return false;
		if (this.accessoryStack != null && !this.accessoryStack.isEmpty()) return false;

		ItemStack copy = stack.copy();
		copy.setCount(1);
		this.accessoryStack = copy;

		int rawId = Registries.ITEM.getRawId(copy.getItem());
		this.dataTracker.set(ACCESSORY_RAW_ID, rawId);

		if (!this.getWorld().isClient) {
			BiConsumer<World, UUID> behavior = ITEM_BEHAVIORS.get(copy.getItem());
			if (behavior != null) {
				try {
					behavior.accept(this.getWorld(), this.getUuid());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		this.syncAccessoryToNbt();

		return true;
	}

	public ItemStack unequipAccessory() {
		if (this.accessoryStack == null || this.accessoryStack.isEmpty()) {
			this.accessoryStack = ItemStack.EMPTY;
			this.dataTracker.set(ACCESSORY_RAW_ID, -1);
			this.syncAccessoryToNbt();
			return ItemStack.EMPTY;
		}
		ItemStack old = this.accessoryStack;
		this.accessoryStack = ItemStack.EMPTY;
		this.dataTracker.set(ACCESSORY_RAW_ID, -1);
		this.syncAccessoryToNbt();
		return old;
	}

	public boolean canEquipAccessory(ItemStack stack) {
		return stack != null && !stack.isEmpty() && ITEM_BEHAVIORS.containsKey(stack.getItem());
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

		ItemStack held = player.getStackInHand(hand);

		if (!held.isEmpty() && canEquipAccessory(held) && (this.accessoryStack == null || this.accessoryStack.isEmpty()) && !player.isSneaking()) {
			ItemStack taken = held.split(1);
			boolean ok = this.equipAccessoryFromStack(taken);
			if (ok) {
				player.setStackInHand(hand, held);
				if (!this.getWorld().isClient) {
					player.sendMessage(Text.of("Accessory equipped"), true);
				}
				return ActionResult.SUCCESS;
			} else {
				held.increment(1);
				player.setStackInHand(hand, held);
				return ActionResult.FAIL;
			}
		}

		if ((held.isEmpty() || player.isSneaking())) {
			if (this.accessoryStack != null && !this.accessoryStack.isEmpty()) {
				ItemStack toGive = this.unequipAccessory();
				boolean given = player.giveItemStack(toGive);
				if (!given) {
					this.dropStack((ServerWorld) this.getWorld(), toGive);
				}
				if (!this.getWorld().isClient) {
					player.sendMessage(Text.of("Accessory removed"), true);
				}
				return ActionResult.SUCCESS;
			} else {
				if (!this.getWorld().isClient) {
					player.sendMessage(Text.of("Accessory slot empty"), true);
				}
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}

	private void syncAccessoryToNbt() {
		NbtComponent comp = this.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound()));
		NbtCompound root = comp.copyNbt();
		if (this.accessoryStack != null && !this.accessoryStack.isEmpty()) {
			NbtCompound accNbt = new NbtCompound();
			Identifier id = Registries.ITEM.getId(this.accessoryStack.getItem());
			accNbt.putString("Item", (id != null) ? id.toString() : "");
			accNbt.putInt("Count", this.accessoryStack.getCount());
			root.put("Accessory", accNbt);
		} else {
			root.remove("Accessory");
		}
		this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		super.onTrackedDataSet(data);

		if (data == ACCESSORY_RAW_ID) {
			if (this.getWorld() != null && this.getWorld().isClient) {
				int rawId = this.dataTracker.get(ACCESSORY_RAW_ID);
				if (rawId >= 0) {
					Item item = Registries.ITEM.get(rawId);
					if (item != null) {
						BiConsumer<World, UUID> behavior = ITEM_BEHAVIORS.get(item);
						if (behavior != null) {
							try {
								behavior.accept(this.getWorld(), this.getUuid());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
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

		double dragCoefficient = 0.02;
		double dragMagnitude = dragCoefficient * speed * speed;

		Vec3d dragForce = velocity.normalize().multiply(-dragMagnitude);

		velocity = velocity.add(dragForce.multiply(0.002));

		velocity = velocity.add(new Vec3d(0, -0.02, 0));

		this.setVelocity(velocity);

		this.prevYaw = this.getHeadYaw();
		this.prevPitch = this.pitch;
		this.prevRoll = this.roll;

		this.setHeadYaw((float) (this.getHeadYaw() + this.yawRate));
		this.pitch += this.pitchRate;
		this.roll  += this.rollRate;

		this.move(MovementType.SELF, this.getVelocity());
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

	public void setManualVelocity(double vx, double vy, double vz) {
		this.setVelocity(vx, vy, vz);
	}

	public void setRotationVelocity(double yawRate, double pitchRate, double rollRate) {
		this.yawRate = yawRate;
		this.pitchRate = pitchRate;
		this.rollRate = rollRate;
	}
}
