package net.Neomoon.dronebox.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MoveC2SPayload(
	String droneUuid,
	float forward,
	float strafe,
	boolean up,
	boolean down,
	float yawInput
) implements CustomPayload {

	public static final Identifier ID_RAW = Identifier.of("dronebox", "move_c2s");

	public static final CustomPayload.Id<MoveC2SPayload> ID = new CustomPayload.Id<>(ID_RAW);

	public static final PacketCodec<RegistryByteBuf, MoveC2SPayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING,    MoveC2SPayload::droneUuid,
			PacketCodecs.FLOAT,   MoveC2SPayload::forward,
			PacketCodecs.FLOAT,   MoveC2SPayload::strafe,
			PacketCodecs.BOOLEAN, MoveC2SPayload::up,
			PacketCodecs.BOOLEAN, MoveC2SPayload::down,
			PacketCodecs.FLOAT,   MoveC2SPayload::yawInput,
			MoveC2SPayload::new
		);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
