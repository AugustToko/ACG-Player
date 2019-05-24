package top.geek_studio.chenlongcould.musicplayer;

import android.content.Context;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import org.jetbrains.annotations.NotNull;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
	@Override
	public void applyOptions(@NotNull Context context, @NotNull GlideBuilder builder) {
	}

	//    针对V4用户可以提升速度
	@Override
	public boolean isManifestParsingEnabled() {
		return false;
	}
}