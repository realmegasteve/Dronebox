package net.Neomoon.dronebox;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DroneEntityModel extends EntityModel<EntityRenderState> {
	private final ModelPart root;
	private final ModelPart accessories;
	private final ModelPart fireworks;
	private final ModelPart right;
	private final ModelPart string2;
	private final ModelPart left;
	private final ModelPart string;
	private final ModelPart toplamp;
	private final ModelPart googlyeyes;
	private final ModelPart spotlight;
	private final ModelPart bone;
	private final ModelPart wing1;
	private final ModelPart propel1;
	private final ModelPart wing2;
	private final ModelPart propel2;
	private final ModelPart wing3;
	private final ModelPart propel3;
	private final ModelPart wing4;
	private final ModelPart propel4;

	public DroneEntityModel(ModelPart root) {
		super(root);
		this.root = root;
		this.accessories = root.getChild("accessories");
		this.fireworks = this.accessories.getChild("fireworks");
		this.right = this.fireworks.getChild("right");
		this.string2 = this.right.getChild("string2");
		this.left = this.fireworks.getChild("left");
		this.string = this.left.getChild("string");
		this.toplamp = this.accessories.getChild("toplamp");
		this.googlyeyes = this.accessories.getChild("googlyeyes");
		this.spotlight = this.accessories.getChild("spotlight");
		this.bone = this.spotlight.getChild("bone");
		this.wing1 = root.getChild("wing1");
		this.propel1 = this.wing1.getChild("propel1");
		this.wing2 = root.getChild("wing2");
		this.propel2 = this.wing2.getChild("propel2");
		this.wing3 = root.getChild("wing3");
		this.propel3 = this.wing3.getChild("propel3");
		this.wing4 = root.getChild("wing4");
		this.propel4 = this.wing4.getChild("propel4");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		ModelPartData rootmodel = root.addChild("rootmodel",
			ModelPartBuilder.create()
				.uv(0, 0).cuboid(-3.5F, -1.0F, -1.5F, 7.0F, 2.0F, 3.0F, new Dilation(0.0F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F)
		);

		ModelPartData accessories = root.addChild("accessories",
			ModelPartBuilder.create(),

			ModelTransform.of(0.5F, 1.0F, 1.0F, 0, 67.55F, 0)
		);

		ModelPartData fireworks = accessories.addChild("fireworks",
			ModelPartBuilder.create(),
			ModelTransform.origin(0.0F, 0.0F, 0.0F)
		);

		ModelPartData right = fireworks.addChild("right",
			ModelPartBuilder.create().uv(6, 11)
				.cuboid(-3.0F, -2.0F, 3.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)),
			ModelTransform.origin(0.0F, 1.0F, -1.0F)
		);

		ModelPartData string2 = right.addChild("string2",
			ModelPartBuilder.create().uv(8, 28)
				.cuboid(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.01F)),
			ModelTransform.of(2.0F, -1.5F, 3.5F, -0.7854F, 0.0F, 0.0F)
		);

		string2.addChild("cube_r1",
			ModelPartBuilder.create().uv(8, 28)
				.cuboid(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F)
		);

		ModelPartData left = fireworks.addChild("left",
			ModelPartBuilder.create().uv(6, 11)
				.cuboid(-3.0F, -2.0F, -1.0F, 4.0F, 1.0F, 1.0F, new Dilation(0.0F)),
			ModelTransform.origin(0.0F, 1.0F, -1.0F)
		);

		ModelPartData string = left.addChild("string",
			ModelPartBuilder.create().uv(8, 28)
				.cuboid(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.01F)),
			ModelTransform.of(2.0F, -1.5F, -0.5F, -0.7854F, 0.0F, 0.0F)
		);

		string.addChild("cube_r2",
			ModelPartBuilder.create().uv(8, 28)
				.cuboid(-1.0F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F)
		);

		accessories.addChild("toplamp",
			ModelPartBuilder.create()
				.uv(18, 18).cuboid(-2.0F, -3.0F, -1.0F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(18, 13).cuboid(-2.0F, -4.0F, -1.0F, 3.0F, 2.0F, 3.0F, new Dilation(-0.25F)),
			ModelTransform.origin(0.0F, 0.0F, 0.0F)
		);

		accessories.addChild("googlyeyes",
			ModelPartBuilder.create()
				.uv(0, 15).cuboid(-5.0F, -1.5F, 1.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F))
				.uv(0, 11).cuboid(-5.0F, -1.5F, -2.0F, 1.0F, 2.0F, 2.0F, new Dilation(0.0F)),
			ModelTransform.origin(0.0F, 0.0F, 0.0F)
		);

		ModelPartData spotlight = accessories.addChild("spotlight",
			ModelPartBuilder.create()
				.uv(6, 13).cuboid(-1.5F, 0.0F, -1.5F, 3.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(6, 17).cuboid(-1.5F, 1.0F, -1.5F, 3.0F, 2.0F, 3.0F, new Dilation(0.0F)),
			ModelTransform.origin(-0.5F, 0.0F, 0.5F)
		);

		spotlight.addChild("bone",
			ModelPartBuilder.create().uv(0, 5)
				.cuboid(-1.5F, -1.0F, -1.5F, 3.0F, 3.0F, 3.0F, new Dilation(-0.1F)),
			ModelTransform.origin(0.0F, 2.5F, 0.0F)
		);

		ModelPartData wing1 = root.addChild("wing1",
			ModelPartBuilder.create()
				.uv(0, 26).cuboid(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(8, 26).cuboid(-0.5F, -2.0F, 2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)),
			ModelTransform.of(1.2F, 0.05F, -1.2F, -3.1416F, 0.6109F, 3.1416F)
		);

		ModelPartData propel1 = wing1.addChild("propel1",
			ModelPartBuilder.create(),
			ModelTransform.origin(0.0F, -2.0F, 2.5F)
		);

		propel1.addChild("cube_r3",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.6109F, 0.0F)
		);

		propel1.addChild("cube_r4",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.9599F, 0.0F)
		);

		ModelPartData wing2 = root.addChild("wing2",
			ModelPartBuilder.create()
				.uv(0, 26).cuboid(-1.8596F, -1.0F, 2.4169F, 1.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(8, 26).cuboid(-1.8596F, -2.0F, 4.4169F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)),
			ModelTransform.of(1.3F, 0.05F, 0.0F, 0.0F, -0.6109F, 0.0F)
		);

		ModelPartData propel2 = wing2.addChild("propel2",
			ModelPartBuilder.create(),
			ModelTransform.origin(-1.3596F, -2.0F, 4.9169F)
		);

		propel2.addChild("cube_r5",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, -3.1416F, -0.6109F, -3.1416F)
		);

		propel2.addChild("cube_r6",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.9599F, 0.0F)
		);

		ModelPartData wing3 = root.addChild("wing3",
			ModelPartBuilder.create()
				.uv(0, 26).cuboid(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(8, 26).cuboid(-0.5F, -2.0F, 2.0F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)),
			ModelTransform.of(1.2F, 0.05F, 1.2F, 0.0F, 0.6109F, 0.0F)
		);

		ModelPartData propel3 = wing3.addChild("propel3",
			ModelPartBuilder.create(),
			ModelTransform.origin(0.0F, -2.0F, 2.5F)
		);

		propel3.addChild("cube_r7",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.6109F, 0.0F)
		);

		propel3.addChild("cube_r8",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.9599F, 0.0F)
		);

		ModelPartData wing4 = root.addChild("wing4",
			ModelPartBuilder.create()
				.uv(0, 26).cuboid(0.8596F, -1.0F, 2.4169F, 1.0F, 1.0F, 3.0F, new Dilation(0.0F))
				.uv(8, 26).cuboid(0.8596F, -2.0F, 4.4169F, 1.0F, 1.0F, 1.0F, new Dilation(0.0F)),
			ModelTransform.of(1.3F, 0.05F, 0.0F, -3.1416F, -0.6109F, 3.1416F)
		);

		ModelPartData propel4 = wing4.addChild("propel4",
			ModelPartBuilder.create(),
			ModelTransform.origin(1.3596F, -2.0F, 4.9169F)
		);

		propel4.addChild("cube_r9",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.6109F, 0.0F)
		);

		propel4.addChild("cube_r10",
			ModelPartBuilder.create().uv(12, 5)
				.cuboid(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 5.0F, new Dilation(0.01F)),
			ModelTransform.of(0.0F, 0.0F, 0.0F, -3.1416F, -0.9599F, 3.1416F)
		);

		return TexturedModelData.of(modelData, 64, 64);
	}



}
