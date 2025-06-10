package com.github.cargocats.illicitblocks.client;

import com.github.cargocats.illicitblocks.IllicitBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedSimpleModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.ModelSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class JsonItemModelAdapter implements ItemModel.Unbaked {
    private final List<TintSource> tints;
    private final JsonUnbakedModel jsonUnbakedModel;
    private final Identifier id;

    @Override
    public MapCodec<? extends ItemModel.Unbaked> getCodec() {
        throw new UnsupportedOperationException("Codec not implemented for JsonItemModelAdapter");
    }

    public JsonItemModelAdapter(Identifier id, JsonUnbakedModel jsonUnbakedModel, List<TintSource> tints) {
        this.id = id;
        this.jsonUnbakedModel = jsonUnbakedModel;
        this.tints = tints;
    }

    @Override
    public ItemModel bake(ItemModel.BakeContext context) {
        Baker baker = context.blockModelBaker();

        BakedSimpleModel bakedSimpleModel = new BakedSimpleModel() {
            @Override
            public UnbakedModel getModel() {
                return jsonUnbakedModel;
            }

            @Override
            public @Nullable BakedSimpleModel getParent() {
                IllicitBlocks.LOG.info("The parent model is {}", baker.getModel(jsonUnbakedModel.parent()));
                return baker.getModel(jsonUnbakedModel.parent());
            }

            @Override
            public String name() {
                return id.toString();
            }
        };

        ModelTextures modelTextures = bakedSimpleModel.getTextures();
        List<BakedQuad> list = bakedSimpleModel.bakeGeometry(modelTextures, baker, ModelRotation.X0_Y0).getAllQuads();
        ModelSettings modelSettings = ModelSettings.resolveSettings(baker, bakedSimpleModel, modelTextures);

        return new BasicItemModel(this.tints, list, modelSettings);
    }

    @Override
    public void resolve(Resolver resolver) {
        resolver.markDependency(id);
    }
}
