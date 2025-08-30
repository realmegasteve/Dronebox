package net.Neomoon.dronebox.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// TODO: implement receiver or remove this payload
public record CameraFrameS2CPayload(String droneUuid, byte[] imageData) implements CustomPayload {
	public static final Identifier ID_RAW = Identifier.of("dronebox", "camera_frame");
	public static final CustomPayload.Id<CameraFrameS2CPayload> ID = new CustomPayload.Id<>(ID_RAW);

	public static final PacketCodec<RegistryByteBuf, CameraFrameS2CPayload> CODEC =
		PacketCodec.tuple(
			PacketCodecs.STRING, CameraFrameS2CPayload::droneUuid,
			PacketCodecs.BYTE_ARRAY, CameraFrameS2CPayload::imageData,
			CameraFrameS2CPayload::new
		);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
