package org.jenson.heartrate

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

private const val RESOURCES_VERSION = "1"

class HeartRateTileService : androidx.wear.tiles.TileService() {

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> {
        val deviceParams = requestParams.deviceConfiguration

        val launchAction = ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder()
                    .setPackageName(packageName)
                    .setClassName(MainActivity::class.java.name)
                    .build()
            )
            .build()

        val clickable = androidx.wear.protolayout.ModifiersBuilders.Clickable.Builder()
            .setOnClick(launchAction)
            .build()

        val chip = CompactChip.Builder(this, "Open", clickable, deviceParams)
            .setChipColors(ChipColors.primaryChipColors(androidx.wear.protolayout.material.Colors.DEFAULT))
            .build()

        val layout = PrimaryLayout.Builder(deviceParams)
            .setResponsiveContentInsetEnabled(true)
            .setPrimaryLabelTextContent(
                Text.Builder(this, "Heart Rate")
                    .setTypography(Typography.TYPOGRAPHY_CAPTION1)
                    .setColor(
                        androidx.wear.protolayout.ColorBuilders.argb(0xFFFFFFFF.toInt())
                    )
                    .build()
            )
            .setContent(
                LayoutElementBuilders.Image.Builder()
                    .setResourceId("ic_heart")
                    .setWidth(DimensionBuilders.DpProp.Builder(48f).build())
                    .setHeight(DimensionBuilders.DpProp.Builder(48f).build())
                    .build()
            )
            .setPrimaryChipContent(chip)
            .build()

        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.fromLayoutElement(layout)
            )
            .build()

        return Futures.immediateFuture(tile)
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> {
        val resources = ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addIdToImageMapping(
                "ic_heart",
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.ic_heart)
                            .build()
                    )
                    .build()
            )
            .build()

        return Futures.immediateFuture(resources)
    }
}
