package net.Neomoon.dronebox.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record ToggleC2SPayload(String droneUuid) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "toggle_c2s");
	public static final CustomPayload.Id<ToggleC2SPayload> ID = new CustomPayload.Id<>(ID_RAW);

	public static final PacketCodec<net.minecraft.network.RegistryByteBuf, ToggleC2SPayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, ToggleC2SPayload::droneUuid,
			ToggleC2SPayload::new
		);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
