package net.Neomoon.dronebox.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record DroneBatchPayload(
    List<DroneStatePayload> droneStates
) implements CustomPayload {
    
    public static final Id<DroneBatchPayload> ID = new Id<>(Identifier.of("dronebox", "batch_drone_state_c2s"));
    
    public static final PacketCodec<RegistryByteBuf, DroneBatchPayload> CODEC =
        PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, DroneStatePayload.CODEC),
            DroneBatchPayload::droneStates,
            DroneBatchPayload::new
        );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
