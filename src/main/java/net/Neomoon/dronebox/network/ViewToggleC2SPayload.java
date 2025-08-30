package net.Neomoon.dronebox.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record ViewToggleC2SPayload(
		String target
	) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "viewtoggle_payload");
	public static final Id<ViewToggleC2SPayload> ID = new Id<>(ID_RAW);

	public static final PacketCodec<net.minecraft.network.RegistryByteBuf, ViewToggleC2SPayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, ViewToggleC2SPayload::target,
			ViewToggleC2SPayload::new
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
