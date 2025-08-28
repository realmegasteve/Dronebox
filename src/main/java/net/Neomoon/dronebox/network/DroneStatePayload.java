package net.Neomoon.dronebox.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public record DroneStatePayload(
	String drone,
	double X,
	double Y,
	double Z,
	double XS,
	double YS,
	double ZS,
	boolean accessoryState
) implements CustomPayload {

	public static final Identifier ID_RAW = Identifier.of("dronebox", "drone_state_c2s");
	public static final Id<DroneStatePayload> ID = new Id<>(ID_RAW);

	public static final PacketCodec<RegistryByteBuf, DroneStatePayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING , DroneStatePayload::drone,
			PacketCodecs.DOUBLE, DroneStatePayload::X,
			PacketCodecs.DOUBLE, DroneStatePayload::Y,
			PacketCodecs.DOUBLE, DroneStatePayload::Z,
			PacketCodecs.DOUBLE, DroneStatePayload::XS,
			PacketCodecs.DOUBLE, DroneStatePayload::YS,
			PacketCodecs.DOUBLE, DroneStatePayload::ZS,
			PacketCodecs.BOOLEAN, DroneStatePayload::accessoryState,
			DroneStatePayload::new
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
