package io.github.halffocused.diamond_is_uncraftable.client.entity.render;

import io.github.halffocused.diamond_is_uncraftable.client.entity.model.WeatherReportModel;
import io.github.halffocused.diamond_is_uncraftable.entity.stand.WeatherReportEntity;
import io.github.halffocused.diamond_is_uncraftable.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class WeatherReportRenderer extends AbstractStandRenderer<WeatherReportEntity, WeatherReportModel> {
    public WeatherReportRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new WeatherReportModel());
    }

    @Override
    public ResourceLocation getEntityTexture(WeatherReportEntity entity) {
        return Util.ResourceLocations.WEATHER_REPORT;
    }
}
