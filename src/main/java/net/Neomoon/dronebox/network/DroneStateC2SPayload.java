package net.Neomoon.dronebox.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DroneStateC2SPayload(
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
	public static final Id<DroneStateC2SPayload> ID = new Id<>(ID_RAW);

	public static final PacketCodec<RegistryByteBuf, DroneStateC2SPayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING , DroneStateC2SPayload::drone,
			PacketCodecs.DOUBLE, DroneStateC2SPayload::X,
			PacketCodecs.DOUBLE, DroneStateC2SPayload::Y,
			PacketCodecs.DOUBLE, DroneStateC2SPayload::Z,
			PacketCodecs.DOUBLE, DroneStateC2SPayload::XS,
			PacketCodecs.DOUBLE, DroneStateC2SPayload::YS,
			PacketCodecs.DOUBLE, DroneStateC2SPayload::ZS,
			PacketCodecs.BOOLEAN, DroneStateC2SPayload::accessoryState,
			DroneStateC2SPayload::new
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
