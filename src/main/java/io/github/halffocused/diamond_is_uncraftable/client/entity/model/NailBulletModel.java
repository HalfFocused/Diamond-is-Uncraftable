package io.github.halffocused.diamond_is_uncraftable.client.entity.model;

import io.github.halffocused.diamond_is_uncraftable.entity.stand.attack.NailBulletEntity;
import net.minecraft.client.renderer.model.ModelRenderer;

public class NailBulletModel extends AbstractStandAttackModel<NailBulletEntity> {
    private final ModelRenderer Bullet;

    public NailBulletModel() {
        textureWidth = 8;
        textureHeight = 8;

        Bullet = new ModelRenderer(this);
        Bullet.setTextureOffset(0, 0).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, 0.0F, false);
    }


    @Override
    protected ModelRenderer getAttackModel() {
        return Bullet;
    }
}
