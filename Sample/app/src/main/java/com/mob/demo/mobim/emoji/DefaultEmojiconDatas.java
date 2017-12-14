package com.mob.demo.mobim.emoji;

import com.mob.demo.mobim.R;

public class DefaultEmojiconDatas {

	private static String[] emojis = new String[]{
			SmileUtils.EE_1,
			SmileUtils.EE_2,
			SmileUtils.EE_3,
			SmileUtils.EE_4,
			SmileUtils.EE_5,
			SmileUtils.EE_6,
			SmileUtils.EE_7,
			SmileUtils.EE_8,
			SmileUtils.EE_9,
			SmileUtils.EE_10,
			SmileUtils.EE_11,
			SmileUtils.EE_12,
			SmileUtils.EE_13,
			SmileUtils.EE_14,
			SmileUtils.EE_15,
			SmileUtils.EE_16,
			SmileUtils.EE_17,
			SmileUtils.EE_18,
			SmileUtils.EE_19,
			SmileUtils.EE_20,
			SmileUtils.EE_21,
			SmileUtils.EE_22,
			SmileUtils.EE_23,
			SmileUtils.EE_24,
			SmileUtils.EE_25,
			SmileUtils.EE_26,
			SmileUtils.EE_27,
			SmileUtils.EE_28,
			SmileUtils.EE_29,
			SmileUtils.EE_30,
			SmileUtils.EE_31,
			SmileUtils.EE_32,
			SmileUtils.EE_33,
			SmileUtils.EE_34,
			SmileUtils.EE_35,

	};


	private static int[] icons = new int[]{
			R.drawable.ee_1,
			R.drawable.ee_2,
			R.drawable.ee_3,
			R.drawable.ee_4,
			R.drawable.ee_5,
			R.drawable.ee_6,
			R.drawable.ee_7,
			R.drawable.ee_8,
			R.drawable.ee_9,
			R.drawable.ee_10,
			R.drawable.ee_11,
			R.drawable.ee_12,
			R.drawable.ee_13,
			R.drawable.ee_14,
			R.drawable.ee_15,
			R.drawable.ee_16,
			R.drawable.ee_17,
			R.drawable.ee_18,
			R.drawable.ee_19,
			R.drawable.ee_20,
			R.drawable.ee_21,
			R.drawable.ee_22,
			R.drawable.ee_23,
			R.drawable.ee_24,
			R.drawable.ee_25,
			R.drawable.ee_26,
			R.drawable.ee_27,
			R.drawable.ee_28,
			R.drawable.ee_29,
			R.drawable.ee_30,
			R.drawable.ee_31,
			R.drawable.ee_32,
			R.drawable.ee_33,
			R.drawable.ee_34,
			R.drawable.ee_35,
	};

//private static String[] emojis = new String[]{
//SmileUtils.ee_no,
//SmileUtils.ee_ok,
//SmileUtils.ee_rain,
//SmileUtils.ee_mmd,
//SmileUtils.ee_pingpang,
//SmileUtils.ee_shit,
//SmileUtils.ee_envelop,
//SmileUtils.ee_titter,
//SmileUtils.ee_haughty,
//SmileUtils.ee_bye,
//SmileUtils.ee_coldsweat,
//SmileUtils.ee_fade,
//SmileUtils.ee_sword,
//SmileUtils.ee_seduce,
//SmileUtils.ee_daze,
//SmileUtils.ee_shake,
//SmileUtils.ee_pity,
//
//SmileUtils.ee_lovely,
//SmileUtils.ee_rightgroan,
//SmileUtils.ee_righttaiji,
//SmileUtils.ee_rightcar,
//SmileUtils.ee_vomit,
//
//
//};
//private static int[] icons = new int[]{
//R.drawable.ee_no,
//R.drawable.ee_ok,
//R.drawable.ee_rain,
//R.drawable.ee_mmd,
//R.drawable.ee_pingpang,
//R.drawable.ee_shit,
//R.drawable.ee_envelop,
//R.drawable.ee_titter,
//R.drawable.ee_haughty,
//R.drawable.ee_bye,
//R.drawable.ee_coldsweat,
//R.drawable.ee_fade,
//R.drawable.ee_sword,
//R.drawable.ee_seduce,
//R.drawable.ee_daze,
//R.drawable.ee_shake,
//R.drawable.ee_pity,
//R.drawable.ee_lovely,
//R.drawable.ee_rightgroan,
//R.drawable.ee_righttaiji,
//R.drawable.ee_rightcar,
//R.drawable.ee_vomit,
//};


	private static final Emojicon[] DATA = createData();

	private static Emojicon[] createData() {
		Emojicon[] datas = new Emojicon[icons.length];
		for (int i = 0; i < icons.length; i++) {
			datas[i] = new Emojicon(icons[i], emojis[i], Emojicon.Type.NORMAL);
		}
		return datas;
	}

	public static Emojicon[] getData() {
		return DATA;
	}
}
