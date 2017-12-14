package com.mob.demo.mobim;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;

import java.io.File;

public class GlideConfig implements GlideModule {
	public void applyOptions(Context context, GlideBuilder builder) {
		try {
			File cacheDir = context.getExternalCacheDir();//指定的是数据的缓存地址
			//设置磁盘缓存大小
			builder.setDiskCache(new DiskLruCacheFactory(cacheDir.getPath(), "images", 1024 * 1024 * 100));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void registerComponents(Context context, Glide glide) {

	}
}
