package net.Neomoon.dronebox.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ControllerComponent(List<String> linkedDrones, Map<String, Boolean> controlStates, Map<String, Boolean> cameraStates, Map<String, Boolean> enabledStates, Map<String, String> droneNames) {

	public static final ControllerComponent EMPTY = new ControllerComponent(new ArrayList<>(), new Object2ObjectOpenHashMap<>(), new Object2ObjectOpenHashMap<>(), new Object2ObjectOpenHashMap<>(), new Object2ObjectOpenHashMap<>());

	public static final Codec<Map<String, Boolean>> STRING_TO_BOOL_CODEC = new MutableMapCodec<>(Codecs.strictUnboundedMap(Codec.STRING, Codec.BOOL));
	public static final Codec<Map<String, String>> STRING_TO_STRING_CODEC = new MutableMapCodec<>(Codecs.strictUnboundedMap(Codec.STRING, Codec.STRING));

	public static final Codec<ControllerComponent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				new MutableListCodec<>(Codec.STRING.listOf()).optionalFieldOf(DroneControllerItem.LINKED_LIST_KEY, new ArrayList<>()).forGetter(component -> component.linkedDrones),
				STRING_TO_BOOL_CODEC.optionalFieldOf(DroneControllerItem.CONTROL_STATES_KEY, new Object2ObjectOpenHashMap<>()).forGetter(component -> component.controlStates),
				STRING_TO_BOOL_CODEC.optionalFieldOf(DroneControllerItem.CAMERA_STATES_KEY, new Object2ObjectOpenHashMap<>()).forGetter(component -> component.cameraStates),
				STRING_TO_BOOL_CODEC.optionalFieldOf(DroneControllerItem.ENABLED_STATES_KEY, new Object2ObjectOpenHashMap<>()).forGetter(component -> component.enabledStates),
				STRING_TO_STRING_CODEC.optionalFieldOf(DroneControllerItem.NAMES_KEY, new Object2ObjectOpenHashMap<>()).forGetter(component -> component.droneNames)
			)
			.apply(instance, ControllerComponent::new)
	);

	public static final PacketCodec<PacketByteBuf, Map<String, Boolean>> STRING_TO_BOOL_PACKET_CODEC = PacketCodecs.map(Object2ObjectOpenHashMap::new, PacketCodecs.STRING, PacketCodecs.BOOLEAN);

	public static final PacketCodec<PacketByteBuf, ControllerComponent> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.STRING.collect(PacketCodecs.toList()), component -> component.linkedDrones,
		STRING_TO_BOOL_PACKET_CODEC, component -> component.controlStates,
		STRING_TO_BOOL_PACKET_CODEC, component -> component.cameraStates,
		STRING_TO_BOOL_PACKET_CODEC, component -> component.enabledStates,
		PacketCodecs.map(Object2ObjectOpenHashMap::new, PacketCodecs.STRING, PacketCodecs.STRING), component -> component.droneNames,
		ControllerComponent::new
	);

	public ControllerComponent withLinkedDrones(List<String> linkedDrones) {
		return new ControllerComponent(linkedDrones, this.controlStates, this.cameraStates, this.enabledStates, this.droneNames);
	}

	public ControllerComponent withControlStates(Map<String, Boolean> controlStates) {
		return new ControllerComponent(this.linkedDrones, controlStates, this.cameraStates, this.enabledStates, this.droneNames);
	}

	public ControllerComponent withCameraStates(Map<String, Boolean> cameraStates) {
		return new ControllerComponent(this.linkedDrones, this.controlStates, cameraStates, this.enabledStates, this.droneNames);
	}

	public ControllerComponent withEnabledStates(Map<String, Boolean> enabledStates) {
		return new ControllerComponent(this.linkedDrones, this.controlStates, this.cameraStates, enabledStates, this.droneNames);
	}

	public ControllerComponent withDroneNames(Map<String, String> droneNames) {
		return new ControllerComponent(this.linkedDrones, this.controlStates, this.cameraStates, this.enabledStates, droneNames);
	}

	public ControllerComponent copy() {
		return new ControllerComponent(this.linkedDrones, this.controlStates, this.cameraStates, this.enabledStates, this.droneNames);
	}

	// This was a very good idea with no unforeseen consequences
	public record MutableListCodec<E>(Codec<List<E>> delegate) implements Codec<List<E>> {
		@Override
		public <T> DataResult<Pair<List<E>, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<Pair<List<E>, T>> result = this.delegate.decode(ops, input);
			result = result.map(pair ->
				pair.mapFirst(list -> {
					if (list != null) {
						return new ArrayList<>(list);
					}
					return new ArrayList<>();
				})
			);
			return result;
		}

		@Override
		public <T> DataResult<T> encode(List<E> input, DynamicOps<T> ops, T prefix) {
			return this.delegate.encode(input, ops, prefix);
		}
	}

	public record MutableMapCodec<K, V>(Codec<Map<K, V>> delegate) implements Codec<Map<K, V>> {
		@Override
		public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<Pair<Map<K, V>, T>> result = this.delegate.decode(ops, input);
			result = result.map(pair ->
				pair.mapFirst(map -> {
					if (map != null) {
						return new Object2ObjectOpenHashMap<>(map);
					}
					return new Object2ObjectOpenHashMap<>();
				})
			);
			return result;
		}

		@Override
		public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
			return this.delegate.encode(input, ops, prefix);
		}
	}
}
