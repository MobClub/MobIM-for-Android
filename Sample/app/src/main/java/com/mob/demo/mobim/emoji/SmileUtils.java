/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mob.demo.mobim.emoji;

import android.content.Context;
import android.net.Uri;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmileUtils {
	public static final String DELETE_KEY = "em_delete_delete_expression";

	public static final String EE_1 = "[):]";
	public static final String EE_2 = "[:D]";
	public static final String EE_3 = "[;)]";
	public static final String EE_4 = "[:-o]";
	public static final String EE_5 = "[:p]";
	public static final String EE_6 = "[(H)]";
	public static final String EE_7 = "[:@]";
	public static final String EE_8 = "[:s]";
	public static final String EE_9 = "[:$]";
	public static final String EE_10 = "[:(]";
	public static final String EE_11 = "[:'(]";
	public static final String EE_12 = "[:|]";
	public static final String EE_13 = "[(a)]";
	public static final String EE_14 = "[8o|]";
	public static final String EE_15 = "[8-|]";
	public static final String EE_16 = "[+o(]";
	public static final String EE_17 = "[<o)]";
	public static final String EE_18 = "[|-)]";
	public static final String EE_19 = "[*-)]";
	public static final String EE_20 = "[:-#]";
	public static final String EE_21 = "[:-*]";
	public static final String EE_22 = "[^o)]";
	public static final String EE_23 = "[8-)]";
	public static final String EE_24 = "[(|)]";
	public static final String EE_25 = "[(u)]";
	public static final String EE_26 = "[(S)]";
	public static final String EE_27 = "[(*)]";
	public static final String EE_28 = "[(#)]";
	public static final String EE_29 = "[(R)]";
	public static final String EE_30 = "[({)]";
	public static final String EE_31 = "[(})]";
	public static final String EE_32 = "[(k)]";
	public static final String EE_33 = "[(F)]";
	public static final String EE_34 = "[(W)]";
	public static final String EE_35 = "[(D)]";

//	public static final String ee_no = "[NO]";
//	public static final String ee_ok = "[OK]";
//	public static final String ee_rain = "[下雨]";
//	public static final String ee_mmd = "[么么哒]";
//
//	public static final String ee_pingpang = "[乒乓]";
//	public static final String ee_shit = "[便便]";
//	public static final String ee_envelop = "[信封]";
//	public static final String ee_titter = "[偷笑]";
//	public static final String ee_haughty = "[傲慢]";
//	public static final String ee_bye = "[再见]";
//	public static final String ee_coldsweat = "[冷汗]";
//	public static final String ee_fade = "[凋谢]";
//
//	public static final String ee_sword = "[刀]";
//	public static final String ee_seduce = "[勾引]";
//	public static final String ee_daze = "[发呆]";
//	public static final String ee_shake = "[发抖]";
//	public static final String ee_pity = "[可怜]";
//
//	public static final String ee_lovely = "[可爱]";
//	public static final String ee_rightgroan = "[右哼哼]";
//	public static final String ee_righttaiji = "[右太极]";
//	public static final String ee_rightcar = "[右车头]";
//	public static final String ee_vomit = "[吐]";
	private static final Factory SPANNABLEFACTORY = Factory
			.getInstance();
	private static Map<Pattern, Object> emoticons = new HashMap<Pattern, Object>();
	static {
		Emojicon[] emojicons = DefaultEmojiconDatas.getData();
		for (Emojicon emojicon : emojicons) {
			addPattern(emojicon.getEmojiText(), Integer.valueOf(emojicon.getIcon()));
		}
//	EmojiconInfoProvider emojiconInfoProvider = EaseUI.getInstance().getEmojiconInfoProvider();
//	if(emojiconInfoProvider != null && emojiconInfoProvider.getTextEmojiconMapping() != null){
//	for(Entry<String, Object> entry : emojiconInfoProvider.getTextEmojiconMapping().entrySet()){
//	addPattern(entry.getKey(), entry.getValue());
//	}
//	}
	}

	/**
	 * add text and icon to the map
	 *
	 * @param emojiText-- text of emoji
	 * @param icon        -- resource id or local path
	 */
	public static void addPattern(String emojiText, Object icon) {
		emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
	}

	/**
	 * get the icon from the map
	 *
	 * @param emojiText text of emoji
	 * @return -- resource id or local path
	 */
	public static Object getIcon(String emojiText) {
		//Utils.showLog("","getIcon emotions size >> "+emoticons.size());
		Object icon = null;
		//	boolean b = false;
		for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
			Matcher matcher = entry.getKey().matcher(emojiText);
			if (matcher.find()) {
				//			b = true;
				icon = entry.getValue();
				break;
			}
		}
		return icon;
	}

	/**
	 * replace existing spannable with smiles
	 *
	 * @param context
	 * @param spannable
	 * @return
	 */
	public static boolean addSmiles(Context context, Spannable spannable) {
		boolean hasChanges = false;
		for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
			Matcher matcher = entry.getKey().matcher(spannable);
			while (matcher.find()) {
				boolean set = true;
				for (ImageSpan span : spannable.getSpans(matcher.start(),
						matcher.end(), ImageSpan.class)) {
					if (spannable.getSpanStart(span) >= matcher.start()
							&& spannable.getSpanEnd(span) <= matcher.end()) {
						spannable.removeSpan(span);
					} else {
						set = false;
						break;
					}
				}
				if (set) {
					hasChanges = true;
					Object value = entry.getValue();
					if (value instanceof String && !((String) value).startsWith("http")) {
						File file = new File((String) value);
						if (!file.exists() || file.isDirectory()) {
							return false;
						}
						spannable.setSpan(new ImageSpan(context, Uri.fromFile(file)),
								matcher.start(), matcher.end(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} else {
						spannable.setSpan(new ImageSpan(context, (Integer) value),
								matcher.start(), matcher.end(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}
		}
		return hasChanges;
	}
	public static Spannable getSmiledText(Context context, CharSequence text) {
		Spannable spannable = SPANNABLEFACTORY.newSpannable(text);
		addSmiles(context, spannable);
		return spannable;
	}
	public static boolean containsKey(String key) {
		boolean b = false;
		for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
			Matcher matcher = entry.getKey().matcher(key);
			if (matcher.find()) {
				b = true;
				break;
			}
		}
		return b;
	}
	public static int getSmilesSize() {
		return emoticons.size();
	}
}
